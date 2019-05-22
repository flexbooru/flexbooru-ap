package onlymash.flexbooru.ap.extension

sealed class NetResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetResult<T>()
    data class Error(val errorMsg: String): NetResult<Nothing>()
}