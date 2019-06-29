package onlymash.flexbooru.ap.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.ap.data.model.Tag
import onlymash.flexbooru.ap.data.repository.suggestion.SuggestionRepository

class SuggestionViewModel(private val repo: SuggestionRepository) : ScopeViewModel() {

    val tags = MutableLiveData<List<Tag>>()

    fun fetch(scheme: String,
              host: String,
              tag: String,
              token: String) {
        viewModelScope.launch {
            val data = repo.fetchSuggestion(
                scheme = scheme,
                host = host,
                token = token,
                tag = tag
            )
            tags.postValue(data)
        }
    }
}