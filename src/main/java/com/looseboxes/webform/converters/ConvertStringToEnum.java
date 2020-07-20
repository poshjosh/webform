package com.looseboxes.webform.converters;

import com.looseboxes.webform.Errors;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 * @param <T>
 */
public class ConvertStringToEnum<T extends Enum> implements Converter<String, T> {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertStringToEnum.class);

    private final Class<T> enumType;

    private final T [] values;
    
    public ConvertStringToEnum(Class<T> enumType) {
        if( ! enumType.isEnum()) {
            throw Errors.unexpected(enumType, "instance of Enum");
        }
        this.enumType = Objects.requireNonNull(enumType);
        this.values = this.enumType.getEnumConstants();
    }
    
    @Override
    public T convert(String toConvert) {
        Exception exception = null;
        try{
            
            T update;
            try{
                update = this.tryOrdinal(toConvert);
            }catch(RuntimeException e) {
                exception = e;
                update = this.tryName(toConvert);
            }
            
            LOG.trace("Converted: {} to: {}", toConvert, update);
            
            return update;
            
        }catch(RuntimeException e) {
            if(exception != null) {
                e.addSuppressed(exception);
            }
            LOG.debug("Failed to convert {}", toConvert, e);
            throw e;
        }
    }

    private T tryOrdinal(String toConvert) {
        final int id = Integer.parseInt(toConvert);
        final T update = values[id];
        return update;
    }
    
    private T tryName(String toConvert) {
        final T update = (T)Enum.valueOf(enumType, toConvert);
        return update;
    }
}


