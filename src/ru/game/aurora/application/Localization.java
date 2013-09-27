/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.09.13
 * Time: 17:29
 */
package ru.game.aurora.application;

import de.lessvoid.nifty.Nifty;

import java.util.Locale;
import java.util.ResourceBundle;


public class Localization
{
    private static Locale currentLocale;
    static {
        currentLocale = Locale.forLanguageTag("ru");
    }

    private static String getBundleName(String key)
    {
        return "localization/" + currentLocale.getLanguage() + "/" + key;
    }

    public static String getText(String bundleId, String textId)
    {
        ResourceBundle bundle = ResourceBundle.getBundle(getBundleName(bundleId), currentLocale, new UTF8Control());
        return bundle.getString(textId);
    }

    public static void registerGUIBungles(Nifty nifty)
    {
        nifty.setLocale(currentLocale);
        //nifty.addResourceBundle("gui", getBundleName("gui"));
        nifty.getResourceBundles().put("gui", ResourceBundle.getBundle(getBundleName("gui"), currentLocale, new UTF8Control()));
    }
}
