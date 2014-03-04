package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.IMonster;
import ru.game.aurora.world.MonsterController;
import ru.game.aurora.world.Movable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetObject;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 17:00
 */
public class Animal extends Movable implements PlanetObject, IMonster {

    private static final long serialVersionUID = 1L;

    private int hp;

    private AnimalSpeciesDesc desc;

    private Planet myPlanet;

    private boolean pickedUp = false;

    private boolean wasAttacked = false;

    private MonsterController controller;

    public Animal(Planet p, int x, int y, AnimalSpeciesDesc desc) {
        super(x, y);
        this.desc = desc;
        this.myPlanet = p;
        this.hp = desc.getHp();
        controller = new MonsterController(p.getMap(), this);
    }

    @Override
    public void update(GameContainer container, World world) {
        super.update(container, world);
        controller.update(container, world);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        if (desc.getImage() == null || desc.getDeadImage() == null) {
            AnimalGenerator.getInstance().getImageForAnimal(desc);
        }
        final Image image = hp > 0 ? desc.getImage() : desc.getDeadImage();
        graphics.drawImage(image, camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
    }

    public AnimalSpeciesDesc getDesc() {
        return desc;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public void changeHp(int amount) {
        this.hp += amount;
    }

    @Override
    public int getSpeed() {
        return desc.getSpeed();
    }

    @Override
    public LandingPartyWeapon getWeapon() {
        return desc.getWeapon();
    }

    @Override
    public boolean canBePickedUp() {
        return hp <= 0;
    }

    @Override
    public boolean canBeShotAt() {
        return hp > 0;
    }

    @Override
    public void onShotAt(World world, int damage) {
        if (desc.getModifiers().contains(AnimalModifier.ARMOR)) {
            damage = Math.max(0, damage - desc.getArmor());
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.armor_consumed_damage"), desc.getArmor()));
        }
        hp -= damage;
        if (!wasAttacked && desc.getBehaviour() == AnimalSpeciesDesc.Behaviour.SELF_DEFENSIVE) {
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_enraged"), getName()));
            wasAttacked = true;
        }
        if (hp <= 0) {
            // clean obstacle flag
            myPlanet.getSurface().setTilePassable(x, y, true);
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.killed_message"), getName()));
        }
    }

    @Override
    public void onPickedUp(World world) {
        pickedUp = true;
        GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.picked_up"), getName()));
        world.getPlayer().getLandingParty().pickUp(new AnimalCorpseItem(desc), 1);
    }

    @Override
    public boolean isAlive() {
        // object is alive untill picked up, even if animal is actually dead
        return !pickedUp;
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
    public AnimalSpeciesDesc.Behaviour getBehaviour() {
        return (desc.getBehaviour() == AnimalSpeciesDesc.Behaviour.SELF_DEFENSIVE && wasAttacked) ? AnimalSpeciesDesc.Behaviour.AGGRESSIVE : desc.getBehaviour();
    }

    @Override
    public void printStatusInfo() {
        if (hp <= 0) {
        }
    }


}
