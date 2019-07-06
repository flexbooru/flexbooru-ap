package onlymash.flexbooru.ap.data.repository.comment

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import kotlinx.coroutines.CoroutineScope
import okhttp3.HttpUrl
import onlymash.flexbooru.ap.data.model.Comment

class CommentDataSourceFactory(
    private val scope: CoroutineScope,
    private val repo: CommentRepository,
    private val url: HttpUrl
) : DataSource.Factory<Int, Comment>() {
    val sourceLiveData = MutableLiveData<CommentDataSource>()
    override fun create(): DataSource<Int, Comment> {
        val source = CommentDataSource(scope, repo, url)
        sourceLiveData.postValue(source)
        return source
    }
}