package net.library.object;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class Book {

    private String book;
    @NotEmpty
    @NotNull
    private String title;
    @NotNull
    @NotEmpty
    private String author;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
