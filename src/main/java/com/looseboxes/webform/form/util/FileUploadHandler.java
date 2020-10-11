package com.looseboxes.webform.form.util;

import com.bc.fileupload.UploadFileResponse;
import com.bc.fileupload.services.FileStorageHandler;
import com.bc.reflection.ReflectionUtil;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.web.FormRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.web.multipart.MultipartFile;
import com.bc.fileupload.functions.FilePathProvider;

/**
 * @author hp
 */
public class FileUploadHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadHandler.class);
    
    private final FileStorageHandler fileStorageHandler;
    
    private final ModelObjectImagePathsProvider imagePathsProvider;
    
    private final FilePathProvider getUniquePathForFilename;

    public FileUploadHandler(
            FileStorageHandler fileStorageHandler,
            ModelObjectImagePathsProvider imagePathsProvider,
            FilePathProvider getUniquePathForFilename) {
        this.fileStorageHandler = Objects.requireNonNull(fileStorageHandler);
        this.imagePathsProvider = Objects.requireNonNull(imagePathsProvider);
        this.getUniquePathForFilename = Objects.requireNonNull(getUniquePathForFilename);
    }

    public void deleteUploadedFiles(FormConfigDTO formConfig) {
        final Collection<String> files = formConfig.removeUploadedFiles();
        if(files == null || files.isEmpty()) {
            return;
        }
        for(String file : files) {
            final Path path = Paths.get(file).toAbsolutePath().normalize();
            this.fileStorageHandler.delete(path);
        }
    }
    
    public void delete(FormRequest<Object> formRequest) {
        
        List<String> imagePaths = imagePathsProvider.getImagePaths(formRequest);
        
        LOG.debug("Images to delete: {}", imagePaths);
        
        for(String imagePathStr : imagePaths) {
            
            Path imagePath = this.getUniquePathForFilename.getPath(imagePathStr);
        
            this.delete(imagePath);
        }
    }
    
    public void delete(Path path) {
        this.fileStorageHandler.delete(path);
    }
    
    public Collection<String> upload(FormRequest<Object> formRequest) {
        
        final Collection<String> output;

        if( ! formRequest.hasFiles()) {
            output = Collections.EMPTY_LIST;
        }else{
        
            final List result = new ArrayList<>();
            final String id = formRequest.getSessionId();
            final Object modelobject = formRequest.getFormConfig().getModelobject();
            final Map<String, List<MultipartFile>> multiValueFiles = formRequest.getMultiValueFiles();

            final Function<UploadFileResponse, String> toFileName = 
                    response -> response.getFileName();
            
            final Consumer<String> addToOutput = filename -> result.add(filename);

            final Map<String, List<UploadFileResponse>> multiOutput = 
                    uploadMultipleFiles(id, modelobject, multiValueFiles);

            if(multiOutput != null && ! multiOutput.isEmpty()) {
                multiOutput.values().stream()
                        .flatMap((list) -> list.stream())
                        .map(toFileName)
                        .forEach(addToOutput);
            }

            final Map<String, MultipartFile> files = formRequest.getFiles();

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
            
            output = result.isEmpty() ? 
                    Collections.EMPTY_LIST : Collections.unmodifiableCollection(result);

            formRequest.getFormConfig().setUploadedFiles(output);
        }
        
        return output;
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
    
    public Map<String, List<UploadFileResponse>> uploadMultipleFiles(
            String id, Object modelobject, Map<String, List<MultipartFile>> multiValueMap) {
        
        LOG.debug("Uploading multi-value multipart file(s): {}", 
                (multiValueMap == null ? null : multiValueMap.keySet()));

        final Map<String, List<UploadFileResponse>> output;
        
        if(multiValueMap == null || multiValueMap.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{    
            output = new HashMap<>(multiValueMap.size(), 1.0f);
            
            final BeanWrapper bean = PropertyAccessorFactory
                    .forBeanPropertyAccess(modelobject);

            for(String name : multiValueMap.keySet()) {

                final List<MultipartFile> multipartFiles = multiValueMap.get(name);
    
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

    private String getPropertyName(Object modelobject, 
            UploadFileResponse response, BeanWrapper bean, String propertyName) {
        return propertyName;
    }
    
    private Object getPropertyValue(Object modelobject, 
            UploadFileResponse response, BeanWrapper bean, String propertyName) {
        String relativePath = response.getFileName();
        if( ! relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        return relativePath;
    }

    private String getPropertyName(Object modelobject, 
            List<UploadFileResponse> responseList, BeanWrapper bean, String propertyName) {
        return propertyName;
    }
    
    private List<Object> getPropertyValues(Object modelobject, 
            List<UploadFileResponse> responseList, BeanWrapper bean, String propertyName) {
        return responseList.stream().map((response) -> getPropertyValue(modelobject, response, bean, propertyName))
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
