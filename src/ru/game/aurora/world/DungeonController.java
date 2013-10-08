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
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
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
public class DungeonController implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private Dialog successDialog;

    private IStateChangeListener successListener;

    private transient Effect currentEffect = null;

    /**
     * When in fire mode, this is currently selected target
     */
    private transient PlanetObject target = null;

    public DungeonController(World world, Room prevRoom, ITileMap map, boolean wrap) {
        this.isWrap = wrap;
        this.prevRoom = prevRoom;
        this.map = map;
        this.world = world;
        this.landingParty = world.getPlayer().getLandingParty();
    }

    public DungeonController(World world, Room prevRoom, ITileMap map, boolean wrap, Dialog successDialog) {
        this(world, prevRoom, map, wrap);
        this.successDialog = successDialog;
    }


    /**
     * This update is used in MOVE mode. Moving landing party around.
     */
    private void updateMove(GameContainer container, World world) {
        int x = landingParty.getX();
        int y = landingParty.getY();

        if (container.getInput().isKeyPressed(Input.KEY_UP)) {
            y--;
            world.setUpdatedThisFrame(true);
        }
        if (container.getInput().isKeyPressed(Input.KEY_DOWN)) {
            y++;
            world.setUpdatedThisFrame(true);
        }

        if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            x--;
            world.setUpdatedThisFrame(true);
        }
        if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            x++;
            world.setUpdatedThisFrame(true);
        }
        if (isWrap) {
            x = EngineUtils.wrap(x, map.getWidth());
            y = EngineUtils.wrap(y, map.getHeight());
        } else {
            if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight()) {
                x = landingParty.getX();
                y = landingParty.getY();
            }
        }

        if (!map.isTilePassable(landingParty, x, y)) {
            world.setUpdatedThisFrame(false);
            x = world.getPlayer().getLandingParty().getX();
            y = world.getPlayer().getLandingParty().getY();
        }

        final boolean enterPressed = container.getInput().isKeyPressed(Input.KEY_ENTER);
        if (enterPressed) {
            interactWithObject(world);
        }

        int tilesExplored = map.updateVisibility(x, y, 1);
        landingParty.addCollectedGeodata(tilesExplored);


        world.getPlayer().getLandingParty().setPos(x, y);
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

            if (p.getX() != x || p.getY() != y) {
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
            if (exitPoint.getDistance(landingParty) == 0) {
                returnToPrevRoom();
            }
        }
    }

    private int getDist(int first, int second, int total) {
        int max = Math.max(first, second);
        int min = Math.min(first, second);

        return Math.min(max - min, total + min - max);

    }

    private int getRange(LandingParty party, Positionable target) {
        int xDist = getDist(party.getX(), target.getX(), map.getWidth());
        int yDist = getDist(party.getY(), target.getY(), map.getHeight());
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

        if (target != null && getRange(landingParty, target) > landingParty.getWeapon().getRange()) {
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
            // firing
            final int damage = landingParty.calcDamage();

            currentEffect = new BlasterShotEffect(landingParty, world.getCamera().getXCoordWrapped(target.getX(), map.getWidth()), world.getCamera().getYCoordWrapped(target.getY(), map.getHeight()), world.getCamera(), 800, landingParty.getWeapon().getShotImage());

            target.onShotAt(damage);
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.damage_message"), damage, target.getName()));
            if (!target.isAlive()) {
                GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.killed_message"), target.getName()));
                map.getObjects().remove(target);
                target = null;
            }
            world.setUpdatedThisFrame(true);
        }

    }

    public void changeMode() {
        mode = (mode == MODE_MOVE) ? MODE_SHOOT : MODE_MOVE;
        GUI.getInstance().getNifty().getCurrentScreen().findElementByName("shoot_panel").setVisible(mode == MODE_SHOOT);
    }

    public void update(GameContainer container, World world) {
        if (currentEffect != null) {
            currentEffect.update(container, world);
            if (currentEffect.isOver()) {
                currentEffect = null;
            }
            if (!world.isUpdatedThisFrame()) {
                return;
            }
        }

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
        for (PlanetObject a : map.getObjects()) {
            a.update(container, world);
            if (landingParty.getDistance(a) == 0) {
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
            if (allConditionsSatisfied) {
                GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.objectives_completed"));
                returnToPrevRoom();
                if (successDialog != null) {
                    world.addOverlayWindow(successDialog);
                }
                if (successListener != null) {
                    successListener.stateChanged(world);
                }
            }
        }
        if (landingParty.getTotalMembers() == 0) {
            onLandingPartyDestroyed(world);
        }
    }

    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        if (landingParty != null) {

            graphics.setColor(Color.red);
            for (PlanetObject a : map.getObjects()) {
                // draw only if tile under this animal is visible
                if (map.isTileVisible(a.getX(), a.getY())) {
                    a.draw(container, graphics, camera);

                    // in shoot mode, all available targets are surrounded with red square
                    if (mode == MODE_SHOOT && a.canBeShotAt() && landingParty.getDistance(a) < landingParty.getWeapon().getRange()) {
                        graphics.drawRect(camera.getXCoordWrapped(a.getX(), map.getWidth()), camera.getYCoordWrapped(a.getY(), map.getHeight()), camera.getTileWidth(), camera.getTileHeight());
                    }
                }

                if (a.getX() == landingParty.getX() && a.getY() == landingParty.getY()) {
                    a.printStatusInfo();
                }

            }

            landingParty.draw(container, graphics, camera);

            if (mode == MODE_SHOOT && target != null) {
                // draw target mark
                graphics.drawImage(ResourceManager.getInstance().getImage("target"), camera.getXCoordWrapped(target.getX(), map.getWidth()), camera.getYCoordWrapped(target.getY(), map.getHeight()));
            }
        }
        if (currentEffect != null) {
            currentEffect.draw(container, graphics, camera);
        }
    }

    public void onLandingPartyDestroyed(World world) {
        GameLogger.getInstance().logMessage(Localization.getText("gui", "landing_party_lost"));

        // do not call returnToPrevRoom()
        world.setCurrentRoom(prevRoom);
        prevRoom.enter(world);

        final Nifty nifty = GUI.getInstance().getNifty();
        Element popup = nifty.createPopup("landing_party_lost");
        nifty.setIgnoreKeyboardEvents(false);
        nifty.showPopup(nifty.getCurrentScreen(), popup.getId(), null);
        world.onCrewChanged();
    }

    public void returnToPrevRoom() {
        world.setCurrentRoom(prevRoom);
        prevRoom.enter(world);
        landingParty.onReturnToShip(world);
    }

    public void setCurrentEffect(Effect currentEffect) {
        this.currentEffect = currentEffect;
    }

    public void setSuccessDialog(Dialog successDialog) {
        this.successDialog = successDialog;
    }

    public void setSuccessListener(IStateChangeListener successListener) {
        this.successListener = successListener;
    }
}
