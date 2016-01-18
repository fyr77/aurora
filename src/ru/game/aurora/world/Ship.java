/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:11
 */
package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.*;
import ru.game.aurora.common.Drawable;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.CrewMember;
import ru.game.aurora.npc.crew.GordonMainDialogListener;
import ru.game.aurora.npc.crew.HenryMainDialogListener;
import ru.game.aurora.npc.crew.SarahMainDialogListener;
import ru.game.aurora.player.Player;
import ru.game.aurora.player.engineering.ShipUpgrade;
import ru.game.aurora.player.engineering.upgrades.*;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.space.StarSystem;

import java.util.*;

public class Ship extends BaseGameObject {

    public static final int BASE_SCIENTISTS = 5;
    public static final int BASE_ENGINEERS = 5;
    public static final int BASE_MILITARY = 5;

    private static final long serialVersionUID = 3;
    private final List<WeaponInstance> weapons = new ArrayList<>();
    private final List<ShipUpgrade> upgrades = new ArrayList<>();
    private int hull;
    private int maxHull;
    private int scientists;
    private int engineers;
    private int military;
    private int maxMilitary;
    // is added to all weapons ranges
    private int rangeBuff;
    private int maxScientists;
    private int maxEngineers;
    private Map<String, CrewMember> crewMembers = new HashMap<>();

    private int freeSpace;

    public Ship(World world, int x, int y) {
        super(x, y, new Drawable("aurora"));
        setFaction(world.getFactions().get(HumanityGenerator.NAME));
        name = "Aurora-2";
        hull = maxHull = 10;
        maxScientists = scientists = BASE_SCIENTISTS;
        maxMilitary = military = BASE_MILITARY;
        maxEngineers = engineers = BASE_ENGINEERS;

        freeSpace = Configuration.getIntProperty("upgrades.ship_free_space");

    }

    public void addCrewMember(World world, CrewMember member) {
        crewMembers.put(member.getId(), member);
        member.onAdded(world);
    }

    public void removeCrewMember(World world, String id) {
        CrewMember cm = crewMembers.remove(id);
        if (cm != null) {
            cm.onRemoved(world);
        }
    }

    public Map<String, CrewMember> getCrewMembers() {
        return crewMembers;
    }

    public void installInitialUpgrades(World world) {
        addUpgrade(world, new LabUpgrade());
        addUpgrade(world, new BarracksUpgrade());
        addUpgrade(world, new WorkshopUpgrade());
        addUpgrade(world, new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon")));

        final CrewMember henry = new CrewMember("henry", "marine_dialog", Dialog.loadFromFile("dialogs/tutorials/marine_intro.json"));
        henry.getDialog().addListener(new HenryMainDialogListener(henry));
        henry.setCustomMusic(ResourceManager.getInstance().getPlaylist("henry_dialog"));
        addCrewMember(world, henry);

        final CrewMember gordon = new CrewMember("gordon", "scientist_dialog", Dialog.loadFromFile("dialogs/tutorials/scientist_intro.json"));
        gordon.getDialog().addListener(new GordonMainDialogListener(gordon));
        gordon.setCustomMusic(ResourceManager.getInstance().getPlaylist("gordon_dialog"));
        addCrewMember(world, gordon);

        final CrewMember sarah = new CrewMember("sarah", "engineer_dialog", Dialog.loadFromFile("dialogs/tutorials/engineer_intro.json"));
        sarah.getDialog().addListener(new SarahMainDialogListener(sarah));
        sarah.setCustomMusic(ResourceManager.getInstance().getPlaylist("sarah_dialog"));
        addCrewMember(world, sarah);

        refillCrew(world);
    }

    public void addUpgrade(World world, ShipUpgrade upgrade) {
        if (freeSpace < upgrade.getSpace()) {
            throw new IllegalArgumentException("Upgrade can not be installed because thiere is not enough space");
        }
        freeSpace -= upgrade.getSpace();
        upgrade.onInstalled(world, this);
        upgrades.add(upgrade);
    }

    public void removeUpgrade(World world, ShipUpgrade upgrade) {
        for (Iterator<ShipUpgrade> iterator = upgrades.iterator(); iterator.hasNext(); ) {
            ShipUpgrade u = iterator.next();
            if (u.equals(upgrade)) {
                iterator.remove();
                u.onRemoved(world, this);
                freeSpace += u.getSpace();
                break;
            }
        }
    }

    public void addFreeSpace(int amount) {
        freeSpace += amount;
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (world.isUpdatedThisFrame()) {
            for (WeaponInstance weapon : weapons) {
                weapon.reload();
            }
        }
    }

    public int getHull() {
        return hull;
    }

    public void setHull(int hull) {
        this.hull = hull;
    }

    public int getMaxHull() {
        return maxHull;
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera, World world) {
        if (hull > 0) {
            super.draw(container, g, camera, world);
        }
    }

    public int getScientists() {
        return scientists;
    }

    public void setScientists(int scientists) {
        this.scientists = scientists;
    }

    public int getEngineers() {
        return engineers;
    }

    public void setEngineers(int engineers) {
        this.engineers = engineers;
    }

    public int getMilitary() {
        return military;
    }

    public void setMilitary(int military) {
        this.military = military;
    }

    public int getTotalCrew() {
        return scientists + engineers + military;
    }

    public int getMaxCrew() {
        return maxEngineers + maxMilitary + maxScientists;
    }

    public List<WeaponInstance> getWeapons() {
        return weapons;
    }


    @Override
    public void onAttack(World world, GameObject attacker, int dmg) {
        if (Configuration.getBooleanProperty("cheat.invulnerability")) {
            return;
        }


        int loseCrewChance = Configuration.getIntProperty("game.crew.lose_chance");
        int loseChanceReduce = MedBayUpgrade.getCrewDeathReduceValue();
        loseCrewChance -= loseCrewChance * 0.01f * loseChanceReduce;

        if (world.getGlobalVariables().containsKey("tutorial.started")) {
            // crew memebers can not die during a tutorial
            loseCrewChance = 0;
        }
        
        Random rnd = CommonRandom.getRandom();
        if(rnd.nextInt(100) < loseCrewChance && getTotalCrew() > 0) {
            int[] crewSize = {engineers, military, scientists};
            int[] availableCrew = new int[crewSize.length];
            
            int i = 0;  //counter of available crew
            for(int j = 0; j < crewSize.length; ++j) {
                if(crewSize[j] > 0)
                    availableCrew[i++] = j;
            }
            
            String lostMember;
            int toDelete = i > 1 ? rnd.nextInt(i) : 0;
            switch(availableCrew[toDelete]) {
                case 0:
                    engineers--;
                    world.getPlayer().getEngineeringState().removeEngineers(1);
                    lostMember = Localization.getText("crew", "engineer.name");
                    break;
                case 1:
                    military--;
                    lostMember = Localization.getText("crew", "military.name");
                    break;
                default:
                    scientists--;
                    world.getPlayer().getResearchState().removeScientists(1);
                    lostMember = Localization.getText("crew", "scientist.name");
            }
            
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.crew.lost"), lostMember));
            world.onCrewChanged();
        }
        
        hull -= dmg;
        world.onPlayerShipDamaged();
        if (hull <= 0) {
            explode(world);

        }
    }

    private void explode(World world) {
        ExplosionEffect ship_explosion = new ExplosionEffect(x, y, "ship_explosion", false, true);
        ship_explosion.getAnim().setSpeed(0.5f);
        ((StarSystem) world.getCurrentRoom()).addEffect(ship_explosion);
        ship_explosion.setEndListener(new GameOverEffectListener());
    }

    @Override
    public boolean isAlive() {
        return hull > 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canBeAttacked() {
        return true;
    }

    public int getLostCrewMembers() {
        return getMaxCrew() - getTotalCrew();
    }

    public void refillCrew(World world) {
        Player player = world.getPlayer();
        
        player.getResearchState().setIdleScientists(maxScientists - player.getResearchState().getBusyScientists(true));
        player.getEngineeringState().setIdleEngineers(maxEngineers - player.getEngineeringState().getBusyEngineers(true));

        scientists = maxScientists;
        engineers = maxEngineers;
        military = maxMilitary;

        world.onCrewChanged();
    }

    public void fullRepair(World world) {
        hull = maxHull;
        world.getPlayer().getEngineeringState().getHullRepairs().cancel(world);

    }

    private void setHenryDefaultDialog()
    {
        CrewMember henry = crewMembers.get("henry");
        Dialog defaultDialog = Dialog.loadFromFile("dialogs/crew/henry/henry_default.json");
        defaultDialog.addListener(new HenryMainDialogListener(henry));
        henry.setDialog(defaultDialog);
    }

    private void setGordonDefaultDialog()
    {
        CrewMember gordon = crewMembers.get("gordon");
        Dialog defaultDialog = Dialog.loadFromFile("dialogs/crew/gordon/gordon_default.json");
        defaultDialog.addListener(new HenryMainDialogListener(gordon));
        gordon.setDialog(defaultDialog);
    }

    private void setSarahDefaultDialog()
    {
        CrewMember sarah = crewMembers.get("sarah");
        Dialog defaultDialog = Dialog.loadFromFile("dialogs/crew/sarah/sarah_default.json");
        defaultDialog.addListener(new HenryMainDialogListener(sarah));
        sarah.setDialog(defaultDialog);
    }

    public void setDefaultCrewDialogs(World world)
    {
        setHenryDefaultDialog();
        setGordonDefaultDialog();
        setSarahDefaultDialog();
    }

    public List<ShipUpgrade> getUpgrades() {
        return upgrades;
    }

    public int getMaxMilitary() {
        return maxMilitary;
    }

    public void setMaxMilitary(int maxMilitary) {
        this.maxMilitary = maxMilitary;
    }

    public int getMaxScientists() {
        return maxScientists;
    }

    public void setMaxScientists(int maxScientists) {
        this.maxScientists = maxScientists;
    }

    public int getMaxEngineers() {
        return maxEngineers;
    }

    public void setMaxEngineers(int maxEngineers) {
        this.maxEngineers = maxEngineers;
    }

    public int getFreeSpace() {
        return freeSpace;
    }

    public void changeMaxHull(int amount)
    {
        int damage = maxHull - hull;
        maxHull += amount;
        hull = maxHull - damage;
        if (hull <= 0) {
            explode(World.getWorld());
        }
    }

    public void changeRangeBuff(int delta) {
        this.rangeBuff += delta;
    }
}
