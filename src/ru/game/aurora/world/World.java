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
import ru.game.aurora.application.AuroraGame;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.ResolutionChangeListener;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.gui.*;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.player.Player;
import ru.game.aurora.player.earth.EvacuationState;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.RnDSet;
import ru.game.aurora.world.generation.StarSystemNamesCollection;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.space.GalaxyMap;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class World implements Serializable, ResolutionChangeListener
{

    private static final long serialVersionUID = 2L;

    private Camera camera;

    /**
     * Set by game logic, shows that game is over and on next update this world will be deallocated and main menu will be shown
     */
    private boolean isGameOver;

    private Room currentRoom;

    // if true, current room is not updated
    private boolean isPaused;

    private GalaxyMap galaxyMap;

    private Player player;

    private Calendar currentDate;

    private Reputation reputation;

    private transient boolean updatedThisFrame;

    private transient boolean updatedNextFrame;

    private transient StarSystemNamesCollection starSystemNamesCollection = new StarSystemNamesCollection();

    private transient List<OverlayWindow> overlayWindows = new LinkedList<OverlayWindow>();

    private static DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    private StarSystem currentStarSystem = null;

    private int turnCount = 0;

    private List<GameEventListener> listeners = new LinkedList<GameEventListener>();

    private Map<String, AlienRace> races = new HashMap<String, AlienRace>();

    private Map<String, Serializable> globalVariables = new HashMap<String, Serializable>();

    private RnDSet researchAndDevelopmentProjects;


    public World(int sizeX, int sizeY) {
        player = new Player();
        updatedThisFrame = false;
        currentRoom = galaxyMap = new GalaxyMap(this, sizeX, sizeY);
        researchAndDevelopmentProjects = new RnDSet();
        reputation = new Reputation();
        currentDate = new GregorianCalendar(Configuration.getIntProperty("world.startYear"), 1, 1);
    }

    public Map<String, Serializable> getGlobalVariables() {
        return globalVariables;
    }

    public Reputation getReputation() {
        return reputation;
    }

    public void update(GameContainer container) {
        if (overlayWindows != null && !overlayWindows.isEmpty()) {
            Iterator<OverlayWindow> iter = overlayWindows.iterator();
            OverlayWindow w = iter.next();
            w.update(container, this);
            if (w.isOver()) {
                iter.remove();
            }
            // only one active overlay window
            return;
        }

        final Nifty nifty = GUI.getInstance().getNifty();
        if (!isPaused) {
            // update game world
            currentRoom.update(container, this);
            if (isUpdatedThisFrame()) {
                player.getResearchState().update(this);
                player.getEngineeringState().update(this);
                player.getEarthState().update(this);
                turnCount++;
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                EvacuationState es = player.getEarthState().getEvacuationState();
                if (es != null && es.isGameOver(this)) {
                    es.showEndGameScreen(this);
                    return;
                }
                // to prevent concurrent modification if some of listeners adds new ones
                List<GameEventListener> oldListeners = new LinkedList<GameEventListener>(listeners);
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
            return;
        }

        if (container.getInput().isKeyPressed(Input.KEY_F1)) {
            addOverlayWindow(new HelpScreen());
        }

    }

    public void draw(GameContainer container, Graphics graphics) {
        currentRoom.draw(container, graphics, camera);
        if (overlayWindows != null && !overlayWindows.isEmpty()) {
            overlayWindows.get(0).draw(container, graphics, camera);
        }
    }

    public Camera getCamera() {
        return camera;
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

    public IDungeon getCurrentDungeon() {
        return (currentRoom instanceof IDungeon) ? (IDungeon) currentRoom : null;
    }

    public GalaxyMap getGalaxyMap() {
        return galaxyMap;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void addOverlayWindow(OverlayWindow currentDialog) {
        if (overlayWindows == null) {
            overlayWindows = new LinkedList<>();
        }
        overlayWindows.add(currentDialog);
        currentDialog.enter(this);
    }

    public void addOverlayWindow(Dialog d, Map<String, String> flags) {
        GUI.getInstance().pushCurrentScreen();
        d.enter(this);
        d.setFlags(flags);
        Nifty nifty = GUI.getInstance().getNifty();
        ((DialogController) nifty.getScreen("dialog_screen").getScreenController()).pushDialog(d);
        nifty.gotoScreen("dialog_screen");
    }

    public void addOverlayWindow(Dialog d) {
        GUI.getInstance().pushCurrentScreen();
        d.enter(this);
        Nifty nifty = GUI.getInstance().getNifty();
        ((DialogController) nifty.getScreen("dialog_screen").getScreenController()).pushDialog(d);
        nifty.gotoScreen("dialog_screen");
    }

    public void addOverlayWindow(StoryScreen s) {
        GUI.getInstance().pushCurrentScreen();
        Nifty nifty = GUI.getInstance().getNifty();
        ((StoryScreenController) nifty.getScreen("story_screen").getScreenController()).setStory(s);
        nifty.gotoScreen("story_screen");
    }

    public void addOverlayWindow(ResearchProjectDesc s) {
        GUI.getInstance().pushCurrentScreen();
        Nifty nifty = GUI.getInstance().getNifty();
        ((ResearchReportScreenController) nifty.getScreen("research_report_screen").getScreenController()).setResearch(s);
        nifty.gotoScreen("research_report_screen");
    }

    public int getTurnCount() {
        return turnCount;
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
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
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
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
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
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
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


    public void onPlayerContactedAlienShip(SpaceObject ship) {
        Set<GameEventListener.EventGroup> calledGroups = new HashSet<>();
        List<GameEventListener> newList = new LinkedList<GameEventListener>(listeners);
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

    public void setCamera(Camera camera) {
        this.camera = camera;
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

    public Map<String, AlienRace> getRaces() {
        return races;
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

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void gameLoaded()
    {
        if (currentDate == null) {
            currentDate = new GregorianCalendar(Configuration.getIntProperty("world.startYear"), 1, 1);
            currentDate.add(Calendar.DAY_OF_MONTH, turnCount);
        }
    }

    public Calendar getCurrentDate() {
        return currentDate;
    }

    public String getCurrentDateString() {
        return dateFormat.format(currentDate.getTimeInMillis());
    }

    @Override
    public void onResolutionChanged(int tilesX, int tilesY, boolean fullscreen) {
        Camera oldCamera = camera;
        camera = new Camera(0, 0, tilesX, tilesY, AuroraGame.tileSize, AuroraGame.tileSize);
        camera.setTarget(oldCamera.getTarget());
    }
}
