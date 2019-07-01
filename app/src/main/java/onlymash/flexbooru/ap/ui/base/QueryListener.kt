package onlymash.flexbooru.ap.ui.base

interface QueryListener {

    fun onOrderChange(order: String)

    fun onDateRangeChange(dateRange: Int)

    fun onAspectRatioChange(aspect: String)

    fun onExtensionChange(isCheckedJpg: Boolean, isCheckedPng: Boolean, isCheckedGif: Boolean)
}