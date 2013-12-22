package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.planet.InventoryItem;

/**
 * Used in inventory screens
 */
public class InventoryViewConverter implements ListBox.ListBoxViewConverter {
    @Override
    public void display(Element element, Object o) {
        Multiset.Entry<InventoryItem> item = (Multiset.Entry<InventoryItem>) o;

        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), item.getCount() + " " + item.getElement().getName());
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getElement().getImage());
        if (!item.getElement().isUsable()) {
            element.findElementByName("#useButton").hide();
        }
    }

    @Override
    public int getWidth(Element element, Object o) {
        Multiset.Entry<InventoryItem> item = (Multiset.Entry<InventoryItem>) o;
        final Element text = element.findElementByName("#line-text");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getCount() + " " + item.getElement().getName()))
                + ((item.getElement().getImage() == null) ? 0 : item.getElement().getImage().getWidth());
    }
}
