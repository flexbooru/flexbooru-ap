package onlymash.flexbooru.ap.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentData(
    @SerialName("datetime")
    val datetime: String,
    @SerialName("id")
    val id: Int,
    @SerialName("text")
    val text: String,
    @SerialName("html")
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