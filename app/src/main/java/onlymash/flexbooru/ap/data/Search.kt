package onlymash.flexbooru.ap.data

import onlymash.flexbooru.ap.common.DATE_RANGE_ANYTIME
import onlymash.flexbooru.ap.common.ORDER_DATE

data class Search(
    var scheme: String,
    var host: String,
    var query: String = "",
    var order: String = ORDER_DATE,
    var dateRange: Int = DATE_RANGE_ANYTIME,
    var limit: Int = 20,
    var type: SearchType = SearchType.NORMAL,
    var userId: Int = -1,
    var token: String,
    var color: String = ""
)

enum class SearchType {
    NORMAL,
    FAVORITE
}
