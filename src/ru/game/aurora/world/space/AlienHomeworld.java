/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 14:39
 */
package ru.game.aurora.world.space;

import jgame.JGRectangle;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;

/**
 * Planet inhabited with aliens.
 * Such planet can not be landed on, only communicated
 */
public class AlienHomeworld extends BasePlanet {
    private AlienRace ownerRace;

    private Dialog dialog;

    private String spriteName;

    public AlienHomeworld(String spriteName, AlienRace ownerRace, Dialog customDialog, int size, int y, StarSystem owner, PlanetAtmosphere atmosphere, int x, PlanetCategory cat) {
        super(size, y, owner, atmosphere, x, cat);
        this.ownerRace = ownerRace;
        this.dialog = customDialog;
        this.spriteName = spriteName;

    }


    @Override
    public void drawOnGlobalMap(JGEngine engine, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }
        JGRectangle rect = engine.getImageBBox(spriteName);
        engine.drawImage(spriteName, camera.getXCoord(globalX) - rect.width / 2 + camera.getTileWidth() / 2, camera.getYCoord(globalY) - rect.height / 2 + camera.getTileHeight() / 2);
    }

    @Override
    public void enter(World world) {
        world.setCurrentDialog(dialog);
    }

    @Override
    public void update(JGEngine engine, World world) {
        world.setCurrentRoom(owner);
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        // nothing
    }

    public AlienRace getOwnerRace() {
        return ownerRace;
    }
}
