package com.fazziclay.opentoday.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorUtil {
    private static final boolean DEBUG_COLORIZE = false;

    public static String colorToHex(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return "#" + byteToHex(a) + byteToHex(r) + byteToHex(g) + byteToHex(b);
    }

    private static String byteToHex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length()-2);
    }

    /**
     * Return {@link SpannableString} from string. Formatting:
     * <p>$[] - system</p>
     * <p>$[0] - 0 - type</p>
     * <p>$[01] - 1 - value</p>
     *
     * <p>- foreground; = background; @text style(default/bold/italic/bolditalic)</p>
     *
     * <code>"Hello $[-#ffffffff;=#66000000] w$[@bolditalic]orld"</code>
     * @see Spannable
     * @see SpannableString
     * **/
    public static SpannableString colorize(String text, int defaultFgColor, int defaultBgColor, int defaultStyle) {
        if (text == null) return null;

        StringBuilder log = new StringBuilder();

        int currentForegroundSpan = defaultFgColor;
        int currentBackgroundSpan = defaultBgColor;
        int currentStyleSpan = defaultStyle;
        boolean currentStrikeOut = false;

        List<SpanText> spanTextList = new ArrayList<>();


        char[] chars = text.toCharArray();
        int oi = 0; // in chars
        int ni = 0; // in new line
        while (oi < chars.length) {
            log.append("startWhile char=").append(chars[oi]).append(" oi=").append(oi).append("; ni=").append(ni).append("; cfg=").append(currentForegroundSpan).append("; cbg=").append(currentBackgroundSpan).append("; cs=").append(currentStyleSpan).append("\n");
            boolean appendOld = true;
            boolean appendNew = true;
            String toAppend = "";

            if (chars[oi] == '\\') {
                if (oi + 1 < chars.length && chars[oi + 1] == '$') {
                    toAppend = "$";
                    oi += 2;
                }
            } else if (chars[oi] == '$' && (oi - 1 < 0 || chars[oi] != '\\')) {
                if (oi + 1 < chars.length && chars[oi + 1] == '[') {
                    boolean closeSymbol = false;
                    int _i = oi + 2;
                    while (_i < chars.length) {
                        if (chars[_i] == ']') {
                            closeSymbol = true;
                            break;
                        }
                        _i++;
                    }
                    if (closeSymbol) {
                        if (oi+2 < chars.length) {
                            String[] systems = text.substring(oi+2, _i).split(";");
                            for (String system : systems) {
                                if (system.length() < 2) continue;
                                if (system.equals("||")) {
                                    // SET DEFAULT VALUES
                                    currentForegroundSpan = defaultFgColor;
                                    currentBackgroundSpan = defaultBgColor;
                                    currentStyleSpan = defaultStyle;
                                    currentStrikeOut = false;
                                    continue;
                                }
                                char systemType = system.charAt(0);
                                String systemValue = system.substring(1);
                                if (systemType == '-') {
                                    int color = Color.MAGENTA;
                                    try {
                                        color = Color.parseColor(systemValue);
                                    } catch (Exception ignored) {
                                        if (systemValue.equals("reset")) color = defaultFgColor;
                                    }
                                    currentForegroundSpan = color;

                                } else if (systemType == '=') {
                                    int color = Color.MAGENTA;
                                    try {
                                        color = Color.parseColor(systemValue);
                                    } catch (Exception ignored) {
                                        if (systemValue.equals("reset")) color = defaultBgColor;
                                    }
                                    currentBackgroundSpan = color;

                                } else if (systemType == '@') {
                                    int style = Typeface.NORMAL;
                                    if (systemValue.contains("italic")) {
                                        style = Typeface.ITALIC;
                                    }
                                    if (systemValue.contains("bold")) {
                                        style = Typeface.ITALIC;
                                    }
                                    if (systemValue.contains("bolditalic") || systemValue.contains("italicbold")) {
                                        style = Typeface.BOLD_ITALIC;
                                    }
                                    if (systemValue.equals("reset")) style = defaultStyle;
                                    currentStyleSpan = style;
                                    currentStrikeOut = systemValue.contains("~");
                                }
                            }
                        }
                        oi = _i;
                    }

                }

            } else {
                toAppend = String.valueOf(chars[oi]);
            }

            if (oi >= chars.length) continue;
            SpanText latestSpan = getLatestElement(spanTextList);
            if (spanTextList.size() > 0 && latestSpan != null && latestSpan.spanEquals(currentForegroundSpan, currentBackgroundSpan, currentStyleSpan, currentStrikeOut)) {
                latestSpan.appendText(toAppend);
            } else {
                int latestStart = ni;
                if (latestSpan != null) {
                    latestStart = latestSpan.end;
                }
                SpanText n = new SpanText(toAppend, currentForegroundSpan, currentBackgroundSpan, currentStyleSpan, latestStart);
                n.strikeOut = currentStrikeOut;
                spanTextList.add(n);
            }


            if (appendOld) oi++;
            if (appendNew) ni++;
        }

        StringBuilder fullText = new StringBuilder();
        for (SpanText spanText : spanTextList) {
            fullText.append(spanText.text);
        }

        SpannableString spannableText = new SpannableString(fullText.toString());
        log.append("SpannableText = ").append(spannableText).append("\n");
        log.append("SpanTextList = ").append(Arrays.toString(spanTextList.toArray()));
        int i = 0;
        while (i < spanTextList.size()) {
            SpanText spanText = spanTextList.get(i);
            int start = Math.min(spanText.start, spannableText.length());
            int end = Math.min(spanText.end, spannableText.length());

            spannableText.setSpan(new ForegroundColorSpan(spanText.fgColor), start, end, Spannable.SPAN_COMPOSING);
            if (spanText.strikeOut) spannableText.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_COMPOSING);
            spannableText.setSpan(new BackgroundColorSpan(spanText.bgColor), start, end, Spanned.SPAN_COMPOSING);
            spannableText.setSpan(new StyleSpan(spanText.style), start, end, Spanned.SPAN_COMPOSING);
            i++;
        }
        return spannableText;
    }

    public static <T> T getLatestElement(List<T> list) {
        return list.size() <= 0 ? null : list.get(list.size()-1);
    }

    public static <T> T getLatestElement(T[] list) {
        return list.length <= 0 ? null : list[list.length-1];
    }

    /**
     * @see ColorUtil#colorize(String, int, int, int)
     * **/
    private static class SpanText {
        boolean strikeOut;
        String text;
        int fgColor;
        int bgColor;
        int style;

        int start;
        int end;

        public SpanText(String text, int fgColor, int bgColor, int style, int start) {
            this.text = text;
            this.fgColor = fgColor;
            this.bgColor = bgColor;
            this.style = style;
            this.start = start;
            this.end = start + text.length();
        }

        public void appendText(String s) {
            text = text + s;
            end = start + text.length();
        }

        public boolean spanEquals(int fgColor, int bgColor, int style, boolean strikeOut) {
            return (this.fgColor == fgColor && this.bgColor == bgColor && this.style == style && this.strikeOut == strikeOut);
        }

        @NonNull
        @Override
        public String toString() {
            return "SpanText{" +
                    "strikeOut=" + strikeOut +
                    ", text='" + text + '\'' +
                    ", fgColor=" + fgColor +
                    ", bgColor=" + bgColor +
                    ", style=" + style +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}
