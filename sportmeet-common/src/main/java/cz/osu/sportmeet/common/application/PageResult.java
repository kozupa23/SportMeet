package cz.osu.sportmeet.common.application;

import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Persistence-independent page of results shared by all modules.
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
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages)
{
    public PageResult
    {
        content = List.copyOf(content);
    }
}
