package ru.game.aurora.gui;

import com.google.common.collect.Lists;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.openal.SoundStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 31.10.13
 * Time: 14:31
 */
public class SettingsScreenController implements ScreenController {
    private DropDown<Resolution> resolutionDropDown;

    private DropDown<String> localeDropDown;

    private CheckBox fullScreen;

    private Scrollbar musicVolume;

    private Scrollbar soundVolume;

    private boolean rebootRequested;

    private static final Logger logger = LoggerFactory.getLogger(SettingsScreenController.class);

    @Override
    public void bind(Nifty nifty, Screen screen) {
        resolutionDropDown = screen.findNiftyControl("resolution_select", DropDown.class);
        localeDropDown = screen.findNiftyControl("locale_select", DropDown.class);

        List<Resolution> resolutions = AuroraGame.getAvailableResolutions();

        resolutionDropDown.addAllItems(resolutions);

        fullScreen = screen.findNiftyControl("fullscreen", CheckBox.class);
        fullScreen.setChecked(AuroraGame.isFullScreen());

        musicVolume = screen.findNiftyControl("music_volume", Scrollbar.class);
        soundVolume = screen.findNiftyControl("sound_volume", Scrollbar.class);

        for (Resolution res : resolutionDropDown.getItems()) {
            if (res.isActive()) {
                resolutionDropDown.selectItem(res);
                break;
            }
        }

        localeDropDown.addAllItems(Lists.newArrayList(Localization.supportedLocales));
    }

    @Override
    public void onStartScreen() {
        musicVolume.setValue(SoundStore.get().getMusicVolume());
        soundVolume.setValue(SoundStore.get().getSoundVolume());
        localeDropDown.selectItem(Localization.getCurrentLocaleTag());
        rebootRequested = false;
    }

    @Override
    public void onEndScreen() {
    }

    public void applySettings() {
        Resolution res = resolutionDropDown.getSelection();
        if (!res.isActive()) {
            AuroraGame.changeResolution(res, fullScreen.isChecked());
        } else {
            AuroraGame.setFullScreen(fullScreen.isChecked());
        }
        Configuration.getSystemProperties().put("screen.resolution", res.toString());
        Configuration.getSystemProperties().put("music.volume", String.valueOf(musicVolume.getValue()));
        Configuration.getSystemProperties().put("sound.volume", String.valueOf(soundVolume.getValue()));
        Configuration.getSystemProperties().put("screen.full_screen", String.valueOf(fullScreen.isChecked()));
        Configuration.getSystemProperties().put("locale", localeDropDown.getSelection());
        Configuration.saveSystemProperties();

        if (rebootRequested) {
            Nifty nifty = GUI.getInstance().getNifty();
            Element popup = nifty.createPopup("restart_confirm");
            nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        } else {
            GUI.getInstance().popAndSetScreen();
        }
    }

    public void rebootApp() {
        try {
            AuroraGame.restartApplication(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closePopup() {
        GUI.getInstance().getNifty().closePopup(GUI.getInstance().getNifty().getTopMostPopup().getId());
        GUI.getInstance().popAndSetScreen();
    }

    public void redefineControls() {
        GUI.getInstance().getNifty().gotoScreen("input_binding_screen");
    }

    @NiftyEventSubscriber(id = "music_volume")
    public void onMusicVolumeChanged(final String id, final ScrollbarChangedEvent event) {
        SoundStore.get().setMusicVolume(event.getValue());
    }

    @NiftyEventSubscriber(id = "locale_select")
    public void onMusicVolumeChanged(final String id, final DropDownSelectionChangedEvent event) {
        if (!event.getSelection().equals(Localization.getCurrentLocaleTag())) {
            rebootRequested = true;
        }
    }

    @NiftyEventSubscriber(id = "sound_volume")
    public void onSoundVolumeChanged(final String id, final ScrollbarChangedEvent event) {
        SoundStore.get().setSoundVolume(event.getValue());
        ResourceManager.getInstance().getSound("laser_1").play();
    }

    public void cancelSettings() {
        GUI.getInstance().popAndSetScreen();
    }
}
