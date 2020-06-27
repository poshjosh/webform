package com.looseboxes.webform.entity;

import java.util.Objects;

/**
 * Class to use in configuring the model object when it is first created for 
 * the form.
 * 
 * This class could be used to initialize some defaults on the model object. 
 * For example we could set the current logged in user if there is a field and/
 * accessor methods for that on the model object.
 * 
 * <p><b>Example usage:</b></p>
 * <code>
 * <pre>
    @Configuration
    public class WebformConfigurerImpl implements WebformConfigurer{
        private static class PostPreconfigurer implements ModelObjectConfigurer<Post>{
            @Override
            public Post configure(Post post) {
                // Configure the Post here
                return post;
            }
        }

        @Override
        public void addModelObjectConfigurers(ModelObjectConfigurerService service) {
            service.addConfigurer(Post.class, new PostPreconfigurer());
        }
    }
 * </pre>
 * </code>
 * 
 * @author hp
 */
public interface EntityConfigurer<T>{
   
    /**
     * @param modelobject The model object to configure
     * @return The configured model object
     */
    T configure(T modelobject);

    default EntityConfigurer<T> andThen(EntityConfigurer<T> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.configure(configure(t));
    }
}
