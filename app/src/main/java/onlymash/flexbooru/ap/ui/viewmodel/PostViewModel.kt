package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.*
import onlymash.flexbooru.ap.data.Search
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.MyDatabase
import onlymash.flexbooru.ap.data.repository.post.PostRepositoryImpl
import java.util.concurrent.Executor

class PostViewModel(
    db: MyDatabase,
    api: Api,
    ioExecutor: Executor
) : ScopeViewModel() {

    private val repo = PostRepositoryImpl(
        scope = viewModelScope,
        db = db,
        api = api,
        ioExecutor = ioExecutor
    )

    private val searchData = MutableLiveData<Search>()

    private val _result = Transformations.map(searchData) {
        repo.getPosts(it)
    }

    val posts = Transformations.switchMap(_result) { it.pagedList }

    val networkState = Transformations.switchMap(_result) { it.networkState }

    val refreshState = Transformations.switchMap(_result) { it.refreshState }

    fun load(search: Search) {
        if (searchData.value == search) {
            return
        }
        searchData.value = search
    }

    fun refresh() {
        _result.value?.refresh?.invoke()
    }

    fun retry() {
        _result.value?.retry?.invoke()
    }
}