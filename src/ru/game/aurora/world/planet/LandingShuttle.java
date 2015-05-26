package ru.game.aurora.world.planet;

import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.ScanGroup;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 11.09.13
 * Time: 12:47
 */

public class LandingShuttle extends BaseGameObject {
    private static final long serialVersionUID = -6432856422423659187L;

    private final Planet myPlanet;

    public LandingShuttle(Planet planet, int x, int y) {
        super(x, y, "shuttle");
        this.myPlanet = planet;
    }

    @Override
    public ScanGroup getScanGroup() {
        return null;
    }

    @Override
    public boolean canBeInteracted() {
        return true;
    }

    @Override
    public boolean interact(World world) {
        myPlanet.leavePlanet(world);
        return true;
    }

    @Override
    public String getName() {
        return "Shuttle";
    }
}
