package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.User
import onlymash.flexbooru.ap.data.repository.login.LoginRepository
import onlymash.flexbooru.ap.extension.NetResult

class LoginViewModel(private val repo: LoginRepository) : ScopeViewModel() {

    val loginResult = MutableLiveData<NetResult<User>>()

    fun login(
        scheme: String,
        host: String,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            val result = repo.login(scheme, host, username, password)
            loginResult.value = result
        }
    }
}
