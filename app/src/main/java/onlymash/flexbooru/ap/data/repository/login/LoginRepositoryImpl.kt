package onlymash.flexbooru.ap.data.repository.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.db.UserManager
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.extension.NetResult
import onlymash.flexbooru.ap.extension.getLoginUrl
import java.util.*

class LoginRepositoryImpl(private val api: Api) : LoginRepository {

    override suspend fun login(
        username: String,
        password: String
    ): NetResult<User> {

        return withContext(Dispatchers.IO) {
            try {
                val user = api.login(
                    url = getLoginUrl(),
                    username = username,
                    password = password,
                    timeZone = TimeZone.getDefault().id
                )
                if (user.success) {
                    NetResult.Success(UserManager.createUser(user))
                } else {
                    NetResult.Error("Username or Password is wrong.")
                }
            } catch (e: Exception) {
                NetResult.Error(e.message.toString())
            }
        }
    }
}