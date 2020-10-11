package com.looseboxes.webform.config;

import com.bc.fileupload.FileuploadConfigurationSource;
import com.bc.fileupload.services.FileStorageHandler;
import com.looseboxes.webform.form.util.FileUploadHandler;
import com.looseboxes.webform.form.util.ModelObjectImagePathsProvider;
import org.springframework.context.annotation.Bean;
import com.bc.fileupload.functions.FilePathProvider;

/**
 * @author hp
 */
public abstract class WebformFileuploadConfigurationSource extends FileuploadConfigurationSource{
    
    @Bean public FileUploadHandler fileUploadHandler(
            FileStorageHandler fileStorageHandler,
            ModelObjectImagePathsProvider imagePathsProvider,
            FilePathProvider getUniquePathForFilename) {

        return new FileUploadHandler(fileStorageHandler, imagePathsProvider, getUniquePathForFilename);
    }
}
