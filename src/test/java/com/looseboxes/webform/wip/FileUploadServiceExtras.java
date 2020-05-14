package com.looseboxes.webform.wip;

import com.bc.fileupload.UploadFileResponse;
import com.bc.reflection.ReflectionUtil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanWrapper;
import org.springframework.core.convert.TypeDescriptor;

/**
 * @author hp
 */
public class FileUploadServiceExtras {
    
    public Optional<Object> getPropertyValue(String modelname, Object modelobject, 
            List<UploadFileResponse> responseList, BeanWrapper bean, String propertyName) {

        final Class type = bean.getPropertyType(propertyName);

        final TypeDescriptor td = bean.getPropertyTypeDescriptor(propertyName);
        
        final TypeDescriptor etd = td == null ? null : td.getElementTypeDescriptor();
        
        final Class elementType = etd == null ? null : etd.getType();

        Object output = null;
        
        if(String.class.equals(elementType)) {
            
            if(Collection.class.isAssignableFrom(type)) {
                
                Collection c = (Collection)bean.getPropertyValue(propertyName);
                
                if(c == null) {
                    c = (Collection)new ReflectionUtil().newInstanceForCollectionType(type);
                }
                
                for(UploadFileResponse response : responseList) {
                
                    final String relativePath = response.getFileName();
                    
                    c.add(relativePath);
                }
                
                output = c;
            }
        }

        return Optional.ofNullable(output);
    }
}
