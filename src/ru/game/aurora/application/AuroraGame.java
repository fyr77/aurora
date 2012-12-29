/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 16:06
 */
package ru.game.aurora.application;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.world.World;


public class AuroraGame extends BasicGame {
    private World world;

    public AuroraGame() {
        super("Aurora");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        world = new World(new Camera(0, 0, 64, 64, gameContainer.getWidth() / 64, gameContainer.getHeight() / 64), 100, 100);
        GameLogger.init(new Rectangle(15 * 64, 0, 5 * 64, 10 * 64), new Rectangle(15 * 64, 10 * 64, 5 * 64, 5 * 64));
    }

    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        world.update(gameContainer);
        gameContainer.getInput().clearKeyPressedRecord();
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        world.draw(gameContainer, graphics);
        world.getCamera().drawBound();
        GameLogger.getInstance().draw(graphics);
        GameLogger.getInstance().clearStatusMessages();
    }

    public static void main(String[] args) throws SlickException {

        AppGameContainer app = new AppGameContainer(new AuroraGame());
        app.setDisplayMode(1024, 768, false);
        app.start();
    }
}
