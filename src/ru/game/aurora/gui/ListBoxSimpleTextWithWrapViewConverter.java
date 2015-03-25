package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;

public class ListBoxSimpleTextWithWrapViewConverter implements ListBox.ListBoxViewConverter
{
    @Override
    public void display(Element element, Object item) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), item.toString());
    }

    @Override
    public int getWidth(Element element, Object item) {
        display(element, item);
        return element.getWidth();
    }

    @Override
    public int getHeight(Element element, Object item) {
        display(element, item);
        return element.getHeight();
    }
}
