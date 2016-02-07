package ru.game.aurora.world.quest;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.player.SellOnlyInventoryItem;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.engineering.upgrades.MedBayUpgrade;
import ru.game.aurora.world.*;
import ru.game.aurora.world.dungeon.DungeonDoor;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.dungeon.DungeonPlaceholder;
import ru.game.aurora.world.dungeon.DungeonTrigger;
import ru.game.aurora.world.generation.WorldGeneratorPart;
import ru.game.aurora.world.planet.*;
import ru.game.aurora.world.planet.nature.Animal;
import ru.game.aurora.world.planet.nature.Plant;

import java.util.*;

/**
 * Created by Егор on 26.12.2015.
 * Quest about sentinent stones.
 * Player can take one on their home planet.
 */
public class SentientStonesQuestGenerator extends GameEventListener implements WorldGeneratorPart, IStateChangeListener<World> {

    private static long serialVersionUID = 1L;

    private Planet planet = null;

    private int stepsCount = 0;

    private int turnsBeforeIncident = -1;

    private boolean initialDialogShown = false;

    private DungeonDoor exitDoor;

    // called when player reaches exit point
    @Override
    public void stateChanged(World param) {
        // open the door, add marines, show dialog
        exitDoor.setState(true);
        param.getPlayer().getLandingParty().setImage("awayteam");
        Dialog d = Dialog.loadFromFile("dialogs/encounters/sentient_stones/sstones_after_incident.json");
        Map<String, String> flags = new HashMap<>();
        final int scientists = Configuration.getIntProperty("sentient_stones.scientists");
        if (param.getPlayer().getLandingParty().getScience() == scientists) {
            flags.put("good", "");
        } else {
            // some scientists were lost
            boolean hasMedbay = false;
            for (ShipUpgrade su : param.getPlayer().getShip().getUpgrades()) {
                if (su instanceof MedBayUpgrade) {
                    hasMedbay = true;
                    break;
                }
            }
            if (hasMedbay) {
                // player has a medbay, all possibly dead crewmembers are restored
                flags.put("good_medbay", "");
                param.getPlayer().getLandingParty().setScience(scientists);
            } else {
                flags.put("casualties", "");
            }
        }
        d.addListener(new DialogListener() {
            @Override
            public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                if (returnCode == 1) {
                    //throw to space
                    world.getPlayer().getJournal().questCompleted("sentient_stones", "throw_to_space");
                    world.getGlobalVariables().remove("sentient_stone.started");
                    isAlive = false;
                }
                world.getCurrentDungeon().getController().returnToPrevRoom(true);
            }
        });
        param.addOverlayWindow(d, flags);
    }

    private class SentientStoneItem extends SellOnlyInventoryItem {

        private static final long serialVersionUID = 1L;

        public SentientStoneItem() {
            super("journal"
                    , "sentient_stone"
                    , (CommonRandom.getRandom().nextBoolean() ? "stone_1" : "stone_2")
                    , 0
                    , true
            );
        }

        @Override
        public void onReceived(World world, int amount) {
            // start quest here
            world.getPlayer().getJournal().addQuestEntries("sentient_stone", "start");
            world.getGlobalVariables().put("sentient_stones.started", true);
            turnsBeforeIncident = Configuration.getIntProperty("sentient_stones.turnsBeforeIncident");
        }

        @Override
        public boolean isDumpable() {
            return true;
        }
    }

    private class SentientStone extends PickableInventoryItem {

        private static final long serialVersionUID = 1L;

        public SentientStone(int x, int y) {
            super(x, y, new SentientStoneItem());
        }

        @Override
        public boolean canBeInteracted(World world) {
            return !world.getGlobalVariables().containsKey("sentient_stones.started");
        }

        @Override
        public ScanGroup getScanGroup() {
            return ScanGroup.BIO;
        }
    }

    @Override
    public boolean onPlayerLandedPlanet(World world, Planet planet) {
        if (this.planet != null) {
            return false;
        }

        if (planet.getOwner().isQuestLocation()) {
            return false;
        }
        if (planet.getExploredTiles() > 121) {
            // already landed here before, 121 is the spot around the landing location that is explored when player landed now
            return false;
        }

        if (CommonRandom.getRandom().nextDouble() > Configuration.getDoubleProperty("sentient_stones.chance")) {
            return false;
        }

        // quest started
        this.planet = planet;

        // replace all plants and animals on the planet with sentinent stones
        List<GameObject> stones = new ArrayList<>(planet.getPlanetObjects().size());
        for (Iterator<GameObject> objectIterator = planet.getPlanetObjects().iterator(); objectIterator.hasNext(); ) {
            GameObject go = objectIterator.next();
            if (go instanceof Animal || go instanceof Plant) {
                objectIterator.remove();
            }
            final SentientStone sentient_stone = new SentientStone(go.getX(), go.getY());
            stones.add(sentient_stone);
        }

        planet.getPlanetObjects().addAll(stones);

        return true;
    }
//////////// Code for the stone moster //////////////

    /**
     * Stone monster works as follows:
     * 1) If there is a direct line between monster and player monster remembers player position and starts charging
     * 2) After 1-2 turns of charging monster in a single turn rolls until it hits a wall. If landing party is still on its
     * way then it gets hit and thrown to a random nearby spot
     * 3) If there is no direct line, monster moves as usual
     * <p/>
     * Monster is drawn as 2x2 tile size image, but is shifted half of tile, so that it is centered around its x,y
     * When attacking monster hists a 3 tile wide corridor
     */
    private static class StoneMonster extends DungeonMonster {

        private static final long serialVersionUID = 1L;

        private transient Image img = null;

        public StoneMonster(ITileMap map) {
            super(
                    "Sentient Stone"
                    , 0
                    , 0
                    , null
                    , null
                    , map
                    , new MonsterDesc("sentient_stone"
                            , null
                            , 30
                            , 1
                            , "melee"
                            , "stone_1"
                            , true
                            , MonsterBehaviour.AGGRESSIVE
                    )
                    , null
            );

            controller = new StoneMonsterController(map, this);
            setMovementSpeed(2);
        }

        @Override
        public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
            if (img == null) {
                img = ResourceManager.getInstance().getImage("stone_1").getScaledCopy(2);
            }
            img.draw(camera.getXCoord(getX()) + getOffsetX() - AuroraGame.tileSize / 2, camera.getYCoord(getY()) + getOffsetY() - AuroraGame.tileSize / 2);
        }
    }

    private static class StoneMonsterController extends MonsterController {

        private static final long serialVersionUID = 1;

        private enum State {
            ROLLING,
            CHARGING,
            MOVING_TO_POSITION
        }

        private State state;

        private Vector2f rollDirection;

        private int countdown;

        public StoneMonsterController(ITileMap map, IMonster myMonster) {
            super(map, myMonster);
            this.state = State.MOVING_TO_POSITION;
            this.countdown = 0;
        }

        @Override
        protected void makeTurn(World world) {
            LandingParty lp = world.getPlayer().getLandingParty();
            if (state == State.MOVING_TO_POSITION) {
                // check if there is a line of sight and we can charge
                boolean canMoveY = Math.abs(myMonster.getX() - lp.getX()) <= 1;
                boolean canMoveX = Math.abs(myMonster.getY() - lp.getY()) <= 1;
                if (canMoveX || canMoveY) {

                    if (map.lineOfSightExists(myMonster.getX(), myMonster.getY(), lp.getX(), lp.getY())) {
                        state = State.CHARGING;
                        countdown = Configuration.getIntProperty("sentient_stones.charge_turns");

                        float dx = canMoveX ? Math.signum(lp.getX() - myMonster.getX()) : 0;
                        float dy = canMoveY ? Math.signum(lp.getY() - myMonster.getY()) : 0;
                        if (canMoveX ^ canMoveY) {
                            rollDirection = new Vector2f(dx, dy);
                        } else {
                            // can charge both x and y, select one of it
                            if (CommonRandom.getRandom().nextBoolean()) {
                                rollDirection = new Vector2f(dx, 0);
                            } else {
                                rollDirection = new Vector2f(0, dy);
                            }
                        }

                        // check that there are at least a couple of cells to roll and we are not stuck to a wall
                        if (!map.isTilePassable((int) (myMonster.getX() + dx), (int) (myMonster.getY() + dy))) {
                            state = State.MOVING_TO_POSITION;
                            super.makeTurn(world);
                            return;
                        }
                        GameLogger.getInstance().logMessage(Localization.getText("journal", "sentient_stone.charging"));
                        return;
                    } else {
                        super.makeTurn(world);
                    }
                } else {
                    // just move towards player
                    super.makeTurn(world);
                }
                return;
            }
            if (state == State.CHARGING) {
                if (--countdown <= 0) {
                    GameLogger.getInstance().logMessage(Localization.getText("journal", "sentient_stone.attack"));
                    // calculate target position

                    boolean lpHit = false;
                    int movesDone = 1;

                    // check all 3x3 squares in the direction of movement untill we find a wall
                    // check if we hit a landing party
                    while (true) {
                        int newMonsterX = (int) (myMonster.getX() + movesDone * rollDirection.getX());
                        int newMonsterY = (int) (myMonster.getY() + (movesDone * rollDirection.getY()));
                        if (lp.getDistance(newMonsterX, newMonsterY) <= 1) {
                            lpHit = true;
                        } else {

                            if (!map.isTilePassable(newMonsterX
                                    , newMonsterY)) {
                                --movesDone;
                                break;
                            }
                        }
                        ++movesDone;
                    }

                    int destinationX = (int) (myMonster.getX() + movesDone * rollDirection.getX());
                    int destinationY = (int) (myMonster.getY() + movesDone * rollDirection.getY());
                    if (lpHit) {
                        GameLogger.getInstance().logMessage(Localization.getText("journal", "sentient_stone.party_damaged"));
                        lp.onAttack(world, myMonster, Configuration.getIntProperty("sentient_stones.damage"));
                        // move to random empty position

                        double v = 0.7 + 0.3 * CommonRandom.getRandom().nextDouble();
                        BasePositionable bp = new BasePositionable((int) (myMonster.getX() + movesDone * v * rollDirection.getX())
                                , (int) (myMonster.getY() + movesDone * v * rollDirection.getY()));

                        AuroraTiledMap.setNearestFreePoint(map, bp, bp.getX(), bp.getY());

                        lp.moveTo(bp.getX()
                                , bp.getY());
                    }
                    map.setTilePassable(myMonster.getX(), myMonster.getY(), true);
                    myMonster.moveTo(destinationX
                            , destinationY);

                    map.setTilePassable(myMonster.getTargetX(), myMonster.getTargetY(), false);
                    state = State.MOVING_TO_POSITION;
                }
                return;
            }
            super.makeTurn(world);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    private void beginIncident(World world) {
        Dungeon dungeon = new Dungeon(world, new AuroraTiledMap("maps/aurora_lab.tmx"), world.getCurrentRoom());
        StoneMonster m = new StoneMonster(dungeon.getMap());
        dungeon.getMap().getObjects().add(m);
        world.setCurrentRoom(dungeon);
        dungeon.enter(world);
        for (GameObject object : dungeon.getMap().getObjects()) {
            if (object instanceof DungeonPlaceholder) {
                m.setPos(object.getX(), object.getY());
            } else if (object instanceof DungeonTrigger) {
                ((DungeonTrigger) object).setListener(this);
            } else if (object instanceof DungeonDoor && object.getName().equals("exitDoor")) {
                exitDoor = (DungeonDoor) object;
            }
        }
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/sentient_stones/sstones_incident.json"));
        world.getPlayer().getJournal().addQuestEntries("sentient_stone", "incident");
        world.getPlayer().getLandingParty().setImage("sci_team");
    }

    @Override
    public boolean onTurnEnded(World world) {
        if (planet != null) {
            if (!initialDialogShown && world.getCurrentRoom().equals(planet) && ++stepsCount > 2) {
                Dialog dialog = Dialog.loadFromFile("dialogs/encounters/sentient_stones/sstones_landing_party_report.json");
                world.addOverlayWindow(dialog);
                initialDialogShown = true;
                return true;
            }

            if (turnsBeforeIncident > 0) {
                --turnsBeforeIncident;
                if (turnsBeforeIncident == 0) {
                    turnsBeforeIncident = -1;
                    beginIncident(world);
                }
            }
        }
        return false;
    }

    @Override
    public void updateWorld(World world) {
        world.addListener(this);
    }
}
