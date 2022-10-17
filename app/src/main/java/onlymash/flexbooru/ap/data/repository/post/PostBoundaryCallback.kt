package onlymash.flexbooru.ap.data.repository.post

import androidx.annotation.MainThread
import androidx.paging.PagedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.PagingRequestHelper
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.createStatusLiveData
import onlymash.flexbooru.ap.extension.getPostsUrl
import retrofit2.HttpException

class PostBoundaryCallback(
    private val scope: CoroutineScope,
    private val api: Api,
    private val search: Search,
    private val handleResponse: (query: String, List<Post>) -> Unit
) : PagedList.BoundaryCallback<Post>() {

    val helper = PagingRequestHelper()

    val networkState = helper.createStatusLiveData()

    var lastResponseSize = search.limit

    private fun execute(page: Int, callback: PagingRequestHelper.Callback) {
        scope.launch {
            when (val result: NetResult<List<Post>> = withContext(Dispatchers.IO) {
                try {
                    val response = api.getPosts(url = search.getPostsUrl(page = page))
                    if (response.isSuccessful) {
                        NetResult.Success(response.body()?.posts ?: listOf())
                    } else {
                        NetResult.Error("code: ${response.code()}")
                    }
                } catch (e: Exception) {
                    if (e is HttpException) {
                        NetResult.Error("code: ${e.code()}")
                    } else {
                        NetResult.Error(e.message.toString())
                    }
                }
            }) {
                is NetResult.Success -> {
                    val data = result.data
                    lastResponseSize = data.size
                    withContext(Dispatchers.IO) {
                        callback.recordSuccess()
                        handleResponse(search.query, data)
                    }
                }
                is NetResult.Error -> {
                    callback.recordFailure(Throwable(result.errorMsg))
                }
            }
        }
    }

    @MainThread
    override fun onZeroItemsLoaded() {
        scope.launch {
            helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
                execute(0, it)
            }
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Post) {
        scope.launch {
            if (lastResponseSize == search.limit) {
                helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                    execute((itemAtEnd.indexInResponse + 1) / search.limit, it)
                }
            }
        }
    }

    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: Post) {

    }
}