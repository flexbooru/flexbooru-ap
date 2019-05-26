package onlymash.flexbooru.ap.data

data class Search(
    var scheme: String,
    var host: String,
    var query: String = "",
    var order: String = "date",
    var limit: Int = 20,
    var type: SearchType = SearchType.NORMAL,
    var userId: Int = -1,
    var token: String,
    var color: String = ""
)

enum class SearchType(type: Int) {
    NORMAL(0),
    FAVORITE(1)
}
