/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.09.13
 * Time: 17:29
 */
package ru.game.aurora.application;

import de.lessvoid.nifty.Nifty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;


public class Localization {

    private static final Logger logger = LoggerFactory.getLogger(Localization.class);

    private static Locale currentLocale;

    private static final String[] supportedLocales = {"ru", "en"};

    private static final UTF8Control utf8Control = new UTF8Control();

    public static String getCurrentLocaleTag() {
        return currentLocale.toLanguageTag();
    }


    public static void init(Locale locale) {
        for (String s : supportedLocales) {
            if (locale.getLanguage().contains(s)) {
                currentLocale = locale;
                logger.info("Using locale " + locale.getLanguage());
                return;
            }
        }

        logger.warn("Locale " + locale.getLanguage() + " is not supported, defaulting to ru");
        currentLocale = Locale.forLanguageTag("ru");
    }

    private static String getBundleName(String key) {
        return "localization/" + currentLocale.getLanguage() + "/" + key;
    }

    public static Boolean bundleExists(String bundleId) {
        // this method is total shit, but no other easy way to check that bundle exists
        // needed by dialogs, after all dialogs are moved to separate files i hope this will be no longer needed
        try {
            ResourceBundle.getBundle(getBundleName(bundleId), currentLocale, utf8Control);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getText(String bundleId, String textId) {
        if (bundleId == null || textId == null) {
            return "";
        }
        ResourceBundle bundle = ResourceBundle.getBundle(getBundleName(bundleId), currentLocale, utf8Control);
        if (!bundle.containsKey(textId)) {
            logger.warn("Localization key {}:{} not found", bundleId, textId);
            return "<" + bundleId + "/" + textId + ">";
        }
        return bundle.getString(textId);
    }

    public static void registerGUIBungles(Nifty nifty) {
        nifty.setLocale(currentLocale);
        nifty.getResourceBundles().put("gui", ResourceBundle.getBundle(getBundleName("gui"), currentLocale, new UTF8Control()));
        nifty.getResourceBundles().put("hints", ResourceBundle.getBundle(getBundleName("hints"), currentLocale, new UTF8Control()));
    }
}
