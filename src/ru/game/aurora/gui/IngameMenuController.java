package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.SaveGameManager;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 29.05.13
 * Time: 17:32
 */
public class IngameMenuController implements Controller {
    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties parameter, Attributes controlDefinitionAttributes) {
    }

    @Override
    public void init(Properties parameter, Attributes controlDefinitionAttributes) {
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onFocus(boolean getFocus) {
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        System.out.println("input event" + inputEvent);
        return true;
    }

    public void continueGame() {
        GUI.getInstance().closeIngameMenu();
    }

    public void saveGame() {
        continueGame();
        SaveGameManager.saveGame(GUI.getInstance().getWorldInstance());
        GameLogger.getInstance().logMessage(Localization.getText("gui", "logging.game_saved"));
    }

    public void exitGame() {
        GUI.getInstance().getContainerInstance().exit();
    }
}
