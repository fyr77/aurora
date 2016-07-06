package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.GameContainer;
import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.player.EarthCountry;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Updatable;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.12.13
 * Time: 23:19
 */
public class CountrySelectScreenController implements ScreenController {

    private final World world;

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
        GUI.getInstance().pushCurrentScreen();

        IntroDialogController introDialogController = (IntroDialogController) GUI.getInstance().getNifty().findScreenController(IntroDialogController.class.getCanonicalName());
        introDialogController.setEndListener(null);
        IntroDialog dialog;
        switch (selectedId) {
            case "america":
                dialog = IntroDialog.load("story/intro_am.json");
                world.getPlayer().setMainCountry(EarthCountry.AMERICA);
                break;
            case "asia":
                dialog = IntroDialog.load("story/intro_as.json");
                world.getPlayer().setMainCountry(EarthCountry.ASIA);
                break;
            case "europe":
                dialog = IntroDialog.load("story/intro_eu.json");
                world.getPlayer().setMainCountry(EarthCountry.EUROPE);
                break;
            default:
                throw new IllegalStateException("Invalid country id " + selectedId);
        }

        introDialogController.pushDialog(dialog);
        introDialogController.pushDialog(IntroDialog.load("story/intro_end.json"));
        GUI.getInstance().getNifty().gotoScreen("intro_dialog");
    }

    @NiftyEventSubscriber(id = "RadioGroup-1")
    public void onRadioButtonStateChanged(final String id, final RadioButtonGroupStateChangedEvent event) {
        EngineUtils.setTextForGUIElement(textElement, Localization.getText("gui", "country_select." + event.getSelectedId() + ".text"));
        selectedId = event.getSelectedId();
    }
}
