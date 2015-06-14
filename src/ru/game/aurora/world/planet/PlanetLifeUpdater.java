package ru.game.aurora.world.planet;

import ru.game.aurora.application.Configuration;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.nature.PlanetFloraAndFauna;
import ru.game.aurora.world.planet.nature.PlanetaryLifeGenerator;

public class PlanetLifeUpdater extends GameEventListener {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean onTurnEnded(World world) {
        Room room = world.getCurrentRoom();
        if(room instanceof Planet) {
            updateLife((Planet)room);
        }
        return false;
    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        updateLife(planet);
        return false;
    }
    
    public static void updateLife(Planet planet) {
        PlanetFloraAndFauna ff = planet.getFloraAndFauna();
        if(ff == null) {
            return;
        }
        
        World world = World.getWorld();
        
        int respawnDelay = Configuration.getIntProperty("animal.respawn.delay");
        int turns = world.getTurnCount() - ff.getLastLifeUpdate();
        int toAdd = Math.min(turns / respawnDelay, ff.getMaxAnimals() - ff.getAnimalCount());
        
        if(toAdd > 0) {
            PlanetaryLifeGenerator.addAnimals(planet, toAdd);
            ff.setLastLifeUpdate(world.getTurnCount() - (turns % respawnDelay));
        }
        else {
            if(ff.getMaxAnimals() - ff.getAnimalCount() < 1) {
                ff.setLastLifeUpdate(world.getTurnCount());
            }
        }
    }

}
