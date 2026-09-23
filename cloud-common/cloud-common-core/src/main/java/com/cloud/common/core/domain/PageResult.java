package com.cloud.common.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果。
 *
 * @param <T> 行数据类型
 */
@Getter
@Setter
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long total;
    private List<T> rows;

    public PageResult() {
        this.total = 0;
        this.rows = Collections.emptyList();
    }

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows == null ? Collections.emptyList() : rows;
    }

    public static <T> PageResult<T> of(long total, List<T> rows) {
        return new PageResult<>(total, rows);
    }
}
