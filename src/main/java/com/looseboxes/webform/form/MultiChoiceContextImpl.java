package com.looseboxes.webform.form;

import com.bc.webform.TypeTests;
import com.bc.webform.choices.SelectOption;
import com.bc.webform.form.member.MultiChoiceContextForPojo;
import com.looseboxes.webform.converters.EntityToSelectOptionConverter;
import com.looseboxes.webform.converters.IdToDomainTypeConverterFactory;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.core.convert.TypeDescriptor;

/**
 * @author hp
 */
public class MultiChoiceContextImpl extends MultiChoiceContextForPojo{
    
    private final EntityToSelectOptionConverter entityToSelectOptionConverter;
    private final IdToDomainTypeConverterFactory idToDomainTypeConverterFactory;

    public MultiChoiceContextImpl(TypeTests typeTests, Locale locale,
            IdToDomainTypeConverterFactory idToDomainTypeConverterFactory,
            EntityToSelectOptionConverter entityToSelectOptionConverter) {
        super(
                typeTests, 
                (obj, loc) -> entityToSelectOptionConverter.print(obj, loc), 
                locale);
        this.idToDomainTypeConverterFactory = Objects.requireNonNull(idToDomainTypeConverterFactory);
        this.entityToSelectOptionConverter= Objects.requireNonNull(entityToSelectOptionConverter);
    }

    @Override
    public Optional<SelectOption> getFieldValueChoice(Object source, Field field, Object fieldValue) {
        final Class fieldType = field.getType();
        if(this.getTypeTests().isDomainType(fieldType)) {
            if(fieldValue != null) {
                final Object entity;
                if(this.idToDomainTypeConverterFactory
                        .matches(TypeDescriptor.valueOf(fieldValue.getClass()), TypeDescriptor.valueOf(fieldType))) {
                    entity = this.idToDomainTypeConverterFactory.getConverter(fieldType).convert(fieldValue);
                }else{
                    entity = fieldValue;
                }
                SelectOption option = entityToSelectOptionConverter.apply(entity, this.getLocale());
                return Optional.of(option);
            }else{
                return Optional.empty();
            }
        }
        return super.getFieldValueChoice(source, field, fieldValue);
    }
}
