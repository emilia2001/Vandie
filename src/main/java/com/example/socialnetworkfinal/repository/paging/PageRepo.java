package com.example.socialnetworkfinal.repository.paging;

import java.util.stream.Stream;

public interface PageRepo<E> {
    PageableRepo getPageable();

    PageableRepo nextPageable();

    Stream<E> getContent();


}