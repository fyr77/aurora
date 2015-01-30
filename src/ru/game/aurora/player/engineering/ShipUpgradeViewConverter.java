package ru.game.aurora.player.engineering;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.util.EngineUtils;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 01.05.14
 * Time: 14:41
 */
public class ShipUpgradeViewConverter implements ListBox.ListBoxViewConverter<ShipUpgrade> {
    @Override
    public void display(Element element, ShipUpgrade upgrade) {
        Element text = element.findElementByName("#item");
        EngineUtils.setTextForGUIElement(text, upgrade.getLocalizedName(upgrade.getLocalizationGroup()));
    }

    @Override
    public int getWidth(Element element, ShipUpgrade upgrade) {
        Element text = element.findElementByName("#item");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(upgrade.getLocalizedName(upgrade.getLocalizationGroup())) + 32);
    }

    @Override
    public int getHeight(Element element, ShipUpgrade shipUpgrade) {
        return 138;
    }
}
