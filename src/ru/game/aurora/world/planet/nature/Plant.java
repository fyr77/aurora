/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 20.08.13
 * Time: 14:41
 */
package ru.game.aurora.world.planet.nature;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.BasePositionable;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.Planet;
import ru.game.aurora.world.planet.PlanetObject;


public class Plant extends BasePositionable implements PlanetObject
{
    private static final long serialVersionUID = 1L;

    private PlantSpeciesDesc desc;

    private Planet myPlanet;

    public Plant(int x, int y, PlantSpeciesDesc desc, Planet myPlanet) {
        super(x, y);
        this.desc = desc;
        this.myPlanet = myPlanet;
    }

    public PlantSpeciesDesc getDesc() {
        return desc;
    }

    public void setDesc(PlantSpeciesDesc desc) {
        this.desc = desc;
    }

    @Override
    public boolean canBePickedUp() {
        return false;
    }

    @Override
    public boolean canBeShotAt() {
        return false;
    }

    @Override
    public void onShotAt(World world, int damage) {
    }

    @Override
    public void onPickedUp(World world) {
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return desc.getName();
    }

    @Override
    public void printStatusInfo() {

    }

    @Override
    public void update(GameContainer container, World world) {

    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        graphics.drawImage(desc.getImage(), camera.getXCoordWrapped(x, myPlanet.getWidth()), camera.getYCoordWrapped(y, myPlanet.getHeight()));
    }
}
