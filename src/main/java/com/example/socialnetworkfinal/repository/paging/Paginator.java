package com.example.socialnetworkfinal.repository.paging;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Paginator<E> {
    private PageableRepo pageable;
    private Iterable<E> elements;

    public Paginator(PageableRepo pageable, Iterable<E> elements) {
        this.pageable = pageable;
        this.elements = elements;
    }

    public PageRepo<E> paginate() {
        Stream<E> result = StreamSupport.stream(elements.spliterator(), false)
                .skip(pageable.getPageNumber()  * pageable.getPageSize())
                .limit(pageable.getPageSize());
        return new PageRepoImplementation<>(pageable, result);
    }
}
