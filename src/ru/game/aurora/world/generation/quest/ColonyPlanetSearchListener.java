package ru.game.aurora.world.generation.quest;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.OreDeposit;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.AnimalCorpseItem;
import ru.game.aurora.world.planet.nature.AnimalModifier;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 15.01.14
 * Time: 14:03
 *
 * Listener created for a quest for finding a suitable planet for colonization.
 * 1) Plane must be in an inhabited starsystem (not a quest location)
 * 2) Must be medium size, with breathable atmosphere and life on it
 *
 * When player explores at least X tiles on it, it will be enough for a planet to become explored. Player will be attacked by
 * dangerous monsters
 */
public class ColonyPlanetSearchListener extends GameEventListener implements WorldGeneratorPart
{
    private static final long serialVersionUID = -3430060862654143997L;

    // set to true if first suitable planet found, there will be some tough monsters
    private boolean firstPlanetFound = false;

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }

    private static class PlanetData implements Serializable
    {
        private static final long serialVersionUID = -8703254150937720100L;

        public boolean resourcesCollected = false;

        public boolean animalCaptured = false;

        public boolean dialogShown = false;
    }

    private Map<Planet, PlanetData> planetDataMap = new HashMap<>();

    private Planet currentPlanet = null;

    private int targetTiles;

    public ColonyPlanetSearchListener()
    {
        targetTiles = Configuration.getIntProperty("quest.colony_search.requiredTiles");
    }

    private Planet checkCurrentPlanet(Planet p)
    {
        if (p.getOwner().isQuestLocation()) {
            return null;
        }

        if (p.getAtmosphere() != PlanetAtmosphere.BREATHABLE_ATMOSPHERE
                || p.getSize() != 3
                ) {
            return null;
        }

        return p;
    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        currentPlanet = checkCurrentPlanet(planet);
        return false;
    }

    @Override
    public boolean onPlayerLeftPlanet(World world, Planet planet) {
        if (currentPlanet == null) {
            return false;
        }

        PlanetData data = planetDataMap.get(planet);
        if (data == null) {
            data = new PlanetData();
        }

        for (InventoryItem i : world.getPlayer().getLandingParty().getInventory()) {
            if (i instanceof AnimalCorpseItem) {
                data.animalCaptured = true;
            } else if (i instanceof OreDeposit.OreUnit) {
                data.resourcesCollected = true;
            }
        }

        if (data.animalCaptured && data.resourcesCollected && planet.getExploredTiles() > targetTiles) {
            world.getGlobalVariables().put("colony_search.explored_fully", true);
            world.getGlobalVariables().put("colony_search.coords", currentPlanet);
        }

        planetDataMap.put(planet, data);
        currentPlanet = null;
        return false;
    }


    private void addMonsters(World world)
    {
        Set<AnimalModifier> modifierSet = new HashSet<>();
        modifierSet.add(AnimalModifier.ARMOR);
        modifierSet.add(AnimalModifier.LARGE);
        modifierSet.add(AnimalModifier.REGEN);
        AnimalSpeciesDesc desc = new AnimalSpeciesDesc(
                currentPlanet
                , "Dangerous animal from earth-like planet"
                , true
                , false
                , 25
                , new LandingPartyWeapon("melee", 6, 1, "", "", "")
                , 2
                , AnimalSpeciesDesc.Behaviour.AGGRESSIVE
                , modifierSet
        );

        Animal animal1 = new Animal(currentPlanet, 0, 0, desc);
        currentPlanet.setNearestFreePoint(animal1, world.getPlayer().getLandingParty().getX() + 3, world.getPlayer().getLandingParty().getY());
        Animal animal2 = new Animal(currentPlanet, 0, 0, desc);
        currentPlanet.setNearestFreePoint(animal2, world.getPlayer().getLandingParty().getX() + 1, world.getPlayer().getLandingParty().getY() + 3);
        Animal animal3 = new Animal(currentPlanet, 0, 0, desc);
        currentPlanet.setNearestFreePoint(animal3, currentPlanet.getShuttle().getX(), currentPlanet.getShuttle().getY());

    }

    @Override
    public boolean onTurnEnded(World world) {
        if (currentPlanet == null) {
            return false;
        }

        if (currentPlanet.getExploredTiles() < targetTiles) {
            return false;
        }

        PlanetData data = planetDataMap.get(currentPlanet);
        if (data.dialogShown) {
            return false;
        }

        data.dialogShown = true;
        // show dialog like 'hey, we have explored enough and this planet seems to be suitable
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/colony_search_surface_explored.json"));

        if (!firstPlanetFound) {
            return false;
        }
        firstPlanetFound = true;

        addMonsters(world);

        world.getGlobalVariables().put("colony_search.explored", true);
        if (!world.getGlobalVariables().containsKey("colony_search.coords")) {
            world.getGlobalVariables().put("colony_search.coords", currentPlanet);
        }
        return false;
    }
}
