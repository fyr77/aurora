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

    private Planet myPlanet;

    public LandingShuttle(Planet planet, int x, int y) {
        super(x, y);
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
    public void interact(World world) {
        myPlanet.leavePlanet(world);
    }

    @Override
    public String getName() {
        return "Shuttle";
    }
}
