package ru.game.aurora.player.engineering;

import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

/**
 */
public class ShipUpgradeViewConverter2 implements ListBox.ListBoxViewConverter<ShipUpgrade> {
    @Override
    public void display(Element element, ShipUpgrade item) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), item.getLocalizedDescription());
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getDrawable().getImage());
        final Element useButton = element.findNiftyControl("#use_button", Button.class).getElement();
        useButton.setVisible(item.isUsable());
        if (item.canBeUsedNow(World.getWorld())) {
            useButton.enable();
        } else {
            useButton.disable();
        }
        element.setUserData(item);
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
