package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.util.EngineUtils;

/**
 * Created by Егор on 27.08.2015.
 * Shows earth upgrades
 */
public class EarthUpgradeViewConverter implements ListBox.ListBoxViewConverter<EarthUpgrade> {
    @Override
    public void display(Element element, EarthUpgrade earthUpgrade) {
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), earthUpgrade.getDrawable().getImage());
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), earthUpgrade.getLocalizedText("upgrades"));

        Element unlockedImage = element.findElementByName("#unlock-icon");
        Element unlockedPrice = element.findElementByName("#unlock-text");
        if (earthUpgrade.isUnlocked()) {
            unlockedImage.show();
            unlockedPrice.hide();
        } else {
            EngineUtils.setTextForGUIElement(unlockedPrice, String.valueOf(earthUpgrade.getValue()));
            unlockedImage.hide();
            unlockedPrice.show();
        }
        element.findElementByName("#use-button").setVisible(earthUpgrade.canBeUsed());
    }

    @Override
    public int getWidth(Element element, EarthUpgrade earthUpgrade) {
        display(element, earthUpgrade);
        return element.getWidth();
    }

    @Override
    public int getHeight(Element element, EarthUpgrade earthUpgrade) {
        display(element, earthUpgrade);
        return element.getHeight();
    }
}
