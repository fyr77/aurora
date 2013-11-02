package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 31.10.13
 * Time: 14:31
 */
public class SettingsScreenController implements ScreenController {
    private DropDown<Resolution> resolutionDropDown;

    private CheckBox fullScreen;

    private boolean oldFullScreen;

    public static final class Resolution {
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
        public String toString() {
            return width + "x" + height;
        }

        public boolean isActive() {
            return AuroraGame.tilesX * AuroraGame.tileSize == width && AuroraGame.tilesY * AuroraGame.tileSize == height;
        }

        public int getTilesX() {
            return width / AuroraGame.tileSize;
        }

        public int getTilesY() {
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

        Set<Integer> resolutionset = new HashSet<>();
        try {
            for (DisplayMode mode : Display.getAvailableDisplayModes()) {
                if (!mode.isFullscreenCapable() || mode.getBitsPerPixel() < 32) {
                    continue;
                }
                if (mode.getWidth() < 1024 || mode.getHeight() < 768) {
                    continue;
                }
                if (mode.getWidth() % 64 != 0 || mode.getHeight() % 64 != 0) {
                    continue;
                }

                if (resolutionset.contains(mode.getWidth() * mode.getHeight())) {
                    continue;
                }
                resolutionDropDown.addItem(new Resolution(mode.getWidth(), mode.getHeight()));
                resolutionset.add(mode.getWidth() * mode.getHeight());
            }
        } catch (LWJGLException e) {
            e.printStackTrace();
            return;
        }


        fullScreen = screen.findNiftyControl("fullscreen", CheckBox.class);
        fullScreen.setChecked(AuroraGame.isFullScreen());


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

    public void applySettings() {
        Resolution res = resolutionDropDown.getSelection();
        if (!res.isActive()) {
            AuroraGame.changeResolution(res.getTilesX(), res.getTilesY(), fullScreen.isChecked());
        } else {
            AuroraGame.setFullScreen(fullScreen.isChecked());
        }
        GUI.getInstance().popAndSetScreen();
        Configuration.getSystemProperties().put("screen.resolution", res.toString());
        Configuration.getSystemProperties().put("screen.full_screen", String.valueOf(fullScreen.isChecked()));
        Configuration.saveSystemProperties();

    }

    public void cancelSettings() {
        GUI.getInstance().popAndSetScreen();
    }
}
