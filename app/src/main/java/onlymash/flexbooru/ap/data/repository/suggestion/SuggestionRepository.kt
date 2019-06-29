package onlymash.flexbooru.ap.data.repository.suggestion

import onlymash.flexbooru.ap.data.model.Tag

interface SuggestionRepository {

    suspend fun fetchSuggestion(
        scheme: String,
        host: String,
        tag: String,
        token: String
    ): List<Tag>
}