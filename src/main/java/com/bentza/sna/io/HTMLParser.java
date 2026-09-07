package com.bentza.sna.io;

/**
 * A class that converts HTML files to text and vice versa.
 */
public class HTMLParser
    {
    /**
     * Constructor
     */
    public HTMLParser()
        {
        }

    public static String parseTextToHTML(String str)
        {
        StringBuffer sb = new StringBuffer(str);
        String lb = System.getProperty("line.separator");
        int i = sb.toString().indexOf(lb);
        int len = lb.length();
        while (i != -1 && i + len + 1 <= sb.length())
            {
            sb.replace(i, i + len, "<br>");
            i = sb.toString().indexOf(lb);
            }
        lb = "\n";
        i = sb.toString().indexOf(lb);
        len = lb.length();
        while (i != -1 && i + len + 1 <= sb.length())
            {
            sb.replace(i, i + len, "<br>");
            i = sb.toString().indexOf(lb);
            }
        return "<html><head></head><body>" + sb.toString() + "</body></html>";
        }

    /*
     * Probably never used: public static String parseHTMLToText(String str) {
     * StringBuffer sb = new StringBuffer(str); String nlb =
     * System.getProperty("line.separator"); String lb = "<br>"; int len =
     * lb.length(); int i = sb.toString().indexOf(lb); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.replace(i, i + len, nlb); i =
     * sb.toString().indexOf(lb); } lb = "<b>"; i = sb.toString().indexOf(lb);
     * len = lb.length(); while (i != -1 && i + len + 1 <= sb.length()) {
     * sb.delete(i, i + len); i = sb.toString().indexOf(lb); } lb = "</b>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "<hr>"; i = sb.toString().indexOf(lb); len = lb.length(); while (i !=
     * -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "<table>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "</table>"; i = sb.toString().indexOf(lb); len = lb.length(); while
     * (i != -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "<tr>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "</tr>"; i = sb.toString().indexOf(lb); len = lb.length(); while
     * (i != -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "<td>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "</td>"; i = sb.toString().indexOf(lb); len = lb.length(); while
     * (i != -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "<html>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "</html>"; i = sb.toString().indexOf(lb); len = lb.length(); while
     * (i != -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "<body>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "</body>"; i = sb.toString().indexOf(lb); len = lb.length(); while
     * (i != -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "<p>"; i = sb.toString().indexOf(lb);
     * len = lb.length(); while (i != -1 && i + len + 1 <= sb.length()) {
     * sb.delete(i, i + len); i = sb.toString().indexOf(lb); } lb = "</p>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "<head>"; i = sb.toString().indexOf(lb); len = lb.length(); while
     * (i != -1 && i + len + 1 <= sb.length()) { sb.delete(i, i + len); i =
     * sb.toString().indexOf(lb); } lb = "</head>"; i =
     * sb.toString().indexOf(lb); len = lb.length(); while (i != -1 && i + len +
     * 1 <= sb.length()) { sb.delete(i, i + len); i = sb.toString().indexOf(lb); }
     * lb = "\n"; i = sb.toString().indexOf(lb); len = lb.length(); while (i !=
     * -1 && i + len + 1 <= sb.length() && sb.toString().indexOf(lb) != i) {
     * sb.delete(i, i + len); i = sb.toString().indexOf(lb); } return
     * sb.toString(); }
     */

    // End of class
    }