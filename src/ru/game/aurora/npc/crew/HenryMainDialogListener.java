package ru.game.aurora.npc.crew;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Listener used for Henry default dialog, that can be accessed from a crew screen
 */
public class HenryMainDialogListener implements DialogListener
{

    private static final long serialVersionUID = 1;

    private CrewMember henry;

    private void loadAndShowDialog(World world, String file)
    {
        Dialog d = Dialog.loadFromFile(file);
        d.addListener(this);
        world.addOverlayWindow(d);
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("henry_default")) {
            processDefaultDialog(world, dialog, returnCode, flags);
        } else {
            if (returnCode != -1) {
                // this was some secondary dialog, return to main one
                henry.interact(world);
            }
        }
    }

    private void processDefaultDialog(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

        // dialogs about quests and encounters

        if (flags.containsKey("gardener_dialog")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_about_gardener.json");
            return;
        }

        if (flags.containsKey("zorsan_embassy")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_zorsan_embassy.json");
            return;
        }

        if (flags.containsKey("first_contact")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_first_contact.json");
            return;
        }

        if (flags.containsKey("zorsan_war_preparations")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_zorsan_war_preparations.json");
            return;
        }

        if (flags.containsKey("about_officers")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_about_officers.json");
            return;
        }

        if (flags.containsKey("inside")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_inside.json");
            return;
        }

        // additional dialogs
        if (flags.containsKey("service")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_service.json");
            return;
        }

        if (flags.containsKey("family")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_family.json");
            return;
        }

        if (flags.containsKey("religion")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_religion.json");
            return;
        }

        if (flags.containsKey("private_time")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_private_time.json");
            return;
        }


        if (flags.containsKey("henry_secrets_1")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_secrets_1.json");
            return;
        }

    }
}
