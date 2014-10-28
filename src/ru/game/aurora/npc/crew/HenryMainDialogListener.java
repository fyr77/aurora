package ru.game.aurora.npc.crew;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Listener used for Henry default dialog, that can be accessed from a crew screen
 */
public class HenryMainDialogListener extends BaseCrewDialogListener
{

    public HenryMainDialogListener(CrewMember crewMember) {
        super(crewMember);
    }


    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("henry_default")) {
            processDefaultDialog(world, dialog, returnCode, flags);
        } else if (dialog.getId().equals("marine_tutorial")) {
            if (returnCode != 0) {
                Dialog defaultDialog = Dialog.loadFromFile("dialogs/crew/henry/henry_default.json");
                defaultDialog.addListener(this);
                crewMember.setDialog(defaultDialog);
            }
        } else {
            if (returnCode != -1) {
                // this was some secondary dialog, return to main one
                crewMember.interact(world);
            }
        }
    }

    private void processDefaultDialog(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

        // dialogs about quests and encounters

        if (flags.containsKey("gardener_dialog")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_about_gardener.json");
            crewMember.getDialogFlags().put("gardener_dialog", "");
            return;
        }

        if (flags.containsKey("zorsan_embassy")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_zorsan_embassy.json");
            crewMember.getDialogFlags().put("zorsan_embassy", "");
            return;
        }

        if (flags.containsKey("first_contact")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_first_contact.json");
            crewMember.getDialogFlags().put("first_contact", "");
            return;
        }

        if (flags.containsKey("zorsan_war_preparations")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_zorsan_war_preparations.json");
            crewMember.getDialogFlags().put("zorsan_war_preparations", "");
            return;
        }

        if (flags.containsKey("about_officers")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_about_officers.json");
            crewMember.getDialogFlags().put("about_officers", "");
            return;
        }

        if (flags.containsKey("inside")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_inside.json");
            crewMember.getDialogFlags().put("inside", "");
            return;
        }

        // additional dialogs
        if (flags.containsKey("service")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_service.json");
            crewMember.getDialogFlags().put("service", "");
            return;
        }

        if (flags.containsKey("henry_family")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_family.json");
            crewMember.getDialogFlags().put("henry_family", "");
            return;
        }

        if (flags.containsKey("henry_religion")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_religion.json");
            crewMember.getDialogFlags().put("henry_religion", "");
            return;
        }

        if (flags.containsKey("henry_private_life")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_private_life.json");
            crewMember.getDialogFlags().put("henry_private_life", "");
            return;
        }

        if (flags.containsKey("henry_private_time")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_private_time.json");
            crewMember.getDialogFlags().put("henry_private_time", "");
            return;
        }


        if (flags.containsKey("henry_secrets_1")) {
            loadAndShowDialog(world, "dialogs/crew/henry/henry_secrets_1.json");
            crewMember.getDialogFlags().put("henry_secrets_1", "");
            crewMember.changeReputation(1);
            return;
        }

    }
}
