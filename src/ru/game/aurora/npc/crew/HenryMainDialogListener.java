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

    private final CrewMember henry;

    public HenryMainDialogListener(CrewMember henry) {
        this.henry = henry;
    }

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
            henry.getDialogFlags().put("gardener_dialog", "");
            return;
        }

        if (flags.containsKey("zorsan_embassy")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_zorsan_embassy.json");
            henry.getDialogFlags().put("zorsan_embassy", "");
            return;
        }

        if (flags.containsKey("first_contact")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_first_contact.json");
            henry.getDialogFlags().put("first_contact", "");
            return;
        }

        if (flags.containsKey("zorsan_war_preparations")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_zorsan_war_preparations.json");
            henry.getDialogFlags().put("zorsan_war_preparations", "");
            return;
        }

        if (flags.containsKey("about_officers")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_about_officers.json");
            henry.getDialogFlags().put("about_officers", "");
            return;
        }

        if (flags.containsKey("inside")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_inside.json");
            henry.getDialogFlags().put("inside", "");
            return;
        }

        // additional dialogs
        if (flags.containsKey("service")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_service.json");
            henry.getDialogFlags().put("service", "");
            return;
        }

        if (flags.containsKey("henry_family")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_family.json");
            henry.getDialogFlags().put("henry_family", "");
            return;
        }

        if (flags.containsKey("henry_religion")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_religion.json");
            henry.getDialogFlags().put("henry_religion", "");
            return;
        }

        if (flags.containsKey("henry_private_life")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_private_life.json");
            henry.getDialogFlags().put("henry_private_life", "");
            return;
        }

        if (flags.containsKey("henry_private_time")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_private_time.json");
            henry.getDialogFlags().put("henry_private_time", "");
            return;
        }


        if (flags.containsKey("henry_secrets_1")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_secrets_1.json");
            henry.getDialogFlags().put("henry_secrets_1", "");
            return;
        }

    }
}
