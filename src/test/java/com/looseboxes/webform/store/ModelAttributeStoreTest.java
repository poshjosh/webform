package com.looseboxes.webform.store;

import com.looseboxes.webform.TestConfig;
import com.looseboxes.webform.store.AttributeStoreTest.Context;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public class ModelAttributeStoreTest extends AttributeStoreTest<ModelMap>{
    
    private static final class ModelMapContext implements Context<ModelMap>{

        @Override
        public AttributeStore<ModelMap> getAttributeStore(ModelMap backingStore) {
            return new TestConfig().getAttributeStoreProvider().forModel(backingStore);
        }
        
        @Override
        public ModelMap getBackingStore(String name, Object value) {
            return this.getBackingStore().addAttribute(name, value);
        }

        @Override
        public ModelMap getBackingStore() {
            return new ModelMap();
        }

        @Override
        public int getSize(ModelMap backingStore) {
            return backingStore.size();
        }
    }

    public ModelAttributeStoreTest() {
        super(new ModelMapContext());
    }
}
