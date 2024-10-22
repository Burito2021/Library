package net.library.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    private Integer pageSize;
    private Integer pageNumber;
    private long total;
    private List<T> items;

    public Page(Integer pageSize, Integer pageNumber, long totalItems, List<T> items) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.total = totalItems;
        this.items = items;
    }
}
