/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.NPC;
import ru.game.aurora.npc.shipai.CombatAI;
import ru.game.aurora.npc.shipai.NPCShipAI;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.equip.StarshipWeaponDesc;

public class NPCShip extends BasePositionable implements SpaceObject {

    private static final long serialVersionUID = 4304196228941570752L;

    private String sprite;

    private AlienRace race;

    private NPC capitain;

    private int hp = 3;

    private String name;

    private static final int speed = 3;

    private int curSpeed = 3;

    private NPCShipAI ai;

    private boolean isHostile;

    private boolean canBeHailed = true;

    private StarshipWeapon[] weapons;

    // this ship can not move
    private boolean isStationary;

    public NPCShip(int x, int y, String sprite, AlienRace race, NPC capitain, String name) {
        super(x, y);
        this.sprite = sprite;
        this.race = race;
        this.capitain = capitain;
        this.name = name;
    }

    public void setAi(NPCShipAI ai) {
        this.ai = ai;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (weapons != null) {
            for (StarshipWeapon w : weapons) {
                w.reload();
            }
        }
        if (curSpeed-- > 0) {
            return;
        }
        curSpeed = speed;
        if (ai != null) {
            if (!(world.getCurrentRoom() instanceof StarSystem)) {
                return;
            }
            ai.update(this, world, (StarSystem) world.getCurrentRoom());
        }
    }

    @Override
    public void draw(GameContainer container, Graphics g, Camera camera) {
        g.drawImage(ResourceManager.getInstance().getImage(sprite), camera.getXCoord(x), camera.getYCoord(y));
    }

    /**
     * Returns true if this ship is hostile to player
     * Hostile ships can not be hailed and will attack player when they see it
     */
    public boolean isHostile() {
        return race.isHostileToPlayer() || isHostile;
    }

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
    public String getScanDescription() {
        StringBuilder sb = new StringBuilder("This is a spaceship of ").append(race.getName()).append(" race. ");
        sb.append('\n');
        sb.append("It is currently ").append(isHostile() ? "hostile" : "friendly");
        return sb.toString();
    }

    @Override
    public void onContact(World world) {
        if (!canBeHailed || isHostile) {
            GameLogger.getInstance().logMessage("This ship does not respond to our hail.");
            return;
        }
        world.addOverlayWindow(capitain != null ? capitain.getCustomDialog() : race.getDefaultDialog());
    }

    @Override
    public void onAttack(World world, SpaceObject attacker, int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            GameLogger.getInstance().logMessage(getName() + " destroyed");
            ((StarSystem) world.getCurrentRoom()).addEffect(new ExplosionEffect(x, y, "ship_explosion", false));
        }
        if (ai == null || !(ai instanceof CombatAI)) {
            GameLogger.getInstance().logMessage(getName() + " is now hostile to " + attacker.getName());
            if (attacker.equals(world.getPlayer().getShip())) {
                isHostile = true;
            }
            ai = new CombatAI(attacker);
        }
    }

    public StarshipWeapon[] getWeapons() {
        return weapons;
    }

    public void setWeapons(StarshipWeapon... weapons) {
        this.weapons = weapons;
    }

    public void fire(World world, StarSystem ss, int weaponIdx, SpaceObject target) {
        weapons[weaponIdx].fire();
        final StarshipWeaponDesc weaponDesc = weapons[weaponIdx].getWeaponDesc();
        GameLogger.getInstance().logMessage(String.format("%s fires at %s with %s, dealing %d damage"
                , getName()
                , target.getName()
                , weaponDesc.name
                , weaponDesc.damage
        ));
        target.onAttack(world, this, weaponDesc.damage);

        ss.addEffect(new BlasterShotEffect(this, target, world.getCamera(), 800, weaponDesc.shotSprite));

        if (!target.isAlive()) {
            GameLogger.getInstance().logMessage(target.getName() + " destroyed");
            ss.getShips().remove(target);
        }
    }

    public void move(int dx, int dy) {
        if (isStationary) {
            return;
        }
        x += dx;
        y += dy;
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

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public void setCapitain(NPC capitain) {
        this.capitain = capitain;
    }

    public NPC getCapitain() {
        return capitain;
    }

    public boolean isCanBeHailed() {
        return canBeHailed;
    }

    public void setCanBeHailed(boolean canBeHailed) {
        this.canBeHailed = canBeHailed;
    }


}
