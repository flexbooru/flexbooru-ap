package onlymash.flexbooru.ap.data.repository.post

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.extension.getPostsUrl

class PostRemoteMediator(
    private val api: Api,
    private val search: Search,
    private val db: MyDatabase
) : RemoteMediator<Int, Post>() {

    private var lastResponseSize = search.limit
    private val hasMore get() = lastResponseSize == search.limit

    private val nextIndex: Int
        get() = db.postDao().getNextIndex(search.query)

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Post>): MediatorResult {
        var indexNext = nextIndex
        val data = when (loadType) {
            LoadType.REFRESH -> {
                indexNext = 0
                try {
                    api.getPosts(url = search.getPostsUrl(0))
                } catch (e: Exception) {
                    return MediatorResult.Error(e)
                }
            }
            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                if (!hasMore && indexNext != 0) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                try {
                    api.getPosts(url = search.getPostsUrl((indexNext + 1) / search.limit))
                } catch (e: Exception) {
                    return MediatorResult.Error(e)
                }
            }
        }
        lastResponseSize = data.posts.size
        data.posts.forEachIndexed { index, post ->
            post.index = index + indexNext
        }
        try {
          db.withTransaction {
              if (loadType == LoadType.REFRESH) {
                  db.postDao().deletePosts(search.query)
              }
              db.postDao().insert(data.posts)
          }
        } catch ( _: Exception) {

        }
        return MediatorResult.Success(endOfPaginationReached = !hasMore)
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }
}