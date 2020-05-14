package com.looseboxes.webform.repositories;

import com.looseboxes.webform.domain.Post;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author hp
 */
public interface PostRepository extends PagingAndSortingRepository<Post, Integer>{
    
}
