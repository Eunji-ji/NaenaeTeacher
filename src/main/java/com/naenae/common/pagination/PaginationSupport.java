package com.naenae.common.pagination;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationSupport {

    public static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_GROUP_SIZE = 5;

    private PaginationSupport() {
    }

    public static Pageable pageRequest(int page) {
        return pageRequest(page, DEFAULT_PAGE_SIZE);
    }

    public static Pageable pageRequest(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1));
    }

    public static <T> PageView<T> toView(Page<T> page) {
        int startPage = page.getTotalPages() == 0
                ? 0
                : (page.getNumber() / DEFAULT_PAGE_GROUP_SIZE) * DEFAULT_PAGE_GROUP_SIZE;
        int endPage = Math.min(startPage + DEFAULT_PAGE_GROUP_SIZE, page.getTotalPages());
        List<Integer> pageNumbers = IntStream.range(startPage, endPage).boxed().toList();
        return new PageView<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                pageNumbers
        );
    }
}
