package ftn.siit.nvt.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class PaginatedResponse<T> implements Serializable {
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;
    private int numberOfElements;

    // Ovaj konstruktor automatski "vadi" sve potrebne podatke iz Spring-ove stranice
    public PaginatedResponse(Page<T> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
        this.empty = page.isEmpty();
        this.numberOfElements = page.getNumberOfElements();
    }
}