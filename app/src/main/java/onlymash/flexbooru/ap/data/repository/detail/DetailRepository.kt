package onlymash.flexbooru.ap.data.repository.detail

import androidx.lifecycle.LiveData
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.VoteResponse
import onlymash.flexbooru.ap.extension.NetResult

interface DetailRepository {

    suspend fun getDetail(
        postId: Int,
        token: String): NetResult<Detail>

    suspend fun getLocalDetails(): LiveData<List<Detail>>

    suspend fun votePost(
        vote: Int = 9, // 9: vote 0: remove vote
        token: String,
        detail: Detail): NetResult<VoteResponse>
}