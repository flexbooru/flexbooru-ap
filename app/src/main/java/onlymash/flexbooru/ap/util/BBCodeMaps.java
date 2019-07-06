package onlymash.flexbooru.ap.util;

import java.util.*;

public class BBCodeMaps {
    public static Map<String, String> getHTMLMap() {
        Map<String, String> htmlMap = new HashMap<String, String>();

        /* lowercase */

        // br
        htmlMap.put("<br />", "\n");
        htmlMap.put("<br>", "\n");

        // hr
        htmlMap.put("<hr />", "[hr]");
        htmlMap.put("<hr>", "[hr]");

        // strong
        htmlMap.put("<strong>(.+?)</strong>", "\\[b\\]$1\\[/b\\]");
        htmlMap.put("<b>(.+?)</b>", "\\[b\\]$1\\[/b\\]");

        // italic
        htmlMap.put("<i>(.+?)</i>", "\\[i\\]$1\\[/i\\]");
        htmlMap.put("<span style='font-style:italic;'>(.+?)</span>", "\\[i\\]$1\\[/i\\]");
        htmlMap.put("<span style=\"font-style:italic;\">(.+?)</span>", "\\[i\\]$1\\[/i\\]");

        // underline
        htmlMap.put("<u>(.+?)</u>", "\\[u\\]$1\\[/u\\]");
        htmlMap.put("<span style='text-decoration:underline;'>(.+?)</span>", "\\[u\\]$1\\[/u\\]");
        htmlMap.put("<span style=\"text-decoration:underline;\">(.+?)</span>", "\\[u\\]$1\\[/u\\]");

        // h title
        htmlMap.put("<h1>(.+?)</h1>", "\\[h1\\]$1\\[/h1\\]");
        htmlMap.put("<h2>(.+?)</h2>", "\\[h2\\]$1\\[/h2\\]");
        htmlMap.put("<h3>(.+?)</h3>", "\\[h3\\]$1\\[/h3\\]");
        htmlMap.put("<h4>(.+?)</h4>", "\\[h4\\]$1\\[/h4\\]");
        htmlMap.put("<h5>(.+?)</h5>", "\\[h5\\]$1\\[/h5\\]");
        htmlMap.put("<h6>(.+?)</h6>", "\\[h6\\]$1\\[/h6\\]");

        // blockquote
        htmlMap.put("<blockquote>(.+?)</blockquote>", "\\[quote\\]$1\\[/quote\\]");

        // p & aligns
        htmlMap.put("<p>(.+?)</p>", "\\[p\\](.+?)\\[/p\\]");
        htmlMap.put("<p style='text-indent:(.+?)px;line-height:(.+?)%;'>(.+?)</p>", "\\[p=$1,$2\\]$3\\[/p\\]");
        htmlMap.put("<div align='center'>(.+?)</div>", "\\[center\\]$1\\[/center\\]");
        htmlMap.put("<div align=\"center\">(.+?)</div>", "\\[center\\]$1\\[/center\\]");
        htmlMap.put("<p align='center'>(.+?)</p>", "\\[center\\]$1\\[/center\\]");
        htmlMap.put("<p align=\"center\">(.+?)</p>", "\\[center\\]$1\\[/center\\]");
        htmlMap.put("<div align='(.+?)'>(.+?)", "\\[align=$1\\]$2\\[/align\\]");
        htmlMap.put("<div align=\"(.+?)\">(.+?)", "\\[align=$1\\]$2\\[/align\\]");

        // fonts
        htmlMap.put("<span style='color:(.+?);'>(.+?)</span>", "\\[color=$1\\]$2\\[/color\\]");
        htmlMap.put("<span style=\"color:(.+?);\">(.+?)</span>", "\\[color=$1\\]$2\\[/color\\]");
        htmlMap.put("<span style='font-size:(.+?);'>(.+?)</span>", "\\[size=$1\\]$2\\[/size\\]");
        htmlMap.put("<span style=\"font-size:(.+?);\">(.+?)</span>", "\\[size=$1\\]$2\\[/size\\]");
        htmlMap.put("<font color=\"(.+?);\">(.+?)</span>", "\\[color=$1\\]$2\\[/color\\]");
        htmlMap.put("<font color='(.+?);'>(.+?)</span>", "\\[color=$1\\]$2\\[/color\\]");
        htmlMap.put("<font face=\"(.+?);\">(.+?)</span>", "$2");
        htmlMap.put("<font face='(.+?);'>(.+?)</span>", "$2]");
        htmlMap.put("<font face='(.+?);' color=\"(.+?);\">(.+?)</span>", "\\[color=$2\\]$3\\[/color\\]");
        htmlMap.put("<font face='(.+?);' color='(.+?);'>(.+?)</span>", "\\[color=$2\\]$3\\[/color\\]");
        htmlMap.put("<font color=\"(.+?);\" face=\"(.+?)\">(.+?)</span>", "\\[color=$1\\]$3\\[/color\\]");
        htmlMap.put("<font color='(.+?);' face='(.+?);'>(.+?)</span>", "\\[color=$1\\]$3\\[/color\\]");

        // images
        htmlMap.put("<img src='(.+?)' />", "\\[img\\]$1\\[/img\\]");
        htmlMap.put("<img src=\"(.+?)\" />", "\\[img\\]$1\\[/img\\]");
        htmlMap.put("<img width='(.+?)' height='(.+?)' src='(.+?)' />", "\\[img=$1,$2\\]$3\\[/img\\]");
        htmlMap.put("<img width=\"(.+?)\" height=\"(.+?)\" src=\"(.+?)\" />", "\\[img=$1,$2\\]$3\\[/img\\]");
        htmlMap.put("<img src='(.+?)'>", "\\[img\\]$1\\[/img\\]");
        htmlMap.put("<img src=\"(.+?)\">", "\\[img\\]$1\\[/img\\]");
        htmlMap.put("<img width='(.+?)' height='(.+?)' src='(.+?)'>", "\\[img=$1,$2\\]$3\\[/img\\]");
        htmlMap.put("<img width=\"(.+?)\" height=\"(.+?)\" src=\"(.+?)\">", "\\[img=$1,$2\\]$3\\[/img\\]");

        // links & mails
        htmlMap.put("<a href='mailto:(.+?)'>(.+?)</a>", "\\[email=$1\\]$2\\[/email\\]");
        ;
        htmlMap.put("<a href=\"mailto:(.+?)\">(.+?)</a>", "\\[email=$1\\]$2\\[/email\\]");
        ;
        htmlMap.put("<a href='(.+?)'>(.+?)</a>", "\\[url=$1\\]$2\\[/url\\]");
        htmlMap.put("<a href=\"(.+?)\">(.+?)</a>", "\\[url=$1\\]$2\\[/url\\]");

        // videos
        htmlMap.put("<object width='(.+?)' height='(.+?)'><param name='(.+?)' value='http://www.youtube.com/v/(.+?)'></param><embed src='http://www.youtube.com/v/(.+?)' type='(.+?)' width='(.+?)' height='(.+?)'></embed></object>", "\\[youtube\\]$4\\[/youtube\\]");
        htmlMap.put("<object width=\"(.+?)\" height=\"(.+?)\"><param name=\"(.+?)\" value=\"http://www.youtube.com/v/(.+?)\"></param><embed src=\"http://www.youtube.com/v/(.+?)\" type=\"(.+?)\" width=\"(.+?)\" height=\"(.+?)\"></embed></object>", "\\[youtube\\]$4\\[/youtube\\]");
        htmlMap.put("<video src='(.+?)' />", "\\[video\\]$1\\[/video\\]");
        htmlMap.put("<video src=\"(.+?)\" />", "\\[video\\]$1\\[/video\\]");
        htmlMap.put("<video src='(.+?)'>", "\\[video\\]$1\\[/video\\]");
        htmlMap.put("<video src=\"(.+?)\">", "\\[video\\]$1\\[/video\\]");


        /* UPPERCASE */

        // BR
        htmlMap.put("<BR />", "\n");
        htmlMap.put("<BR>", "\n");

        // HR
        htmlMap.put("<HR>", "[HR]");
        htmlMap.put("<HR />", "[HR]");

        // STRONG
        htmlMap.put("<STRONG>(.+?)</STRONG>", "\\[B\\]$1\\[/B\\]");
        htmlMap.put("<B>(.+?)</B>", "\\[B\\]$1\\[/B\\]");

        // ITALIC
        htmlMap.put("<I>(.+?)</I>", "\\[I\\]$1\\[/I\\]");
        htmlMap.put("<SPAN STYLE='font-style:italic;'>(.+?)</SPAN>", "\\[I\\]$1\\[/I\\]");
        htmlMap.put("<SPAN STYLE=\"font-style:italic;\">(.+?)</SPAN>", "\\[I\\]$1\\[/I\\]");

        // UNDERLINE
        htmlMap.put("<U>(.+?)</U>", "\\[U\\]$1\\[/U\\]");
        htmlMap.put("<SPAN STYLE='text-decoration:underline;'>(.+?)</SPAN>", "\\[U\\]$1\\[/U\\]");
        htmlMap.put("<SPAN STYLE=\"text-decoration:underline;\">(.+?)</SPAN>", "\\[U\\]$1\\[/U\\]");

        // H TITLE
        htmlMap.put("<H1>(.+?)</H1>", "\\[H1\\]$1\\[/H1\\]");
        htmlMap.put("<H2>(.+?)</H2>", "\\[H2\\]$1\\[/H2\\]");
        htmlMap.put("<H3>(.+?)</H3>", "\\[H3\\]$1\\[/H3\\]");
        htmlMap.put("<H4>(.+?)</H4>", "\\[H4\\]$1\\[/H4\\]");
        htmlMap.put("<H5>(.+?)</H5>", "\\[H5\\]$1\\[/H5\\]");
        htmlMap.put("<H6>(.+?)</H6>", "\\[H6\\]$1\\[/H6\\]");

        // BLOCKQUOTE
        htmlMap.put("<BLOCKQUOTE>(.+?)</BLOCKQUOTE>", "\\[QUOTE\\]$1\\[/QUOTE\\]");

        // P & ALIGNS
        htmlMap.put("<P>(.+?)</P>", "\\[P\\](.+?)\\[/P\\]");
        htmlMap.put("<P STYLE='text-indent:(.+?)px;line-height:(.+?)%;'>(.+?)</P>", "\\[P=$1,$2\\]$3\\[/P\\]");
        htmlMap.put("<DIV ALIGN='CENTER'>(.+?)</DIV>", "\\[CENTER\\]$1\\[/CENTER\\]");
        htmlMap.put("<DIV ALIGN=\"CENTER\">(.+?)</DIV>", "\\[CENTER\\]$1\\[/CENTER\\]");
        htmlMap.put("<P ALIGN='CENTER'>(.+?)</P>", "\\[CENTER\\]$1\\[/CENTER\\]");
        htmlMap.put("<P ALIGN=\"CENTER\">(.+?)</P>", "\\[CENTER\\]$1\\[/CENTER\\]");
        htmlMap.put("<DIV ALIGN='(.+?)'>(.+?)", "\\[ALIGN=$1\\]$2\\[/ALIGN\\]");
        htmlMap.put("<DIV ALIGN=\"(.+?)\">(.+?)", "\\[ALIGN=$1\\]$2\\[/ALIGN\\]");

        // FONTS
        htmlMap.put("<SPAN STYLE='color:(.+?);'>(.+?)</SPAN>", "\\[COLOR=$1\\]$2\\[/COLOR\\]");
        htmlMap.put("<SPAN STYLE=\"color:(.+?);\">(.+?)</SPAN>", "\\[COLOR=$1\\]$2\\[/COLOR\\]");
        htmlMap.put("<SPAN STYLE='font-size:(.+?);'>(.+?)</SPAN>", "\\[SIZE=$1\\]$2\\[/SIZE\\]");
        htmlMap.put("<SPAN STYLE=\"font-size:(.+?);\">(.+?)</SPAN>", "\\[SIZE=$1\\]$2\\[/SIZE\\]");
        htmlMap.put("<FONT COLOR=\"(.+?);\">(.+?)</SPAN>", "\\[COLOR=$1\\]$2\\[/COLOR\\]");
        htmlMap.put("<FONT COLOR='(.+?);'>(.+?)</SPAN>", "\\[COLOR=$1\\]$2\\[/COLOR\\]");
        htmlMap.put("<FONT FACE=\"(.+?);\">(.+?)</SPAN>", "$2");
        htmlMap.put("<FONT FACE='(.+?);'>(.+?)</SPAN>", "$2]");
        htmlMap.put("<FONT FACE='(.+?);' COLOR=\"(.+?);\">(.+?)</SPAN>", "\\[COLOR=$2\\]$3\\[/COLOR\\]");
        htmlMap.put("<FONT FACE='(.+?);' COLOR='(.+?);'>(.+?)</SPAN>", "\\[COLOR=$2\\]$3\\[/COLOR\\]");
        htmlMap.put("<FONT COLOR=\"(.+?);\" FACE=\"(.+?)\">(.+?)</SPAN>", "\\[COLOR=$1\\]$3\\[/COLOR\\]");
        htmlMap.put("<FONT COLOR='(.+?);' FACE='(.+?);'>(.+?)</SPAN>", "\\[COLOR=$1\\]$3\\[/COLOR\\]");

        // IMAGES
        htmlMap.put("<IMG SRC='(.+?)' />", "\\[IMG\\]$1\\[/IMG\\]");
        htmlMap.put("<IMG SRC=\"(.+?)\" />", "\\[IMG\\]$1\\[/IMG\\]");
        htmlMap.put("<IMG WIDTH='(.+?)' HEIGHT='(.+?)' SRC='(.+?)' />", "\\[IMG=$1,$2\\]$3\\[/IMG\\]");
        htmlMap.put("<IMG WIDTH=\"(.+?)\" HEIGHT=\"(.+?)\" SRC=\"(.+?)\" />", "\\[IMG=$1,$2\\]$3\\[/IMG\\]");
        htmlMap.put("<IMG SRC='(.+?)'>", "\\[IMG\\]$1\\[/IMG\\]");
        htmlMap.put("<IMG SRC=\"(.+?)\">", "\\[IMG\\]$1\\[/IMG\\]");
        htmlMap.put("<IMG WIDTH='(.+?)' HEIGHT='(.+?)' SRC='(.+?)'>", "\\[IMG=$1,$2\\]$3\\[/IMG\\]");
        htmlMap.put("<IMG WIDTH=\"(.+?)\" HEIGHT=\"(.+?)\" SRC=\"(.+?)\">", "\\[IMG=$1,$2\\]$3\\[/IMG\\]");

        // LINKS & MAILS
        htmlMap.put("<A HREF='mailto:(.+?)'>(.+?)</A>", "\\[EMAIL=$1\\]$2\\[/EMAIL\\]");
        ;
        htmlMap.put("<A HREF=\"mailto:(.+?)\">(.+?)</A>", "\\[EMAIL=$1\\]$2\\[/EMAIL\\]");
        ;
        htmlMap.put("<A HREF='(.+?)'>(.+?)</A>", "\\[URL=$1\\]$2\\[/URL\\]");
        htmlMap.put("<A HREF=\"(.+?)\">(.+?)</A>", "\\[URL=$1\\]$2\\[/URL\\]");

        // VIDEOS
        htmlMap.put("<OBJECT WIDTH='(.+?)' HEIGHT='(.+?)'><PARAM NAME='(.+?)' VALUE='HTTP://WWW.YOUTUBE.COM/V/(.+?)'></PARAM><EMBED SRC='http://www.youtube.com/v/(.+?)' TYPE='(.+?)' WIDTH='(.+?)' HEIGHT='(.+?)'></EMBED></OBJECT>", "\\[YOUTUBE\\]$4\\[/YOUTUBE\\]");
        htmlMap.put("<OBJECT WIDTH=\"(.+?)\" HEIGHT=\"(.+?)\"><PARAM NAME=\"(.+?)\" VALUE=\"HTTP://WWW.YOUTUBE.COM/V/(.+?)\"></PARAM><EMBED SRC=\"http://www.youtube.com/v/(.+?)\" TYPE=\"(.+?)\" WIDTH=\"(.+?)\" HEIGHT=\"(.+?)\"></EMBED></OBJECT>", "\\[YOUTUBE\\]$4\\[/YOUTUBE\\]");
        htmlMap.put("<VIDEO SRC='(.+?)' />", "\\[VIDEO\\]$1\\[/VIDEO\\]");
        htmlMap.put("<VIDEO SRC=\"(.+?)\" />", "\\[VIDEO\\]$1\\[/VIDEO\\]");
        htmlMap.put("<VIDEO SRC='(.+?)'>", "\\[VIDEO\\]$1\\[/VIDEO\\]");
        htmlMap.put("<VIDEO SRC=\"(.+?)\">", "\\[VIDEO\\]$1\\[/VIDEO\\]");

        return htmlMap;
    }

    public static Map<String, String> getBBcodeMap() {
        Map<String, String> bbMap = new HashMap<String, String>();

        /* lowercase */

        bbMap.put("\n", "<br />");
        bbMap.put("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>");
        bbMap.put("\\[i\\](.+?)\\[/i\\]", "<i>$1</i>");
        bbMap.put("\\[u\\](.+?)\\[/u\\]", "<u>$1</u>");
        bbMap.put("\\[s\\](.+?)\\[/s\\]", "<s>$1</s>");
        bbMap.put("\\[h1\\](.+?)\\[/h1\\]", "<h1>$1</h1>");
        bbMap.put("\\[h2\\](.+?)\\[/h2\\]", "<h2>$1</h2>");
        bbMap.put("\\[h3\\](.+?)\\[/h3\\]", "<h3>$1</h3>");
        bbMap.put("\\[h4\\](.+?)\\[/h4\\]", "<h4>$1</h4>");
        bbMap.put("\\[h5\\](.+?)\\[/h5\\]", "<h5>$1</h5>");
        bbMap.put("\\[h6\\](.+?)\\[/h6\\]", "<h6>$1</h6>");
        bbMap.put("\\[quote\\](.+?)\\[/quote\\]", "<blockquote>$1</blockquote>");
        bbMap.put("\\[quote=(.+?)\\](.+?)\\[/quote\\]", "<blockquote>$2</blockquote>");
        bbMap.put("\\[p\\](.+?)\\[/p\\]", "<p>$1</p>");
        bbMap.put("\\[p=(.+?),(.+?)\\](.+?)\\[/p\\]", "<p style=\"text-indent:$1px;line-height:$2%;\">$3</p>");
        bbMap.put("\\[center\\](.+?)\\[/center\\]", "<div align=\"center\">$1");
        bbMap.put("\\[align=(.+?)\\](.+?)\\[/align\\]", "<div align=\"$1\">$2");
        bbMap.put("\\[color=(.+?)\\](.+?)\\[/color\\]", "<font color='$1'>$2</font>");
        bbMap.put("\\[size=(.+?)\\](.+?)\\[/size\\]", "<font size=\"$1\">$2</span>");
        bbMap.put("\\[img\\](.+?)\\[/img\\]", "<img src=\"$1\" />");
        bbMap.put("\\[img=(.+?),(.+?)\\](.+?)\\[/img\\]", "<img width=\"$1\" height=\"$2\" src=\"$3\" />");
        bbMap.put("\\[email\\](.+?)\\[/email\\]", "<a href=\"mailto:$1\">$1</a>");
        bbMap.put("\\[email=(.+?)\\](.+?)\\[/email\\]", "<a href=\"mailto:$1\">$2</a>");
        bbMap.put("\\[url\\](.+?)\\[/url\\]", "<a href=\"$1\">$1</a>");
        bbMap.put("\\[url=(.+?)\\](.+?)\\[/url\\]", "<a href=\"$1\">$2</a>");
        bbMap.put("\\[youtube\\](.+?)\\[/youtube\\]", "<object width=\"640\" height=\"380\"><param name=\"movie\" value=\"http://www.youtube.com/v/$1\"></param><embed src=\"http://www.youtube.com/v/$1\" type=\"application/x-shockwave-flash\" width=\"640\" height=\"380\"></embed></object>");
        bbMap.put("\\[video\\](.+?)\\[/video\\]", "<video src=\"$1\" />");

        bbMap.put("\\[li\\](.+?)\\[/il\\]", "<li>$1</li>");
        bbMap.put("\\[ol\\](.+?)\\[/ol\\]", "<ol>$1</ol>");
        bbMap.put("\\[ul\\](.+?)\\[/ul\\]", "<ul>$1</ul>");

        bbMap.put("\\[list\\](.+?)\\[/list\\]", "<ul>$1</ul>");
        bbMap.put("\\[code\\](.+?)\\[/code\\]", "<code>$1</code>");
        bbMap.put("\\[center\\](.+?)\\[/center\\]", "<br /><center>$1</center>");

        /* UPPERCASE */

        bbMap.put("\\[B\\](.+?)\\[/B\\]", "<STRONG>$1</STRONG>");
        bbMap.put("\\[I\\](.+?)\\[/I\\]", "<I>$1</I>");
        bbMap.put("\\[U\\](.+?)\\[/U\\]", "<U>$1</U>");
        bbMap.put("\\[S\\](.+?)\\[/S\\]", "<S>$1</S>");
        bbMap.put("\\[H1\\](.+?)\\[/H1\\]", "<H1>$1</H1>");
        bbMap.put("\\[H2\\](.+?)\\[/H2\\]", "<H2>$1</H2>");
        bbMap.put("\\[H3\\](.+?)\\[/H3\\]", "<H3>$1</H3>");
        bbMap.put("\\[H4\\](.+?)\\[/H4\\]", "<H4>$1</H4>");
        bbMap.put("\\[H5\\](.+?)\\[/H5\\]", "<H5>$1</H5>");
        bbMap.put("\\[H6\\](.+?)\\[/H6\\]", "<H6>$1</H6>");
        bbMap.put("\\[QUOTE\\](.+?)\\[/QUOTE\\]", "<BLOCKQUOTE>$1</BLOCKQUOTE>");
        bbMap.put("\\[QUOTE=(.+?)\\](.+?)\\[/QUOTE\\]", "<BLOCKQUOTE>$2</BLOCKQUOTE>");
        bbMap.put("\\[P\\](.+?)\\[/P\\]", "<P>$1</P>");
        bbMap.put("\\[P=(.+?),(.+?)\\](.+?)\\[/P\\]", "<P STYLE=\"TEXT-INDENT:$1PX;LINE-HEIGHT:$2%;\">$3</P>");
        bbMap.put("\\[CENTER\\](.+?)\\[/CENTER\\]", "<DIV ALIGN=\"CENTER\">$1");
        bbMap.put("\\[ALIGN=(.+?)\\](.+?)\\[/ALIGN\\]", "<DIV ALIGN=\"$1\">$2");
        bbMap.put("\\[COLOR=(.+?)\\](.+?)\\[/COLOR\\]", "<FONT COLOR='$1'>$2</FONT>");
        bbMap.put("\\[SIZE=(.+?)\\](.+?)\\[/SIZE\\]", "<SPAN STYLE=\"FONT-SIZE:$1;\">$2</SPAN>");
        bbMap.put("\\[IMG\\](.+?)\\[/IMG\\]", "<IMG SRC=\"$1\" />");
        bbMap.put("\\[IMG=(.+?),(.+?)\\](.+?)\\[/IMG\\]", "<IMG WIDTH=\"$1\" HEIGHT=\"$2\" SRC=\"$3\" />");
        bbMap.put("\\[EMAIL\\](.+?)\\[/EMAIL\\]", "<A HREF=\"MAILTO:$1\">$1</A>");
        bbMap.put("\\[EMAIL=(.+?)\\](.+?)\\[/EMAIL\\]", "<A HREF=\"MAILTO:$1\">$2</A>");
        bbMap.put("\\[URL\\](.+?)\\[/URL\\]", "<A HREF=\"$1\">$1</A>");
        bbMap.put("\\[URL=(.+?)\\](.+?)\\[/URL\\]", "<A HREF=\"$1\">$2</A>");
        bbMap.put("\\[YOUTUBE\\](.+?)\\[/YOUTUBE\\]", "<OBJECT WIDTH=\"640\" HEIGHT=\"380\"><PARAM NAME=\"MOVIE\" VALUE=\"HTTP://WWW.YOUTUBE.COM/V/$1\"></PARAM><EMBED SRC=\"HTTP://WWW.YOUTUBE.COM/V/$1\" TYPE=\"APPLICATION/X-SHOCKWAVE-FLASH\" WIDTH=\"640\" HEIGHT=\"380\"></EMBED></OBJECT>");
        bbMap.put("\\[VIDEO\\](.+?)\\[/VIDEO\\]", "<VIDEO SRC=\"$1\" />");

        bbMap.put("\\[LIST\\](.+?)\\[/LIST\\]", "<UL>$1</UL>");

        bbMap.put("\\[LI\\](.+?)\\[/IL\\]", "<LI>$1</LI>");
        bbMap.put("\\[OL\\](.+?)\\[/OL\\]", "<OL>$1</OL>");
        bbMap.put("\\[UL\\](.+?)\\[/UL\\]", "<UL>$1</UL>");

        bbMap.put("\\[CODE\\](.+?)\\[/CODE\\]", "<CODE>$1</CODE>");
        bbMap.put("\\[CENTER\\](.+?)\\[/CENTER\\]", "<br /><CENTER>$1</CENTER>");

        return bbMap;
    }

}