package ru.game.aurora.world.generation.quest.asteroidbelt;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

public class AsteroidBeltQuestGenerator extends GameEventListener {

    private static final long serialVersionUID = 2428087533042522137L;

    private final StarSystem.AsteroidBelt asteroidBelt;

    public AsteroidBeltQuestGenerator(StarSystem.AsteroidBelt asteroidBelt){
        this.asteroidBelt = asteroidBelt;
    }

    @Override
    public boolean onTurnEnded(World world) {
        if(asteroidBelt == null){ // it checked in StarSystem.enter(), but somebody can forget
            return false;
        }

        final int shipX = world.getPlayer().getShip().getX();
        final int shipY = world.getPlayer().getShip().getY();
        final long rangeFromCenter = Math.round(Math.sqrt(shipX * shipX + shipY * shipY));

        if (rangeFromCenter >= asteroidBelt.innerRadius && rangeFromCenter < asteroidBelt.innerRadius + asteroidBelt.width) {
            if (CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("encounter.asteroid_belt.chance")) {
                world.getListeners().remove(this);
                startEncounter(world);
                return true;
            }
        }

        return false;
    }

    private void startEncounter(World world) {
        Room encounterRoom = new AsteroidBeltEncounter(world.getCurrentStarSystem());
        world.setCurrentRoom(encounterRoom);
        encounterRoom.enter(world);
    }
}