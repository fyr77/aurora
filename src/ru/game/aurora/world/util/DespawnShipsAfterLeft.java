package ru.game.aurora.world.util;

import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by di Grigio on 10.04.2017.
 * - Simple way to remove single or group of ship like objects
 */
public class DespawnShipsAfterLeft extends GameEventListener {

    private static final long serialVersionUID = 2945396197166747131L;

    private Set<GameObject> objList;
    private StarSystem targetStarSystem;

    private DespawnShipsAfterLeft(StarSystem starSystem){
        objList = new HashSet<GameObject>();
        targetStarSystem = starSystem;
    }

    public DespawnShipsAfterLeft(GameObject object, StarSystem starSystem){
        this(starSystem);
        objList.add(object);

    }

    public DespawnShipsAfterLeft(Collection<GameObject> objects, StarSystem starSystem){
        this(starSystem);
        objList.addAll(objects);
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem starSystem) {
        if(targetStarSystem != null && objList.size() > 0){
            List<GameObject> objects = this.targetStarSystem.getShips();

            if(objects != null && objects.size() > 0){
                objects.removeAll(objList);
            }
        }

        removeListener(world);
        return true;
    }
}
