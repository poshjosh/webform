package com.looseboxes.webform.store;

import java.util.Objects;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import org.springframework.ui.ModelMap;

/**
 * @author hp
 */
public abstract class AttributeStoreTest<S> {
    
    public static interface Context<S>{
        int getSize(S backingStore);
        S getBackingStore();
        S getBackingStore(String name, Object value);
        AttributeStore<S> getAttributeStore(S backingStore);
    }
    
    private final Context<S> context;
    
    public AttributeStoreTest(Context<S> context) {
        this.context = Objects.requireNonNull(context);
    }
    
    @Test
    public void wrap_ShouldReturnANewInstance() {
        System.out.println("wrap_ShouldReturnANewInstance");
        AttributeStore<S> instance = this.getInstance();
        S backingStore = this.getBackingStore();
        AttributeStore<ModelMap> result = instance.wrap(backingStore);
        assertThat(result, not(instance));
    }

    @Test
    public void unwrap_ShouldReturnBackingBuffer() {
        System.out.println("unwrap_ShouldReturnBackingBuffer");
        S expResult = this.getBackingStore();
        AttributeStore<S> instance = this.getInstance(expResult);
        S result = instance.unwrap();
        assertEquals(expResult, result);
    }

    @Test
    public void put_givenNewName_shouldReturnNullAndIncreaseSizeByOne() {
        System.out.println("put_givenNewName_shouldReturnNullAndIncreaseSizeByOne");
        S backingStore = this.getBackingStore("name", "value");
        AttributeStore<S> instance = this.givenStoreWithEntry(backingStore);
        int sizeBeforePut = getSize(backingStore);
        Object result = instance.put("another name", "another value");
        assertThat(getSize(backingStore), is(sizeBeforePut + 1));
        assertThat(result, nullValue());
    }
    
    @Test
    public void put_givenExistingName_shouldReplaceExistingValueWithSizeRemainingTheSame() {
        System.out.println("put_givenExistingName_shouldReplaceExistingValueWithSizeRemainingTheSame");
        String name = "name";
        Object expResult = "value";
        S backingStore = this.getBackingStore(name, expResult);
        AttributeStore<S> instance = this.givenStoreWithEntry(backingStore);
        int sizeBeforePut = getSize(backingStore);
        Object result = instance.put(name, "another value");
        assertThat(getSize(backingStore), is(sizeBeforePut));
        assertThat(result, is(expResult));
    }

    /**
     * Test of remove method, of class ModelAttributeStore.
     */
    @Test
    public void remove_ShouldDecreaseSizeByOneAndReturnPreviouslyAdded() {
        System.out.println("remove_ShouldDecreaseSizeByOneAndReturnPreviouslyAdded");
        String name = "name";
        Object expResult = "value";
        S backingStore = this.getBackingStore(name, expResult);
        AttributeStore<S> instance = this.givenStoreWithEntry(backingStore);
        int sizeBeforeRemove = getSize(backingStore);
        int expSize = sizeBeforeRemove - 1 < 0 ? 0 : sizeBeforeRemove - 1;
        Object result = instance.remove(name);
        assertThat(getSize(backingStore), is(expSize));
        assertThat(result, is(expResult));
    }

    @Test
    public void getOrDefault_GivenEmptyStore_ShouldReturnDefault() {
        System.out.println("getOrDefault_ShouldReturnExistingStoreEntry");
        String name = "name";
        Object expResult = null;
        AttributeStore<S> instance = this.givenEmptyStore();
        Object result = instance.getOrDefault(name, null);
        assertThat(result, is(expResult));
    }
    
    @Test
    public void getOrDefault_ShouldReturnExistingStoreEntry() {
        System.out.println("getOrDefault_ShouldReturnExistingStoreEntry");
        String name = "name";
        Object expResult = "value";
        AttributeStore<S> instance = this.givenStoreWithEntry(name, expResult);
        Object result = instance.getOrDefault(name, null);
        assertThat(result, is(expResult));
    }

    public AttributeStore<S> givenEmptyStore() {
        return this.getInstance(this.getBackingStore());
    }
    
    public AttributeStore<S> givenStoreWithEntry(S backingStore) {
        return this.getInstance(backingStore);
    }

    public AttributeStore<S> givenStoreWithEntry(String name, Object value) {
        S backingStore = this.getBackingStore(name, value);
        return this.getInstance(backingStore);
    }
    
    public AttributeStore<S> getInstance() {
        return this.getInstance(this.getBackingStore());
    }
    
    public AttributeStore<S> getInstance(S backingStore) {
        return context.getAttributeStore(backingStore);
    }
    
    public S getBackingStore() {
        return context.getBackingStore();
    }
    
    public S getBackingStore(String name, Object value) {
        return context.getBackingStore(name, value);
    }
    
    public int getSize(S backingStore){
        return context.getSize(backingStore);
    }
}
