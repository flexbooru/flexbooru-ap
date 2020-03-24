package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.*
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.repository.post.PostRepositoryImpl

class PostViewModel(
    db: MyDatabase,
    api: Api
) : ScopeViewModel() {

    private val repo = PostRepositoryImpl(
        scope = viewModelScope,
        db = db,
        api = api
    )

    private val _search = MutableLiveData<Search>()

    private val _result = Transformations.map(_search) {
        repo.getPosts(it)
    }

    val posts = Transformations.switchMap(_result) { it.pagedList }

    val networkState = Transformations.switchMap(_result) { it.networkState }

    val refreshState = Transformations.switchMap(_result) { it.refreshState }

    fun load(search: Search) {
        if (_search.value != search) {
            _search.value = search
        }
    }

    fun refresh() {
        _result.value?.refresh?.invoke()
    }

    fun retry() {
        _result.value?.retry?.invoke()
    }
}