package com.looseboxes.webform.configurers;

import com.looseboxes.webform.web.FormRequest;
import java.util.Objects;

/**
 * Class to use in configuring the model object when it is first created for 
 * the form.
 * 
 * <p>
 * This class could be used to initialize some defaults on the model object. 
 * For example we could set the current logged in user if there is a field and/
 * access methods for that on the model object.
 * </p>
 * 
 * <p>
 * Configuration of model objects happens at the earlier part of all 
 * {@link com.looseboxes.webform.FormStage FormStage}s (i.e begin/validate/submit) 
 * and only for create/update {@link com.looseboxes.webform.CRUDAction CRUDAction}s 
 * not read/delete. 
 * </p>
 * 
 * <p><b>Example usage:</b></p>
 * 
 * <pre>
 *  @Configuration
 *  public class WebformConfigurerImpl implements WebformConfigurer{
 *      private static class PostPreconfigurer implements EntityConfigurer{
 *          @Override
 *          public Post configure(Post post, FormRequest formRequest) {
 *              // Configure the Post here
 *              return post;
 *          }
 *      }
 * 
 *      @Override
 *      public void addModelObjectConfigurers(EntityConfigurerService service) {
 *          service.addConfigurer(Post.class, new PostPreconfigurer());
 *      }
 *  }
 * </pre>
 * @author hp
 */
public interface EntityConfigurer<T>{
   
    /**
     * Configure the domain/model object
     * <p>
     * This method is called at the earlier part of all 
     * {@link com.looseboxes.webform.FormStage FormStage}s (i.e begin/validate/submit) 
     * and only for create/update {@link com.looseboxes.webform.CRUDAction CRUDAction}s 
     * not read/delete. 
     * </p>
     * @param entity The domain object to configure
     * @param formRequest The object to use in configuring the domain object
     * @return The configured model object
     */
    T configure(T entity, FormRequest<T> formRequest);

    default EntityConfigurer<T> andThen(EntityConfigurer<T> after) {
        Objects.requireNonNull(after);
        return (T t, FormRequest<T> req) -> after.configure(configure(t, req), req);
    }
}
