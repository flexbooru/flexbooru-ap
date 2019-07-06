package onlymash.flexbooru.ap.util;

import java.util.Map;

public class BBCodeParse {

    public static String bbcode2Html(String text) {
        String html = text;
        Map<String, String> bbMap = BBCodeMaps.getBBcodeMap();

        for (Map.Entry entry : bbMap.entrySet()) {
            if (entry.getKey().toString().contains("\\[list\\](.+?)\\[/list\\]")) {
                html = bbcodeListParse(html);
            }
            html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }

        return html;
    }

    /**
     * Parse List Tag: [list] [*]Entry 1 [*]Entry 2 [/list] or [list] *Entry 1 *Entry 2 [/list]
     * @param html html text
     * @return html
     */
    private static String bbcodeListParse(String html) {
        String listTagStart = "[list]";
        String listTagEnd = "[/list]";
        String asteriskTag1 = "[*]";
        String asteriskTag2 = "*";

        int pos = 0;
        // Only replace * which contains in [list]...[/list]
        while (html.indexOf(listTagStart, pos) != -1) {
            int sPos = html.indexOf(listTagStart, pos);
            int ePos = html.indexOf(listTagEnd, sPos) + listTagEnd.length();
            pos = ePos;

            boolean isAsteriskTag = false;

            String str1 = html.substring(sPos, ePos);
            String str2 = html.substring(sPos, ePos);

            // This must be first step
            if (str1.contains(asteriskTag1)) {
                while (str1.contains(asteriskTag1)) {
                    str1 = str1.replaceAll("\\[\\*\\](.+?)\\[", "<li>$1</li>\\[");
                }
                isAsteriskTag = true;
            }
            if (html.contains(asteriskTag2)) {
                str1 = str1.replaceAll("\\*", asteriskTag1);
                while (str1.contains(asteriskTag1)) {
                    str1 = str1.replaceAll("\\[\\*\\](.+?)\\[", "<li>$1</li>\\[");
                }
                isAsteriskTag = true;
            }
            if (isAsteriskTag) {
                html = html.substring(0, html.indexOf(str2)) + str1 + html.substring(html.indexOf(str2) + str2.length(), html.length());
            }
        }
        return  html;
    }
}