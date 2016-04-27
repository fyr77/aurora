package ru.game.aurora.gui;

import com.google.common.collect.Multiset;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.planet.InventoryItem;

import java.text.DecimalFormat;

/**
 * Used in inventory screens
 */
public class InventoryViewConverter implements ListBox.ListBoxViewConverter<Multiset.Entry<InventoryItem>>
{
    private static final DecimalFormat priceFormat = new DecimalFormat("#.#####");
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
        element.setUserData(o);
    }

    private String getText(Multiset.Entry<InventoryItem> item) {

        String rz= " ";
        if (item.getCount() > 1) {
            rz += item.getCount() + " ";
        }

        rz += item.getElement().getName();
        if (showPrice) {
            if (item.getCount() > 1) {
                rz += " x" + priceFormat.format(item.getElement().getPrice()) + "CR = " + priceFormat.format(item.getCount() * item.getElement().getPrice()) + " CR";
            } else {
                rz += " " + priceFormat.format(item.getElement().getPrice()) + " CR";
            }
        }
        return rz;
    }

    @Override
    public int getWidth(Element element, Multiset.Entry<InventoryItem> o) {
        final Element text = element.findElementByName("#line-text");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(getText(o)))
                + ((o.getElement().getImage() == null) ? 0 : o.getElement().getImage().getWidth());
    }

    @Override
    public int getHeight(Element element, Multiset.Entry<InventoryItem> inventoryItemEntry) {
        display(element, inventoryItemEntry);
        return element.getConstraintHeight().getValueAsInt(1);
    }
}
