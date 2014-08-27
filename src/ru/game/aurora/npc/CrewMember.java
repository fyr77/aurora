package ru.game.aurora.npc;

import ru.game.aurora.application.Localization;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.common.ItemWithTextAndImage;
import ru.game.aurora.dialog.Dialog;
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

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public void changeReputation(int amount) {
        reputation += amount;
    }

    // these can be overriden by subclasses

    public void interact(World world) {
        Map<String, String> additionalFlags = new HashMap<>();
        additionalFlags.put("reputation", String.valueOf(reputation));
        dialog.setFlags(additionalFlags);
        world.addOverlayWindow(dialog);
    }

    public void onAdded(World world) {
        // nothing
    }

    public void onRemoved(World world) {
        // nothing
    }
}
