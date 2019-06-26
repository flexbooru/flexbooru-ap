package onlymash.flexbooru.ap.data.repository.post

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.Listing
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getPostsUrl
import retrofit2.HttpException
import java.lang.Exception
import java.util.concurrent.Executor

class PostRepositoryImpl(
    private val scope: CoroutineScope,
    private val db: MyDatabase,
    private val api: Api,
    private val ioExecutor: Executor
) : PostRepository {

    private var boundaryCallback: PostBoundaryCallback? = null

    //IO thread
    private fun insertResultIntoDb(query: String, posts: List<Post>) {
        if (posts.isEmpty()) return
        val start = db.postDao().getNextIndex(query)
        val items = posts.mapIndexed { index, post ->
            post.query = query
            post.indexInResponse = start + index
            post
        }
        db.postDao().insert(items)
    }

    @MainThread
    override fun getPosts(search: Search): Listing<Post> {
        boundaryCallback = PostBoundaryCallback(
            api = api,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor,
            scope = scope,
            search = search
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh(search)
        }
        val livePagedList = db.postDao()
            .getPosts(search.query)
            .toLiveData(
                config = Config(
                    pageSize = search.limit,
                    enablePlaceholders = true
                ),
                boundaryCallback = boundaryCallback
            )
        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback!!.networkState,
            retry = {
                boundaryCallback!!.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refresh(search: Search): LiveData<NetworkState> {
        boundaryCallback?.lastResponseSize = search.limit
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = api.getPosts(search.getPostsUrl(0))
                    if (response.isSuccessful) {
                        val posts = response.body()?.posts
                        if (posts != null) {
                            db.runInTransaction {
                                db.postDao().deletePosts(search.query)
                                insertResultIntoDb(search.query, posts)
                            }
                        }
                        NetResult.Success(true)
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
                is NetResult.Success -> networkState.postValue(NetworkState.LOADED)
                is NetResult.Error -> networkState.value = NetworkState.error(result.errorMsg)
            }
        }
        return networkState
    }

}