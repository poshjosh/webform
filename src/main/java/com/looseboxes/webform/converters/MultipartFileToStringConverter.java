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
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 2:53:09 AM
 */
public class MultipartFileToStringConverter implements Converter<MultipartFile, String> {

    private static final Logger LOG = LoggerFactory.getLogger(MultipartFileToStringConverter.class);

    public MultipartFileToStringConverter() { }

    @Override
    public String convert(MultipartFile from) {
        LOG.trace("Converting {} of {} to class java.lang.String", from, (from==null?null:from.getClass()));
        final String original = from.getOriginalFilename();
        final String output = original == null || original.isEmpty() ? null : original;
        LOG.trace("Converted: {}, to: {}", from, output);
        return output;
    }
}
