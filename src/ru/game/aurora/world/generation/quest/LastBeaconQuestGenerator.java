package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.Localization;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.util.Pair;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.GardenersShip;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.StarSystem;

import java.util.Map;

/**
 */
public class LastBeaconQuestGenerator implements WorldGeneratorPart {
    private static final long serialVersionUID = -8675041991712792298L;

    @Override
    public void updateWorld(World world) {
        final StarSystem lastBeaconLocation = WorldGenerator.generateRandomStarSystem(world, 7, 7, 2);
        lastBeaconLocation.setFirstEnterDialog(Dialog.loadFromFile("dialogs/gardener_ship_detected.json"));
        lastBeaconLocation.setQuestLocation(true);
        world.getGalaxyMap().addObjectAtDistance(lastBeaconLocation, (Positionable) world.getGlobalVariables().get("solar_system"), 7);
        world.getGlobalVariables().put("last_beacon.coords", new Pair<>(lastBeaconLocation.getX(), lastBeaconLocation.getY()));

        AlienRace gardenerRace = world.getRaces().get("Gardeners");
        final GardenersShip gardenerShip = new GardenersShip(0, 0, gardenerRace.getShipSprite(), gardenerRace, null, "Sequoia");
        gardenerShip.setAi(null);
        final Dialog dialog = Dialog.loadFromFile("dialogs/gardener_1.json");
        dialog.addListener(new DialogListener() {
            private static final long serialVersionUID = -743686006546787750L;

            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                gardenerShip.setCanBeHailed(false);
                gardenerShip.warpAwayNextTurn();
                world.getPlayer().getJournal().getQuests().get("last_beacon").addMessage("gardener");
            }
        });
        gardenerShip.setCaptain(new NPC(dialog));

        gardenerShip.setPos(-lastBeaconLocation.getRadius() + 2, 1);
        lastBeaconLocation.getShips().add(gardenerShip);


        AlienRace kliskRace = world.getRaces().get(KliskGenerator.NAME);
        final NPCShip kliskShip = kliskRace.getDefaultFactory().createShip(world, KliskGenerator.STATION);
        lastBeaconLocation.setRandomEmptyPosition(kliskShip);
        lastBeaconLocation.getShips().add(kliskShip);

        world.getListeners().add(new GameEventListener() {
            private static final long serialVersionUID = -6110311603197327730L;

            @Override
            public boolean onPlayerContactedOtherShip(World world, GameObject ship) {
                if (ship == kliskShip) {
                    isAlive = false;
                }
                return false;
            }

            @Override
            public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
                if (galaxyMapObject == lastBeaconLocation) {
                    return Localization.getText("journal", "last_beacon.title");
                }
                return null;
            }
        });

    }
}
