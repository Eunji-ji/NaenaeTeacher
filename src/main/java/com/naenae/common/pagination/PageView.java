package com.naenae.common.pagination;

import java.util.List;

public record PageView<T>(
        List<T> content,
        int number,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last,
        List<Integer> pageNumbers
) {
    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean hasPrevious() {
        return !first;
    }

    public boolean hasNext() {
        return !last;
    }
}