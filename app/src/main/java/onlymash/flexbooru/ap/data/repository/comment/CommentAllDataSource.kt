package onlymash.flexbooru.ap.data.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.CommentAll
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getAllCommentsUrl
import retrofit2.HttpException

class CommentAllDataSource(
    private val scope: CoroutineScope,
    private val api: Api,
    private val token: String) : PageKeyedDataSource<Int, CommentAll>() {

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
        callback: LoadInitialCallback<Int, CommentAll>
    ) {
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = api.getAllComments(
                        url = getAllCommentsUrl(page = 0, token = token))
                    val data = response.body()?.comments
                    if (data != null) {
                        NetResult.Success(data)
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
                    callback.onResult(result.data, null, 1)
                }
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, CommentAll>) {
        val page = params.key
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                try {
                    val response = api.getAllComments(
                        url = getAllCommentsUrl(page = page, token = token))
                    val data = response.body()?.comments
                    if (data != null) {
                        NetResult.Success(data)
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
                is NetResult.Error -> {
                    retry = {
                        loadAfter(params, callback)
                    }
                    val error = NetworkState.error(result.errorMsg)
                    networkState.postValue(error)
                }
                is NetResult.Success -> {
                    retry = null
                    networkState.postValue(NetworkState.LOADED)
                    if (result.data.size == 30) {
                        callback.onResult(result.data, page + 1)
                    } else {
                        callback.onResult(result.data, null)
                    }
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, CommentAll>) {

    }
}