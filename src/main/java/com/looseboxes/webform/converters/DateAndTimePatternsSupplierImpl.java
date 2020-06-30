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

import com.looseboxes.webform.util.PropertySearch;
import com.looseboxes.webform.WebformProperties;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on May 1, 2019 6:56:28 PM
 */
public class DateAndTimePatternsSupplierImpl 
        implements DateAndTimePatternsSupplier{
    
    private static final Logger LOG = LoggerFactory
            .getLogger(DateAndTimePatternsSupplierImpl.class);
    
    private final Set<String> datetimePatterns;
    private final Set<String> datePatterns;
    private final Set<String> timePatterns;

    public DateAndTimePatternsSupplierImpl(PropertySearch propertySearch) {
        Objects.requireNonNull(propertySearch);
        
        datetimePatterns = addProperties(
                propertySearch, WebformProperties.FORMATS_DATETIME);
        datePatterns = addProperties(
                propertySearch, WebformProperties.FORMATS_DATE);
        timePatterns = addProperties(
                propertySearch, WebformProperties.FORMATS_TIME);
        LOG.debug("{} = {}\n{} = {}\n{} = {}", 
                WebformProperties.FORMATS_TIME, timePatterns,
                WebformProperties.FORMATS_DATE, datePatterns,
                WebformProperties.FORMATS_DATETIME, datetimePatterns);
    }
    
    private Set<String> addProperties(PropertySearch propertySearch, String propertyName) {
        final List<String> arr = propertySearch.findAll(propertyName);
        return new HashSet(arr);
    }

    @Override
    public Set<String> getDatetimePatterns() {
        return datetimePatterns;
    }

    @Override
    public Set<String> getDatePatterns() {
        return datePatterns;
    }

    @Override
    public Set<String> getTimePatterns() {
        return timePatterns;
    }
}
