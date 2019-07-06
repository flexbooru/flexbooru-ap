package onlymash.flexbooru.ap.data.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.extension.NetResult

class CommentDataSource(
    private val scope: CoroutineScope,
    private val repo: CommentRepository,
    private val url: HttpUrl) : PageKeyedDataSource<Int, Comment>() {

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    //retry failed request
    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.invoke()
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Comment>
    ) {
        scope.launch {
            when (val result = repo.getComments(url)) {
                is NetResult.Error -> {
                    retry = {
                        loadInitial(params, callback)
                    }
                    val error = NetworkState.error(result.errorMsg)
                    networkState.postValue(error)
                    initialLoad.postValue(error)
                }
                is NetResult.Success -> {
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)
                    callback.onResult(result.data, null, null)
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Comment>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Comment>) {

    }
}