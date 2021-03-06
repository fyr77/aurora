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
import ru.game.aurora.modding.ModManager;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Localization {

    public static final String[] supportedLocales = {"ru", "en"};
    private static final Logger logger = LoggerFactory.getLogger(Localization.class);
    private static final UTF8Control utf8Control = new UTF8Control();
    private static Locale currentLocale;

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

        logger.warn("Locale " + locale.getLanguage() + " is not supported, defaulting to en");
        currentLocale = Locale.forLanguageTag("en");
    }

    private static String getBundleName(String key) {
        String bundleName = "localization/" + currentLocale.getLanguage() + "/" + key;
        
        String overrided = ResourceManager.getOverridedResources().get(bundleName + "_" + currentLocale.getLanguage() + ".properties");
        if(overrided != null) {
            return overrided.replaceAll("_" + currentLocale.getLanguage() + ".properties", "");
        } else {
            return bundleName;
        }
    }

    public static Boolean bundleExists(String bundleId) {
        // this method is total shit, but no other easy way to check that bundle exists
        // needed by dialogs, after all dialogs are moved to separate files i hope this will be no longer needed
        final String bundleName = getBundleName(bundleId);

        try {
            ResourceBundle.getBundle(bundleName, currentLocale, utf8Control);
            return true;
        } catch (Exception ex) {
            List<ResourceBundle> modBundles = ModManager.getInstance().getResourceBundles(bundleName, currentLocale, utf8Control);
            return !modBundles.isEmpty();
        }
    }

    public static String getText(String bundleId, String textId, Object... params) {
        return String.format(getText(bundleId, textId), params);
    }

    public static String getText(String bundleId, String textId) {
        if (bundleId == null || textId == null) {
            return "";
        }
        final String bundleName = getBundleName(bundleId);
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(bundleName, currentLocale, utf8Control);
        } catch (MissingResourceException ex) {
            // ignore, probably this bundle is in a mod
        }
        if (bundle == null || !bundle.containsKey(textId)) {
            List<ResourceBundle> modBundles = ModManager.getInstance().getResourceBundles(bundleName, currentLocale, utf8Control);
            for (ResourceBundle b : modBundles) {
                if (b.containsKey(textId)) {
                    return b.getString(textId);
                }
            }
            logger.warn("Localization key {}:{} not found", bundleId, textId);
            return "<" + bundleId + "/" + textId + ">";
        }
        return bundle.getString(textId);
    }

    public static void registerGUIBungles(Nifty nifty) {
        nifty.setLocale(currentLocale);
        nifty.getResourceBundles().put("gui", ResourceBundle.getBundle(getBundleName("gui"), currentLocale, new UTF8Control()));
        nifty.getResourceBundles().put("crew", ResourceBundle.getBundle(getBundleName("crew"), currentLocale, new UTF8Control()));
        nifty.getResourceBundles().put("hints", ResourceBundle.getBundle(getBundleName("hints"), currentLocale, new UTF8Control()));
        nifty.getResourceBundles().put("planets", ResourceBundle.getBundle(getBundleName("planets"), currentLocale, new UTF8Control()));
    }
}
