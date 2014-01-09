package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.Resolution;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 31.10.13
 * Time: 14:31
 */
public class SettingsScreenController implements ScreenController {
    private DropDown<Resolution> resolutionDropDown;

    private CheckBox fullScreen;

    private static final Logger logger = LoggerFactory.getLogger(SettingsScreenController.class);

    @Override
    public void bind(Nifty nifty, Screen screen) {
        resolutionDropDown = screen.findNiftyControl("resolution_select", DropDown.class);

        List<Resolution> resolutions = AuroraGame.getAvailableResolutions();

        resolutionDropDown.addAllItems(resolutions);

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
