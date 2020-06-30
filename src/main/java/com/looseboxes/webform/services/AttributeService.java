package com.looseboxes.webform.services;

import com.looseboxes.webform.store.StoreComposite;
import com.looseboxes.webform.Wrapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import com.looseboxes.webform.store.AttributeStore;
import com.looseboxes.webform.store.AttributeStoreProvider;
import com.looseboxes.webform.store.Store;
import com.looseboxes.webform.store.StoreDelegate;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;
import com.looseboxes.webform.HttpSessionAttributes;
import com.looseboxes.webform.store.UnbackedStoreException;

/**
 * @author hp
 */
@Service
public class AttributeService 
        extends StoreComposite<String, Object> 
        implements Wrapper<StoreDelegate, AttributeService>{
//        implements AttributeStore<StoreDelegate>{
    
    private static final Logger LOG = LoggerFactory.getLogger(AttributeService.class);
    
    private final AttributeStoreProvider attributeStoreProvider;
    
    @Nullable private StoreDelegate storeDelegate;

    @Autowired
    public AttributeService(AttributeStoreProvider provider) {
        this(provider, null);
    }
    
    public AttributeService(
            AttributeStoreProvider provider, 
            @Nullable StoreDelegate delegate) {
        super(delegate == null ? 
                new Store[0] : 
                new Store[]
                {
                    provider.forModel(delegate.getModelMap()),
                    provider.forRequest(delegate.getRequest()),
                    provider.forSession(delegate.getRequest().getSession())
                }
        );
        this.attributeStoreProvider = Objects.requireNonNull(provider);
        this.storeDelegate = delegate;
    }

    @Override
    public AttributeService wrap(StoreDelegate delegate) {
        return new AttributeService(this.attributeStoreProvider, delegate);
    }

    @Override
    @Nullable public StoreDelegate unwrap() {
        return this.storeDelegate;
    }

    public boolean addUploadedFiles(Collection<String> filesToAdd) {
        if(filesToAdd != null && !filesToAdd.isEmpty()) {
            Collection<String> attrVal = getUploadedFiles(null);
            if(attrVal == null) {
                attrVal = new ArrayList<>(filesToAdd.size());
                final String attrName = HttpSessionAttributes.UPLOADED_FILES_PENDING;
                sessionAttributes().put(attrName, attrVal);
            }
            return attrVal.addAll(filesToAdd);
        }else{
            return false;
        }
    }

    public void deleteUploadedFiles() {
        final Collection<String> files = removeUploadedFiles(null);
        if(files == null || files.isEmpty()) {
            return;
        }
        for(String file : files) {
            final Path path = Paths.get(file).toAbsolutePath().normalize();
            // @TODO
            // Walk through files to local disc and delete orphans (i.e those
            // without corresponding database entry), aged more than a certain
            // limit, say 24 hours.s
            try{
                if( ! Files.deleteIfExists(path)) {
                    LOG.info("Will delete on exit: {}", path);
                    path.toFile().deleteOnExit();
                }
            }catch(IOException e) {
                LOG.warn("Problem deleting: " + path, e);
                LOG.info("Will delete on exit: {}", path);
                path.toFile().deleteOnExit();
            }
        }
    }
    
    public Collection<String> removeUploadedFiles(Collection<String> resultIfNone) {
        final Collection<String> removed = (Collection<String>)sessionAttributes()
                .remove(HttpSessionAttributes.UPLOADED_FILES_PENDING);
        return removed == null ? resultIfNone : removed;
    }
    
    public Collection<String> getUploadedFiles(Collection<String> resultIfNone) {
        return (Collection<String>)sessionAttributes()
                .getOrDefault(HttpSessionAttributes.UPLOADED_FILES_PENDING, resultIfNone);
    }

    public AttributeStore<ModelMap> modelAttributes() {
        if(storeDelegate == null) {
            throw new UnbackedStoreException(ModelMap.class);
        }
        return attributeStoreProvider.forModel(storeDelegate.getModelMap());
    }

    public AttributeStore<HttpServletRequest> requestAttributes() {
        if(storeDelegate == null) {
            throw new UnbackedStoreException(HttpServletRequest.class);
        }
        return attributeStoreProvider.forRequest(storeDelegate.getRequest());
    }

    public AttributeStore<HttpSession> sessionAttributes() {
        if(storeDelegate == null) {
            throw new UnbackedStoreException(HttpSession.class);
        }
        return attributeStoreProvider.forSession(storeDelegate.getRequest().getSession());
    }

    public AttributeStoreProvider getAttributeStoreProvider() {
        return attributeStoreProvider;
    }
}
