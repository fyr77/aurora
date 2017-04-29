package ru.game.aurora.world.generation.quest.ambush;

import ru.game.aurora.application.*;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.factions.NeutralFaction;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.npc.shipai.FollowAI;
import ru.game.aurora.npc.shipai.LandAI;
import ru.game.aurora.npc.shipai.LeaveSystemAI;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanGenerator;
import ru.game.aurora.world.generation.aliens.zorsan.ZorsanWarData;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.space.*;

import java.util.Map;

/**
 * Created by User on 15.06.2016.
 * Created after Zorsan esape.
 * Player meets a Bork that asks for help as his family was kidnapped.
 * After player agrees to follow him he leads player into a zorsan trap
 */
public class AmbushQuest extends GameEventListener implements DialogListener {
    private static final long serialVersionUID = 1L;

    private double countdown;

    /**
     * 0 - waiting for quest to start
     * 1 - in the next star system player will meet a bork ship
     * 2 - bork ship spawned
     */
    private int state = 0;

    private NPCShip borkShip;

    private StarSystem ambushSystem;

    public AmbushQuest() {
        setGroups(EventGroup.ENCOUNTER_SPAWN);
        countdown = Configuration.getIntProperty("quest.ambush.countdown");
    }

    @Override
    public boolean onTurnEnded(World world) {
        countdown -= world.getCurrentRoom().getTurnToDayRelation();
        if (state == 0 && countdown <= 0) {
            state = 1;
        }

        if (state == 2 && borkShip.isAlive() && borkShip.getDistance(world.getPlayer().getShip()) <= 3) {
            GameLogger.getInstance().logMessage(Localization.getText("journal", "ambush.communicate"));
        }

        if (state == 3 && ambushSystem == world.getCurrentStarSystem()) {
            final BasePlanet planet = ambushSystem.getPlanets()[0];
            if (planet.getDistance(borkShip) <= 3) {
                isAlive = false;
                borkShip.setHostile(true);
                borkShip.setAi(new CombatAI(world.getPlayer().getShip()));

                NPCShip ship1 = ((AlienRace) world.getFactions().get(ZorsanGenerator.NAME)).getDefaultFactory().createShip(world, ZorsanGenerator.SCOUT_SHIP);
                ship1.setPos(planet.getX() + 1, planet.getY());
                ambushSystem.getObjects().add(ship1);
                ship1.setHostile(true);
                NPCShip ship2 = ((AlienRace) world.getFactions().get(ZorsanGenerator.NAME)).getDefaultFactory().createShip(world, ZorsanGenerator.CRUISER_SHIP);
                ship2.setPos(planet.getX(), planet.getY() + 1);
                ambushSystem.getObjects().add(ship2);
                ship2.setHostile(true);

                if (!world.getGlobalVariables().containsKey("zorsan.war_preparations")) {
                    // player has not yet collected zorsan war data drop. Force it to be dropped by this ship
                    ProbabilitySet<GameObject> newLoot = new ProbabilitySet<>();
                    newLoot.put(new ZorsanWarData(), 10.0);
                    ship2.setLoot(newLoot);
                }

                // all these ships will drop something to reward player
                ship2.setForceLootDrop(true);
                ship1.setForceLootDrop(true);
                borkShip.setForceLootDrop(true);
                world.getPlayer().getJournal().questCompleted("ambush", "end");

                world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/ambush_zorsan.json"));
                state = 4;
            }
        }
        return false;
    }

    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        if (ss.isQuestLocation() || ss.isVisited()) {
            return false;
        }
        if (state == 1 && CommonRandom.getRandom().nextDouble() < Configuration.getDoubleProperty("quest.ambush.chance")) {
            final AlienRace zorsan = (AlienRace) world.getFactions().get(ZorsanGenerator.NAME);
            StarSystem zorsanHomeworld = zorsan.getHomeworld();
            ambushSystem = world.getGalaxyMap().getRandomNonQuestStarsystemInRange(
                    zorsanHomeworld.getX()
                    , zorsanHomeworld.getY()
                    , 20
                    , new StarSystemListFilter() {
                        @Override
                        public boolean filter(StarSystem ss) {
                            return ss.getPlanets() != null && ss.getPlanets().length > 0;
                        }
                    }
            );
            world.getGlobalVariables().put("ambush.coordinates", ambushSystem.getCoordsString());
            Dialog borkDialog = Dialog.loadFromFile("dialogs/encounters/ambush_bork.json");
            borkDialog.addListener(this);

            borkShip = new NPCShip("bork_ship_large",  world.getPlayer().getShip().getX() + 4, world.getPlayer().getShip().getY() + 3);
            borkShip.setFaction( world.getFactions().get(NeutralFaction.NAME));
            borkShip.setCaptain(new NPC(borkDialog));

            borkShip.setSpeed(1);
            borkShip.setWeapons(
                    ResourceManager.getInstance().getWeapons().getEntity("bork_cannon")
                    , ResourceManager.getInstance().getWeapons().getEntity("bork_missiles")
                    , ResourceManager.getInstance().getWeapons().getEntity("zorsan_cannon")
            );
            borkShip.enableRepairs(4);
            borkShip.setAi(new FollowAI(world.getPlayer().getShip()));
            borkShip.setForceLootDrop(true);

            // bork ship always drops zorsan weapon loot
            ProbabilitySet<GameObject> borkLoot = new ProbabilitySet<>();
            final SpaceDebris.ItemDebris zorsanWeaponLoot = new SpaceDebris.ItemDebris(new ShipLootItem(ShipLootItem.Type.WEAPONS, zorsan)) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean interact(World world) {
                    // show this only if player killed bork ship instantly without following him
                    if (world.getCurrentRoom() != ambushSystem) {
                        world.getPlayer().getJournal().questCompleted("ambush", "bork_ship_looted");
                    }
                    return super.interact(world);
                }
            };
            borkLoot.put(zorsanWeaponLoot, 1.0);
            borkShip.setLoot(borkLoot);
            ss.getObjects().add(borkShip);

            state = 2;
            return true;
        }

        if (state == 2 && ss == ambushSystem) {
            final Ship ship = world.getPlayer().getShip();
            borkShip.setPos(ship.getX(), ship.getY());
            ss.getObjects().add(borkShip);
            world.addOverlayWindow(Dialog.loadFromFile("dialogs/encounters/ambush_bork2.json"));
            state = 3;
            borkShip.setAi(new LandAI(ss.getPlanets()[0]));
            return true;
        }

        return false;
    }

    @Override
    public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
        world.getPlayer().getJournal().addQuestEntries("ambush", "start");
        if (returnCode == 0) {
            // player refused to help, bork attacks
            borkShip.setHostile(true);
            borkShip.setAi(new CombatAI(world.getPlayer().getShip()));
            world.getPlayer().getJournal().addQuestEntries("ambush", "refuse");
        } else {
            world.getPlayer().getJournal().addQuestEntries("ambush", "agree");
            borkShip.setAi(new LeaveSystemAI());
            borkShip.setCanBeHailed(false);
        }
    }

    @Override
    public String getLocalizedMessageForStarSystem(World world, GalaxyMapObject galaxyMapObject) {
        if (ambushSystem != null && ambushSystem == galaxyMapObject) {
            return Localization.getText("journal", "ambush.title");
        }
        return null;
    }
}
