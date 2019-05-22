package onlymash.flexbooru.ap.data.repository.detail

import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.extension.NetResult

interface DetailRepository {
    suspend fun getDetail(scheme: String, host: String, postId: Int): NetResult<Detail>
}