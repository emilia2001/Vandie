package com.example.socialnetworkfinal.repository.paging;

import java.util.stream.Stream;

public class PageRepoImplementation<T> implements PageRepo<T> {
    private PageableRepo pageable;
    private Stream<T> content;

    PageRepoImplementation(PageableRepo pageable, Stream<T> content) {
        this.pageable = pageable;
        this.content = content;
    }

    @Override
    public PageableRepo getPageable() {
        return this.pageable;
    }

    @Override
    public PageableRepo nextPageable() {
        return new PageableRepoImplementation(this.pageable.getPageNumber() + 1, this.pageable.getPageSize());
    }

    @Override
    public Stream<T> getContent() {
        return this.content;
    }
}

