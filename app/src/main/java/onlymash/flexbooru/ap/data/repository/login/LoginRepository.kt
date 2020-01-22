package onlymash.flexbooru.ap.data.repository.login

import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.extension.NetResult

interface LoginRepository {
    suspend fun login(
        username: String,
        password: String
    ): NetResult<User>
}