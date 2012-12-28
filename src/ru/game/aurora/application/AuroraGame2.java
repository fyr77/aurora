/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 16:06
 */
package ru.game.aurora.application;

import org.newdawn.slick.*;
import ru.game.aurora.world.World;


public class AuroraGame2 extends BasicGame {
    private World world;

    public AuroraGame2() {
        super("Aurora");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        world = new World(new Camera(0, 0, 64, 64, gameContainer.getWidth() / 64, gameContainer.getHeight() / 64), 100, 100);
    }

    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        world.update(gameContainer);
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        world.draw(gameContainer, graphics);
    }

    public static void main(String[] args) throws SlickException {

        AppGameContainer app = new AppGameContainer(new AuroraGame2());
        app.setDisplayMode(1024, 768, false);
        app.start();
    }
}
