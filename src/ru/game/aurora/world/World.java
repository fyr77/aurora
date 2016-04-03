/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:09
 */
package ru.game.aurora.world;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.*;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.player.Player;
import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.player.earth.EvacuationState;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.RnDSet;
import ru.game.aurora.world.generation.StarSystemNamesCollection;
import ru.game.aurora.world.generation.WorldGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.Environment;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class World implements Serializable, ResolutionChangeListener {

    private static final long serialVersionUID = 3L;
    private static final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", 
    		Locale.forLanguageTag(Configuration.getSystemProperties().getProperty("locale")));
    
    private static final Logger logger = LoggerFactory.getLogger(World.class);
    private static World world;
    private final GalaxyMap galaxyMap;
    private final Player player;
    private final Calendar currentDate;
    private final Reputation reputation;
    private final transient StarSystemNamesCollection starSystemNamesCollection = new StarSystemNamesCollection();
    private final List<GameEventListener> listeners = new LinkedList<>();
    private final Map<String, Faction> factions = new HashMap<>();
    private final Map<String, Serializable> globalVariables = new HashMap<>();
    private final RnDSet researchAndDevelopmentProjects;
    // to distinguish save games made by different players
    private final UUID uuid;
    private Camera camera;
    /**
     * Set by game logic, shows that game is over and on next update this world will be deallocated and main menu will be shown
     */
    private boolean isGameOver;
    private Room currentRoom;
    // if true, current room is not updated
    private boolean isPaused;
    private transient boolean updatedThisFrame;
    private transient boolean updatedNextFrame;
    private StarSystem currentStarSystem = null;
    private int dayCount = 0;
    // each turn a value defined by currentRoom.getTurnToDayRelation() is added to this value
    // if it becomes greater than 1 then a day has passed and listeners are called
    private double dayFraction = 0.0;
    // Version of a game that created this world. Can be used on save loading to detect if save conversion is required
    private String gameVersion = Version.VERSION;

    private boolean cheatsUsed = false;

    public World(int sizeX, int sizeY) {
        player = new Player();
        updatedThisFrame = false;
        currentRoom = galaxyMap = new GalaxyMap(this, sizeX, sizeY);
        researchAndDevelopmentProjects = new RnDSet();
        reputation = new Reputation();
        currentDate = new GregorianCalendar(Configuration.getIntProperty("world.startYear"), 1, 1);
        uuid = UUID.randomUUID();
        
        world = this;
    }

    public static World getWorld() {
        return world;
    }

    public Map<String, Serializable> getGlobalVariables() {
        return globalVariables;
    }

    public Reputation getReputation() {
        return reputation;
    }

    public void update(GameContainer container) {
        final Nifty nifty = GUI.getInstance().getNifty();
        if (!isPaused) {
            // update game world
            currentRoom.update(container, this);
            if (isUpdatedThisFrame()) {
                dayFraction += currentRoom.getTurnToDayRelation();
                while (dayFraction >= 1) {
                    dayFraction -= 1;
                    currentDate.add(Calendar.DAY_OF_MONTH, 1);
                    dayCount++;
                    player.getResearchState().update(this);
                    player.getEngineeringState().update(this);
                    player.getEarthState().update(this);
                }
                EvacuationState es = player.getEarthState().getEvacuationState();
                if (es != null && es.isGameOver(this)) {
                    es.showEndGameScreen(this);
                    return;
                }
                // to prevent concurrent modification if some of listeners adds new ones
                List<GameEventListener> oldListeners = new LinkedList<>(listeners);
                for (GameEventListener listener : oldListeners) {
                    listener.onTurnEnded(this);
                }
            }

            for (Iterator<GameEventListener> listenerIterator = listeners.iterator(); listenerIterator.hasNext(); ) {
                GameEventListener l = listenerIterator.next();
                if (!l.isAlive()) {
                    listenerIterator.remove();
                }
            }
            updatedThisFrame = updatedNextFrame;
            updatedNextFrame = false;

            if (container.getInput().isKeyPressed(Input.KEY_R)) {
                GUI.getInstance().pushCurrentScreen();
                nifty.gotoScreen("research_screen");
                return;
            }

            if (container.getInput().isKeyPressed(Input.KEY_E)) {
                GUI.getInstance().pushCurrentScreen();
                nifty.gotoScreen("engineering_screen");
                return;
            }

        }

        // should be the last so that ESC event is not consumed
        if (container.getInput().isKeyPressed(Input.KEY_ESCAPE) && (currentRoom instanceof GalaxyMap || currentRoom instanceof Planet || currentRoom instanceof StarSystem || currentRoom instanceof Dungeon)) {
            Element popup = nifty.getTopMostPopup();
            if (popup != null && popup.findElementByName("menu_window") != null) {
                GUI.getInstance().closeIngameMenu();
            } else {
                GUI.getInstance().showIngameMenu();
            }
        }
    }

    public void draw(GameContainer container, Graphics graphics) {
        currentRoom.draw(container, graphics, camera, this);
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public boolean isUpdatedThisFrame() {
        return updatedThisFrame;
    }

    public void setUpdatedThisFrame(boolean updatedThisFrame) {
        this.updatedThisFrame = updatedThisFrame;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public IDungeon getCurrentDungeon() {
        return (currentRoom instanceof IDungeon) ? (IDungeon) currentRoom : null;
    }

    public GalaxyMap getGalaxyMap() {
        return galaxyMap;
    }

    public Player getPlayer() {
        return player;
    }

    public void addOverlayWindow(Dialog d, Map<String, String> flags) {
        logger.info("Opening dialog " + d.getId());
        if (!GUI.getInstance().peekScreen().equals("dialog_screen")) {
            // do not push dialog if it is already on top
            GUI.getInstance().pushCurrentScreen();
        }
        d.enter(this);
        d.setFlags(flags);
        Nifty nifty = GUI.getInstance().getNifty();
        ((DialogController) nifty.getScreen("dialog_screen").getScreenController()).pushDialog(d);
        GUI.getInstance().goToScreen("dialog_screen");
    }

    public void addOverlayWindow(Dialog d) {
        logger.info("Opening dialog " + d.getId());
        if (!GUI.getInstance().peekScreen().equals("dialog_screen")) {
            // do not push dialog if it is already on top
            GUI.getInstance().pushCurrentScreen();
        }
        d.enter(this);
        Nifty nifty = GUI.getInstance().getNifty();
        ((DialogController) nifty.getScreen("dialog_screen").getScreenController()).pushDialog(d);
        GUI.getInstance().goToScreen("dialog_screen");
    }

    public void addOverlayWindow(StoryScreen s) {
        GUI.getInstance().pushCurrentScreen();
        Nifty nifty = GUI.getInstance().getNifty();
        ((StoryScreenController) nifty.getScreen("story_screen").getScreenController()).setStory(s);
        GUI.getInstance().goToScreen("story_screen");
    }

    public void addOverlayWindow(ResearchProjectDesc s) {
        Nifty nifty = GUI.getInstance().getNifty();
        ((ResearchReportScreenController) nifty.getScreen("research_report_screen").getScreenController()).addResearch(s);

        if (!nifty.getCurrentScreen().getScreenId().equals("research_report_screen")) {
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().goToScreen("research_report_screen");
        }
    }

    public int getDayCount() {
        return dayCount;
    }

    public void onPlayerEnteredSystem(StarSystem ss) {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onPlayerEnterStarSystem(this, ss)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void onPlayerLeftSystem(StarSystem ss) {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onPlayerLeftStarSystem(this, ss)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void onPlayerShipDamaged() {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onPlayerShipDamaged(this)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void onCrewChanged() {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onCrewChanged(this)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void onPlayerReturnToEarth() {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onReturnToEarth(this)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void onPlayerEnteredDungeon(Dungeon dungeon) {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onPlayerEnteredDungeon(this, dungeon)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void onPlayerLandedPlanet(Planet planet) {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onPlayerLandedPlanet(this, planet);
        }
    }

    public void onPlayerLeftPlanet(Planet planet) {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onPlayerLeftPlanet(this, planet);
        }
    }

    public void onEarthUpgradeUnlocked(EarthUpgrade upgrade) {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onEarthUpgradeUnlocked(this, upgrade);
        }
    }

    public void onNewGameStarted() {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onNewGameStarted(this);
        }
    }

    public void onGameObjectAttacked(GameObject attacker, GameObject target, int damage) {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onGameObjectAttacked(this, attacker, target, damage);
        }
    }

    public void onItemAmountChanged(InventoryItem item, int amount) {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onItemAmountChanged(this, item, amount);
        }
    }

    public void onLandingPartyDestroyed() {
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {
            l.onLandingPartyDestroyed(this);
        }
    }

    public void onPlayerContactedAlienShip(GameObject ship) {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<>(listeners);
        for (GameEventListener l : newList) {

            boolean alreadyCalledThisGroup = false;
            for (GameEventListener.EventGroup group : l.getGroups()) {
                if (calledGroups.contains(group)) {
                    alreadyCalledThisGroup = true;
                    break;
                }
            }

            if (alreadyCalledThisGroup) {
                continue;
            }

            if (l.onPlayerContactedOtherShip(this, ship)) {
                calledGroups.addAll(l.getGroups());
            }
        }
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public List<GameEventListener> getListeners() {
        return listeners;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public StarSystem getCurrentStarSystem() {
        return currentStarSystem;
    }

    public void setCurrentStarSystem(StarSystem currentStarSystem) {
        this.currentStarSystem = currentStarSystem;
    }

    public Map<String, Faction> getFactions() {
        return factions;
    }

    public StarSystemNamesCollection getStarSystemNamesCollection() {
        return starSystemNamesCollection;
    }

    public RnDSet getResearchAndDevelopmentProjects() {
        return researchAndDevelopmentProjects;
    }

    public void setUpdatedNextFrame(boolean updatedNextFrame) {
        this.updatedNextFrame = updatedNextFrame;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void gameLoaded() {
        logger.info("Game loaded");
        GUI.getInstance().resetIngameMenu();
        if (player.getUniqueItemsPurchased() == null) {
            player.setUniqueItemsPurchased(new HashSet<String>());
        }

        if (gameVersion == null || gameVersion.equals("0.4.0") || gameVersion.equals("0.4.1")) {
            logger.info("Detected game version 0.4.0-0.4.1, applying fixes");
            gameVersion = Version.VERSION;
            listeners.add(new Environment.PlanetProcessor());
            listeners.add(new LoggingListener());
            for (GalaxyMapObject gmo : galaxyMap.getGalaxyMapObjects()) {
                if (gmo instanceof StarSystem) {
                    for (BasePlanet p : ((StarSystem) gmo).getPlanets()) {
                        if (p instanceof Planet) {
                            WorldGenerator.addEnvironmentDangers((Planet) p);
                            if (p.getSatellites() != null) {
                                for (BasePlanet pp : p.getSatellites()) {
                                    if (pp instanceof Planet) {
                                        WorldGenerator.addEnvironmentDangers((Planet) pp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        world = this;
    }

    public String getCurrentDateString() {
        return dateFormat.format(currentDate.getTimeInMillis());
    }

    @Override
    public void onResolutionChanged(int tilesX, int tilesY, boolean fullscreen) {
        logger.info("Changing resolution to {}x{} tiles, fullscreen is {}", tilesX, tilesY, fullscreen);
        Camera oldCamera = camera;
        camera = new Camera(0, 0, tilesX, tilesY, AuroraGame.tileSize, AuroraGame.tileSize);
        camera.setTarget(oldCamera.getTarget());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void checkCheats() {
        if (Configuration.getBooleanProperty("cheat.invulnerability") || Configuration.getBooleanProperty("cheat.skipDungeons")) {
            logger.info("Detected cheats");
            cheatsUsed = true;
        }
    }

    public Object getGlobalVariable(String key, Object defaultValue) {
        Object rz = globalVariables.get(key);
        if (rz == null) {
            rz = defaultValue;
        }
        return rz;
    }
}
