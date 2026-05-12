package com.campus.activity.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    private long total;
    private long page;
    private long pageSize;
    private long totalPages;
    private List<T> records;

    public PageResult(long total, long page, long pageSize, List<T> records) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (total + pageSize - 1) / pageSize;
        this.records = records;
    }
}
