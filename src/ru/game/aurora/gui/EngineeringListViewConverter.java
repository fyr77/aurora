
/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 16.07.13
 * Time: 15:21
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.player.engineering.EngineeringProject;

public class EngineeringListViewConverter implements ListBox.ListBoxViewConverter
{
    private String getText(Object obj)
    {
        EngineeringProject ep = (EngineeringProject) obj;
        return ep.getName() + ", engineers: " + ep.getEngineersAssigned();
    }

    @Override
    public void display(Element listBoxItem, Object obj) {
        Label l = listBoxItem.findNiftyControl("#name_text", Label.class);
        l.setText(getText(obj));
    }

    @Override
    public int getWidth(Element element, Object item) {
        final Element text = element.findElementByName("#name_text");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return 64 + ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(getText(item)));
    }
}