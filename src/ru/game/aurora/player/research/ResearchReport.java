/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.02.13
 * Time: 14:41
 */

package ru.game.aurora.player.research;

import ru.game.aurora.application.Localization;

import java.io.Serializable;

public class ResearchReport implements Serializable
{
    private static final long serialVersionUID = 1L;

    public final String icon;

    public final String text;

    public ResearchReport(String icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public String getText()
    {
        return Localization.getText("research", text);
    }
}
