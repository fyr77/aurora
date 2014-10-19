package ru.game.aurora.world.generation.quest;

import org.newdawn.slick.Color;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.WeaponDesc;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.planet.nature.*;
import ru.game.aurora.world.space.HomeworldGenerator;
import ru.game.aurora.world.space.Star;
import ru.game.aurora.world.space.StarSystem;

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
 * <p/>
 * Listener created for a quest for finding a suitable planet for colonization.
 * 1) Plane must be in an inhabited starsystem (not a quest location)
 * 2) Must be medium size, with breathable atmosphere and life on it
 * <p/>
 * When player explores at least X tiles on it, it will be enough for a planet to become explored. Player will be attacked by
 * dangerous monsters
 */
public class ColonyPlanetSearchListener extends GameEventListener implements WorldGeneratorPart {
    private static final long serialVersionUID = -3430060862654143997L;

    // set to true if first suitable planet found, there will be some tough monsters
    private boolean firstPlanetFound = false;

    private StarSystem createStarsystemWithEarthLikePlanet(World world) {
        BasePlanet[] planets = new BasePlanet[3];
        StarSystem ss = new StarSystem(world.getStarSystemNamesCollection().popName(), new Star(1, Color.yellow), 13, 2);

        planets[0] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0);
        HomeworldGenerator.setCoord(planets[0], 2);

        planets[1] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.PASSIVE_ATMOSPHERE, 3, 0, 0);
        HomeworldGenerator.setCoord(planets[1], 3);

        planets[2] = new Planet(world, ss, PlanetCategory.PLANET_ROCK, PlanetAtmosphere.BREATHABLE_ATMOSPHERE, 3, 0, 0);
        final Planet planet = (Planet) planets[2];
        PlanetaryLifeGenerator.setPlanetHasLife(planet);
        PlanetaryLifeGenerator.addAnimals(planet);
        PlanetaryLifeGenerator.addPlants(planet);
        planet.addOreDeposits(10);

        HomeworldGenerator.setCoord(planets[2], 5);
        planets[2].addSatellite(new Planet(world, ss, PlanetCategory.PLANET_ICE, PlanetAtmosphere.NO_ATMOSPHERE, 4, 0, 0));
        ss.setPlanets(planets);
        ss.setRadius(8);
        return ss;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);

        // make sure that there is at least one planet suitable for colonization within certain radius from earth
        StarSystem suitableStarSystem = createStarsystemWithEarthLikePlanet(world);
        world.getGalaxyMap().addObjectAtDistance(suitableStarSystem, (StarSystem) world.getGlobalVariables().get("solar_system"), 40);
        world.getGlobalVariables().put("colony_search.klisk_coords", suitableStarSystem.getCoordsString());
    }

    private static class PlanetData implements Serializable {
        private static final long serialVersionUID = -8703254150937720100L;

        public boolean resourcesCollected = false;

        public boolean animalCaptured = false;

        public boolean dialogShown = false;
    }

    private final Map<Planet, PlanetData> planetDataMap = new HashMap<>();

    private Planet currentPlanet = null;

    private final int targetTiles;

    public ColonyPlanetSearchListener() {
        targetTiles = Configuration.getIntProperty("quest.colony_search.requiredTiles");
    }

    private Planet checkCurrentPlanet(Planet p) {
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
        if (currentPlanet != null) {
            PlanetData data = planetDataMap.get(currentPlanet);
            if (data == null) {
                data = new PlanetData();
                planetDataMap.put(currentPlanet, data);
            }
        }
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
            world.getPlayer().getJournal().getQuests().get("colony_search").addMessage("explored");
            isAlive = false;
        }

        planetDataMap.put(planet, data);
        currentPlanet = null;
        return false;
    }


    private void addMonsters(World world) {
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
                , new WeaponDesc("melee", null, 6, 1, 0, "", "melee_1", 0, "melee_1", null, 0)
                , 2
                , MonsterBehaviour.AGGRESSIVE
                , modifierSet
        );
        desc.setArmor(1);

        Animal animal1 = new Animal(currentPlanet, 0, 0, desc);
        currentPlanet.setNearestFreePoint(animal1, world.getPlayer().getLandingParty().getX() + 3, world.getPlayer().getLandingParty().getY());
        Animal animal2 = new Animal(currentPlanet, 0, 0, desc);
        currentPlanet.setNearestFreePoint(animal2, world.getPlayer().getLandingParty().getX() + 1, world.getPlayer().getLandingParty().getY() + 3);
        Animal animal3 = new Animal(currentPlanet, 0, 0, desc);
        currentPlanet.setNearestFreePoint(animal3, currentPlanet.getShuttle().getX(), currentPlanet.getShuttle().getY());
        currentPlanet.getPlanetObjects().add(animal1);
        currentPlanet.getPlanetObjects().add(animal2);
        currentPlanet.getPlanetObjects().add(animal3);
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
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/quest/colony_search/surface_explored.json"));

        if (firstPlanetFound) {
            return false;
        }
        firstPlanetFound = true;

        if (currentPlanet.hasLife()) {
            addMonsters(world);
        }

        world.getGlobalVariables().put("colony_search.explored", true);
        if (!world.getGlobalVariables().containsKey("colony_search.coords")) {
            world.getGlobalVariables().put("colony_search.coords", currentPlanet);
            world.getPlayer().getJournal().getQuests().get("colony_search").addMessage("found");
        }
        return false;
    }
}
