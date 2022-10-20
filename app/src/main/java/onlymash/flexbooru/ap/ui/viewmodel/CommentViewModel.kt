package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.Comment
import onlymash.flexbooru.ap.data.repository.comment.CommentRepository
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.States
import onlymash.flexbooru.ap.extension.getCommentsUrl

class CommentViewModel(
    private val repo: CommentRepository,
    private val token: String) : ScopeViewModel() {

    val comments = MutableLiveData<List<Comment>>()
    val status = MutableLiveData<States>()

    fun loadComments(postId: Int) {
        status.postValue(States.Loading())
        viewModelScope.launch {
            when (val result = repo.getComments(getCommentsUrl(postId, token))) {
                is NetResult.Success -> {
                    comments.postValue(result.data)
                    status.postValue(States.Success())
                }
                is NetResult.Error -> {
                    status.postValue(States.Error(result.errorMsg))
                }
            }
        }
    }
}