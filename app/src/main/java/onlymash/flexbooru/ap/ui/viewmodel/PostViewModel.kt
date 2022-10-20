package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.repository.post.PostRepositoryImpl

class PostViewModel(
    db: MyDatabase,
    api: Api
) : ScopeViewModel() {

    private val repo = PostRepositoryImpl(
        db = db,
        api = api
    )

    private val _search = MutableLiveData<Search>()
    private val _clearListCh = Channel<Unit>(Channel.CONFLATED)

    val posts = flowOf(
        _clearListCh.receiveAsFlow().map { PagingData.empty() },
        _search.asFlow()
            .flatMapLatest { search ->
                repo.getPosts(search)
            }
            .cachedIn(viewModelScope)
    ).flattenMerge(2)

    fun load(search: Search) {
        if (_search.value != search) {
            _search.value = search
        }
    }
}