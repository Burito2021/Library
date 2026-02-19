package net.library.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class RefreshToken implements Serializable {
    private String t;
    private String f;
    private LocalDateTime c;

    public RefreshToken() {
        this.c = LocalDateTime.now();
    }
}
