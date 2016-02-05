package ru.game.aurora.world.quest;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
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

        Dialog d = Dialog.loadFromFile("dialogs/sentient_stones/after_incident.csv");
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
                    world.getGlobalVariables().remove("sentient_stones.started");
                    isAlive = false;
                }
                world.getCurrentDungeon().getController().returnToPrevRoom(true);
            }
        });
        param.addOverlayWindow(d);
    }

    private class SentientStoneItem extends SellOnlyInventoryItem {

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
            world.getPlayer().getJournal().addQuestEntries("sentient_stones", "start");
            world.getGlobalVariables().put("sentient_stones.started", true);
            turnsBeforeIncident = Configuration.getIntProperty("sentient_stones.turnsBeforeIncident");
        }

        @Override
        public boolean isDumpable() {
            return true;
        }
    }

    private class SentientStone extends PickableInventoryItem {

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
     * Monster is 2x2, x,y shows left bottom
     */
    private static class StoneMonster extends DungeonMonster {

        private boolean hidden = false;

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

        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        @Override
        public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
            if (hidden) {
                return;
            }
            super.draw(container, graphics, camera, world);
        }
    }

    private static class StoneMonsterController extends MonsterController {

        private enum State {
            ROLLING,
            CHARGING,
            MOVING_TO_POSITION
        }

        private State state;

        private BasePositionable stopPosition;

        private Vector2f rollDirection;

        private int countdown;

        public StoneMonsterController(ITileMap map, IMonster myMonster) {
            super(map, myMonster);
            this.state = State.MOVING_TO_POSITION;
            this.stopPosition = null;
            this.countdown = 0;
        }

        @Override
        protected void makeTurn(World world) {
            LandingParty lp = world.getPlayer().getLandingParty();
            if (state == State.MOVING_TO_POSITION) {
                // check if there is a line of sight and we can charge
                if (myMonster.getX() == lp.getX() || myMonster.getX() + 1 == lp.getX()
                        || myMonster.getY() == lp.getY() || myMonster.getY() + 1 == lp.getY()) {

                    if (map.lineOfSightExists(myMonster.getX(), myMonster.getY(), lp.getX(), lp.getY()) &&
                            map.lineOfSightExists(myMonster.getX() + 1, myMonster.getY() + 1, lp.getX(), lp.getY())) {
                        state = State.CHARGING;
                        countdown = Configuration.getIntProperty("sentient_stones.charge_turns");
                        rollDirection = new Vector2f(Math.signum(lp.getX() - myMonster.getX())
                                , Math.signum(lp.getY() - myMonster.getY()));
                        GameLogger.getInstance().logMessage(Localization.getText("journal", "sentient_stone.charging"));
                    }
                } else {
                    // just move towards player
                    super.makeTurn(world);
                }
                return;
            }
            if (state == State.CHARGING) {
                if (--countdown == 0) {
                    GameLogger.getInstance().logMessage(Localization.getText("journal", "sentient_stone.attack"));
                    // calculate target position
                    BasePositionable[] targetPos = new BasePositionable[]{
                            new BasePositionable(myMonster.getX(), myMonster.getY())
                            , new BasePositionable(myMonster.getX() + 1, myMonster.getY())
                            , new BasePositionable(myMonster.getX(), myMonster.getY() + 1)
                            , new BasePositionable(myMonster.getX() + 1, myMonster.getY() + 1)
                    };
                    boolean lpHit = false;
                    boolean wallHit = false;
                    int movesDone = 0;

                    // check all 2x2 squares in the direction of movement untill we find a wall
                    // check if we hit a landing party
                    while (true) {
                        for (int i = 0; i < 4; ++i) {
                            targetPos[i].setPos(targetPos[i].getX() + (int) rollDirection.getX(), targetPos[i].getY() + (int) rollDirection.getY());
                            if (lp.getDistance(targetPos[i]) == 0) {
                                lpHit = true;
                            } else {
                                if (!map.isTilePassable(targetPos[i].getX(), targetPos[i].getY())) {
                                    wallHit = true;
                                    break;
                                }
                            }
                        }
                        if (!wallHit) {
                            ++movesDone;
                        } else {
                            break;
                        }
                    }

                    if (lpHit) {
                        GameLogger.getInstance().logMessage(Localization.getText("journal", "sentient_stone.party_damaged"));
                        lp.onAttack(world, myMonster, Configuration.getIntProperty("sentient_stones.damage"));
                    }
                    map.setTilePassable(myMonster.getX(), myMonster.getY(), true);
                    myMonster.moveTo((int) (myMonster.getX() + movesDone * rollDirection.getX())
                            , (int) (myMonster.getY() + movesDone * rollDirection.getY()));

                    map.setTilePassable(myMonster.getTargetX(), myMonster.getTargetY(), false);
                }
                return;
            }
            super.makeTurn(world);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    private void beginIncident(World world) {
        Dungeon dungeon = new Dungeon(world, new AuroraTiledMap("maps/aurora_lab.tmx"), world.getCurrentRoom());
        for (GameObject object : dungeon.getMap().getObjects()) {
            if (object instanceof DungeonPlaceholder) {
                StoneMonster m = new StoneMonster(dungeon.getMap());
                m.setPos(object.getX(), object.getY());
            } else if (object instanceof DungeonTrigger) {
                ((DungeonTrigger) object).setListener(this);
            } else if (object instanceof DungeonDoor && object.getName().equals("exitDoor")) {
                exitDoor = (DungeonDoor) object;
            }
        }
        world.setCurrentRoom(dungeon);
        dungeon.enter(world);
        world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/sentient_stones/sstones_incident.json"));
        world.getPlayer().getJournal().addQuestEntries("sentient_stones", "incident");
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
