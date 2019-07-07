package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.NetworkState
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.data.repository.comment.CommentRepository
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getCommentsUrl

class CommentViewModel(
    private val repo: CommentRepository,
    private val scheme: String,
    private val host: String,
    private val token: String) : ScopeViewModel() {

    val comments = MutableLiveData<List<Comment>>()
    val status = MutableLiveData<NetworkState>()

    fun loadComments(postId: Int) {
        status.postValue(NetworkState.LOADING)
        viewModelScope.launch {
            when (val result = repo.getComments(getCommentsUrl(scheme, host, postId, token))) {
                is NetResult.Success -> {
                    comments.postValue(result.data)
                    status.postValue(NetworkState.LOADED)
                }
                is NetResult.Error -> {
                    status.postValue(NetworkState.error(result.errorMsg))
                }
            }
        }
    }
}