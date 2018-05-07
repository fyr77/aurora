/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 19.08.14
 * Time: 23:25
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.SaveGameManager;
import ru.game.aurora.util.EngineUtils;

import java.text.SimpleDateFormat;

public class SaveViewConverter implements ListBox.ListBoxViewConverter<SaveGameManager.SaveGameSlot> {

    private String buildString(SaveGameManager.SaveGameSlot item) {
        if (!item.isLoaded()) {
            return item.isAutosave ? Localization.getText("gui", "saveload.autosave") + "\n" : ""
                    + Localization.getText("gui", "saveload.empty_slot");
        }

        SimpleDateFormat format1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("z yyyy");

        String dateStr1 = format1.format(item.date);
        String dateStr2 = format2.format(item.date);

        return (item.isAutosave ? Localization.getText("gui", "saveload.autosave") + "\n" : "")
                + dateStr1 + "\n"
                + dateStr2 + "\n"
                + item.gameLocation + "\n"
                + item.gameDate;
    }

    @Override
    public void display(Element element, SaveGameManager.SaveGameSlot item) {
        EngineUtils.setTextForGUIElement(element.findElementByName("#line-text"), buildString(item));
        EngineUtils.setImageForGUIElement(element.findElementByName("#line-icon"), item.getScreenshot());
        if (item.isAutosave) {
            element.findElementByName("#saveButton").hide();
        } else {
            element.findElementByName("#saveButton").show();
        }
        if (!AuroraGame.isGameRunning()) {
            element.findElementByName("#saveButton").disable();
        } else {
            element.findElementByName("#saveButton").enable();
        }
        if (!item.isLoaded()) {
            element.findElementByName("#loadButton").disable();
        } else {
            element.findElementByName("#loadButton").enable();
        }
        element.setUserData(item);
    }

    @Override
    public int getWidth(Element element, SaveGameManager.SaveGameSlot item) {
        return element.getWidth();
    }

    @Override
    public int getHeight(Element element, SaveGameManager.SaveGameSlot saveGameSlot) {
        return 138;
    }
}
