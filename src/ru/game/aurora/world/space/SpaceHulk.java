/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 07.02.13
 * Time: 15:16
 */

package ru.game.aurora.world.space;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;

public class SpaceHulk extends BasePositionable implements SpaceObject
{
    private static final long serialVersionUID = 1122189215297259875L;

    private String name;

    private String image;

    public SpaceHulk(int x, int y, String name, String image) {
        super(x, y);
        this.name = name;
        this.image = image;
    }

    @Override
    public void onContact(World world) {

    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
        // nothing
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(GameContainer container, World world) {
        // nothing
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(ResourceManager.getInstance().getImage(image), camera.getXCoord(x), camera.getYCoord(y));
    }
}
