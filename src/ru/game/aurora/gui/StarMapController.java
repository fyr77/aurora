package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.*;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.Movable;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.GardenerGenerator;
import ru.game.aurora.world.generation.aliens.KliskGenerator;
import ru.game.aurora.world.generation.aliens.RoguesGenerator;
import ru.game.aurora.world.generation.aliens.bork.BorkGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 31.03.14
 * Time: 17:24
 */
public class StarMapController implements ScreenController {
    private World world;

    private Element mapPanel;

    private Element myWindow;

    private Camera myCamera;

    private GalaxyMap galaxyMap;

    private static final Map<String, Color> alienColorMap;

    static {
        alienColorMap = new HashMap<>();
        alienColorMap.put(KliskGenerator.NAME, new Color(255, 190, 0, 50));
        alienColorMap.put(RoguesGenerator.NAME, new Color(89, 81, 113, 50));
        alienColorMap.put(ZorsanGenerator.NAME, new Color(44, 20, 38, 50));
        alienColorMap.put(BorkGenerator.NAME, new Color(180, 150, 82, 50));
    }

    public StarMapController(World world) {
        this.world = world;
        galaxyMap = world.getGalaxyMap();
    }

    private void draw(GameContainer container, Graphics g) {
        g.setBackground(Color.black);
        g.clear();
        StarSystem solarSystem = (StarSystem) world.getGlobalVariables().get("solar_system");
        for (int i = 0; i < galaxyMap.getTilesY(); ++i) {
            for (int j = 0; j < galaxyMap.getTilesX(); ++j) {
                GalaxyMapObject obj = galaxyMap.getObjectAt(j, i);
                if (obj != null) {
                    obj.drawOnGlobalMap(container, g, myCamera, j, i);
                }
                if (solarSystem == obj) {
                    g.setColor(Color.yellow);
                    g.drawString("Solar system", myCamera.getXCoord(j), myCamera.getYCoord(i));
                }
            }
        }

        // draw alien areas

        for (AlienRace race : world.getRaces().values()) {
            if (race.getName().equals(HumanityGenerator.NAME)
                    || race.getName().equals(GardenerGenerator.NAME)) {
                continue;
            }

            final StarSystem homeworld = race.getHomeworld();
            if (homeworld == null) {
                continue;
            }
            if (!race.isKnown()) {
                //   continue;
            }

            Color alienColor = alienColorMap.get(race.getName());
            if (alienColor == null) {
                alienColor = Color.gray;
            }
            g.setColor(alienColor);
            final float width = myCamera.getTileWidth() * race.getTravelDistance();
            final float height = myCamera.getTileHeight() * race.getTravelDistance();
            g.fillOval(myCamera.getXCoord(homeworld.getX()) - width / 2, myCamera.getYCoord(homeworld.getY()) - height / 2, width, height);
            g.setColor(alienColor.brighter());
            g.drawOval(myCamera.getXCoord(homeworld.getX()) - width / 2, myCamera.getYCoord(homeworld.getY()) - height / 2, width, height);
            g.setColor(new Color(alienColor.r, alienColor.g, alienColor.b));
            g.drawString(race.getName(), myCamera.getXCoord(homeworld.getX()), myCamera.getYCoord(homeworld.getY()));
        }

    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        myWindow = screen.findElementByName("star_map_window");
        mapPanel = screen.findElementByName("map_panel");

        final float newTileWidth = mapPanel.getWidth() / (float) (world.getGalaxyMap().getTilesX() + 2);
        final float newTileHeight = mapPanel.getHeight() / (float) (world.getGalaxyMap().getTilesY() + 2);
        myCamera = new Camera(1, 1, world.getGalaxyMap().getTilesX() + 1, world.getGalaxyMap().getTilesY() + 1, newTileWidth, newTileHeight);

        myCamera.setTarget(new Movable(world.getGalaxyMap().getTilesX() / 2, world.getGalaxyMap().getTilesY() / 2));
    }

    public GalaxyMapObject getGalaxyMapObjectAtMouseCoords() {
        int x = GUI.getInstance().getNifty().getNiftyMouse().getX() - mapPanel.getX();
        int y = GUI.getInstance().getNifty().getNiftyMouse().getY() - mapPanel.getY();

        if (!myCamera.isInViewportScreen(x, y)) {
            return null;
        }

        x = myCamera.getPointTileX(x);
        y = myCamera.getPointTileY(y);
        for (GalaxyMapObject gmo : world.getGalaxyMap().getObjects()) {
            if (BasePositionable.getDistance(gmo.getX(), gmo.getY(), x, y) < 3) {
                return gmo;
            }
        }
        return null;
    }

    private Image createGlobalMap() throws SlickException {
        final Ship ship = world.getPlayer().getShip();

        Image result = new Image(mapPanel.getWidth(), mapPanel.getHeight());
        Graphics g = result.getGraphics();
        draw(null, g);
        final Image aurora = ResourceManager.getInstance().getImage("aurora");
        g.drawImage(aurora, myCamera.getXCoord(ship.getX()) - aurora.getWidth() / 2, myCamera.getYCoord(ship.getY()) - aurora.getHeight() / 2);
        g.flush();
        return result;
    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);

        Image map = null;
        try {
            map = createGlobalMap();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        EngineUtils.setImageForGUIElement(mapPanel, map);

        world.setPaused(true);
    }

    @Override
    public void onEndScreen() {
        world.setPaused(false);
    }

    @NiftyEventSubscriber(id = "star_map_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }
}
