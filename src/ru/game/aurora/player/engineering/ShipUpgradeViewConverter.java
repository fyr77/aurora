package ru.game.aurora.player.engineering;

import com.google.common.collect.Multiset;
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
public class ShipUpgradeViewConverter implements ListBox.ListBoxViewConverter<Multiset.Entry<ShipUpgrade>> {

    private String getText(Multiset.Entry<ShipUpgrade> upgrade)
    {
        return (upgrade.getCount() > 1 ? "x" + upgrade.getCount() : "") + " " +
                upgrade.getElement().getLocalizedName(upgrade.getElement().getLocalizationGroup());
    }

    @Override
    public void display(Element element, Multiset.Entry<ShipUpgrade> upgrade) {
        Element text = element.findElementByName("#item");
        element.setUserData(upgrade);
        EngineUtils.setTextForGUIElement(text, getText(upgrade));
    }

    @Override
    public int getWidth(Element element, Multiset.Entry<ShipUpgrade> upgrade) {
        Element text = element.findElementByName("#item");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(getText(upgrade)) + 32);
    }

    @Override
    public int getHeight(Element element, Multiset.Entry<ShipUpgrade> shipUpgrade) {
        display(element, shipUpgrade);
        return element.getHeight();
    }
}
