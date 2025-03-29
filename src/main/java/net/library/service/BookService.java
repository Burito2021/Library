package net.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.library.converter.Converter;
import net.library.exception.NotFoundException;
import net.library.model.dto.*;
import net.library.model.entity.Book;
import net.library.model.request.BookItemRequest;
import net.library.model.request.BookRequest;
import net.library.model.request.GenreRequest;
import net.library.model.response.BookItemMapper;
import net.library.model.response.BookMapper;
import net.library.model.response.GenreMapper;
import net.library.repository.*;
import net.library.repository.enums.BookItemStatus;
import net.library.util.Utils;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final BookItemRepository bookItemRepository;
    private final BookGenresRepository bookGenresRepository;
    private final BookItemHistoryRepository bookItemHistoryRepository;
    private final Converter converter;
    private static final int DUE_DATE = 14;

    /**
     * This method updates a random book item (in status of 'AVAILABLE') to mark it as borrowed by setting
     * STATUS to 'IN_PROGRESS'
     * the 'BORROWED_AT' timestamp and 'USER_ID' for the user borrowing the book.
     * Additionally, it creates an entry in `BookItemHistory` with details such as
     * 'userId', 'bookId', 'bookItemId', and the corresponding action status.
     * The method returns the updated entity from 'BOOK_ITEMS'.
     * If no available book item is found (i.e., no records match the 'AVAILABLE' status),
     * a 'NOT FOUND' error is thrown.
     */

    public BookItemDto addBookItem(final BookItemRequest bookItem) {
        final var bookItemConverted = converter.bookItemConverter(bookItem);
        final var savedBookItem = bookItemRepository.save(bookItemConverted);

        return BookItemMapper.toDto(savedBookItem);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE,propagation = Propagation.REQUIRED)
    public BookItemIdDto borrowActionForAnyBookItem(final UUID bookId, final UUID userId) {
        final var currentTime = Utils.currentDate();
        final var dueDate = Utils.currentDate().plusWeeks(DUE_DATE).toLocalDate();

        var bookItemIdList = bookItemRepository.selectAvailableBookItemIdAndUpdate(bookId, userId, currentTime, dueDate);

        if (bookItemIdList.isEmpty()) {
            throw new NotFoundException("User" + userId + " not found");
        }
        var bookItemId = bookItemIdList.get(0);

        return new BookItemIdDto(bookItemId);
    }

    /**
     *
     * This method updates a specific book item (by specific bookItemId received from incoming param)
     * to mark it as 'IN_PROGRESS' in STATUS and setting the 'BORROWED_AT' timestamp and 'USER_ID' for the user borrowing the book.
     * Additionally, it creates an entry in 'BookItemHistory' with details such as
     * 'userId', 'bookId', 'bookItemId', and the corresponding action status.
     * The method does not return ana entity.
     * If no available book item is found
     * a 'NOT FOUND' error is thrown.
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void borrowActionBookItemById(final UUID bookItemId, final UUID userId, final BookItemStatus bookItemStatus) {
        final var currentTime = Utils.currentDate();
        final var dueDate = Utils.currentDate().plusDays(DUE_DATE).toLocalDate();
        final var result = bookItemRepository.borrowAction(bookItemId, userId, bookItemStatus, currentTime, dueDate);

        if (result == 0) {
            throw new NotFoundException("User" + userId + " not found");
        }
    }

    /**
     *
     * This method updates a specific book item (by specific bookItemId received from incoming param)
     * by a specific user (by specific userId received from incoming param)
     * to mark it as returned by setting STATUS to 'AVAILABLE'
     * and 'RETURNED_AT' timestamp and updating  'UPDATED_AT'
     * and removing 'DUE_DATE' for the user borrowing the book.
     * Additionally, it creates an entry in `BookItemHistory` with details such as
     * 'userId', 'bookId', 'bookItemId', and the corresponding action status.
     * If no available book item or user is found,
     * a 'NOT FOUND' error is thrown.
     */

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void returnActionForBookItem(final UUID bookItemId, final UUID userId) {
        final var currentTime = Utils.currentDate();
        final var result = bookItemRepository.returnAction(bookItemId, userId, currentTime);

        if (result == 0) {
            throw new NotFoundException("User" + userId + " not found");
        }
    }

    public GenreDto addGenre(final GenreRequest genreRequest) {
        final var genreConverted = converter.genreConverter(genreRequest);
        return GenreMapper.toDto(genreRepository.save(genreConverted));
    }

    public BookDto addBook(final BookRequest bookRequest) {
        final var newBook = converter.bookConverter(bookRequest);
        return BookMapper.toDto(bookRepository.save(newBook));
    }

    public AllBookDto getById(UUID bookId){
      final var book =bookRepository.findById(bookId)
              .orElseThrow(() -> new NotFoundException("Book not found with id: " + bookId));

      return new AllBookDto(
              book.getId(),
              book.getTitle(),
              book.getAuthor(),
              book.getDescription(),
              book.getEdition(),
              book.getPublicationYear(),
              book.getUpdatedAt(),
              book.getCreatedAt(),
              book.getDeletedAt(),
              Optional.ofNullable(book.getBookItemList()).orElse(Collections.emptyList()).stream().map(
                      bookItem -> new BookItemDto(
                              bookItem.getId(),
                              Optional.ofNullable(bookItem.getUserId()).map(user -> new UserDto(
                                      bookItem.getUserId().getId(),
                                      bookItem.getUserId().getUsername(),
                                      bookItem.getUserId().getName(),
                                      bookItem.getUserId().getSurname(),
                                      bookItem.getUserId().getEmail(),
                                      bookItem.getUserId().getPhoneNumber(),
                                      bookItem.getUserId().getAddress()
                              )).orElse(null),
                              bookItem.getStatus(),
                              bookItem.getBorrowedAt(),
                              bookItem.getReturnedAt()
                      )).collect(Collectors.toList()),
              Optional.ofNullable(book.getBookGenres()).orElse(Collections.emptyList()).stream().map(
                      genre -> new BookGenreDto(
                              genre.getGenre().getName()
                      )).collect(Collectors.toList())
      );
    }

    public Page<AllBookDto> getAllEntities(Pageable pageable) {

        final var bookPage = bookRepository.findAll(pageable);
        final var page = bookPage.map(item ->
                new AllBookDto(item.getId(),
                        item.getTitle(),
                        item.getAuthor(),
                        item.getDescription(),
                        item.getEdition(),
                        item.getPublicationYear(),
                        item.getUpdatedAt(),
                        item.getCreatedAt(),
                        item.getDeletedAt(),
                        Optional.ofNullable(item.getBookItemList()).orElse(Collections.emptyList()).stream().map(
                                bookItem -> new BookItemDto(
                                        bookItem.getId(),
                                        Optional.ofNullable(bookItem.getUserId()).map(user -> new UserDto(
                                                bookItem.getUserId().getId(),
                                                bookItem.getUserId().getUsername(),
                                                bookItem.getUserId().getName(),
                                                bookItem.getUserId().getSurname(),
                                                bookItem.getUserId().getEmail(),
                                                bookItem.getUserId().getPhoneNumber(),
                                                bookItem.getUserId().getAddress()
                                        )).orElse(null),
                                        bookItem.getStatus(),
                                        bookItem.getBorrowedAt(),
                                        bookItem.getReturnedAt()
                                )).collect(Collectors.toList()),
                        Optional.ofNullable(item.getBookGenres()).orElse(Collections.emptyList()).stream().map(
                                genre -> new BookGenreDto(
                                        genre.getGenre().getName()
                                )).collect(Collectors.toList())
                )
        );

        return page;
    }

    public Page<GenreDto> getGenres(Pageable pageable) {

        var genre = genreRepository.findAll(pageable);
        return converter.toGenreDtoPage(genre);
    }

    public Book getBookById(final UUID bookId) {
        return bookRepository.findById(bookId).orElseThrow();
    }

    public void removeBookById(final UUID bookId) {
        bookRepository.deleteById(bookId);
    }

    public void removeAllBooks() {
        bookRepository.deleteAll();
    }

    public void removeAllBookItems() {
        bookItemRepository.deleteAll();
    }

    public void removeAllGenres() {
        genreRepository.deleteAll();
    }

    public void removeAllBookGenres() {
        bookGenresRepository.deleteAll();
    }

    public void removeAllHistory() {
        bookItemHistoryRepository.deleteAll();
    }

    public void removeAll() {
        removeAllHistory();
        removeAllGenres();
        removeAllBookGenres();
        removeAllBookItems();
        removeAllBooks();
    }


    public Page<BookItemDto> getBookItems(UUID bookItemId, UUID bookId, String bookItemStatus, String startDate, String endDate, Pageable pageable
    ) {
        var startDateConverted = Utils.stringToLocalDateConverter(startDate);
        var endDateConverted = Utils.stringToLocalDateConverter(endDate);
        var bookItemStatusEnum = Utils.convertToEnum(bookItemStatus, BookItemStatus.class);

        var specification = BookItemSpecification.filterBookItem(bookItemId, bookId, bookItemStatusEnum, startDateConverted, endDateConverted);
        var bookPage = bookItemRepository.findAll(specification, pageable);
        return converter.toBookItemDto(bookPage);
    }
}