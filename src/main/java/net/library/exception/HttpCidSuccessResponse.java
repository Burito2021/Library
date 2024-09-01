package net.library.exception;

import java.util.Objects;

public class HttpCidSuccessResponse {

    private String cid;

    public static HttpCidSuccessResponse of(final String cid) {
        final var response = new HttpCidSuccessResponse();
        response.setCid(cid);

        return response;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(final String cid) {
        this.cid = cid;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpCidSuccessResponse that)) {
            return false;
        }
        return Objects.equals(getCid(), that.getCid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCid());
    }

}

