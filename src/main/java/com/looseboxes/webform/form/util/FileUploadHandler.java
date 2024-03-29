package com.looseboxes.webform.form.util;

import com.bc.fileupload.UploadFileResponse;
import com.bc.fileupload.services.FileStorageHandler;
import com.bc.reflection.ReflectionUtil;
import com.looseboxes.webform.Errors;
import com.looseboxes.webform.web.FormConfigDTO;
import com.looseboxes.webform.web.FormRequest;
import java.nio.file.Path;
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
    
    private final FilePathProvider filePathProvider;

    public FileUploadHandler(
            FileStorageHandler fileStorageHandler,
            ModelObjectImagePathsProvider imagePathsProvider,
            FilePathProvider filePathProvider) {
        this.fileStorageHandler = Objects.requireNonNull(fileStorageHandler);
        this.imagePathsProvider = Objects.requireNonNull(imagePathsProvider);
        this.filePathProvider = Objects.requireNonNull(filePathProvider);
    }
    
    public void deleteUploadedFiles(FormConfigDTO formConfig) {
        final Collection<String> files = formConfig.removeUploadedFiles();
        if(files == null || files.isEmpty()) {
            return;
        }
        for(String file : files) {
            this.delete(file);
        }
    }

    /**
     * Deletes all the files in the the root form contained in the FormRequest
     * 
     * The root form is the form created from the model object.
     * @param formRequest 
     */
    public void deleteFilesOfRootEntityOnly(FormRequest<Object> formRequest) {
        
        this.deleteAll(this.getFilesOfRootEntityOnly(formRequest));
    }

    public boolean rootEntityHasFiles(FormRequest<Object> formRequest) {
    
        return ! this.getFilesOfRootEntityOnly(formRequest).isEmpty();
    }
    
    public List<String> getFilesOfRootEntityOnly(FormRequest<Object> formRequest) {
        
        List<String> imagePaths = imagePathsProvider.getImagePathsOfRootEntityOnly(formRequest);
        
        return imagePaths;
    }
    
    /**
     * Deletes all the files in the the form(s) contained in the
     * {@code FormRequest} The forms in the form request are those of all
     * the entities belonging to the entity graph built from the modelobject
     * in the form request's form
     * @param formRequest 
     */
    public void deleteFilesOfRootAndNestedEntities(FormRequest<Object> formRequest) {
        
        this.deleteAll(this.getFilesOfRootAndNestedEntities(formRequest));
    }

    public boolean rootOrNestedEntitiesHasFiles(FormRequest<Object> formRequest) {
        
        return ! this.getFilesOfRootAndNestedEntities(formRequest).isEmpty();
    }
    
    public List<String> getFilesOfRootAndNestedEntities(FormRequest<Object> formRequest) {
    
        List<String> imagePaths = imagePathsProvider.getImagePathsOfRootAndNestedEntities(formRequest);
        
        return imagePaths;
    }
    
    public void deleteAll(List<String> imagePaths) {
        
        LOG.debug("Images to delete: {}", imagePaths);
        
        for(String imagePathStr : imagePaths) {
            this.delete(imagePathStr);
        }
    }
    
    public boolean delete(String pathStr) {
        if(pathStr != null && (pathStr.startsWith("http:") || pathStr.startsWith("https:"))) {
            LOG.warn("Cannot delete. Not a file: {}", pathStr);
            return false;
        }else{
            Path path = this.filePathProvider.getPath(pathStr);
            this.delete(path);
            return true;
        }
    }

    public void delete(Path path) {
        path = path.toAbsolutePath().normalize();
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

            final Function<UploadFileResponse, String> toFileName = 
                    response -> response.getFileName();
            
            final Consumer<String> addToOutput = filename -> result.add(filename);

            // Upload multi-value files
            final Map<String, List<MultipartFile>> multiValueFiles = formRequest.getMultiValueFiles();
            final Map<String, List<UploadFileResponse>> multiOutput =
                    uploadMultipleFiles(id, modelobject, multiValueFiles);

            if(multiOutput != null && ! multiOutput.isEmpty()) {
                multiOutput.values().stream()
                        .flatMap((list) -> list.stream())
                        .map(toFileName)
                        .forEach(addToOutput);
            }

            // Upload single-value files, not yet uploaded via the multi-value operation above
            final Map<String, MultipartFile> singleValueFiles = collectYetToBeUploadedFiles(formRequest, multiOutput);

            final Map<String, UploadFileResponse> singleOutput =
                    uploadSingleFiles(id, modelobject, singleValueFiles);

            if(singleOutput != null && ! singleOutput.isEmpty()) {
                singleOutput.values().stream()
                        .map(toFileName)
                        .forEach(addToOutput);
            }
            
            output = result.isEmpty() ? 
                    Collections.EMPTY_LIST : Collections.unmodifiableCollection(result);

            formRequest.getFormConfig().setUploadedFiles(output);
        }
        
        return output;
    }

    private Map<String, MultipartFile> collectYetToBeUploadedFiles(
            FormRequest<Object> formRequest, Map<String, List<UploadFileResponse>> multiOutput) {

        final Map<String, MultipartFile> files = formRequest.getFiles();

        final Map<String, MultipartFile> fileMap = files == null ?
                Collections.EMPTY_MAP : new HashMap<>(files);

        if(multiOutput != null && ! multiOutput.isEmpty()) {
            fileMap.keySet().removeAll(multiOutput.keySet());
        }

        return fileMap;
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
                
                final String propertyName = name;
                
                if(propertyName == null) {
                    continue;
                }
                
                final Object propertyValue = getPropertyValue(response);
                
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
                
                final String propertyName = name;
                
                if(propertyName == null) {
                    continue;
                }
                
                final List<Object> propertyValueList = getPropertyValues(responseList);
                
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

    private Object getPropertyValue(UploadFileResponse response) {
        String relativePath = response.getFileName();
        if( ! relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        return relativePath;
    }

    private List<Object> getPropertyValues(List<UploadFileResponse> responseList) {
        return responseList.stream().map((response) -> getPropertyValue(response))
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
