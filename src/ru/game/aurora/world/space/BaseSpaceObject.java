package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 08.02.14
 * Time: 23:03
 */
public class BaseSpaceObject extends BasePositionable implements SpaceObject {
    private static final long serialVersionUID = -409390232375701404L;

    public BaseSpaceObject(int x, int y) {
        super(x, y);
    }

    @Override
    public void onContact(World world) {
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getScanDescription(World world) {
        return "";
    }

    @Override
    public AlienRace getRace() {
        return null;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
    }

    @Override
    public void update(GameContainer container, World world) {
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
