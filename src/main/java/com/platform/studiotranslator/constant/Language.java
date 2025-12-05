package com.platform.studiotranslator.constant;

import lombok.Getter;

@Getter
public enum Language {

    EN("English", "Widely used international language"),
    ES("Español", "Primary language of Spain and Latin America"),
    ZH("中文", "Chinese Mandarin, most spoken native language"),
    HI("हिन्दी", "Primary language of India"),
    AR("العربية", "Arabic, widely spoken in the Middle East & North Africa"),
    BN("বাংলা", "Bengali, spoken in Bangladesh & parts of India"),
    PT("Português", "Portuguese, spoken in Portugal & Brazil"),
    RU("Русский", "Russian, widely spoken across CIS countries"),
    JA("日本語", "Japanese, spoken in Japan"),
    PA("ਪੰਜਾਬੀ", "Punjabi, spoken in India & Pakistan"),
    DE("Deutsch", "German, spoken in Germany, Austria, Switzerland"),
    FR("Français", "French, spoken in Europe, Africa, Canada"),
    TR("Türkçe", "Turkish, spoken in Türkiye"),
    KO("한국어", "Korean, spoken in South & North Korea"),
    VI("Tiếng Việt", "Vietnamese, spoken in Vietnam"),
    IT("Italiano", "Italian, spoken in Italy & Switzerland"),
    FA("فارسی", "Persian/Farsi, spoken in Iran & Afghanistan"),
    UZ("Oʻzbek", "Uzbek, official language of Uzbekistan"),
    PL("Polski", "Polish, spoken in Poland"),
    UK("Українська", "Ukrainian, spoken in Ukraine");

    private final String label;
    private final String description;

    Language(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
