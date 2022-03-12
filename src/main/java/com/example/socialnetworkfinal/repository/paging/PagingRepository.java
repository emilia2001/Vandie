package com.example.socialnetworkfinal.repository.paging;

import com.example.socialnetworkfinal.domain.Entity;
import com.example.socialnetworkfinal.repository.Repository;

public interface PagingRepository<ID ,
        E extends Entity<ID>>
        extends Repository<ID, E> {

    PageRepo<E> findAll(PageableRepo pageable);   // Pageable e un fel de paginator
}

