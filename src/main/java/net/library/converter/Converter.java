package net.library.converter;

import net.library.model.dto.*;
import net.library.model.entity.*;
import net.library.model.request.BookGenreRequest;
import net.library.model.request.BookItemRequest;
import net.library.model.request.BookRequest;
import net.library.model.request.GenreRequest;
import net.library.repository.enums.BookAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface Converter {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(source = "publication", target = "publicationYear")
    Book bookConverter(BookRequest bookRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "borrowedAt", ignore = true)
    @Mapping(target = "returnedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    BookItem bookItemConverter(BookItemRequest bookItemRequest);

    @Mapping(target = "id", ignore = true)
    Genre genreConverter(GenreRequest genreRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "bookItemId", target = "itemId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "currentTime", target = "actionAt")
    @Mapping(source = "action", target = "actionType")
    BookItemHistory bookItemHistoryConverter(UUID bookItemId, UUID userId, LocalDateTime currentTime, BookAction action);

    @Mapping(source = "id", target = "bookItemId")
    BookItemIdDto bookItemIdDtoConverter(BookItem bookItem);

    @Mapping(source = "id", target = "genreId")
    @Mapping(source = "name", target = "genreName")
    GenreDto genreDtoConverter(Genre genre);

    default Page<GenreDto> toGenreDtoPage(Page<Genre> genres) {
        return genres.map(this::genreDtoConverter);
    }

    @Mapping(source = "id", target = "bookItemId")
    BookItemDto bookItemDtoConverter(BookItem bookItem);

    default Page<BookItemDto> toBookItemDto(Page<BookItem> bookItem) {
        return bookItem.map(this::bookItemDtoConverter);
    }
}