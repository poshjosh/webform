package com.looseboxes.webform.converters;

import com.looseboxes.webform.Errors;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author hp
 */
public class ConvertStringToEnum implements Converter<String, Enum> {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertOrdinalToEnum.class);

    private final Class enumType;

    public ConvertStringToEnum(Class enumType) {
        if( ! enumType.isEnum()) {
            throw Errors.unexpected(enumType, "instance of Enum");
        }
        this.enumType = Objects.requireNonNull(enumType);
    }
    
    @Override
    public Enum convert(String toConvert) {
        try{
            final Enum update = Enum.valueOf(enumType, toConvert);
            LOG.trace("Converted: {} to: {}", toConvert, update);
            return update;
        }catch(RuntimeException e) {
            LOG.debug("Failed to convert {}", toConvert, e);
            throw e;
        }
    }
}

