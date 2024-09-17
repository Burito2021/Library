package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {

    @JsonProperty("x-Correlation-Id")
    private String xCorrelationId;

    @JsonProperty("data")
    private T data;

    public ApiResponse(String xCorrelationId, T data) {
        this.xCorrelationId = xCorrelationId;
        this.data = data;
    }

    public String getxCorrelationId() {
        return xCorrelationId;
    }

    public void setxCorrelationId(String xCorrelationId) {
        this.xCorrelationId = xCorrelationId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
