package onlymash.flexbooru.ap.data.repository.detail

import androidx.lifecycle.LiveData
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.extension.NetResult

interface DetailRepository {

    suspend fun getDetail(scheme: String, host: String, postId: Int): NetResult<Detail>

    suspend fun getLocalDetails(): LiveData<List<Detail>>
}