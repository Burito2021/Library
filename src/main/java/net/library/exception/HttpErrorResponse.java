package net.library.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
public class HttpErrorResponse {
    private String cid;
    private Integer errorId;
    private String errorMsg;

    public HttpErrorResponse(String cid, Integer errorId, String errorMsg) {
        this.cid = cid;
        this.errorId = errorId;
        this.errorMsg = errorMsg;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public Integer getErrorId() {
        return errorId;
    }

    public void setErrorId(Integer errorId) {
        this.errorId = errorId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
