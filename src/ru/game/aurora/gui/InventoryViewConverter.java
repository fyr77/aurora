package ru.game.aurora.gui;

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
        InventoryItem item = (InventoryItem) o;

        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), item.getName());
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getImage());
    }

    @Override
    public int getWidth(Element element, Object o) {
        InventoryItem item = (InventoryItem) o;
        final Element text = element.findElementByName("#line-text");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = element.findElementByName("#line-icon");
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getName()))
                + ((item.getImage() == null) ? 0 : item.getImage().getWidth());
    }
}
