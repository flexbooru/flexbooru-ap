package onlymash.flexbooru.ap.ui.base

interface QueryListener {

    fun onOrderChange(order: String)

    fun onDateRangeChange(dateRange: Int)
}