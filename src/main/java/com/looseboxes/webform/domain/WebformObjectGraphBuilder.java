package com.looseboxes.webform.domain;

import com.bc.webform.TypeTests;
import com.looseboxes.webform.mappers.EntityMapperService;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * @author hp
 */
public class WebformObjectGraphBuilder extends ObjectGraphBuilderImpl{

    private final TypeTests typeTests;
    private final EntityMapperService entityMapperService;
    
    public WebformObjectGraphBuilder(TypeTests typeTests, EntityMapperService entityMapperService) {
        super(-1);
        this.typeTests = Objects.requireNonNull(typeTests);
        this.entityMapperService = Objects.requireNonNull(entityMapperService);
    }

    @Override
    public List build(Object object) {
        
        // Accepting Enum types lead to Stackoverflow
        //
        final BiPredicate<Field, Object> test = (field, fieldValue) -> 
                typeTests.isDomainType(field.getType()) && 
                        ! typeTests.isEnumType(field.getType());
        
        final Object entity = this.entityMapperService.toEntity(object);
        
        return this.build(entity, test);
    }
}
