package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import onlymash.flexbooru.ap.data.repository.comment.CommentAllRepository

class CommentAllViewModel(private val repo: CommentAllRepository) : ScopeViewModel() {

    private val _token = MutableLiveData<String?>()

    private val _result = Transformations.map(_token) { token ->
        if (token == null) {
            null
        } else {
            repo.getComments(viewModelScope, token)
        }
    }

    val comments = Transformations.switchMap(_result) { it?.pagedList }
    val networkState = Transformations.switchMap(_result) { it?.networkState }
    val refreshState = Transformations.switchMap(_result) { it?.refreshState }

    fun show(token: String): Boolean {
        if (_token.value == token) {
            return false
        }
        _token.value = token
        return true
    }

    fun refresh() {
        _result.value?.refresh?.invoke()
    }

    fun retry() {
        _result.value?.retry?.invoke()
    }
}