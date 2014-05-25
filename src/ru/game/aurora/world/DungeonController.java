/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.09.13
 * Time: 22:02
 */

package ru.game.aurora.world;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.dungeon.IVictoryCondition;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.PlanetObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains logic for player landing party movement and combat
 */
public class DungeonController extends Listenable implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Mode for moving. Arrows control landing party movement.
     */
    private static final int MODE_MOVE = 0;

    /**
     * Mode for shooting. Arrows control target selection.
     */
    private static final int MODE_SHOOT = 1;

    private Room prevRoom;
    /**
     * Current mode
     */
    private int mode = MODE_MOVE;

    private ITileMap map;

    private boolean isWrap;

    private World world;

    private LandingParty landingParty;

    private IDungeon myDungeon;

    private Dialog successDialog;

    private int tilesExploredThisTurn = 0;

    private transient List<Effect> effects = new ArrayList<>();

    private transient Effect currentEffect = null;

    /**
     * When in fire mode, this is currently selected target
     */
    private transient PlanetObject target = null;

    private int xClick;
    private int yClick;

    public DungeonController(World world, Room prevRoom, IDungeon myDungeon) {
        this.myDungeon = myDungeon;
        this.prevRoom = prevRoom;
        this.world = world;
        this.landingParty = world.getPlayer().getLandingParty();

        this.isWrap = myDungeon.getMap().isWrapped();
        this.map = myDungeon.getMap();
    }


    /**
     * This update is used in MOVE mode. Moving landing party around.
     */
    private void updateMove(GameContainer container, World world) {
        if (landingParty.nowMoving()) {
            landingParty.update(container, world);
            if (isWrap) {
                landingParty.setPos(EngineUtils.wrap(landingParty.getX(), map.getWidthInTiles()), EngineUtils.wrap(landingParty.getY(), map.getHeightInTiles()));
            }
            return;
        }
        int x = landingParty.getX();
        int y = landingParty.getY();
        boolean actuallyMoved = false;

        int dx = 0;
        int dy = 0;

        if (container.getInput().isKeyPressed(Input.KEY_UP)) {
            y--;
            dy = -1;
            partyMove(world);
            actuallyMoved = true;
        } else if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            y++;
            dy = 1;
            partyMove(world);
            actuallyMoved = true;
        } else if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            x--;
            dx = -1;
            partyMove(world);
            actuallyMoved = true;
        } else if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            x++;
            dx = 1;
            partyMove(world);
            actuallyMoved = true;
        }

        if (!actuallyMoved) {
            return;
        }

        if (isWrap) {
            x = EngineUtils.wrap(x, map.getWidthInTiles());
            y = EngineUtils.wrap(y, map.getHeightInTiles());
        } else {
            if (x < 0 || x >= map.getWidthInTiles() || y < 0 || y >= map.getHeightInTiles()) {
                x = landingParty.getX();
                y = landingParty.getY();
                actuallyMoved = false;
            }
        }

        if (!map.isTilePassable(landingParty, x, y)) {
            world.setUpdatedThisFrame(false);
            actuallyMoved = false;
        }

        final boolean enterPressed = container.getInput().isKeyPressed(Input.KEY_ENTER);
        if (enterPressed) {
            interactWithObject(world);
        }

        if (!actuallyMoved) {
            return;
        }

        tilesExploredThisTurn = map.updateVisibility(x, y, 3);
        landingParty.addCollectedGeodata(tilesExploredThisTurn);

        if (dy < 0) {
            landingParty.moveUp();
        }
        if (dy > 0) {
            landingParty.moveDown();
        }

        if (dx < 0) {
            landingParty.moveLeft();
        }
        if (dx > 0) {
            landingParty.moveRight();
        }

        // world.getPlayer().getLandingParty().setPos(x, y);
    }

    private void partyMove(World world) {
        world.getCamera().resetViewPort();
        world.setUpdatedThisFrame(true);
    }

    public void interactWithObject(World world) {
        int x = world.getPlayer().getLandingParty().getX();
        int y = world.getPlayer().getLandingParty().getY();
        // check if can pick up smth
        for (Iterator<PlanetObject> iter = map.getObjects().iterator(); iter.hasNext(); ) {
            PlanetObject p = iter.next();

            if (!p.canBePickedUp()) {
                continue;
            }

            if ((int) BasePositionable.getDistance(x, y, p.getX(), p.getY()) != 0
                    && (!map.isWrapped() || (int) BasePositionable.getDistanceWrapped(x, y, p.getX(), p.getY(), map.getWidthInTiles(), map.getHeightInTiles()) != 0)) {
                continue;
            }
            p.onPickedUp(world);
            world.setUpdatedThisFrame(true);
            // some items (like ore deposits) can be picked up more than once, do not remove them in this case
            if (!p.isAlive()) {
                iter.remove();
            }
        }

        for (BasePositionable exitPoint : map.getExitPoints()) {
            if (getDistance(exitPoint, landingParty) == 0) {
                returnToPrevRoom(map.getVictoryConditions().isEmpty());
            }
        }
    }

    private int getDist(int first, int second, int total) {
        int max = Math.max(first, second);
        int min = Math.min(first, second);

        return Math.min(max - min, total + min - max);

    }

    private int getRange(LandingParty party, Positionable target) {
        int xDist = getDist(party.getX(), target.getX(), map.getWidthInTiles());
        int yDist = getDist(party.getY(), target.getY(), map.getHeightInTiles());
        return xDist + yDist;
    }

    /**
     * This update method is used in FIRE mode. Selecting targets and shooting.
     */
    public void updateShoot(World world, boolean nextTarget, boolean prevTarget, boolean shoot) {
        if (map.getObjects().isEmpty()) {
            return;
        }
        int targetIdx = 0;
        List<PlanetObject> availableTargets = new ArrayList<>();

        if (target != null && (!target.isAlive() || getRange(landingParty, target) > landingParty.getWeapon().getRange())) {
            // target moved out of range
            target = null;
        }

        for (PlanetObject planetObject : map.getObjects()) {
            if (!planetObject.canBeShotAt()) {
                continue;
            }
            if (!map.isTileVisible(planetObject.getX(), planetObject.getY())) {
                // do not target animals on unexplored tiles
                continue;
            }
            if (landingParty.getWeapon().getRange() >= getRange(landingParty, planetObject)) {
                availableTargets.add(planetObject);
                if (target == null) {
                    target = planetObject;
                }

                if (target == planetObject) {
                    targetIdx = availableTargets.size() - 1;
                }
            }

        }

        if (availableTargets.isEmpty()) {
            // no target available in weapon range
            return;
        }

        if (nextTarget) {
            targetIdx++;
            if (targetIdx >= availableTargets.size()) {
                targetIdx = 0;
            }
        } else if (prevTarget) {
            targetIdx--;
            if (targetIdx < 0) {
                targetIdx = availableTargets.size() - 1;
            }
        }

        target = availableTargets.get(targetIdx);

        if (shoot) {
            // check line of sight
            if (!map.lineOfSightExists(landingParty.getX(), landingParty.getY(), target.getX(), target.getY())) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.no_line_of_sight"));
                return;
            }

            // firing
            final int damage = landingParty.calcDamage(world);

            BlasterShotEffect blasterShotEffect = new BlasterShotEffect(
                    landingParty
                    , world.getCamera().getXCoordWrapped(target.getX(), map.getWidthInTiles()) + world.getCamera().getTileWidth() / 2
                    , world.getCamera().getYCoordWrapped(target.getY(), map.getHeightInTiles()) + world.getCamera().getTileHeight() / 2
                    , world.getCamera()
                    , 800
                    , landingParty.getWeapon());
            effects.add(blasterShotEffect);

            ResourceManager.getInstance().getSound(landingParty.getWeapon().getShotSound()).play();

            blasterShotEffect.setEndListener(new IStateChangeListener() {
                private static final long serialVersionUID = -7742240385490245306L;

                @Override
                public void stateChanged(World world) {
                    target.onShotAt(world, damage);
                    GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.damage_message"), damage, target.getName()));
                    if (!target.isAlive()) {
                        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.killed_message"), target.getName()));
                        map.getObjects().remove(target);
                        target = null;
                    }
                }
            });

            world.setUpdatedThisFrame(true);
        }

    }

    public void changeMode() {
        mode = (mode == MODE_MOVE) ? MODE_SHOOT : MODE_MOVE;
        GUI.getInstance().getNifty().getCurrentScreen().findElementByName("shoot_panel").setVisible(mode == MODE_SHOOT);
    }

    public void update(GameContainer container, World world) {
        Camera myCamera = world.getCamera();
        if (container.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
            xClick = container.getInput().getMouseX() - myCamera.getViewportX();
            yClick = container.getInput().getMouseY() - myCamera.getViewportY();
        }
        if (container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            myCamera.setViewportX(container.getInput().getMouseX() - xClick);
            myCamera.setViewportY(container.getInput().getMouseY() - yClick);
        }
        if (container.getInput().isKeyPressed(Input.KEY_HOME)) {
            myCamera.resetViewPort();
        }
        if (effects == null) {
            effects = new ArrayList<>();
        }
        if (currentEffect == null && !effects.isEmpty()) {
            currentEffect = effects.remove(0);
        }

        if (currentEffect != null) {
            currentEffect.update(container, world);
            if (currentEffect.isOver()) {
                currentEffect.onOver(world);
                currentEffect = null;
            }
            if (!world.isUpdatedThisFrame()) {
                return;
            }
        }
        tilesExploredThisTurn = 0;
        switch (mode) {
            case MODE_MOVE:
                if (container.getInput().isKeyPressed(Input.KEY_F)) {
                    changeMode();
                    return;
                }
                updateMove(container, world);
                break;
            case MODE_SHOOT:
                if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                    changeMode();
                    return;
                }
                updateShoot(
                        world
                        , container.getInput().isKeyPressed(Input.KEY_UP) || container.getInput().isKeyPressed(Input.KEY_RIGHT)
                        , container.getInput().isKeyPressed(Input.KEY_DOWN) || container.getInput().isKeyPressed(Input.KEY_LEFT)
                        , container.getInput().isKeyPressed(Input.KEY_F) || container.getInput().isKeyPressed(Input.KEY_ENTER)
                );
                break;
            default:
                throw new IllegalStateException("Unknown planet update type " + mode);

        }
        // should always be done after player update, so that world.isUpdatedThisFrame() flag is set
        boolean isAtObject = false;
        for (PlanetObject a : new ArrayList<>(map.getObjects())) {
            a.update(container, world);
            if (getDistance(landingParty, a) == 0 && a.canBePickedUp()) {
                isAtObject = true;
            }
        }

        for (BasePositionable exitPoint : map.getExitPoints()) {
            if (getDistance(landingParty, exitPoint) == 0) {
                isAtObject = true;
            }
        }

        final Element interactPanel = GUI.getInstance().getNifty().getScreen("surface_gui").findElementByName("interactPanel");
        if (interactPanel != null) {
            boolean interactPanelVisible = interactPanel.isVisible();
            if (!isAtObject && interactPanelVisible) {
                interactPanel.setVisible(false);
            } else if (isAtObject && !interactPanelVisible) {
                interactPanel.setVisible(true);
            }
        }


        if (world.isUpdatedThisFrame() && !map.getVictoryConditions().isEmpty()) {
            boolean allConditionsSatisfied = true;
            for (IVictoryCondition cond : map.getVictoryConditions()) {
                if (!cond.isSatisfied(world)) {
                    allConditionsSatisfied = false;
                    break;
                }
            }
            if (Configuration.getBooleanProperty("cheat.skipDungeons")) {
                allConditionsSatisfied = true;
            }
            if (allConditionsSatisfied) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.objectives_completed"));
                returnToPrevRoom(allConditionsSatisfied);
            }
        }
        // check that no crew member left, AND landing party window is not opened, because if it is - then landing party can have 0 members in process of configuration
        if (landingParty.getTotalMembers() <= 0 && !GUI.getInstance().getNifty().getCurrentScreen().getScreenId().equals("landing_party_equip_screen")) {
            onLandingPartyDestroyed(world);
        }
    }

    private double getDistance(BasePositionable a, Positionable b) {
        if (isWrap) {
            return a.getDistanceWrapped(b, map.getWidthInTiles(), map.getHeightInTiles());
        }
        return a.getDistance(b);
    }

    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        if (landingParty != null) {

            graphics.setColor(Color.red);
            for (PlanetObject a : map.getObjects()) {
                // draw only if tile under this animal is visible
                if (map.isTileVisible(a.getX(), a.getY())) {
                    a.draw(container, graphics, camera);

                    // in shoot mode, all available targets are surrounded with red square
                    if (mode == MODE_SHOOT && a.canBeShotAt() && getDistance(landingParty, a) < landingParty.getWeapon().getRange()) {
                        graphics.drawRect(camera.getXCoordWrapped(a.getX(), map.getWidthInTiles()), camera.getYCoordWrapped(a.getY(), map.getHeightInTiles()), camera.getTileWidth(), camera.getTileHeight());
                    }
                }

                if (a.getX() == landingParty.getX() && a.getY() == landingParty.getY()) {
                    a.printStatusInfo();
                }

            }

            landingParty.draw(container, graphics, camera);

            if (mode == MODE_SHOOT) {
                graphics.setColor(Color.yellow);
                EngineUtils.drawTileCircleCentered(graphics, camera, landingParty.getWeapon().getRange());

                if (target != null) {
                    // draw target mark
                    graphics.drawImage(ResourceManager.getInstance().getImage("target"), camera.getXCoordWrapped(target.getX(), map.getWidthInTiles()), camera.getYCoordWrapped(target.getY(), map.getHeightInTiles()));
                }
            }
        }
        if (currentEffect != null) {
            currentEffect.draw(container, graphics, camera);
        }
    }

    public void onLandingPartyDestroyed(World world) {
        GameLogger.getInstance().logMessage(Localization.getText("gui", "landing_party_lost"));

        if (world.getCurrentDungeon().isCommanderInParty()) {
            GUI.getInstance().getNifty().gotoScreen("fail_screen");
            FailScreenController controller = (FailScreenController) GUI.getInstance().getNifty().findScreenController(FailScreenController.class.getCanonicalName());
            controller.set("captain_lost_gameover", "commander_lost");
            return;
        }

        // do not call returnToPrevRoom()
        world.setCurrentRoom(prevRoom);
        prevRoom.returnTo(world);

        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopup("landing_party_lost");
        nifty.setIgnoreKeyboardEvents(false);
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        world.onCrewChanged();
        if (myDungeon.hasCustomMusic()) {
            ResourceManager.getInstance().getPlaylist("background").play();
        }
    }

    public void returnToPrevRoom(boolean conditionsSatisfied) {
        if (conditionsSatisfied) {
            fireEvent(world);
        }
        world.setCurrentRoom(prevRoom);
        prevRoom.returnTo(world);
        landingParty.onReturnToShip(world);

        if (conditionsSatisfied && successDialog != null) {
            world.addOverlayWindow(successDialog);
        }

        if (myDungeon.hasCustomMusic()) {
            ResourceManager.getInstance().getPlaylist("background").play();
        }
    }

    public void addEffect(Effect currentEffect) {
        if (effects == null) {
            effects = new ArrayList<>();
        }
        effects.add(currentEffect);
    }

    public void setSuccessDialog(Dialog successDialog) {
        this.successDialog = successDialog;
    }

    public int getTilesExploredThisTurn() {
        return tilesExploredThisTurn;
    }
}
