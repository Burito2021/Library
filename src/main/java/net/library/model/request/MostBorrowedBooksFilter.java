package net.library.model.request;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Optional filters of the most borrowed books report. Bound and validated at the controller level:
 * a malformed date or year is rejected with 400, so the service only gets typed values.
 * A {@code null} field means the filter is not applied.
 */
public record MostBorrowedBooksFilter(
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actionFrom,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actionTo,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime createdFrom,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime createdTo,
        Integer publicationYearFrom,
        Integer publicationYearTo
) {

    @AssertTrue(message = "actionFrom must not be after actionTo")
    public boolean isActionRangeValid() {
        return actionFrom == null || actionTo == null || !actionFrom.isAfter(actionTo);
    }

    @AssertTrue(message = "createdFrom must not be after createdTo")
    public boolean isCreatedRangeValid() {
        return createdFrom == null || createdTo == null || !createdFrom.isAfter(createdTo);
    }

    @AssertTrue(message = "publicationYearFrom must not be greater than publicationYearTo")
    public boolean isPublicationYearRangeValid() {
        return publicationYearFrom == null || publicationYearTo == null || publicationYearFrom <= publicationYearTo;
    }
}
