package com.looseboxes.webform.repositories;

import com.looseboxes.webform.domain.Blog;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author hp
 */
public interface BlogRepository extends PagingAndSortingRepository<Blog, Integer>{
}
