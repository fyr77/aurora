/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.03.13
 * Time: 12:58
 */
package ru.game.aurora.world.generation.aliens;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.Iterator;

/**
 * Processes outcome of default dialog with Klisk race
 */
public class KliskMainDialogListener implements DialogListener
{
    private static final long serialVersionUID = -2351530187782245878L;

    private AlienRace kliskRace;

    public KliskMainDialogListener(AlienRace kliskRace) {
        this.kliskRace = kliskRace;
    }

    private void destroyRogueScout(World world)
    {
        StarSystem ss = (StarSystem) world.getGlobalVariables().get("rogues.damaged_scout_found");
        if (ss == null) {
            throw new IllegalStateException("Can not sell information about rogues scout to Klisk. 'rogues.damaged_scout_found' global variable not set");
        }

        for (Iterator<SpaceObject> iter = ss.getShips().iterator(); iter.hasNext();){
            SpaceObject so = iter.next();
            if (so instanceof NPCShip && so.getName().equals("Rogue scout")) {
                iter.remove();
                break;
            }
        }

        world.getPlayer().changeCredits(10);
        GameLogger.getInstance().logMessage("Received 10 credits");
        kliskRace.setRelationToPlayer(kliskRace.getRelationToPlayer() + 1);
        world.getGlobalVariables().put("rogues.damage_scout_result", "sold_to_klisk");

        world.getGlobalVariables().remove("rogues.damaged_scout_found");
    }

    @Override
    public void onDialogEnded(World world, int returnCode)
    {
        switch (returnCode) {
            case 100:
                // player has given location of a damaged rogue scout
                destroyRogueScout(world);
                break;
        }
    }
}
