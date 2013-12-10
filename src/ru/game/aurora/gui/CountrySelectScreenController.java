package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.Localization;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.12.13
 * Time: 23:19
 */
public class CountrySelectScreenController implements ScreenController {

    private World world;

    private Element textElement;

    private String selectedId = "america";

    public CountrySelectScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        textElement = screen.findElementByName("description");
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
        world.getGlobalVariables().put("player.country", selectedId);
    }

    @NiftyEventSubscriber(id = "RadioGroup-1")
    public void onRadioButtonStateChanged(final String id, final RadioButtonGroupStateChangedEvent event) {
        EngineUtils.setTextForGUIElement(textElement, Localization.getText("gui", "country_select." + event.getSelectedId() + ".text"));
        selectedId = event.getSelectedId();
    }

}
