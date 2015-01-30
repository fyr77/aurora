package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.planet.InventoryItem;

/**
 * Date: 02.01.14
 * Time: 21:35
 */
public class StorageViewConverter implements ListBox.ListBoxViewConverter {

    @Override
    public void display(Element element, Object o) {
        Multiset.Entry<InventoryItem> item = (Multiset.Entry<InventoryItem>) o;
        EngineUtils.setTextForGUIElement(element.findElementByName("#item"), item.getCount() + " " + item.getElement().getName());
    }

    @Override
    public int getWidth(Element element, Object o) {
        Multiset.Entry<InventoryItem> item = (Multiset.Entry<InventoryItem>) o;
        Element text = element.findElementByName("#item");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getCount() + " " + item.getElement().getName()) + 32);
    }

    @Override
    public int getHeight(Element element, Object o) {
        final Element text = element.findElementByName("#item");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return textRenderer.getFont().getHeight();
    }
}
