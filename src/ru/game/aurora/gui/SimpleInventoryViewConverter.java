package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.planet.InventoryItem;

/**
 * Date: 03.01.14
 * Time: 3:48
 */
public class SimpleInventoryViewConverter implements ListBox.ListBoxViewConverter  {
    @Override
    public void display(Element element, Object o) {
        Multiset.Entry<InventoryItem> item = (Multiset.Entry<InventoryItem>) o;
        EngineUtils.setTextForGUIElement(element.findElementByName("#invtem"), " " + item.getCount() + " " + item.getElement().getName());
    }

    @Override
    public int getWidth(Element element, Object o) {
        Multiset.Entry<InventoryItem> item = (Multiset.Entry<InventoryItem>) o;
        Element text = element.findElementByName("#invtem");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getCount() + " " + item.getElement().getName()) + 32);
    }
}
