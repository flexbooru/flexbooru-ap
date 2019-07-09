package onlymash.flexbooru.ap.data.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import kotlinx.coroutines.CoroutineScope
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.model.CommentAll

class CommentAllDataSourceFactory(
    private val scope: CoroutineScope,
    private val api: Api,
    private val scheme: String,
    private val host: String,
    private val token: String) : DataSource.Factory<Int, CommentAll>() {

    val sourceLiveData = MutableLiveData<CommentAllDataSource>()

    override fun create(): DataSource<Int, CommentAll> {
        val source = CommentAllDataSource(scope, api, scheme, host, token)
        sourceLiveData.postValue(source)
        return source
    }
}