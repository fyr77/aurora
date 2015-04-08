package ru.game.aurora.player.engineering;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;

/**
 */
public class ShipUpgradeViewConverter2 implements ListBox.ListBoxViewConverter<ShipUpgrade> {
    @Override
    public void display(Element element, ShipUpgrade item) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), item.getLocalizedDescription());
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getDrawable().getImage());
    }

    @Override
    public int getWidth(Element element, ShipUpgrade item) {
        display(element, item);
        return element.getWidth();
    }

    @Override
    public int getHeight(Element element, ShipUpgrade shipUpgrade) {
        display(element, shipUpgrade);
        return element.getHeight();
    }
}
