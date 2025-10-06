package net.library.service;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.library.converter.Converter;
import net.library.exception.NotFoundException;
import net.library.model.dto.BookGenreDto;
import net.library.model.dto.BookItemDto;
import net.library.model.dto.BookItemIdDto;
import net.library.model.dto.UserDto;
import net.library.model.mapper.BookItemMapper;
import net.library.model.mapper.BookMapper;
import net.library.model.request.BookItemRequest;
import net.library.model.request.BookRequest;
import net.library.model.response.AddBookResponse;
import net.library.model.response.BookResponse;
import net.library.repository.*;
import net.library.repository.enums.BookItemStatus;
import net.library.util.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    private static final int DUE_DATE = 14;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final BookItemRepository bookItemRepository;
    private final BookGenresRepository bookGenresRepository;
    private final BookItemHistoryRepository bookItemHistoryRepository;
    private final Converter converter;

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
        var bookItemConverted = converter.bookItemConverter(bookItem);
        var savedBookItem = bookItemRepository.save(bookItemConverted);

        return BookItemMapper.toDto(savedBookItem);
    }

    @Transactional
    public BookItemIdDto borrowActionForAnyBookItem(final UUID bookId, final UUID userId) {
        var currentTime = Utils.currentDate();
        var dueDate = Utils.currentDate().plusWeeks(DUE_DATE).toLocalDate();

        var bookItemIdList = bookItemRepository.selectAvailableBookItemIdAndUpdate(bookId, userId, currentTime, dueDate);

        if (bookItemIdList.isEmpty()) {
            throw new NotFoundException("User" + userId + " not found");
        }

        var bookItemId = bookItemIdList.getFirst();

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
        var currentTime = Utils.currentDate();
        var dueDate = Utils.currentDate().plusDays(DUE_DATE).toLocalDate();

        var result = bookItemRepository.borrowAction(bookItemId, userId, bookItemStatus, currentTime, dueDate, BookItemStatus.AVAILABLE);

        if (result == 0) {
            throw new NotFoundException("User" + userId + " not found");
        }
    }

    @Lock(LockModeType.OPTIMISTIC)
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public void returnActionForBookItemHibernateImpl(final UUID bookItemId, final UUID userId) {
        var status = TransactionAspectSupport.currentTransactionStatus();
        String transactionId = String.valueOf(System.identityHashCode(status));
        boolean newTransaction = status.isNewTransaction();

        log.info("Transaction info [id={}, isNew={}, bookItemId={}]",
                transactionId, newTransaction, bookItemId);

        var currentTime = Utils.currentDate();
        var bookItem = bookItemRepository.findByIdAndUserId_Id(bookItemId, userId).orElseThrow(
                () -> new NotFoundException("Book item not found with id: " + bookItemId));

        bookItem.setStatus(BookItemStatus.AVAILABLE);
        bookItem.setReturnedAt(currentTime);
        bookItem.setBorrowedAt(null);
        bookItem.setDueDate(null);

        log.info("Saving book item in transaction [id={}, isNew={}]", transactionId, newTransaction);
        bookItemRepository.save(bookItem);

        log.info("Transaction completed [id={}]", transactionId);
    }

    /**
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
    @Transactional
    public void returnActionForBookItem(final UUID bookItemId, final UUID userId) {
        var currentTime = Utils.currentDate();
        var result = bookItemRepository.returnAction(bookItemId, userId, currentTime);

        if (result == 0) {
            throw new NotFoundException("User" + userId + " not found");
        }
    }

    public AddBookResponse addBook(final BookRequest bookRequest) {
        var newBook = converter.bookConverter(bookRequest);
        return BookMapper.toDto(bookRepository.save(newBook));
    }

    public BookResponse getById(UUID bookId) {
        var book = bookRepository.findById(bookId)
              .orElseThrow(() -> new NotFoundException("Book not found with id: " + bookId));

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .edition(book.getEdition())
                .publicationYear(book.getPublicationYear())
                .updatedAt(book.getUpdatedAt())
                .createdAt(book.getCreatedAt())
                .deletedAt(book.getDeletedAt())
                .bookItems(Optional.ofNullable(book.getBookItemList()).orElse(Collections.emptyList()).stream().map(
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
                        )).collect(Collectors.toList()))
                .genres(Optional.ofNullable(book.getBookGenres()).orElse(Collections.emptyList()).stream().map(
                        genre -> new BookGenreDto(
                                genre.getGenre().getName()
                        )).collect(Collectors.toList()))
                .build();
    }

    public Page<BookResponse> getAllEntities(Pageable pageable) {

        var bookPage = bookRepository.findAll(pageable);
        var page = bookPage.map(item ->
                BookResponse.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .author(item.getAuthor())
                        .description(item.getDescription())
                        .edition(item.getEdition())
                        .publicationYear(item.getPublicationYear())
                        .updatedAt(item.getUpdatedAt())
                        .createdAt(item.getCreatedAt())
                        .deletedAt(item.getDeletedAt())
                        .bookItems(Optional.ofNullable(item.getBookItemList()).orElse(Collections.emptyList()).stream().map(
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
                                )).collect(Collectors.toList()))
                        .genres(Optional.ofNullable(item.getBookGenres()).orElse(Collections.emptyList()).stream().map(
                                genre -> new BookGenreDto(
                                        genre.getGenre().getName()
                                )).collect(Collectors.toList())
                        )
                        .build()
        );

        return page;
    }

    public void removeBookById(UUID bookId) {
        bookRepository.deleteById(bookId);
    }

    public void removeBookItemById(UUID bookItemId) {
        bookItemRepository.deleteById(bookItemId);
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