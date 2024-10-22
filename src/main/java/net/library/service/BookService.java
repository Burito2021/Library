package net.library.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.library.converter.*;
import net.library.exception.UserNotFoundException;
import net.library.model.dto.BookDto;
import net.library.model.dto.BookGenreDto;
import net.library.model.dto.BookItemDto;
import net.library.model.dto.GenreDto;
import net.library.model.entity.Book;
import net.library.model.request.BookGenreRequest;
import net.library.model.request.BookItemRequest;
import net.library.model.request.BookRequest;
import net.library.model.request.GenreRequest;
import net.library.model.response.BookGenreMapper;
import net.library.model.response.BookItemMapper;
import net.library.model.response.BookMapper;
import net.library.model.response.GenreMapper;
import net.library.repository.*;
import net.library.repository.enums.BookAction;
import net.library.repository.enums.BookItemStatus;
import net.library.util.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    @PersistenceContext
    private EntityManager entityManager;

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final BookItemRepository bookItemRepository;
    private final BookGenresRepository bookGenresRepository;
    private final BookItemHistoryRepository bookItemHistoryRepository;

    @Transactional
    public void updateBookItemBorrow(final UUID bookItemId, final UUID userId, final BookItemStatus bookItemStatus) {
        final var currentTime = Utils.currentDate();
        final var result = bookItemRepository.updateBookItemBorrow(bookItemId, userId, bookItemStatus, currentTime);
        final var bookItemHistory = BookItemHistoryConverter.of(bookItemId, userId, currentTime, BookAction.BORROWED);
        bookItemHistoryRepository.save(bookItemHistory);
        if (result == 0) {
            throw new UserNotFoundException("User" + userId + " not found");
        }
    }

    @Transactional
    public void updateBookItemReturn(final UUID bookItemId, final UUID userId) {
        final var currentTime = Utils.currentDate();
        final var result = bookItemRepository.updateBookItemReturn(bookItemId, userId, currentTime);

        final var bookItemHistory = BookItemHistoryConverter.of(bookItemId, userId, currentTime, BookAction.RETURNED);
        bookItemHistoryRepository.save(bookItemHistory);
        if (result == 0) {
            throw new UserNotFoundException("User" + userId + " not found");
        }
    }

    //    public void updateBookItemToBorrowed(final BookItemRequest bookItem) {
//        final var bookItemConverted = BookItemConverter.of(bookItem);
//        bookItemRepository.save(bookItemConverted);
//    }
    public BookItemDto addBookItem(final BookItemRequest bookItem) {
        final var bookItemConverted = BookItemConverter.of(bookItem);
        final var savedBookItem = bookItemRepository.save(bookItemConverted);

        return BookItemMapper.toDto(savedBookItem);
    }

    public GenreDto addGenre(final GenreRequest genreRequest) {
        final var genreConverted = GenreConverter.of(genreRequest);
        return GenreMapper.toDto(genreRepository.save(genreConverted));
    }

    public BookDto addBook(final BookRequest bookRequest) {
        final var newBook = BookConverter.of(bookRequest);
        return BookMapper.toDto(bookRepository.save(newBook));
    }

    public List<BookGenreDto> addBookGenres(final BookGenreRequest bookGenreRequest) {
        final var bookGenresList = BookGenresConverter.of(bookGenreRequest);

        return BookGenreMapper.toDto(bookGenresRepository.saveAll(bookGenresList));
    }

    public Page<BookItemDto> getAllAvailableBooks(UUID bookItemId, String bookItemStatus, String startDate, String endDate, Pageable pageable
    ) {
        var startDateConverted = Utils.stringToLocalDateConverter(startDate);
        var endDateConverted = Utils.stringToLocalDateConverter(endDate);
        var bookItemStatusEnum = Utils.convertToEnum(bookItemStatus, BookItemStatus.class);

        var specification = BookItemSpecification.filterBookItemByStatus(bookItemId, bookItemStatusEnum, startDateConverted, endDateConverted);
        var bookPage = bookItemRepository.findAll(specification, pageable);

        return bookPage.map(BookItemMapper::toDto);
    }

    public Page<GenreDto> getGenres(Pageable pageable) {

        var genre = genreRepository.findAll(pageable);
        return genre.map(GenreMapper::toDto);
    }

    public Page<BookDto> getAllBooks(
            Pageable pageable
    ) {

        var bookPage = bookRepository.findAll(pageable);

        return bookPage.map(BookMapper::toDto);
    }


    public Page<BookGenreDto> getBookGenre(
            Pageable pageable
    ) {
        var bookGenres = bookGenresRepository.findAll(pageable);
        return bookGenres.map(BookGenreMapper::toDto);
    }

    public Book getBookById(final UUID bookId) {
        return bookRepository.findById(bookId).orElseThrow();
    }

    public void deleteBookById(final UUID bookId) {
        bookRepository.deleteById(bookId);
    }

    public void deleteAllBooks() {
        bookRepository.deleteAll();
    }

    public void deleteAllBookItems() {
        bookItemRepository.deleteAll();
    }

    public void deleteAllGenres() {
        genreRepository.deleteAll();
    }

    public void deleteAllBookGenres() {
        bookGenresRepository.deleteAll();
    }

    public void deleteAllHistory() {
        bookItemHistoryRepository.deleteAll();
    }

    public void deleteAll() {
        deleteAllHistory();
        deleteAllGenres();
        deleteAllBookGenres();
        deleteAllBookItems();
        deleteAllBooks();
    }
}
