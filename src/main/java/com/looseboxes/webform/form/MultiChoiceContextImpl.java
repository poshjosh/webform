package com.looseboxes.webform.form;

import com.bc.webform.TypeTests;
import com.bc.webform.choices.SelectOption;
import com.bc.webform.form.member.MultiChoiceContextForPojo;
import com.looseboxes.webform.converters.EntityToSelectOptionConverter;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import com.looseboxes.webform.converters.IdStringToDomainTypeConverterFactory;

/**
 * @author hp
 */
public class MultiChoiceContextImpl extends MultiChoiceContextForPojo{
    
    private static final Logger LOG = LoggerFactory.getLogger(MultiChoiceContextImpl.class);
    
    private final EntityToSelectOptionConverter entityToSelectOptionConverter;
    private final IdStringToDomainTypeConverterFactory idStringToDomainTypeConverterFactory;

    public MultiChoiceContextImpl(TypeTests typeTests, Locale locale,
            IdStringToDomainTypeConverterFactory idStringToDomainTypeConverterFactory,
            EntityToSelectOptionConverter entityToSelectOptionConverter) {
        super(
                typeTests, 
                (obj, loc) -> entityToSelectOptionConverter.print(obj, loc), 
                locale);
        this.idStringToDomainTypeConverterFactory = Objects.requireNonNull(idStringToDomainTypeConverterFactory);
        this.entityToSelectOptionConverter= Objects.requireNonNull(entityToSelectOptionConverter);
    }

    @Override
    public Optional<SelectOption> getFieldValueChoice(Object source, Field field, Object fieldValue) {
//        LOG.trace("Get SelectOption for {}.{} = {}", source.getClass().getSimpleName(), field.getName(), fieldValue);
        final Class fieldType = field.getType();
        if(this.getTypeTests().isDomainType(fieldType)) {
            if(fieldValue != null) {
                final Object entity = this.toEntity(fieldValue, fieldType, fieldValue);
                SelectOption option = entityToSelectOptionConverter.apply(entity, getLocale());
                return Optional.of(option);
            }else{
                return Optional.empty();
            }
        }
        return super.getFieldValueChoice(source, field, fieldValue);
    }
    
    private Object toEntity(Object fieldValue, Class fieldType, Object resultIfNone) {
        final Object entity;
        if(this.shouldConvertToType(fieldValue, fieldType)) {
            final String sval = fieldValue.toString();
            entity = this.idStringToDomainTypeConverterFactory.getConverter(fieldType).convert(sval);
        }else{
            entity = null;
        }
        return entity == null ? resultIfNone : entity;
    }
    
    private boolean shouldConvertToType(Object fieldValue, Class fieldType) {
        return ! this.isAlreadyConvertedToType(fieldValue, fieldType) 
                && this.isConvertibleToType(fieldType);
    }
    
    private boolean isAlreadyConvertedToType(Object fieldValue, Class fieldType) {
        final Class fieldValueType = fieldValue.getClass();
        boolean convertible = fieldValueType.getName().equals(fieldType.getName()) ||
                fieldType.isAssignableFrom(fieldValueType);
//        LOG.trace("Convertible: {}, from: {} to: {}", 
//                convertible, fieldValueType.getName(), fieldType.getName());
        return convertible;
    }
    
    private boolean isConvertibleToType(Class fieldType) {
        return this.idStringToDomainTypeConverterFactory
                .matches(TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(fieldType));
    }
}
