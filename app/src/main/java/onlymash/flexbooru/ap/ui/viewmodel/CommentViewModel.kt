package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import onlymash.flexbooru.ap.data.repository.comment.CommentPagingRepository

class CommentViewModel(
    private val repo: CommentPagingRepository,
    private val scheme: String,
    private val host: String,
    private val token: String = "") : ScopeViewModel() {

    private val _postId = MutableLiveData(-1)

    private val _result = Transformations.map(_postId) {
        repo.getComments(
            scope = viewModelScope,
            scheme = scheme,
            host = host,
            postId = it,
            token = token
        )
    }

    val comments = Transformations.switchMap(_result) { it.pagedList }

    val networkState = Transformations.switchMap(_result) { it.networkState }

    val refreshState = Transformations.switchMap(_result) { it.refreshState }

    fun loadComments(postId: Int): Boolean {
        if (_postId.value == postId) {
            return false
        }
        _postId.value = postId
        return true
    }

    fun refresh() {
        _result.value?.refresh?.invoke()
    }

    fun retry() {
        _result.value?.retry?.invoke()
    }
}