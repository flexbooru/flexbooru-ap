package onlymash.flexbooru.ap.data.repository.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.Tag
import onlymash.flexbooru.ap.extension.getSuggestionUrl

class SuggestionRepositoryImpl(private val api: Api) : SuggestionRepository {

    override suspend fun fetchSuggestion(
        tag: String,
        token: String
    ): List<Tag> {
        val tags = withContext(Dispatchers.IO) {
            try {
                api.getSuggestion(
                    url = getSuggestionUrl(),
                    tag = tag,
                    token = token
                ).tagsList
            } catch (_: Exception) {
                null
            }
        }
        return tags ?: emptyList()
    }
}