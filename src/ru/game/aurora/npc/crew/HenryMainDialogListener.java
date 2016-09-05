package ru.game.aurora.npc.crew;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.GameEventListener;
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
        } else if (dialog.getId().equals("marine_intro")) {
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

        if (checkFlagAndShowDialog(world, flags, "gardener_dialog", "dialogs/crew/henry/henry_about_gardener.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "zorsan_embassy", "dialogs/crew/henry/henry_zorsan_embassy.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "first_contact", "dialogs/crew/henry/henry_first_contact.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "zorsan_war_preparations", "dialogs/crew/henry/henry_zorsan_war_preparations.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_officers", "dialogs/crew/henry/henry_about_officers.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "inside", "dialogs/crew/henry/henry_inside.json")) {
            return;
        }

        // additional dialogs
        if (checkFlagAndShowDialog(world, flags, "service", "dialogs/crew/henry/henry_service.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "henry_family", "dialogs/crew/henry/henry_family.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "henry_religion", "dialogs/crew/henry/henry_religion.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "henry_private_life", "dialogs/crew/henry/henry_private_life.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "henry_private_time", "dialogs/crew/henry/henry_private_time.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "red_meat_prison_discussed", "dialogs/crew/henry/henry_red_meat_prison.json")) {
            return;
        }


        if (checkFlagAndShowDialog(world, flags, "henry_secrets_1", "dialogs/crew/henry/henry_secrets_1.json")) {
            crewMember.changeReputation(1);
            allDone(world);
            final long currentTurn = world.getDayCount();
            world.addListener(new GameEventListener() {
                @Override
                public boolean onTurnEnded(World world) {
                    if (world.getDayCount() > currentTurn + 350 && world.getCurrentStarSystem() != null) {
                        world.addOverlayWindow(Dialog.loadFromFile("dialogs/crew/henry/henry_the_line.json"));
                        isAlive = false;
                    }
                    return false;
                }
            });
            return;
        }

    }
}
