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
import io.micrometer.core.lang.Nullable;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.looseboxes.webform.SessionAttributes;

/**
 * @author hp
 */
@Service
public class AttributeService 
        extends StoreComposite<String, Object> 
        implements Wrapper<StoreDelegate, AttributeService>{
//        implements AttributeStore<StoreDelegate>{
    
    private static final Logger LOG = LoggerFactory.getLogger(AttributeService.class);
    
    private final AttributeStoreProvider provider;
    
    @Nullable private StoreDelegate delegate;

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
        this.provider = Objects.requireNonNull(provider);
        this.delegate = delegate;
    }

    @Override
    public AttributeService wrap(StoreDelegate delegate) {
        return new AttributeService(this.provider, delegate);
    }

    @Override
    @Nullable public StoreDelegate unwrap() {
        return this.delegate;
    }

    public boolean addUploadedFiles(Collection<String> filesToAdd) {
        if(filesToAdd != null && !filesToAdd.isEmpty()) {
            Collection<String> attrVal = getUploadedFiles(null);
            if(attrVal == null) {
                attrVal = new ArrayList<>(filesToAdd.size());
                final String attrName = SessionAttributes.UPLOADED_FILES_PENDING;
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
                .remove(SessionAttributes.UPLOADED_FILES_PENDING);
        return removed == null ? resultIfNone : removed;
    }
    
    public Collection<String> getUploadedFiles(Collection<String> resultIfNone) {
        return (Collection<String>)sessionAttributes()
                .getOrDefault(SessionAttributes.UPLOADED_FILES_PENDING, resultIfNone);
    }

    public AttributeStore<ModelMap> modelAttributes() {
        return provider.forModel(delegate.getModelMap());
    }

    public AttributeStore<HttpServletRequest> requestAttributes() {
        return provider.forRequest(delegate.getRequest());
    }

    public AttributeStore<HttpSession> sessionAttributes() {
        return provider.forSession(delegate.getRequest().getSession());
    }

}
