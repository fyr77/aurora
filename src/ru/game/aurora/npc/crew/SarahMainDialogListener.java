package ru.game.aurora.npc.crew;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.Map;

public class SarahMainDialogListener extends BaseCrewDialogListener
{
    public SarahMainDialogListener(CrewMember crewMember) {
        super(crewMember);
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("sarah_default")) {
            processDefaultDialog(world, dialog, returnCode, flags);
        } else if (dialog.getId().equals("engineer_tutorial")) {
            if (flags.containsKey("engineer_dinner")) {
                crewMember.getDialogFlags().put("engineer_dinner", "");
                crewMember.changeReputation(4);
            }

            if (returnCode != 0) {
                Dialog defaultDialog = Dialog.loadFromFile("dialogs/crew/sarah/sarah_default.json");
                defaultDialog.addListener(this);
                crewMember.setDialog(defaultDialog);
            }

        } else if (dialog.getId().equals("sarah_red_meat_prison")) {
            if (flags.containsKey("insult")) {
                crewMember.changeReputation(-1);
            }
        } else if (returnCode != -1) {
            // this was some secondary dialog, return to main one
            crewMember.interact(world);
        }
    }

    private void processDefaultDialog(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (checkFlagAndShowDialog(world, flags, "gardener_first_contact", "dialogs/crew/sarah/sarah_gardener.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "zorsan_escape", "dialogs/crew/sarah/sarah_zorsan_escape.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_aliens", "dialogs/crew/sarah/sarah_about_aliens.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_officers", "dialogs/crew/sarah/sarah_about_officers.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "zorsan_war_preparations", "dialogs/crew/sarah/sarah_zorsan_war_preparations.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "damaged_scout", "dialogs/crew/sarah/sarah_damaged_scout.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "inside", "dialogs/crew/sarah/sarah_inside.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_herself", "dialogs/crew/sarah/sarah_about_herself.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "service", "dialogs/crew/sarah/sarah_service.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "family", "dialogs/crew/sarah/sarah_family.json")) {
            return;
        }
        if (checkFlagAndShowDialog(world, flags, "religion", "dialogs/crew/sarah/sarah_religion.json")) {
            return;
        }
        if (checkFlagAndShowDialog(world, flags, "private_time", "dialogs/crew/sarah/sarah_private_time.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "engineer_dinner_shown", "dialogs/crew/sarah/sarah_about_dinner.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "red_meat_prison_discussed", "dialogs/crew/sarah/sarah_red_meat_prison.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "sarah_past", "dialogs/crew/sarah/sarah_past_1.json")) {
            crewMember.getDialogFlags().put("sarah_past", "1");
            crewMember.changeReputation(1);
            return;
        }

    }
}
