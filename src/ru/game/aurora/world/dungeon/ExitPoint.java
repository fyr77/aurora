/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 09.09.14
 * Time: 14:12
 */

package ru.game.aurora.world.dungeon;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.BaseGameObject;
import ru.game.aurora.world.World;

/**
 * Invisible item
 * When activated - leaves current dungeon
 */
public class ExitPoint extends BaseGameObject
{
    private static final long serialVersionUID = 1820108655347101347L;

    public ExitPoint(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean canBeInteracted() {
        return true;
    }

    @Override
    public boolean interact(World world) {
        world.getCurrentDungeon().getController().returnToPrevRoom(world.getCurrentRoom().getMap().getVictoryConditions().isEmpty());
        return true;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {

    }
}
