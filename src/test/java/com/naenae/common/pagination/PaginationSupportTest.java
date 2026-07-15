package com.naenae.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PaginationSupportTest {

    @Test
    void negativePageIsNormalizedToFirstPage() {
        assertThat(PaginationSupport.pageRequest(-3).getPageNumber()).isZero();
        assertThat(PaginationSupport.pageRequest(-3).getPageSize())
                .isEqualTo(PaginationSupport.DEFAULT_PAGE_SIZE);
    }

    @Test
    void pageNumbersAreCreatedInReusableFivePageGroups() {
        var source = new PageImpl<>(List.of("item"), PageRequest.of(6, 10), 123);

        PageView<String> page = PaginationSupport.toView(source);

        assertThat(page.number()).isEqualTo(6);
        assertThat(page.totalPages()).isEqualTo(13);
        assertThat(page.pageNumbers()).containsExactly(5, 6, 7, 8, 9);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }
}