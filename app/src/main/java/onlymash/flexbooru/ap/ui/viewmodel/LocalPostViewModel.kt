package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.data.repository.local.LocalRepository

class LocalPostViewModel(private val repo: LocalRepository) : ScopeViewModel() {

    val posts = MutableLiveData<List<Post>>()

    fun load(query: String) {
        viewModelScope.launch {
            val data = repo.getLocalPosts(query)
            posts.postValue(data)
        }
    }
}