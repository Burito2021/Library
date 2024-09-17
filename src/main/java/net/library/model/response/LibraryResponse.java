package net.library.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class LibraryResponse {

    @JsonProperty("cid")
    private String cid;

    public static LibraryResponse of(final String cid) {
        final var response = new LibraryResponse();
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
        if (!(o instanceof LibraryResponse that)) {
            return false;
        }
        return Objects.equals(getCid(), that.getCid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCid());
    }

}

