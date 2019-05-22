package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
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

    fun load(postId: Int) {
        if (viewModelScope.isActive) {
            viewModelScope.cancel()
        }
        viewModelScope.launch {
            val result = repo.getDetail(scheme, host, postId)
            detail.postValue(result)
        }
    }
}