package onlymash.flexbooru.ap.data

data class Search(
    var scheme: String,
    var host: String,
    var query: String = "",
    var order: String = "date",
    var limit: Int = 20
)
