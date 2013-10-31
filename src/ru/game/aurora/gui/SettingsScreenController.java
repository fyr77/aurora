package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 31.10.13
 * Time: 14:31
 */
public class SettingsScreenController implements ScreenController
{
    private DropDown<Resolution> resolutionDropDown;

    public static final class Resolution
    {
        private int width;
        private int height;

        public Resolution(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Resolution(String propValue) {
            String[] split = propValue.split("[xX]");
            width = Integer.parseInt(split[0]);
            height = Integer.parseInt(split[1]);
        }

        @Override
        public String toString()
        {
            return width + "x" + height;
        }

        public boolean isActive()
        {
            return AuroraGame.tilesX * AuroraGame.tileSize == width && AuroraGame.tilesY * AuroraGame.tileSize == height;
        }

        public int getTilesX()
        {
            return width / AuroraGame.tileSize;
        }

        public int getTilesY()
        {
            return height / AuroraGame.tileSize;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        resolutionDropDown = screen.findNiftyControl("resolution_select", DropDown.class);
        resolutionDropDown.addItem(new Resolution(1280, 960));
        resolutionDropDown.addItem(new Resolution(1024, 768));


        for (Resolution res : resolutionDropDown.getItems()) {
            if (res.isActive()) {
                resolutionDropDown.selectItem(res);
                break;
            }
        }
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {
    }

    public void applySettings()
    {
        Resolution res = resolutionDropDown.getSelection();
        if (!res.isActive()) {
            AuroraGame.changeResolution(res.getTilesX(), res.getTilesY());
        }
        GUI.getInstance().popAndSetScreen();
        Configuration.getSystemProperties().put("screen.resolution", res.toString());
        Configuration.saveSystemProperties();

    }

    public void cancelSettings()
    {
        GUI.getInstance().popAndSetScreen();
    }
}
