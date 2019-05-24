package onlymash.flexbooru.ap.extension

import onlymash.flexbooru.ap.common.*
import onlymash.flexbooru.ap.data.model.Detail
import onlymash.flexbooru.ap.data.model.Post

fun Post.getPreviewUrl(): String {
    return when (Settings.previewSize) {
        FILE_SIZE_SMALL -> smallPreview
        FILE_SIZE_MEDIUM -> mediumPreview
        else -> bigPreview
    }
}

fun Detail.getDetailUrl(): String {
    return when (Settings.detailSize) {
        FILE_SIZE_MEDIUM -> mediumPreview
        FILE_SIZE_BIG -> bigPreview
        FILE_SIZE_ORIGIN -> fileUrl
        else -> smallPreview
    }
}