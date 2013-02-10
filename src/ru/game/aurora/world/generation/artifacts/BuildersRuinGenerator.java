/**
 * User: jedi-philosopher
 * Date: 10.02.13
 * Time: 18:11
 */
package ru.game.aurora.world.generation.artifacts;

import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.player.research.projects.ArtifactResearch;
import ru.game.aurora.util.CollectionUtils;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.AlienArtifact;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.HashSet;
import java.util.Set;

public class BuildersRuinGenerator implements WorldGeneratorPart {
    private static final int SYSTEMS = 50;

    private void updateStarSystem(StarSystem ss) {
        for (BasePlanet p : ss.getPlanets()) {
            // add random artifact on every planet
            if (p instanceof Planet) {
                AlienArtifact artifact = new AlienArtifact(10, 10, "builders_ruins", new ArtifactResearch(new ResearchReport("builders_ruins", "This is an ancient building, a " +
                        " remnant of a some old civilisation. Time and weather has left little of it, a few corroded pieces of metal and stone. Analysis indicate that it is at least thousand years old. It is impossible to" +
                        " understand now what was it built for.")));

                ((Planet) p).setNearestFreePoint(artifact, 10, 10);
                ((Planet) p).getPlanetObjects().add(artifact);
            }
        }
    }

    @Override
    public void updateWorld(World world) {
        // select 10 random star systems
        Set<StarSystem> updatedSystems = new HashSet<StarSystem>();
        for (int i = 0; i < Math.min(SYSTEMS, world.getGalaxyMap().getObjects().size()); ++i) {
            GalaxyMapObject obj = CollectionUtils.selectRandomElement(world.getGalaxyMap().getObjects());
            if (StarSystem.class.isAssignableFrom(obj.getClass()) && !updatedSystems.contains(obj)) {
                updateStarSystem((StarSystem) obj);
                updatedSystems.add((StarSystem) obj);
            }
        }
    }
}
