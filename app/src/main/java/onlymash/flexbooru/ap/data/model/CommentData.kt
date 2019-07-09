package onlymash.flexbooru.ap.data.model

import com.google.gson.annotations.SerializedName

data class CommentData(
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("html")
    val html: String
)

private const val REGEX_STR = "\\[url=(.+?)]((?:.|\n)+?)\\[/url]"

fun CommentData.getMarkdownText(): String {
    return text.replace("[quote]", "```", ignoreCase = true)
        .replace("[/quote]", "```", ignoreCase = true)
        .replace("[b]", "**", ignoreCase = true)
        .replace("[/b]", "**", ignoreCase = true)
        .replace("[i]", "_", ignoreCase = true)
        .replace("[/i]", "_", ignoreCase = true)
        .replace("[img]", "![img](", ignoreCase = true)
        .replace("[/img]", ")", ignoreCase = true)
        .replace("[u]", "", ignoreCase = true)
        .replace("[/u]", "", ignoreCase = true)
        .replace("[code]", "```", ignoreCase = true)
        .replace("[/code]", "```", ignoreCase = true)
        .replace("[spoiler]", "<details><summary>SP: </summary>", ignoreCase = true)
        .replace("[/spoiler]", "</details>", ignoreCase = true)
        .replace("""\n""",   "  \n")
        .replace(Regex(REGEX_STR), "[$2]($1)")
}