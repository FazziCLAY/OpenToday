package com.fazziclay.opentoday.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorUtil {
    private static final boolean DEBUG_COLORIZE = false;
    public static final Profiler PROFILER = App.createProfiler("ColorUtil");

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
    
    public static String sysReset(String sys, char c) {
        PROFILER.push("sysReset");
        String[] args = sys.split(";");
        StringBuilder result = new StringBuilder();
        StringBuilder passed = new StringBuilder();
        for (String arg : args) {
            if (arg.isEmpty() || passed.toString().contains(String.valueOf(arg.charAt(0)))) continue;
            if (!arg.startsWith(String.valueOf(c))) {
                result.append(arg).append(";");
                passed.append(arg.charAt(0));
            }
        }
        PROFILER.pop();
        if (result.toString().isEmpty()) return "";
        return result.substring(0, result.lastIndexOf(";"));
    }

    public static String sysSet(String sys, char c, String val) {
        PROFILER.push("sysSet");
        String[] args = sys.split(";");
        StringBuilder result = new StringBuilder();
        StringBuilder passed = new StringBuilder();
        boolean replaced = false;
        for (String arg : args) {
            if (arg.isEmpty() || passed.toString().contains(String.valueOf(arg.charAt(0)))) continue;
            if (!arg.startsWith(String.valueOf(c))) {
                result.append(arg).append(";");
            } else {
                if (!val.isEmpty()) {
                    result.append(c).append(val).append(";");
                } else {
                    result.append(";");
                }
                replaced = true;
            }
            passed.append(arg.charAt(0));
        }
        if (!replaced) result.append(c).append(val).append(";");
        PROFILER.pop();
        if (result.toString().isEmpty()) return "";
        return result.substring(0, result.lastIndexOf(";"));
    }

    public static String colorizeToPlain(String text) {
        if (!text.contains("$")) return text;
        return colorize(text, Color.RED, Color.GREEN, Typeface.NORMAL).toString();
    }

    /**
     * @see #colorize(String, int, int, int, boolean)
     */
    public static SpannableString colorize(String text, int defaultFgColor, int defaultBgColor, int defaultStyle) {
        return colorize(text, defaultFgColor, defaultBgColor, defaultStyle, false);
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
    public static SpannableString colorize(String text, int defaultFgColor, int defaultBgColor, int defaultStyle, boolean showSystems) {
        PROFILER.push("colorize");
        if (text == null) {
            PROFILER.instant("text == null");
            PROFILER.pop();
            return null;
        }

        // maybe performance boost
        if (!text.contains("$[")) {
            PROFILER.push("(dollar[ not contains optimization)");
            final SpannableString span = new SpannableString(text);
            span.setSpan(new ForegroundColorSpan(defaultFgColor), 0, span.length(), Spannable.SPAN_COMPOSING);
            span.setSpan(new BackgroundColorSpan(defaultBgColor), 0, span.length(), Spanned.SPAN_COMPOSING);
            span.setSpan(new StyleSpan(defaultStyle), 0, span.length(), Spanned.SPAN_COMPOSING);
            PROFILER.pop2();
            return span;
        }

        PROFILER.push("colorize_default");
        final StringBuilder log = new StringBuilder();
        int currentForegroundSpan = defaultFgColor;
        int currentBackgroundSpan = defaultBgColor;
        int currentStyleSpan = defaultStyle;
        boolean currentStrikeOut = false;
        int currentSize = 0;

        List<SpanText> spanTextList = new ArrayList<>();


        char[] chars = text.toCharArray();
        int oi = 0; // in chars
        int ni = 0; // in new line
        while (oi < chars.length) {
            log.append("startWhile char=").append(chars[oi]).append(" oi=").append(oi).append("; ni=").append(ni).append("; cfg=").append(currentForegroundSpan).append("; cbg=").append(currentBackgroundSpan).append("; cs=").append(currentStyleSpan).append("\n");
            boolean appendOld = true;
            boolean appendNew = true;
            String toAppend = "";
            String toAppendSystem = "";

            if (chars[oi] == '\\') {
                if (oi + 1 < chars.length && chars[oi + 1] == '$') {
                    toAppend = "$";
                    oi += 1;
                }
            } else if (chars[oi] == '$' && (oi - 1 < 0 || chars[oi] != '\\') && (oi + 1 < chars.length && chars[oi + 1] == '[')) {
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
                                    currentSize = 0;
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
                                        style = Typeface.BOLD;
                                    }
                                    if (systemValue.contains("bolditalic") || systemValue.contains("italicbold")) {
                                        style = Typeface.BOLD_ITALIC;
                                    }
                                    if (systemValue.equals("reset")) style = defaultStyle;
                                    currentStyleSpan = style;
                                    currentStrikeOut = systemValue.contains("~");
                                } else if (systemType == 'S') {
                                    try {
                                        currentSize = Integer.parseInt(systemValue);
                                    } catch (Exception ignored) {
                                        if (systemValue.equals("reset")) currentSize = 0;
                                    }
                                }
                            }
                        }
                        if (showSystems) {
                            toAppendSystem = text.substring(oi, _i+1);
                        }
                        oi = _i;
                    }

                }

            } else {
                toAppend = String.valueOf(chars[oi]);
            }

            if (oi >= chars.length) continue;
            SpanText latestSpan = getLatestElement(spanTextList);
            if (toAppendSystem.isEmpty() && spanTextList.size() > 0 && latestSpan != null && latestSpan.spanEquals(currentForegroundSpan, currentBackgroundSpan, currentStyleSpan, currentStrikeOut, currentSize)) {
                latestSpan.appendText(toAppend);
            } else {
                int latestStart = ni;
                if (latestSpan != null) {
                    latestStart = latestSpan.end;
                }
                if (showSystems) {
                    SpanText sys = new SpanText(toAppendSystem, Color.LTGRAY, Color.BLACK, Typeface.NORMAL, latestStart);
                    sys.size = 10;
                    spanTextList.add(sys);
                    latestStart = sys.end;
                }

                SpanText n = new SpanText(toAppend, currentForegroundSpan, currentBackgroundSpan, currentStyleSpan, latestStart);
                n.strikeOut = currentStrikeOut;
                n.size = currentSize;
                spanTextList.add(n);
            }


            if (appendOld) oi++;
            if (appendNew) ni++;
        }
        PROFILER.swap("concat result string");
        StringBuilder fullText = new StringBuilder();
        for (SpanText spanText : spanTextList) {
            fullText.append(spanText.text);
        }

        PROFILER.swap("apply span");
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
            if (spanText.size > 0) spannableText.setSpan(new AbsoluteSizeSpan(spanText.size, true), start, end, Spanned.SPAN_COMPOSING);
            i++;
        }
        PROFILER.pop2();
        return spannableText;
    }

    public static <T> T getLatestElement(List<T> list) {
        return list.size() == 0 ? null : list.get(list.size()-1);
    }

    public static <T> T getLatestElement(T[] list) {
        return list.length == 0 ? null : list[list.length-1];
    }

    /**
     * @see ColorUtil#colorize(String, int, int, int)
     * **/
    private static class SpanText {
        public int size;
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

        public boolean spanEquals(int fgColor, int bgColor, int style, boolean strikeOut, int size) {
            return (this.fgColor == fgColor && this.bgColor == bgColor && this.style == style && this.strikeOut == strikeOut && this.size == size);
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
