package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.repository.detail.DetailRepository
import onlymash.flexbooru.ap.extension.NetResult

class DetailViewModel(
    private val repo: DetailRepository,
    private val scheme: String,
    private val host: String
) : ScopeViewModel() {

    val detail = MutableLiveData<NetResult<Detail>>()

    val details = MediatorLiveData<List<Detail>>()

    fun load(postId: Int) {
        viewModelScope.launch {
            val result = repo.getDetail(scheme, host, postId)
            detail.postValue(result)
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            val data = repo.getLocalDetails()
            details.addSource(data) {
                details.postValue(it ?: mutableListOf())
            }
        }
    }
}