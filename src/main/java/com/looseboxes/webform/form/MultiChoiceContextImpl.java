package com.looseboxes.webform.form;

import com.bc.webform.functions.MultiChoiceContextForPojo;
import com.bc.webform.functions.TypeTests;
import com.looseboxes.webform.converters.DomainObjectPrinter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author hp
 */
public class MultiChoiceContextImpl extends MultiChoiceContextForPojo{
    
//    private static final Logger LOG = LoggerFactory.getLogger(MultiChoiceContextImpl.class);
    
    private final DomainObjectPrinter printer;
    private final Locale locale;

    public MultiChoiceContextImpl(
            TypeTests typeTests,
            DomainObjectPrinter printer, 
            Locale locale) {
        super(typeTests);//select * from region where country like 
        this.printer = Objects.requireNonNull(printer);
        this.locale = Objects.requireNonNull(locale);
    }

    @Override
    public Map getEnumChoices(Class type) {
        if(this.getTypeTests().isEnumType(type)) {
            final Object [] enums = type.getEnumConstants();
            if(enums != null) {
                final Map choices = new HashMap<>(enums.length, 1.0f);
                for(int i = 0; i<enums.length; i++) {
                    choices.put(i, this.printer.print(enums[i], locale));
                }
                return Collections.unmodifiableMap(choices);
            }
        }
        return Collections.EMPTY_MAP;
    }

    public DomainObjectPrinter getDomainObjectPrinter() {
        return printer;
    }

    public Locale getLocale() {
        return locale;
    }
}
