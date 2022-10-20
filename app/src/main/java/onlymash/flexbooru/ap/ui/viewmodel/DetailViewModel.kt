package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.VoteResponse
import onlymash.flexbooru.ap.data.repository.detail.DetailRepository
import onlymash.flexbooru.ap.extension.NetResult

class DetailViewModel(
    private val repo: DetailRepository) : ScopeViewModel() {

    val detail = MutableLiveData<NetResult<Detail>>()

    val allDetails = MediatorLiveData<List<Detail>>()

    fun loadAll() {
        viewModelScope.launch {
            val data = repo.getAllLocalDetails()
            allDetails.addSource(data) {
                allDetails.postValue(it ?: mutableListOf())
            }
        }
    }

    val details: Flow<PagingData<Detail>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
            maxSize = 200
        )
    ) {
        repo.getLocalDetails()
    }
        .flow
        .cachedIn(viewModelScope)

    fun load(postId: Int, token: String) {
        viewModelScope.launch {
            val result = repo.getDetail(postId, token)
            detail.postValue(result)
        }
    }

    val voteResult = MutableLiveData<NetResult<VoteResponse>>()

    fun vote(vote: Int, token: String, detail: Detail) {
        viewModelScope.launch {
            val result = repo.votePost(
                vote = vote,
                token = token,
                detail = detail
            )
            voteResult.value = result
        }
    }
}