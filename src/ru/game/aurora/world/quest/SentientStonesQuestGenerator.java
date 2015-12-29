package ru.game.aurora.world.quest;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.PickableInventoryItem;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.Plant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Егор on 26.12.2015.
 * Quest about sentinent stones.
 * Player can take one on their home planet.
 */
public class SentientStonesQuestGenerator extends GameEventListener implements WorldGeneratorPart {

    private static long serialVersionUID = 1L;

    private Planet planet = null;

    private int stepsCount = 0;

    private int turnsBeforeIncident = -1;

    private boolean initialDialogShown = false;

    private class SentientStoneItem extends SellOnlyInventoryItem {

        public SentientStoneItem() {
            super("journal"
                    , "sentient_stone"
                    , (CommonRandom.getRandom().nextBoolean() ? "stone_1" : "stone_2")
                    , 0
                    , true
            );
        }

        @Override
        public void onReceived(World world, int amount) {
            // start quest here
            world.getPlayer().getJournal().addQuestEntries("sentient_stones", "start");
            world.getGlobalVariables().put("sentient_stones.started", true);
            turnsBeforeIncident = Configuration.getIntProperty("sentient_stones.turnsBeforeIncident");
        }
    }

    private class SentientStone extends PickableInventoryItem {

        public SentientStone(int x, int y) {
            super(x, y, new SentientStoneItem());
        }

        @Override
        public boolean canBeInteracted(World world) {
            return !world.getGlobalVariables().containsKey("sentient_stones.started");
        }

        @Override
        public ScanGroup getScanGroup() {
            return ScanGroup.BIO;
        }
    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        if (planet != null) {
            return false;
        }

        if (planet.getOwner().isQuestLocation()) {
            return false;
        }
        if (planet.getExploredTiles() > 0) {
            // already landed here before
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() > Configuration.getDoubleProperty("sentient_stones.chance")) {
            return false;
        }

        // quest started
        this.planet = planet;

        // replace all plants and animals on the planet with sentinent stones
        List<GameObject> stones = new ArrayList<>(planet.getPlanetObjects().size());
        for (Iterator<GameObject> objectIterator = planet.getPlanetObjects().iterator(); objectIterator.hasNext(); ) {
            GameObject go = objectIterator.next();
            if (go instanceof Animal || go instanceof Plant) {
                objectIterator.remove();
            }
            final SentientStone sentient_stone = new SentientStone(go.getX(), go.getY());
            stones.add(sentient_stone);
        }

        planet.getPlanetObjects().addAll(stones);

        return true;
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (planet != null) {
            if (!initialDialogShown && world.getCurrentRoom().equals(planet) && ++stepsCount > 5) {
                Dialog dialog = Dialog.loadFromFile("dialogs/encounters/sentient_stones/sstones_landing_party_report.json");
                world.addOverlayWindow(dialog);
                initialDialogShown = true;
                return true;
            }

            if (turnsBeforeIncident > 0) {
                --turnsBeforeIncident;
                if (turnsBeforeIncident == 0) {
                    turnsBeforeIncident = -1;
                    world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/sentient_stones/sstones_incident.json"));
                }
            }
        }
        return false;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }
}
