package ru.game.aurora.modding.sample;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.modding.ModManager;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.space.ShipLootItem;
import ru.game.aurora.world.space.SpaceDebris;
import ru.game.aurora.world.space.StarSystem;

/**
 * Logic for the sample mod.
 * Methods of this class are called when different game events happen.
 * We need a 'player enters star system' event. When it happens we check the chance of meeting the debris, add them if
 * necessary and remove this mod listener.
 */
public class SampleModEventListener extends GameEventListener {
    public SampleModEventListener() {
        /*
            Event listener may be grouped. If a gouped event returns true from his listener method then all other listeners
            from this group are skipped for this event.
            This is mainly used to prevent more than one random encounter to be spawned on a single event.
         */
        setGroups(EventGroup.ENCOUNTER_SPAWN);
    }

    /**
     * This method is called each time player enters a star system from the galaxy map
     *
     * @param world Game state
     * @param ss    Star system object
     * @return true if this listener has spawned an encounter or modified the world state
     */
    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        /*
            First check that this star system is not a quest one. Quest star systems are Solar System, alien homeworlds or
            other special locations where player should not meet random encounters.
         */
        if (ss.isQuestLocation()) {
            return false;
        }

        /*
         * Our event will happen only when player enters a previously unvisited star system
         */
        if (ss.isVisited()) {
            return false;
        }

        /*
         * Ok the starsystem is suitable, lets check our chance of spawning. Its value is located in the default
         * mod.properties config file so we can easily access it as shown below:
         */
        final String stringValue = ModManager.getModConfiguration(this).getProperty("spawn_chance");
        double probability = Double.parseDouble(stringValue);

        /*
         * We should use globally available random generator
         */
        if (CommonRandom.getRandom().nextDouble() >= probability) {
            // better luck next time!
            return false;
        }

        /*
         * Ok so starsystem is suitable and probability check passed. Create and add debris object.
         * We will use an ItemDebris object that can contain item inside it. We will put there a piece of energy equipment
         * created by the Bork race. Lets imagine that some Bork pirate has found his end here trying to rob someone too tough for him.
         *
         * We could also use ResourceDebris that contain just some Resource Units
         */
        SpaceDebris.ItemDebris debris = new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.ENERGY, BorkGenerator.NAME));

        /*
         * This helper method sets the coordinates of an object so that it is located on an empty tile - not on a planet, star or any
         * other object. So it is clearly visible.
         * We also make sure that it is close enough to the sun (its distance to the sun is somewhere
          * between 10% to 30% of star system size)
         */
        ss.setRandomEmptyPosition(debris, 0.1, 0.3);

        /*
         * Add this object to star system. After it is added it starts rendering and updating
         */
        ss.getObjects().add(debris);

        /*
         * Now lets show a simple dialog
         */

        /*
         * This object has completed its task and should be disposed. Listeners that have isAlive set to false are removed
         * automatically.
         * If we do not do it here our listener will continue to exist and will continue adding debris to other star system
         * when player visits them
         */
        isAlive = false;

        /*
         * As we modified the world state we should return true from this method
         */
        return true;
    }
}
