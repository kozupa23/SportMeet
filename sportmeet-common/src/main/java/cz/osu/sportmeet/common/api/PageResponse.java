package cz.osu.sportmeet.common.api;

import com.google.common.base.Preconditions;
import cz.osu.sportmeet.common.application.PageResult;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Function;

/**
 * REST representation of one page of results, shared by all modules.
 *
 * @param content       items on the requested page
 * @param page          zero-based page index
 * @param size          requested page size
 * @param totalElements total number of items across all pages
 * @param totalPages    total number of pages
 *
 * @author Pavel Kozumplik
 * @since 0.1.0
 */
@NullMarked
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages)
{
    public PageResponse
    {
        content = List.copyOf(content);
    }

    /**
     * Converts an application page into its REST representation.
     *
     * @param result page returned by an application facade
     * @param mapper conversion of a single application item into its REST DTO
     * @return REST page with the same paging metadata
     */
    public static <S, T> PageResponse<T> from(PageResult<S> result, Function<? super S, ? extends T> mapper)
    {
        Preconditions.checkNotNull(result, "result must not be null");
        Preconditions.checkNotNull(mapper, "mapper must not be null");

        List<T> content = result.content().stream().<T>map(mapper).toList();

        return new PageResponse<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
