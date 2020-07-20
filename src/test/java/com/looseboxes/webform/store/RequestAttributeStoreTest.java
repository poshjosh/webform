package com.looseboxes.webform.store;

import com.looseboxes.webform.TestConfig;
import com.looseboxes.webform.store.AttributeStoreTest.Context;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author hp
 */
public class RequestAttributeStoreTest extends AttributeStoreTest<HttpServletRequest>{
    
    private static final class HttpServletRequestContext implements Context<HttpServletRequest>{

        @Override
        public AttributeStore<HttpServletRequest> getAttributeStore(HttpServletRequest backingStore) {
            return new TestConfig().getFormAttributeService()
                    .wrap(new StoreDelegate(null, backingStore)).requestAttributes();
        }

        @Override
        public HttpServletRequest getBackingStore(String name, Object value) {
            HttpServletRequest request = this.getBackingStore();
            request.setAttribute(name, value);
            return request;
        }

        @Override
        public HttpServletRequest getBackingStore() {
            return new MockHttpServletRequest();
        }

        @Override
        public int getSize(HttpServletRequest backingStore) {
            int size = 0;
            Enumeration en = backingStore.getAttributeNames();
            while(en.hasMoreElements()) {
                en.nextElement();
                ++size;
            }
            return size;
        }
    }

    public RequestAttributeStoreTest() {
        super(new HttpServletRequestContext());
    }
}
