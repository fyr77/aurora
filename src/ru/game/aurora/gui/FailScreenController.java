package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.Localization;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 08.10.13
 * Time: 13:35
 */
public class FailScreenController implements ScreenController {
    private World world;

    private Element text;

    private Element image;

    public FailScreenController(World world) {
        this.world = world;
    }

    public void set(String imageId, String textId) {
        EngineUtils.setTextForGUIElement(text, Localization.getText("gameover", textId));
        EngineUtils.setImageForGUIElement(image, imageId);
    }


    @Override
    public void bind(Nifty nifty, Screen screen) {
        text = screen.findElementByName("storyText");
        image = screen.findElementByName("imagePanel");
    }

    @Override
    public void onStartScreen() {
        world.setPaused(true);
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    public void gameOver() {
        world.setGameOver(true);
    }

}
