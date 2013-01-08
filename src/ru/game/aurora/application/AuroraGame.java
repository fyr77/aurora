/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.12.12
 * Time: 16:06
 */
package ru.game.aurora.application;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.GalaxyMapWidget;
import ru.game.aurora.world.World;


public class AuroraGame extends BasicGame {

    private World world;

    private static final int tileSize = 64;

    private static final int tilesX = 20;

    private static final int tilesY = 15;


    public AuroraGame() {
        super("Aurora");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        world = new World(new Camera(0, 0, tilesX, tilesY, tileSize, tileSize), 100, 100);
        GameLogger.init(new Rectangle((tilesX - 5) * tileSize, 0, 5 * tileSize, 10 * tileSize), new Rectangle((tilesX - 5) * tileSize, 10 * tileSize, 5 * tileSize, 5 * tileSize));
        ResourceManager.getInstance().loadResources(AuroraGame.class.getClassLoader().getResourceAsStream("resources.xml"));
        gameContainer.getInput().enableKeyRepeat();

        GUI.init(gameContainer, new Rectangle((tilesX - 5) * tileSize, 0, 5 * tileSize, 15 * tileSize));
        GUI.getInstance().setCurrentScreen(new GalaxyMapWidget(world));
    }

    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        world.update(gameContainer);
        GUI.getInstance().update(gameContainer);
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        world.draw(gameContainer, graphics);
        world.getCamera().drawBound();
        GameLogger.getInstance().draw(graphics);
        GUI.getInstance().draw(gameContainer, graphics);
        GameLogger.getInstance().clearStatusMessages();
    }

    public static void main(String[] args) throws SlickException {
        AppGameContainer app = new AppGameContainer(new AuroraGame());
        app.setDisplayMode(tilesX * tileSize, tilesY * tileSize, false);
        app.start();
    }
}
