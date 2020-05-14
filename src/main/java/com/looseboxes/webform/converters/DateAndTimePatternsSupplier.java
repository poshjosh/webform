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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Chinomso Bassey Ikwuagwu on May 2, 2019 12:21:41 PM
 */
public interface DateAndTimePatternsSupplier extends Supplier<Set<String>> {

    @Override
    default Set<String> get() {
        final Set<String> e = new LinkedHashSet<>();
        e.addAll(getDatetimePatterns());
        e.addAll(getDatePatterns());
        e.addAll(getTimePatterns());
        return Collections.unmodifiableSet(e);
    }
    
    Set<String> getDatetimePatterns();
    Set<String> getDatePatterns();
    Set<String> getTimePatterns();
}
