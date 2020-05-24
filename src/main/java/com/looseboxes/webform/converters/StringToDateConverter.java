/*
 * Copyright 2019 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.looseboxes.webform.converters;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:21:09 AM
 */
public class StringToDateConverter implements Converter<String, Date> {

    private static final Logger LOG = LoggerFactory.getLogger(StringToDateConverter.class);

    private final Collection<String> datePatterns;
    private final SimpleDateFormat dateFormat;
    
    public StringToDateConverter(DateAndTimePatternsSupplier dtp) {
        this(dtp.get());
    }
    
    public StringToDateConverter(Collection<String> datePatterns) {
        this.datePatterns = Objects.requireNonNull(datePatterns);
        this.dateFormat = new SimpleDateFormat();
    }

    @Override
    public Date convert(String from) {
        Date output = null;
        if(from != null && ! from.isEmpty()) {
            ValidationException ex = null;
            for(String datePattern : datePatterns) {
                dateFormat.applyPattern(datePattern);
                try{
                    output = dateFormat.parse(from);
                    break;
                }catch(java.text.ParseException e) {
                    LOG.debug("Exception parsing date: {}, with pattern: {}, {}", 
                            from, datePattern, e.toString());
                    if(ex == null) {
                        ex = new ValidationException("Invalid value: " + from + 
                                ". Valid formats: " + datePatterns, e);
                    }else{
                        ex.addSuppressed(e);
                    }
                }
            }
            if(output == null) {
                throw ex;
            }
        }
        LOG.trace("Converted: {} to: {}, using: {}", 
                from, 
                (output == null ? null : dateFormat.format(output)), 
                dateFormat.toPattern());
        return output;
    }
}
