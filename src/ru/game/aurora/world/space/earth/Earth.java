/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.12.12
 * Time: 14:22
 */
package ru.game.aurora.world.space.earth;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.space.StarSystem;

public class Earth extends Planet {
    private Dialog earthDialog;

    public Earth(StarSystem owner, PlanetCategory cat, PlanetAtmosphere atmosphere, int size, int x, int y, boolean hasLife) {
        super(owner, cat, atmosphere, size, x, y, hasLife);
    }

    @Override
    public void drawOnGlobalMap(JGEngine engine, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }

        engine.drawImage("earth", camera.getXCoord(globalX), camera.getYCoord(globalY));
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(JGEngine engine, Player player) {

    }

    @Override
    public void enter(World world) {
        world.setCurrentDialog(earthDialog);
    }

    @Override
    public void update(JGEngine engine, World world) {

    }

    @Override
    public void draw(JGEngine engine, Camera camera) {

    }
}
