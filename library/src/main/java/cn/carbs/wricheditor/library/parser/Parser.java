package cn.carbs.wricheditor.library.parser;

import android.graphics.Typeface;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

public class Parser {

    // TODO 这一套待优化
    public static void withinContent(StringBuilder out, Spanned text, int start, int end) {
        int next;

        for (int i = start; i < end; i = next) {
            next = TextUtils.indexOf(text, '\n', i, end);
            if (next < 0) {
                next = end;
            }

            int nl = 0;
            while (next < end && text.charAt(next) == '\n') {
                next++;
                nl++;
            }

            withinParagraph(out, text, i, next - nl, nl);
        }
    }

    private static void withinParagraph(StringBuilder out, Spanned text, int start, int end, int nl) {

        CharacterStyle[] spans0 = text.getSpans(start, end, CharacterStyle.class);
        for (CharacterStyle span : spans0) {
            int start0 = text.getSpanStart(span);
            int end0 = text.getSpanEnd(span);
            Log.d("wangwang", "start0 : " + start0 + "  end0 : " + end0);
        }

        int next;

        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, CharacterStyle.class);
            Log.d("wangwang", "nextSpanTransition() i : " + i + " next : " + next);
            CharacterStyle[] spans = text.getSpans(i, next, CharacterStyle.class);
            for (int j = 0; j < spans.length; j++) {
                if (spans[j] instanceof StyleSpan) {
                    int style = ((StyleSpan) spans[j]).getStyle();

                    if ((style & Typeface.BOLD) != 0) {
                        out.append("<b>");
                    }

                    if ((style & Typeface.ITALIC) != 0) {
                        out.append("<i>");
                    }
                }

                if (spans[j] instanceof UnderlineSpan) {
                    out.append("<u>");
                }

                // Use standard strikethrough tag <del> rather than <s> or <strike>
                if (spans[j] instanceof StrikethroughSpan) {
                    out.append("<del>");
                }

                if (spans[j] instanceof URLSpan) {
                    out.append("<a href=\"");
                    out.append(((URLSpan) spans[j]).getURL());
                    out.append("\">");
                }

                if (spans[j] instanceof ImageSpan) {
                    out.append("<img src=\"");
                    out.append(((ImageSpan) spans[j]).getSource());
                    out.append("\">");

                    // Don't output the dummy character underlying the image.
                    i = next;
                }
            }

            withinStyle(out, text, i, next);
            for (int j = spans.length - 1; j >= 0; j--) {
                if (spans[j] instanceof URLSpan) {
                    out.append("</a>");
                }

                if (spans[j] instanceof StrikethroughSpan) {
                    out.append("</del>");
                }

                if (spans[j] instanceof UnderlineSpan) {
                    out.append("</u>");
                }

                if (spans[j] instanceof StyleSpan) {
                    int style = ((StyleSpan) spans[j]).getStyle();

                    if ((style & Typeface.BOLD) != 0) {
                        out.append("</b>");
                    }

                    if ((style & Typeface.ITALIC) != 0) {
                        out.append("</i>");
                    }
                }
            }
        }

        for (int i = 0; i < nl; i++) {
            out.append("<br>");
        }
    }

    private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }

    private static String tidy(String html) {
        return html.replaceAll("</ul>(<br>)?", "</ul>").replaceAll("</blockquote>(<br>)?", "</blockquote>");
    }

}
