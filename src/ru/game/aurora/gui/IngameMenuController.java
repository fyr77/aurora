package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;
import ru.game.aurora.application.AuroraGame;

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
        return true;
    }

    public void continueGame() {
        GUI.getInstance().closeIngameMenu();
    }

    public void saveGame() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("saveload_screen");
    }

    public void openSettings() {
        GUI.getInstance().pushCurrentScreen();
        GUI.getInstance().getNifty().gotoScreen("settings_screen");
    }

    public void mainMenu() {
        GUI.getInstance().closeIngameMenu();
        AuroraGame.showExitConfirmation(true);
    }

    public void exitGame() {
        AuroraGame.showExitConfirmation(false);
    }
}
