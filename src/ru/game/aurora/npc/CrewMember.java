package ru.game.aurora.npc;

import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.ShipScreenController;
import ru.game.aurora.music.MusicDialogListener;
import ru.game.aurora.music.Playlist;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Member of a crew: one of officers or quest aliens
 */
public class CrewMember extends ItemWithTextAndImage {
    private static final long serialVersionUID = 1L;

    private int reputation;

    private Dialog dialog;

    private Playlist customMusic;

    private Map<String, String> dialogFlags = new HashMap<>();

    public CrewMember(String id, String image) {
        super(id, new Drawable(image));
    }

    public CrewMember(String id, String image, Dialog dialog) {
        super(id, new Drawable(image));
        this.dialog = dialog;
    }

    public boolean hasAction() {
        return dialog != null;
    }

    public String getActionButtonCaption() {
        return Localization.getText("gui", "crew.call");
    }

    public void changeReputation(int amount) {
        reputation += amount;
    }

    public void setCustomMusic(Playlist customMusic) {
        this.customMusic = customMusic;
    }

    public void interact(World world) {
        Map<String, String> additionalFlags = new HashMap<>();
        additionalFlags.put("reputation", String.valueOf(reputation));
        additionalFlags.put("turn", String.valueOf(world.getDayCount()));
        additionalFlags.putAll(dialogFlags);
        // this is a ship condition, that can be used in tutorials and some dialogs
        String condition;
        final Ship ship = world.getPlayer().getShip();
        if (ship.getHull() > ship.getMaxHull() * 0.6 && ship.getTotalCrew() > ship.getMaxCrew() * 0.6) {
            condition = "ok";
        } else if (ship.getHull() > ship.getMaxHull() * 0.3 && ship.getTotalCrew() > ship.getMaxCrew() * 0.3) {
            condition = "bad";
        } else {
            condition = "very_bad";
        }
        additionalFlags.put("condition", condition);
        world.addOverlayWindow(dialog, additionalFlags);
        if (customMusic != null && !customMusic.isPlaying()) {
            dialog.addListener(new MusicDialogListener(Playlist.getCurrentPlaylist().getId()));
            customMusic.play();
        }
    }

    // these can be overriden by subclasses

    public Map<String, String> getDialogFlags() {
        return dialogFlags;
    }

    public void onAdded(World world) {
        // nothing
    }

    public void onRemoved(World world) {
        // nothing
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
        // reload crew member list, so that buttons are refreshed
        ((ShipScreenController) GUI.getInstance().getNifty().findScreenController(ShipScreenController.class.getCanonicalName())).refresh();
    }

    public int getReputation() {
        return reputation;
    }
}
