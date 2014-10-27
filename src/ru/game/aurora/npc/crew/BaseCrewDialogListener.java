package ru.game.aurora.npc.crew;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Created by User on 27.10.2014.
 * Base class for dialog listeners for main crew members
 */
public abstract class BaseCrewDialogListener implements DialogListener
{
    private static final long serialVersionUID = 1;

    protected final CrewMember crewMember;

    public BaseCrewDialogListener(CrewMember crewMember) {
        this.crewMember = crewMember;
    }

    protected void loadAndShowDialog(World world, String file)
    {
        Dialog d = Dialog.loadFromFile(file);
        d.addListener(this);
        world.addOverlayWindow(d);
    }

    protected boolean checkFlagAndShowDialog(World world, Map<String, String> flags, String flag, String dialog)
    {
        if (flags.containsKey(flag)) {
            loadAndShowDialog(world, dialog);
            crewMember.getDialogFlags().put(flag, "");
            return true;
        }
        return false;
    }
}
