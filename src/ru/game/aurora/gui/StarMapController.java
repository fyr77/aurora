package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.*;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.research.ResearchProjectState;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.*;
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
    private final World world;

    private Element mapPanel;

    private Element myWindow;

    private Element mouseCoords;

    private Camera myCamera;

    private final GalaxyMap galaxyMap;

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

    private void mark(Graphics g, GalaxyMapObject obj) {
        EngineUtils.drawDashedCircleCentered(g, myCamera.getXCoord(obj.getX()) + myCamera.getTileWidth() / 2, myCamera.getYCoord(obj.getY()) + myCamera.getTileHeight() / 2, (int) (myCamera.getTileHeight() * 4), Color.green, 10);
    }

    public static void updateStarmapLabels(World world)
    {
        GalaxyMap galaxyMap = world.getGalaxyMap();
        for (int i = 0; i < galaxyMap.getTilesY(); ++i) {
            for (int j = 0; j < galaxyMap.getTilesX(); ++j) {
                GalaxyMapObject obj = galaxyMap.getObjectAt(j, i);
                if (obj == null) {
                    continue;
                }

                StringBuilder messageBuilder = new StringBuilder();
                for (GameEventListener listener : world.getListeners()) {
                    final String localizedMessageForStarSystem = listener.getLocalizedMessageForStarSystem(world, obj);
                    if (localizedMessageForStarSystem != null) {
                        messageBuilder.append(localizedMessageForStarSystem).append('\n');
                    }
                }

                if (obj instanceof StarSystem) {
                    ((StarSystem) obj).setMessageForStarMap(messageBuilder.toString());
                }
            }
        }

        for (ResearchProjectState activeResearch : world.getPlayer().getResearchState().getCurrentProjects()) {
            for (StarSystem ss : activeResearch.desc.getTargetStarSystems()) {
                ss.setMessageForStarMap((ss.getMessageForStarMap() != null ? ss.getMessageForStarMap() + activeResearch.desc.getName() : activeResearch.desc.getName()));
            }
        }

        for (Faction f : world.getFactions().values()) {
            if (f instanceof AlienRace && ((AlienRace) f).getHomeworld() != null && ((AlienRace) f).isKnown()) {
                String message = Localization.getText("gui", "starmap." + f.getName().toLowerCase() + "_homeworld");
                if (message != null) {
                    String oldMessage = ((AlienRace) f).getHomeworld().getMessageForStarMap();
                    ((AlienRace) f).getHomeworld().setMessageForStarMap(message + (oldMessage != null ? "\n" + oldMessage : ""));
                }
            }
        }
    }

    private void draw(GameContainer container, Graphics g) {
        g.setBackground(Color.black);
        g.clear();
        updateStarmapLabels(world);
        for (int i = 0; i < galaxyMap.getTilesY(); ++i) {
            for (int j = 0; j < galaxyMap.getTilesX(); ++j) {
                GalaxyMapObject obj = galaxyMap.getObjectAt(j, i);
                if (obj == null) {
                    continue;
                }
                obj.drawOnGlobalMap(container, g, myCamera, j, i);
                if (obj instanceof StarSystem && ((StarSystem) obj).getMessageForStarMap() != null) {
                    mark(g, obj);
                }

            }
        }

        // draw alien areas

        for (Faction faction : world.getFactions().values()) {
            if (!(faction instanceof AlienRace)) {
                continue;
            }
            AlienRace race = (AlienRace) faction;
            if (race.getName().equals(HumanityGenerator.NAME)
                    || race.getName().equals(GardenerGenerator.NAME)) {
                continue;
            }

            final StarSystem homeworld = race.getHomeworld();
            if (homeworld == null) {
                continue;
            }
            if (!race.isKnown()) {
                continue;
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

        mouseCoords = myWindow.findElementByName("mouse_pos");
    }

    public GalaxyMapObject getGalaxyMapObjectAtMouseCoords() {
        int x = GUI.getInstance().getNifty().getNiftyMouse().getX() - mapPanel.getX();
        int y = GUI.getInstance().getNifty().getNiftyMouse().getY() - mapPanel.getY();

        if (!myCamera.isInViewportScreen(x, y)) {
            return null;
        }

        x = myCamera.getPointTileX(x);
        y = myCamera.getPointTileY(y);
        double minDist = Double.POSITIVE_INFINITY;
        GalaxyMapObject result = null;
        for (GalaxyMapObject gmo : world.getGalaxyMap().getGalaxyMapObjects()) {
            double newDist = BasePositionable.getDistance(gmo.getX(), gmo.getY(), x, y);
            if (newDist < minDist) {
                result = gmo;
                minDist = newDist;
            }
        }
        return minDist < 3 ? result : null;
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

        Image map;
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

    public void onMouseMoved() {
        int x = GUI.getInstance().getNifty().getNiftyMouse().getX() - mapPanel.getX();
        int y = GUI.getInstance().getNifty().getNiftyMouse().getY() - mapPanel.getY();

        if (!myCamera.isInViewportScreen(x, y)) {
            return;
        }

        x = myCamera.getPointTileX(x);
        y = myCamera.getPointTileY(y);

        EngineUtils.setTextForGUIElement(mouseCoords, String.format("[%d, %d]", x, y));
    }

    @NiftyEventSubscriber(id = "star_map_window")
    public void onClose(final String id, final WindowClosedEvent event) {
        closeScreen();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }
}
