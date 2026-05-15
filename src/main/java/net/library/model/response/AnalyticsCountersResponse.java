package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsCountersResponse {

    @JsonProperty("totalBooks")
    private long totalBooks;

    @JsonProperty("totalAuthors")
    private long totalAuthors;

    @JsonProperty("totalBorrows")
    private long totalBorrows;
}
