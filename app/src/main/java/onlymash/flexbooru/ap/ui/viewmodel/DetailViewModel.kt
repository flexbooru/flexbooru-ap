package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.VoteResponse
import onlymash.flexbooru.ap.data.repository.detail.DetailRepository
import onlymash.flexbooru.ap.extension.NetResult

class DetailViewModel(
    private val repo: DetailRepository,
    private val scheme: String,
    private val host: String
) : ScopeViewModel() {

    val detail = MutableLiveData<NetResult<Detail>>()

    val details = MediatorLiveData<List<Detail>>()

    fun load(postId: Int, token: String) {
        viewModelScope.launch {
            val result = repo.getDetail(scheme, host, postId, token)
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

    val voteResult = MutableLiveData<NetResult<VoteResponse>>()

    fun vote(vote: Int, token: String, detail: Detail) {
        viewModelScope.launch {
            val result = repo.votePost(
                scheme = scheme,
                host = host,
                vote = vote,
                token = token,
                detail = detail
            )
            voteResult.value = result
        }
    }
}