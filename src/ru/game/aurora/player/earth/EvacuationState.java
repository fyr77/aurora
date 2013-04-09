/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.04.13
 * Time: 13:56
 */
package ru.game.aurora.player.earth;


import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * State of humanity evacuation from Earth
 */
public class EvacuationState implements Serializable {
    private static final long serialVersionUID = -5249504974370213017L;

    private final int turnObliteratorArrives;

    private int evacuated;

    private int evacuationSpeed;

    private StarSystem targetSystem;

    public EvacuationState(World world, int turnObliteratorArrives) {
        this.turnObliteratorArrives = turnObliteratorArrives;
        findSuitableStarSystem(world.getGalaxyMap(), world.getRaces().get("Humanity").getHomeworld());
    }

    public void update(World world) {
        evacuated += evacuationSpeed;
    }

    public int getTurnObliteratorArrives() {
        return turnObliteratorArrives;
    }

    public int getEvacuated() {
        return evacuated;
    }

    public void setEvacuationSpeed(int evacuationSpeed) {
        this.evacuationSpeed = evacuationSpeed;
    }

    public int getEvacuationSpeed() {
        return evacuationSpeed;
    }

    public StarSystem getTargetSystem() {
        return targetSystem;
    }

    private boolean isGameOver(World world) {
        return world.getTurnCount() >= turnObliteratorArrives;
    }

    /**
     * Find a suitable star system for humanity migration
     * It must contain an earth-like planet with breathable atmosphere and life
     */
    private void findSuitableStarSystem(GalaxyMap map, final StarSystem solarSystem) {
        List<StarSystem> suitableStarSystems = new LinkedList<StarSystem>();
        for (GalaxyMapObject o : map.getObjects()) {
            if (!(o instanceof StarSystem)) {
                continue;
            }

            if (((StarSystem) o).isQuestLocation()) {
                continue;
            }

            boolean found = false;
            for (BasePlanet p : ((StarSystem) o).getPlanets()) {
                if (!(p instanceof Planet)) {
                    // this star system has special planets in it, so it is already occupied
                    break;
                }
                if (p.getAtmosphere() == PlanetAtmosphere.BREATHABLE_ATMOSPHERE) {
                    found = true;
                    break;
                }
            }

            if (found) {
                suitableStarSystems.add((StarSystem) o);
            }
        }

        if (suitableStarSystems.isEmpty()) {
            System.err.println("Fatal error, no suitable star system found for main quest");
            throw new IllegalStateException("No suitable star system for humanity evacuation");
        }
        // now sort suitable systems based on their distance to Earth and select the closest one
        Collections.sort(suitableStarSystems, new Comparator<StarSystem>() {
            @Override
            public int compare(StarSystem o1, StarSystem o2) {
                return (int) (GalaxyMap.getDistance(solarSystem, o1) - GalaxyMap.getDistance(solarSystem, o2));
            }
        });

        targetSystem = suitableStarSystems.get(0);
    }
}
