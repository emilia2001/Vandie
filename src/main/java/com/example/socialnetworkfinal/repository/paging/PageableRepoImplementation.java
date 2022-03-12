package com.example.socialnetworkfinal.repository.paging;

public class PageableRepoImplementation implements PageableRepo {

    private int pageNumber;
    private int pageSize;

    public PageableRepoImplementation(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    @Override
    public int getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }
}
