package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import onlymash.flexbooru.ap.data.repository.comment.CommentAllRepository

class CommentAllViewModel(private val repo: CommentAllRepository) : ScopeViewModel() {

    private val _token = MutableLiveData<String>()

    private val _clearListCh = Channel<Unit>(Channel.CONFLATED)

    val comments = flowOf(
        _clearListCh.receiveAsFlow().map { PagingData.empty() },
        _token.asFlow()
            .flatMapLatest { token ->
                repo.getComments(token)
            }
            .cachedIn(viewModelScope)
    ).flattenMerge(2)

    fun show(token: String): Boolean {
        if (_token.value == token) {
            return false
        }
        _token.value = token
        return true
    }

}