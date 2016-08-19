/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.09.13
 * Time: 22:02
 */

package ru.game.aurora.world;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.elements.Element;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.gui.FailScreenController;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.gui.niffy.InteractionTargetSelectorController;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.dungeon.IVictoryCondition;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.nature.RainCloud;
import ru.game.aurora.world.space.StarSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private final Room prevRoom;
    private final ITileMap map;
    private final boolean isWrap;
    private final World world;
    private final IDungeon myDungeon;
    /**
     * Current mode
     */
    private int mode = MODE_MOVE;
    private LandingParty landingParty;
    private Dialog successDialog;

    private int tilesExploredThisTurn = 0;

    private transient List<Effect> effects = new ArrayList<>();

    private transient Effect currentEffect = null;

    /**
     * When in fire mode, this is currently selected target
     */
    private transient GameObject target = null;
    private transient int targetTileX = 0;
    private transient int targetTileY = 0;

    private int xClick;
    private int yClick;

    public DungeonController(World world, Room prevRoom, IDungeon myDungeon) {
        this.myDungeon = myDungeon;
        this.prevRoom = prevRoom;
        this.world = world;
        this.isWrap = myDungeon.getMap().isWrapped();
        this.map = myDungeon.getMap();
    }


    /**
     * This update is used in MOVE mode. Moving landing party around.
     */
    private void updateMove(GameContainer container, World world) {
        if (landingParty.nowMoving()) {
            landingParty.update(container, world);
            if (!landingParty.nowMoving() && isWrap) {
                landingParty.setPos(EngineUtils.wrap(landingParty.getX(), map.getWidthInTiles()), EngineUtils.wrap(landingParty.getY(), map.getHeightInTiles()));
            }
            return;
        }

        Input input = container.getInput();

        int x = landingParty.getX();
        int y = landingParty.getY();
        boolean actuallyMoved = false;

        int dx = 0;
        int dy = 0;

        if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP_SECONDARY))) {
            y--;
            dy = -1;
            actuallyMoved = true;
        } else if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN_SECONDARY))) {
            y++;
            dy = 1;
            actuallyMoved = true;
        } else if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT_SECONDARY))) {
            x--;
            dx = -1;
            actuallyMoved = true;
        } else if (input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT_SECONDARY))) {
            x++;
            dx = 1;
            actuallyMoved = true;
        }

        if (!actuallyMoved) {
            return;
        }

        if (landingParty.overWeightTest()) {
            return;
        }
        world.getCamera().resetViewPort();
        world.setUpdatedThisFrame(true);

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

        final boolean enterPressed = input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))
                || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY));

        if (enterPressed) {
            interactWithObject(world);
        }

        if (!actuallyMoved) {
            return;
        }

        int viewRangeBonus = ((Number) world.getGlobalVariable("landingPartyViewRangeBonus", 0)).intValue();
        tilesExploredThisTurn = map.updateVisibility(x, y, 3 + viewRangeBonus);
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

        String soundId = map.getStepSound(landingParty.getTargetX(), landingParty.getTargetY());
        if (soundId != null) {
            ResourceManager.getInstance().getSound(soundId).play();
        }

        // world.getPlayer().getLandingParty().setPos(x, y);
    }

    private void partyMove(World world) {

    }

    public void interactWithObject(final World world) {
        int x = world.getPlayer().getLandingParty().getX();
        int y = world.getPlayer().getLandingParty().getY();
        // check if can pick up smth
        List<GameObject> gameObjectsAtPlayerPosition = new ArrayList<>();
        for (GameObject p : map.getObjects()) {
            if (!p.canBeInteracted(world)) {
                continue;
            }

            if ((int) BasePositionable.getDistance(x, y, p.getX(), p.getY()) != 0
                    && (!map.isWrapped() || (int) BasePositionable.getDistanceWrapped(x, y, p.getX(), p.getY(), map.getWidthInTiles(), map.getHeightInTiles()) != 0)) {
                continue;
            }
            gameObjectsAtPlayerPosition.add(p);

        }

        if (gameObjectsAtPlayerPosition.isEmpty()) {
            return;
        }

        if (gameObjectsAtPlayerPosition.size() == 1) {
            final GameObject p = gameObjectsAtPlayerPosition.get(0);

            if(!p.interact(world))
                return;

            world.setUpdatedThisFrame(true);
            // some items (like ore deposits) can be picked up more than once, do not remove them in this case
            if (!p.isAlive()) {
                map.getObjects().remove(p);
            }
            return;
        }

        InteractionTargetSelectorController.open(new IStateChangeListener<GameObject>() {
            private static final long serialVersionUID = -8114467555795780919L;

            @Override
            public void stateChanged(GameObject param) {
                if(!param.interact(world))
                    return;

                world.setUpdatedThisFrame(true);
                // some items (like ore deposits) can be picked up more than once, do not remove them in this case
                if (!param.isAlive()) {
                    map.getObjects().remove(param);
                }
            }
        }, gameObjectsAtPlayerPosition);

    }

    private int getDist(int first, int second, int total) {
        int max = Math.max(first, second);
        int min = Math.min(first, second);

        return Math.min(max - min, total + min - max);
    }

    /**
     * This update method is used in FIRE mode. Selecting targets and shooting.
     */
    private void updateShoot(World world, Input input){
        // aiming
        if(landingParty.getWeapon().canTargetEmptySpace()){
            // target: empty space
            aimEmptySpace(world, input);
        }
        else{
            // target: enemy npc
            boolean nextTarget = input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP))
                    || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP_SECONDARY))
                    || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT))
                    || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT_SECONDARY));

            boolean prevTarget = input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN))
                    || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN_SECONDARY))
                    || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT))
                    || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT_SECONDARY));

            if(nextTarget) {
                aimNextTarget(world);
                return;
            }
            else if(prevTarget) {
                aimPrevTarget(world);
                return;
            }
        }

        // shooting
        boolean shoot = input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.SHOOT))
                || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT))
                || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INTERACT_SECONDARY));

        if(shoot){
            updateShootFire(world);
        }
    }

    // aim in to enemy npc
    public void aimNextTarget(World world){
        aimTarget(world, true, false);
    }

    public void aimPrevTarget(World world){
        aimTarget(world, false, true);
    }

    private void aimTarget(World world, boolean nextTarget, boolean prevTarget){
        if (map.getObjects().isEmpty()) {
            return;
        }

        int targetIdx = 0;
        int weaponRange = landingParty.getWeaponRange(world);
        List<GameObject> availableTargets = new ArrayList<>();

        if (target != null && (!target.isAlive() || !target.canBeAttacked() || getRange(landingParty, target) > weaponRange)) {
            // target moved out of range
            target = null;
        }

        for (GameObject planetObject : map.getObjects()) {
            if (!planetObject.canBeAttacked()) {
                continue;
            }
            if (!map.isTileVisible(planetObject.getX(), planetObject.getY())) {
                // do not target animals on unexplored tiles
                continue;
            }
            if (weaponRange >= getRange(landingParty, planetObject)) {
                availableTargets.add(planetObject);
                if (target == null) {
                    target = planetObject;
                }

                if (target == planetObject) {
                    targetIdx = availableTargets.size() - 1;
                }
            }
        }

        // check available targets in weapon range
        if (availableTargets.isEmpty()) {
            return;
        }
        else {
            // select target
            if (nextTarget) {
                targetIdx++;
                if (targetIdx >= availableTargets.size()) {
                    targetIdx = 0;
                }
            }
            else if (prevTarget) {
                targetIdx--;
                if (targetIdx < 0) {
                    targetIdx = availableTargets.size() - 1;
                }
            }

            target = availableTargets.get(targetIdx);
        }
    }

    // aim ground
    private void aimEmptySpace(World world, Input input) {
        if(target != null){
            aimNextTarget(world);
        }
        else{
            if(landingParty.getWeaponRange(world) < getRange(landingParty, targetTileX, targetTileY)){
                // self aim
                targetTileX = landingParty.getX();
                targetTileY = landingParty.getY();
            }
        }

        int dx = 0;
        int dy = 0;

        if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.RIGHT_SECONDARY))){
            ++dx;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.LEFT_SECONDARY))){
            --dx;
        }

        if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.UP_SECONDARY))){
            --dy;
        }
        else if(input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN)) || input.isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.DOWN_SECONDARY))){
            ++dy;
        }

        // out of range check
        if(landingParty.getWeaponRange(world) >= getRange(landingParty, targetTileX + dx, targetTileY + dy)) {
            targetTileX += dx;
            targetTileY += dy;
        }
        else{
            return;
        }
    }

    public void updateShootFire(World world){
        if(landingParty.getWeapon().canTargetEmptySpace()){
            shootFireEmptySpace(world);
        }
        else{
            shootFireTarget(world);
        }
    }

    private void shootFireTarget(World world){
        // check target existing and select next if current target not exist
        if (target == null || !target.isAlive() || !target.canBeAttacked() || getRange(landingParty, target) > landingParty.getWeaponRange(world)) {
            aimNextTarget(world);
            return;
        }

        // check line of sight
        if (!map.lineOfSightExists(landingParty.getX(), landingParty.getY(), target.getX(), target.getY())) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.no_line_of_sight"));
            return;
        }

        // firing
        final int damage = landingParty.calcDamage(world);

        Effect blasterShotEffect = landingParty.getWeapon().createShotEffect(
                world
                , landingParty
                , target
                , world.getCamera()
                , 800
        );
        if (landingParty.getWeapon().getShotSound() != null) {
            blasterShotEffect.setStartSound(landingParty.getWeapon().getShotSound());
        }
        effects.add(blasterShotEffect);

        ResourceManager.getInstance().getSound(landingParty.getWeapon().getShotSound()).play();

        GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("fire", Button.class).disable();
        blasterShotEffect.setEndListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = -7742240385490245306L;

            @Override
            public void stateChanged(World world) {
                target.onAttack(world, landingParty, damage);

                if (!target.isAlive()) {
                    GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.killed_message"), target.getName()));
                    map.getObjects().remove(target);
                    target = null;
                }

                GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("fire", Button.class).enable();
            }
        });

        world.setUpdatedThisFrame(true);
    }

    private void shootFireEmptySpace(World world){
        // check self aiming
        if(targetTileX == landingParty.getX() && targetTileY == landingParty.getY()){
            return;
        }

        // check line of sight
        if (!map.lineOfSightExists(landingParty.getX(), landingParty.getY(), targetTileX, targetTileY)) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.no_line_of_sight"));
            return;
        }

        // firing
        final int damage = landingParty.calcDamage(world);

        Effect blasterShotEffect = landingParty.getWeapon().createShotEffect(
                world
                , landingParty
                , landingParty
                , targetTileX
                , targetTileY
                , world.getCamera()
                , 800
                , map
        );
        if (landingParty.getWeapon().getShotSound() != null) {
            blasterShotEffect.setStartSound(landingParty.getWeapon().getShotSound());
        }
        effects.add(blasterShotEffect);

        ResourceManager.getInstance().getSound(landingParty.getWeapon().getShotSound()).play();

        GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("fire", Button.class).disable();
        blasterShotEffect.setEndListener(new IStateChangeListener<World>() {
            private static final long serialVersionUID = -7742240385490245306L;

            @Override
            public void stateChanged(World world) {
                GUI.getInstance().getNifty().getCurrentScreen().findNiftyControl("fire", Button.class).enable();
            }
        });

        world.setUpdatedThisFrame(true);
    }

    private double getRange(LandingParty landingParty, GameObject target) {
        return map.isWrapped() ? landingParty.getDistanceWrapped(target, map.getWidthInTiles(), map.getHeightInTiles())
                : landingParty.getDistance(target);
    }

    private double getRange(LandingParty landingParty, int targetX, int targetY){
        return map.isWrapped() ? landingParty.getDistanceWrapped(targetX, targetY, map.getWidthInTiles(), map.getHeightInTiles())
                : landingParty.getDistance(targetX, targetY);
    }

    public void changeMode() {
        if (landingParty.getWeapon() == null) {
            mode = MODE_MOVE;
        } else {
            mode = (mode == MODE_MOVE) ? MODE_SHOOT : MODE_MOVE;
        }
        GUI.getInstance().getNifty().getCurrentScreen().findElementByName("shoot_panel").setVisible(mode == MODE_SHOOT);

        if(mode == MODE_SHOOT && landingParty.getWeapon() != null){
            aimNextTarget(world);

            if(landingParty.getWeapon().canTargetEmptySpace()) {
                if (target == null) {
                    targetTileX = landingParty.getX();
                    targetTileY = landingParty.getY();
                }
                else{
                    targetTileX = target.getX();
                    targetTileY = target.getY();
                }
            }
        }
    }

    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.INVENTORY))) {
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("inventory_screen");
            return;
        }
        if (container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.MAP))) {
            GUI.getInstance().pushCurrentScreen();
            GUI.getInstance().getNifty().gotoScreen("surface_map_screen");
            return;
        }

        Camera myCamera = world.getCamera();
        this.landingParty = world.getPlayer().getLandingParty();
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
            if (currentEffect != null && currentEffect.getStartSound() != null) {
                ResourceManager.getInstance().getSound(currentEffect.getStartSound()).play();
            }
        }

        if (currentEffect != null) {
            currentEffect.update(container, world);
            if (currentEffect.isOver()) {
                currentEffect.onOver(world);
                currentEffect = null;
            }
            if ((currentEffect != null && currentEffect.isBlocking()) && !world.isUpdatedThisFrame()) {
                return;
            }
        }
        tilesExploredThisTurn = 0;
        switch (mode) {
            case MODE_MOVE:
                if (container.getInput().isKeyPressed(InputBinding.keyBinding.get(InputBinding.Action.SHOOT))) {
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
                updateShoot(world, container.getInput());
                break;
            default:
                throw new IllegalStateException("Unknown planet update type " + mode);

        }
        // should always be done after player update, so that world.isUpdatedThisFrame() flag is set
        boolean isAtObject = false;
        List<GameObject> toRemove = new ArrayList<>();
        for (GameObject a : new ArrayList<>(map.getObjects())) {
            a.update(container, world);
            if (!a.isAlive()) {
                toRemove.add(a);
                continue;
            }
            if (getDistance(landingParty, a) == 0 && a.canBeInteracted(world)) {
                isAtObject = true;
            }
        }

        map.getObjects().removeAll(toRemove);

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
                returnToPrevRoom(true);
            }
        }
        // check that no crew member left, AND landing party window is not opened, because if it is - then landing party can have 0 members in process of configuration
        if (landingParty.getTotalMembers() <= 0 && !GUI.getInstance().getNifty().getCurrentScreen().getScreenId().equals("landing_party_equip_screen")) {
            onLandingPartyDestroyed(world);
        }

        if (world.isUpdatedThisFrame()) {
            // sort objects by their y coordinate so that they overlap correctly
            Collections.sort(map.getObjects(), new Comparator<GameObject>() {
                @Override
                public int compare(GameObject o1, GameObject o2) {
                    int coordsCompare = Integer.compare(o1.getY(), o2.getY());
                    if (coordsCompare == 0) {
                        return Integer.compare(o1.getDrawOrder(), o2.getDrawOrder());
                    } else {
                        return coordsCompare;
                    }
                }
            });
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
            for (GameObject a : map.getObjects()) {
                // draw only if tile under this object is visible
                // rain clouds have a separate drawing logic
                if (map.isTileVisible(a.getX(), a.getY()) || RainCloud.class.isAssignableFrom(a.getClass())) {
                    a.draw(container, graphics, camera, world);

                    // in shoot mode, all available targets are surrounded with red square
                    if (mode == MODE_SHOOT && a.canBeAttacked() && getDistance(landingParty, a) < landingParty.getWeapon().getRange()) {
                        graphics.drawRect(camera.getXCoord(a.getX(), map), camera.getYCoord(a.getY(), map), camera.getTileWidth(), camera.getTileHeight());
                    }
                }

            }

            landingParty.draw(container, graphics, camera, world);

            if (mode == MODE_SHOOT) {
                graphics.setColor(Color.yellow);
                EngineUtils.drawTileCircleCentered(graphics, camera, landingParty.getWeapon().getRange() + ((Number) world.getGlobalVariable("landingPartyShootRangeBonus", 0)).intValue());

                // draw target mark
                if(landingParty.getWeapon().canTargetEmptySpace()){
                        graphics.drawImage(
                                ResourceManager.getInstance().getImage("target")
                                , camera.getXCoord(targetTileX, map)
                                , camera.getYCoord(targetTileY, map));
                }
                else{
                    if (target != null) {
                        graphics.drawImage(
                                ResourceManager.getInstance().getImage("target")
                                , camera.getXCoord(target.getX(), map)
                                , camera.getYCoord(target.getY(), map));
                    }
                }
            }
        }
        if (currentEffect != null) {
            currentEffect.draw(container, graphics, camera, world);
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

        world.onLandingPartyDestroyed();
    }

    public void returnToPrevRoom(boolean conditionsSatisfied) {
        if (conditionsSatisfied) {
            fireEvent(world);
        }
        world.setCurrentRoom(prevRoom);
        prevRoom.returnTo(world);
        if (prevRoom instanceof StarSystem) {
            landingParty.onReturnToShip(world);
        } else {
            landingParty.setPos(myDungeon.getX(), myDungeon.getY());
        }

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

    public Room getPrevRoom() {
        return prevRoom;
    }
}
