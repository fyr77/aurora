package ru.game.aurora.world.planet.nature;

import com.google.common.collect.Lists;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Configuration;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.*;
import ru.game.aurora.world.equip.WeaponInstance;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.MonsterBehaviour;
import ru.game.aurora.world.planet.Planet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 17:00
 */
public class Animal extends SurfaceLootObject implements IMonster {

    private static final long serialVersionUID = 1L;
    
    private static final int autopsyDamageBonus;
    
    private static final boolean autopsyDamageBonusPercent;

    private int hp;

    private final AnimalSpeciesDesc desc;

    // todo: remove
    private Planet myPlanet;

    private ITileMap myMap;

    private boolean wasAttacked = false;

    private final MonsterController controller;

    private List<WeaponInstance> weapons;
    
    static {
        String str = Configuration.getProperty("animal.autopsy_damage_bonus");
        if(str.contains("%")) {
            autopsyDamageBonus = Integer.parseInt(str.replace("%", ""));
            autopsyDamageBonusPercent = true;
        }
        else {
            autopsyDamageBonus = Integer.parseInt(str);
            autopsyDamageBonusPercent = false;
        }
    }

    public Animal(ITileMap map, int x, int y, AnimalSpeciesDesc desc) {
        super(x, y);
        this.desc = desc;
        this.myMap = map;
        this.hp = desc.getHp();
        if (desc.getWeapon() != null) {
            this.weapons = Lists.newArrayList(new WeaponInstance(desc.getWeapon()));
        } else {
            this.weapons = Collections.emptyList();
        }
        controller = new MonsterController(myMap, this);
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        controller.update(container, world);
    }

    // todo: [save] remove planet
    private void readObject(
            ObjectInputStream aInputStream
    ) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();

        if (myPlanet != null && myMap == null) {
            myMap = myPlanet.getMap();
            myPlanet = null;
        }
    }


    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
        if (desc.getImage() == null || (desc.isCanBePickedUp() && desc.getDeadImage() == null)) {
            AnimalGenerator.getInstance().getImageForAnimal(desc);
        }
        final Image image = hp > 0 ? desc.getImage() : desc.getDeadImage();
        graphics.drawImage(image
                , myMap.isWrapped() ? camera.getXCoordWrapped(x, myMap.getWidthInTiles()) : camera.getXCoord(x)
                , myMap.isWrapped() ? camera.getYCoordWrapped(y, myMap.getHeightInTiles()) : camera.getYCoord(y));

        String hpText;
        
        if(!desc.isOutopsyMade()) {
            hpText = "???";
        } else if (hp < 100) {
            hpText = Integer.toString(Math.max(0, hp));
        } else {
            hpText = "N/A";
        }
        if (hp < desc.getHp() / 4) {
            graphics.setColor(Color.red);
        } else {
            graphics.setColor(Color.white);
        }
        graphics.drawString(hpText, camera.getXCoord(x) + getOffsetX(), camera.getYCoord(y) + getOffsetY());
    }

    public AnimalSpeciesDesc getDesc() {
        return desc;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getSpeed() {
        return desc.getSpeed();
    }

    @Override
    public List<WeaponInstance> getWeapons() {
        return weapons;
    }

    @Override
    public boolean canBeInteracted() {
        return hp <= 0;
    }

    @Override
    public boolean canBeAttacked() {
        return hp > 0;
    }

    @Override
    public ScanGroup getScanGroup() {
        return ScanGroup.BIO;
    }


    @Override
    public void onAttack(World world, GameObject attacker, int damage) {
        if (desc.isOutopsyMade()) {
            damage += getAutopsyDamageBonus(damage);
        }
        if (desc.getModifiers().contains(AnimalModifier.ARMOR)) {
            damage = Math.max(0, damage - desc.getArmor());
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.armor_consumed_damage"), desc.getArmor()));
        }
        hp -= damage;
        super.onAttack(world, attacker, damage);
        if (!wasAttacked && desc.getBehaviour() == MonsterBehaviour.SELF_DEFENSIVE) {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_enraged"), getName()));
            wasAttacked = true;
        }
        if (hp <= 0) {
            // clean obstacle flag
            if (nowMoving()) {
                myMap.setTilePassable(getTargetX(), getTargetY(), true);
            } else {
                myMap.setTilePassable(x, y, true);
            }
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.killed_message"), getName()));
            if (!desc.isCanBePickedUp()) {
                // this alien leaves no corpse
                pickedUp = true;
            }
        }
    }
    
    private static int getAutopsyDamageBonus(int damage) {
        if(autopsyDamageBonusPercent) {
            return Math.max((int) (damage * 0.01f * autopsyDamageBonus), 1);
        }
        
        return autopsyDamageBonus;
    }

    @Override
    public String getName() {
        if (hp > 0) {
            return desc.getName();
        } else {
            return desc.getName() + " " + Localization.getText("gui", "surface.corpse");
        }
    }

    @Override
    public MonsterBehaviour getBehaviour() {
        return (desc.getBehaviour() == MonsterBehaviour.SELF_DEFENSIVE && wasAttacked) ? MonsterBehaviour.AGGRESSIVE : desc.getBehaviour();
    }

    public void setWasAttacked(boolean wasAttacked) {
        this.wasAttacked = wasAttacked;
    }

    @Override
    public Image getImage() {
        return hp > 0 ? desc.getImage() : desc.getDeadImage();
    }
    
    @Override
    protected InventoryItem getLootItem() {
        return new AnimalCorpseItem(desc);
    }
}
