package onlymash.flexbooru.ap.data.repository.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.CommentAll
import onlymash.flexbooru.ap.extension.getAllCommentsUrl

class CommentAllSource(
    private val api: Api,
    private val token: String) : PagingSource<Int, CommentAll>() {

    override fun getRefreshKey(state: PagingState<Int, CommentAll>): Int? {
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)
        }
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CommentAll> {
        val page = params.key ?: return  LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )
        return try {
            val data = api.getAllComments(url = getAllCommentsUrl(page = page, token = token))
            LoadResult.Page(
                data = data.comments,
                prevKey = if (page > 1) page else null,
                nextKey = if (data.comments.size == PAGE_SIZE) page + 1 else null
            )
        } catch ( e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        const val PAGE_SIZE = 30
    }
}