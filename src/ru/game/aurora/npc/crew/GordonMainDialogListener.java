package ru.game.aurora.npc.crew;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Created by User on 27.10.2014.
 * Dialog listener for default Gordon dialog
 */
public class GordonMainDialogListener extends BaseCrewDialogListener
{

    public GordonMainDialogListener(CrewMember crewMember) {
        super(crewMember);
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        if (dialog.getId().equals("gordon_default")) {
            processDefaultDialog(world, dialog, returnCode, flags);
        } else if (dialog.getId().equals("scientist_intro")) {
            if (flags.containsKey("parallel_worlds")) {
                crewMember.getDialogFlags().put("parallel_worlds", "1");
            }

            if (returnCode != 0) {
                Dialog defaultDialog = Dialog.loadFromFile("dialogs/crew/gordon/gordon_default.json");
                defaultDialog.addListener(this);
                crewMember.setDialog(defaultDialog);
            }
        }
        else {
            if (returnCode != -1) {
                // this was some secondary dialog, return to main one
                crewMember.interact(world);
            }
        }
    }

    private void processDefaultDialog(World world, Dialog dialog, int returnCode, Map<String, String> flags) {

        if (checkFlagAndShowDialog(world, flags, "gardener_first_contact", "dialogs/crew/gordon/gordon_gardener.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "zorsan_escape", "dialogs/crew/gordon/gordon_zorsan_escape.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_aliens", "dialogs/crew/gordon/gordon_about_aliens.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "zorsan_war_preparations", "dialogs/crew/gordon/gordon_zorsan_war_preparations.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_officers", "dialogs/crew/gordon/gordon_about_officers.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "energy_sphere", "dialogs/crew/gordon/gordon_energy_sphere.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "inside", "dialogs/crew/gordon/gordon_inside.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_himself", "dialogs/crew/gordon/gordon_about_himself.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "private_time", "dialogs/crew/gordon/gordon_private_time.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "about_service", "dialogs/crew/gordon/gordon_about_service.json")) {
            return;
        }

        if (checkFlagAndShowDialog(world, flags, "parallel_worlds_2", "dialogs/crew/gordon/gordon_parallel_worlds_1.json")) {
            crewMember.getDialogFlags().put("parallel_worlds", "2");
            crewMember.changeReputation(1);
            allDone(world);
            return;
        }


    }
}
