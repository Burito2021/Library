package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
public class HttpErrorResponse {

    @JsonProperty("cid")
    private String cid;

    @JsonProperty("errorId")
    private Integer errorId;

    @JsonProperty("errorMsg")
    private String errorMsg;
}
