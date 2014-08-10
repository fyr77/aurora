package ru.game.aurora.world.dungeon;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 13.09.13
 * Time: 12:40
 */
public class DungeonMonster extends DungeonObject implements IMonster {
    private static final long serialVersionUID = 3L;

    private LandingPartyWeapon weapon;

    private Set<String> tags = null;

    private int speed;

    private int hp;

    private final int maxHp;

    private AnimalSpeciesDesc.Behaviour behaviour;

    private MonsterController controller;

    private ITileMap owner;

    public DungeonMonster(AuroraTiledMap map, int groupId, int objectId) {
        super(map, groupId, objectId);
        owner = map;
        weapon = ResourceManager.getInstance().getLandingPartyWeapons().getEntity(map.getMap().getObjectProperty(groupId, objectId, "weapon", null));
        speed = Integer.parseInt(map.getMap().getObjectProperty(groupId, objectId, "speed", "0"));
        maxHp = hp = Integer.parseInt(map.getMap().getObjectProperty(groupId, objectId, "hp", "1"));
        behaviour = AnimalSpeciesDesc.Behaviour.valueOf(map.getMap().getObjectProperty(groupId, objectId, "behaviour", "AGGRESSIVE"));
        final String tagsString = map.getMap().getObjectProperty(groupId, objectId, "tags", null);
        if (tagsString != null) {
            tags = new HashSet<>();
            Collections.addAll(tags, tagsString.split(","));
        }
        controller = new MonsterController(map, this);
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        if (behaviour == AnimalSpeciesDesc.Behaviour.AGGRESSIVE || behaviour == AnimalSpeciesDesc.Behaviour.FRIENDLY) {
            controller.update(container, world);
        }
    }

    @Override
    public boolean canBeAttacked() {
        return behaviour != AnimalSpeciesDesc.Behaviour.FRIENDLY && hp > 0;
    }

    @Override
    public void onAttack(World world, GameObject attacker, int damage) {
        hp -= damage;
        if (hp <= 0) {
            // clean obstacle flag
            if (!nowMoving()) {
                owner.setTilePassable(x, y, true);
            } else {
                owner.setTilePassable(getTargetX(), getTargetY(), true);
            }
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.killed_message"), getName()));
            world.getCurrentRoom().getMap().getObjects().remove(this);
        }
    }

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public void changeHp(int amount) {
        hp += amount;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public LandingPartyWeapon getWeapon() {
        return weapon;
    }

    public AnimalSpeciesDesc.Behaviour getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(AnimalSpeciesDesc.Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        super.draw(container, graphics, camera, world);
        String hpText;
        if (hp < 100) {
            hpText = Integer.toString(Math.max(0, hp));
        } else {
            hpText = "N/A";
        }
        if (hp < maxHp / 4) {
            graphics.setColor(Color.red);
        } else {
            graphics.setColor(Color.white);
        }
        graphics.drawString(hpText, camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
    }
}
