/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.*;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.npc.shipai.NPCShipAI;
import ru.game.aurora.util.ProbabilitySet;
import ru.game.aurora.world.MovableSprite;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.equip.StarshipWeaponDesc;

public class NPCShip extends MovableSprite implements SpaceObject {

    private static final long serialVersionUID = 1L;

    protected AlienRace race;

    protected NPC captain;

    protected int hp = 3;

    protected String name;

    protected int speed = 3;

    protected int curSpeed = 3;

    protected NPCShipAI ai;

    protected boolean isHostile;

    protected boolean canBeHailed = true;

    protected StarshipWeapon[] weapons;

    // this ship can not move
    private boolean isStationary;

    // map of loot that can be dropped by this ship, with its chances
    private ProbabilitySet<SpaceObject> loot;

    public NPCShip(int x, int y, String sprite, AlienRace race, NPC captain, String name) {
        super(x, y, sprite);
        this.race = race;
        this.captain = captain;
        this.name = name;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setLoot(ProbabilitySet<SpaceObject> loot) {
        this.loot = loot;
    }

    public void setAi(NPCShipAI ai) {
        this.ai = ai;
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);

        if (weapons != null) {
            for (StarshipWeapon w : weapons) {
                w.reload();
            }
        }
        if (world.isUpdatedThisFrame()) {
            curSpeed--;
        }
        if (curSpeed > 0) {
            return;
        }
        curSpeed = speed;
        StarSystem ss = world.getCurrentStarSystem();
        if (ss == null) {
            return;
        }

        for (SpaceObject so : ss.getShips()) {
            if (isHostile(world, so) && (ai == null || !(ai instanceof CombatAI))) {
                ai = new CombatAI(so);
            }
        }

        if (isHostile(world, world.getPlayer().getShip()) && (ai == null || !(ai instanceof CombatAI))) {
            ai = new CombatAI(world.getPlayer().getShip());
        }

        if (ai != null) {
            ai.update(this, world, (StarSystem) world.getCurrentRoom());
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        super.draw(container, g, camera);
    }

    /**
     * Returns true if this ship is hostile to player
     * Hostile ships can not be hailed and will attack player when they see it
     */
    public boolean isHostile(World world, SpaceObject object) {
        return (object instanceof Ship && isHostile)
                || (race != null && object.getRace() != null && world.getCurrentStarSystem().getReputation().isHostile(race.getName(), object.getRace().getName()));
    }

    @Override
    public AlienRace getRace() {
        return race;
    }

    @Override
    public boolean isAlive() {
        boolean rz = hp > 0;
        if (ai != null) {
            rz &= ai.isAlive();
        }
        return rz;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScanDescription(World world) {
        StringBuilder sb = new StringBuilder(String.format(Localization.getText("gui", "scan.ship.race"), race.getName()));
        sb.append('\n');
        sb.append(Localization.getText("gui", "scan.ship.relation_prefix")).append(" ").append(isHostile(world, world.getPlayer().getShip()) ? Localization.getText("gui", "scan.ship.hostile") : Localization.getText("gui", "scan.ship.friendly"));
        return sb.toString();
    }

    @Override
    public void onContact(World world) {
        if (!canBeHailed || isHostile) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "space.hail_not_responded"));
            return;
        }
        world.addOverlayWindow(captain != null ? captain.getCustomDialog() : race.getDefaultDialog());
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
        hp -= dmg;
        final StarSystem currentStarSystem = world.getCurrentStarSystem();
        if (hp <= 0) {
            GameLogger.getInstance().logMessage(getName() + " " + Localization.getText("gui", "space.destroyed"));
            currentStarSystem.addEffect(new ExplosionEffect(x, y, "ship_explosion", false, true));

            if (loot != null) {
                if (CommonRandom.getRandom().nextBoolean()) {
                    currentStarSystem.getShips().add(new SpaceDebris(x, y, loot));
                }
            }
        }
        if (ai == null || !(ai instanceof CombatAI)) {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.hostile"), getName(), attacker.getName()));
            if (attacker.equals(world.getPlayer().getShip())) {
                isHostile = true;
            }
            ai = new CombatAI(attacker);
        }
        if (!currentStarSystem.getReputation().isHostile(race.getName(), attacker.getRace().getName())) {
            currentStarSystem.getReputation().setHostile(race.getName(), attacker.getRace().getName());
        }
    }

    public StarshipWeapon[] getWeapons() {
        return weapons;
    }

    public void setWeapons(StarshipWeapon... weapons) {
        this.weapons = weapons;
    }

    public void setWeapons(StarshipWeaponDesc... weaponDescs) {
        this.weapons = new StarshipWeapon[weaponDescs.length];
        for (int i = 0; i < weaponDescs.length; ++i) {
            this.weapons[i] = new StarshipWeapon(weaponDescs[i], StarshipWeapon.MOUNT_ALL);
        }
    }

    public void fire(World world, StarSystem ss, int weaponIdx, SpaceObject target) {
        weapons[weaponIdx].fire();
        final StarshipWeaponDesc weaponDesc = weapons[weaponIdx].getWeaponDesc();
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "space.attack")
                , getName()
                , target.getName()
                , Localization.getText("weapons", weaponDesc.name)
                , weaponDesc.damage
        ));
        target.onAttack(world, this, weaponDesc.damage);

        ss.addEffect(new BlasterShotEffect(this, target, world.getCamera(), 800, weapons[weaponIdx]));

        ResourceManager.getInstance().getSound(weaponDesc.shotSound).play();

        if (!target.isAlive()) {
            GameLogger.getInstance().logMessage(target.getName() + " " + Localization.getText("gui", "space.destroyed"));
        }
    }

    @Deprecated
    public void move(int dx, int dy) {
        if (isStationary) {
            return;
        }
        x += dx;
        y += dy;
    }

    @Override
    public void moveDown() {
        if (isStationary) {
            return;
        }
        super.moveDown();
    }

    @Override
    public void moveLeft() {
        if (isStationary) {
            return;
        }
        super.moveLeft();
    }

    @Override
    public void moveRight() {
        if (isStationary) {
            return;
        }
        super.moveRight();
    }

    @Override
    public void moveUp() {
        if (isStationary) {
            return;
        }
        super.moveUp();
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setHostile(boolean hostile) {
        isHostile = hostile;
    }

    public boolean isStationary() {
        return isStationary;
    }

    public void setStationary(boolean stationary) {
        isStationary = stationary;
    }

    public void setCaptain(NPC captain) {
        this.captain = captain;
    }

    public NPC getCaptain() {
        return captain;
    }

    public boolean isCanBeHailed() {
        return canBeHailed;
    }

    public void setCanBeHailed(boolean canBeHailed) {
        this.canBeHailed = canBeHailed;
    }
}
