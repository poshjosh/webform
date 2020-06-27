package com.looseboxes.webform.services;

import com.bc.fileupload.UploadFileResponse;
import com.bc.fileupload.services.FileStorageHandler;
import com.bc.reflection.ReflectionUtil;
import com.looseboxes.webform.Errors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hp
 */
@Service
public class FileUploadService {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);
    
    private final FileStorageHandler fileStorageHandler;

    @Autowired
    public FileUploadService(FileStorageHandler fileStorageHandler) {
        this.fileStorageHandler = Objects.requireNonNull(fileStorageHandler);
    }
    
    public Collection<String> upload(FileUploadConfig<Object> config) {
        
        final Collection<String> output = new ArrayList<>();
        
        final String id = config.getId();
        final Object modelobject = config.getModelobject();
        final MultiValueMap<String, MultipartFile> multiValueFiles = config.getMultiValueFiles();
        
        final Function<UploadFileResponse, String> toFileName = 
                response -> response.getFileName();
        final Consumer<String> addToOutput = filename -> output.add(filename);

        final Map<String, List<UploadFileResponse>> multiOutput = 
                uploadMultipleFiles(id, modelobject, multiValueFiles);

        if(multiOutput != null && ! multiOutput.isEmpty()) {
            multiOutput.values().stream()
                    .flatMap((list) -> list.stream())
                    .map(toFileName)
                    .forEach(addToOutput);
        }
        
        final Map<String, MultipartFile> files = config.getFiles();

        final Map<String, MultipartFile> fileMap = files == null ? 
                Collections.EMPTY_MAP : new HashMap<>(files);

        if(multiOutput != null && ! multiOutput.isEmpty()) {
            fileMap.keySet().removeAll(multiOutput.keySet());
        }

        if( ! fileMap.isEmpty()) {
            
            final Map<String, UploadFileResponse> singleOutput = 
                    uploadSingleFiles(id, modelobject, fileMap);

            if(singleOutput != null && ! singleOutput.isEmpty()) {
                singleOutput.values().stream()
                        .map(toFileName)
                        .forEach(addToOutput);
            }
        }
        
        return output.isEmpty() ? 
                Collections.EMPTY_LIST : Collections.unmodifiableCollection(output);
    }

    public Map<String, UploadFileResponse> uploadSingleFiles(
            String id, Object modelobject, Map<String, MultipartFile> fileMap) {
        
        LOG.debug("Uploading single value multipart file(s): {}", (fileMap == null ? null : fileMap.keySet()));

        final Map<String, UploadFileResponse> output;
        
        if(fileMap == null || fileMap.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{    
            output = new HashMap<>(fileMap.size(), 1.0f);
            
            final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(modelobject);

            for(String name : fileMap.keySet()) {

                final MultipartFile multipartFile = fileMap.get(name);
                
                LOG.debug("{} has MultipartFile = {}", name, multipartFile);
                
                final UploadFileResponse response = upload(id, multipartFile).orElse(null);
                
                if(response == null) {
                    continue;
                }
                
                output.put(name, response);
                
                final String propertyName = getPropertyName(
                        modelobject, response, bean, name);
                
                if(propertyName == null) {
                    continue;
                }
                
                final Object propertyValue = getPropertyValue(
                        modelobject, response, bean, propertyName);
                
                final Object previousValue = bean.getPropertyValue(propertyName);
                
                LOG.debug("After uploading multipart file, updating {} to {} from {}", 
                        propertyName, propertyValue, previousValue);
    
                if(bean.isWritableProperty(propertyName)) {
                    bean.setPropertyValue(propertyName, propertyValue);
                }else{
                    LOG.warn("Not writable: {}#{}", 
                            modelobject.getClass().getName(), propertyName);
                }
            }
        }
        
        return output == null || output.isEmpty() ? Collections.EMPTY_MAP : 
                Collections.unmodifiableMap(output);
    }  
    
    public String getPropertyName(Object modelobject, 
            UploadFileResponse response, BeanWrapper bean, String propertyName) {
        return propertyName;
    }
    public Object getPropertyValue(Object modelobject, 
            UploadFileResponse response, BeanWrapper bean, String propertyName) {
        final String relativePath = response.getFileName();
        return relativePath;
    }
    
    public Map<String, List<UploadFileResponse>> uploadMultipleFiles(
            String id, Object modelobject, MultiValueMap<String, MultipartFile> mvm) {
        
        LOG.debug("Uploading multi-value multipart file(s): {}", 
                (mvm == null ? null : mvm.keySet()));

        final Map<String, List<UploadFileResponse>> output;
        
        if(mvm == null || mvm.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{    
            output = new HashMap<>(mvm.size(), 1.0f);
            
            final BeanWrapper bean = PropertyAccessorFactory
                    .forBeanPropertyAccess(modelobject);

            for(String name : mvm.keySet()) {

                final List<MultipartFile> multipartFiles = mvm.get(name);
    
                LOG.debug("{} has {} MultipartFile(s)", name, (multipartFiles == null ? null : multipartFiles.size()));
                
                if( multipartFiles == null || multipartFiles.isEmpty()) {
                    continue;
                }
                
                final List<UploadFileResponse> responseList = new ArrayList<>(multipartFiles.size()) ;

                for(MultipartFile multipartFile : multipartFiles) {
                
                    final UploadFileResponse response = upload(id, multipartFile).orElse(null);

                    if(response == null) {
                        continue;
                    }
                    
                    responseList.add(response);
                }
                
                if( ! responseList.isEmpty()) {
                    output.put(name, responseList);
                }
                
                final String propertyName = getPropertyName(
                        modelobject, responseList, bean, name);
                
                if(propertyName == null) {
                    continue;
                }
                
                final List<Object> propertyValueList = getPropertyValues(
                        modelobject, responseList, bean, propertyName);
                
                final Object previousValue = bean.getPropertyValue(propertyName);
                
                LOG.debug("After uploading {} multipart files, updating {} to {} from {}", 
                        responseList.size(), propertyName, propertyValueList, previousValue);
    
                if( ! propertyValueList.isEmpty() && bean.isWritableProperty(propertyName)) { 

                    LOG.debug("File names: {}", responseList.stream().map((r) -> r.getFileName()).collect(Collectors.toList()));

                    final Class propertyType = bean.getPropertyType(propertyName);
                    
                    final Object value;
                    if(CharSequence.class.isAssignableFrom(propertyType)) {
                        value = (String)propertyValueList.get(0);
                    }else if(Collection.class.isAssignableFrom(propertyType)) {
                        final Collection collection = (Collection)new ReflectionUtil()
                                .newInstanceForCollectionType(propertyType);
                        collection.addAll(propertyValueList);
                        value = collection;
                    }else{
                        throw Errors.unexpected(propertyType, String.class, Collection.class);
                    }
                    
                    bean.setPropertyValue(propertyName, value);
                    
                }else{
                    LOG.warn("Not writable: {}#{}", modelobject.getClass().getName(), propertyName);
                }
            }
        }
        
        return output == null || output.isEmpty() ? Collections.EMPTY_MAP : 
                Collections.unmodifiableMap(output);
    }    

    public String getPropertyName(Object modelobject, 
            List<UploadFileResponse> responseList, BeanWrapper bean, String propertyName) {
        return propertyName;
    }
    
    public List<Object> getPropertyValues(Object modelobject, 
            List<UploadFileResponse> responseList, BeanWrapper bean, String propertyName) {
        return responseList.stream().map((response) -> response.getFileName())
                .collect(Collectors.toList());
    }

    public Optional<UploadFileResponse> upload(String id, MultipartFile multipartFile) {
        
        final String fname = multipartFile.getOriginalFilename();

        final UploadFileResponse response;
        
        if(fname == null || fname.isEmpty() || multipartFile.isEmpty()) {
            response = null;
        }else{
            response = fileStorageHandler.save(multipartFile);
        }  
        
        LOG.debug("Uploaded file: {}", response);
        
        return Optional.ofNullable(response);
    }
}
