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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts empty string to <code>null</code>.
 * 
 * Web forms often return empty string to represent <tt>no value</tt>. This class
 * ensures empty strings coming from web layer are converted to <code>null</code>
 * 
 * @author Chinomso Bassey Ikwuagwu on Apr 19, 2019 4:16:04 PM
 */
public class StringEmptyToNullConverter implements Converter<String, String>{

    private static final Logger LOG = LoggerFactory.getLogger(StringEmptyToNullConverter.class);
    
    @Override
    public String convert(String source) {
        final String output;
        if(source != null && source.isEmpty()) {
            output = null;
            LOG.trace("Converted: {} to: null", source);
        }else{
            output = source;
        }
//        LOG.trace("Converted: {} to: {}", source, output);
        return output;
    }
}
