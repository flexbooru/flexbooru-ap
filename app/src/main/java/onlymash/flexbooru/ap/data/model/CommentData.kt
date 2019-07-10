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

private const val REGEX_STR_1 = "\\[url=(.+?)]((?:.|\n)+?)\\[/url]"
private const val REGEX_STR_2 = "\\[URL=(.+?)]((?:.|\n)+?)\\[/URL]"

fun CommentData.getMarkdownText(): String {
    return text
        .replace("""\n""",   "  \n")
        .replace("[quote]", "> ", ignoreCase = true)
        .replace("[/quote]", "  \n\n", ignoreCase = true)
        .replace("[b]", "**", ignoreCase = true)
        .replace("[/b]", "**", ignoreCase = true)
        .replace("[i]", "_", ignoreCase = true)
        .replace("[/i]", "_", ignoreCase = true)
        .replace("[img]", "![image](", ignoreCase = true)
        .replace("[/img]", ")", ignoreCase = true)
        .replace("[u]", "", ignoreCase = true)
        .replace("[/u]", "", ignoreCase = true)
        .replace("[code]", "`", ignoreCase = true)
        .replace("[/code]", "`  \n", ignoreCase = true)
        .replace("[spoiler]", "  \n<details><summary>SP: </summary>  \n\n", ignoreCase = true)
        .replace("[/spoiler]", "</details>", ignoreCase = true)
        .replace(Regex(REGEX_STR_1), "[$2]($1)")
        .replace(Regex(REGEX_STR_2), "[$2]($1)")
        .replace("[youtube]", "https://youtu.be/")
        .replace("[/youtube]", "")
        .replace("[url]", "")
        .replace("[/url]", "")
        .replace("[s]", "~~")
        .replace("[/s]", "~~")
}