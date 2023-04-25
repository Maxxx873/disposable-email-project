package com.disposableemail.telegram.bot.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class TextCleaner {

    private TextCleaner() {
        throw new IllegalStateException("Utility class");
    }

    public static String clearText(String html) {
        var textHtmlCleaned = Jsoup.clean(html, "", Safelist.none()).trim();
        return textHtmlCleaned.replace("&nbsp;", " ");
    }

}
