package onlymash.flexbooru.ap.extension

/**
 * A generic class that holds a value with its net response status.
 * @param <T>
 */

sealed class NetResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetResult<T>()
    data class Error(val errorMsg: String): NetResult<Nothing>()
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$errorMsg]"
        }
    }
}

sealed class States {
    data class Success(val end: Boolean = false) : States()
    data class Error(val errorMsg: String): States()
    data class Loading(val isRefreshing: Boolean = false) : States()
    override fun toString(): String {
        return when (this) {
            is Success -> "Success"
            is Error -> "Error[exception=$errorMsg]"
            is Loading -> if (isRefreshing) "Refreshing" else "Loading"
        }
    }

    val isLoading get() = this is Loading
    val isFailed get() = this is Error
}