package com.looseboxes.webform.services;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hp
 */
public class FileUploadConfig<T> implements Serializable{
    
    private String id;
    private T modelobject;
    private Map<String, MultipartFile> files;
    private MultiValueMap<String, MultipartFile> multiValueFiles;
    
    public boolean hasFiles() {
        return files != null && ! files.isEmpty() &&
                multiValueFiles != null && ! multiValueFiles.isEmpty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getModelobject() {
        return modelobject;
    }

    public void setModelobject(T modelobject) {
        this.modelobject = modelobject;
    }

    public Map<String, MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(Map<String, MultipartFile> files) {
        this.files = files;
    }

    public MultiValueMap<String, MultipartFile> getMultiValueFiles() {
        return multiValueFiles;
    }

    public void setMultiValueFiles(MultiValueMap<String, MultipartFile> multiValueFiles) {
        this.multiValueFiles = multiValueFiles;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.modelobject);
        hash = 53 * hash + Objects.hashCode(this.files);
        hash = 53 * hash + Objects.hashCode(this.multiValueFiles);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileUploadConfig<?> other = (FileUploadConfig<?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.modelobject, other.modelobject)) {
            return false;
        }
        if (!Objects.equals(this.files, other.files)) {
            return false;
        }
        if (!Objects.equals(this.multiValueFiles, other.multiValueFiles)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FileUploadConfig{" + "id=" + id + ", modelobject=" + modelobject + ", files=" + files + ", multiValueFiles=" + multiValueFiles + '}';
    }
}
