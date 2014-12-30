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
public class InventoryViewConverter implements ListBox.ListBoxViewConverter<Multiset.Entry<InventoryItem>>
{
    private boolean showPrice = false;

    public void setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
    }

    @Override
    public void display(Element element, Multiset.Entry<InventoryItem> o) {

        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), getText(o));
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), o.getElement().getImage());
        if (!o.getElement().isUsable()) {
            Element useButton = element.findElementByName("#useButton");
            if (useButton != null) {
                useButton.hide();
            }
        }
    }

    private String getText(Multiset.Entry<InventoryItem> item) {
        return " " + item.getCount() + " " + item.getElement().getName() + (showPrice ? " x" + item.getElement().getPrice() + "CR = " + (item.getCount() * item.getElement().getPrice() + " CR") : "");
    }

    @Override
    public int getWidth(Element element, Multiset.Entry<InventoryItem> o) {
        final Element text = element.findElementByName("#line-text");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(getText(o)))
                + ((o.getElement().getImage() == null) ? 0 : o.getElement().getImage().getWidth());
    }
}
