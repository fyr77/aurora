/**
 * User: jedi-philosopher
 * Date: 02.12.12
 * Time: 19:38
 */
package ru.game.aurora.world.planet;

import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.world.GameObject;
import ru.game.aurora.world.Positionable;
import ru.game.aurora.world.World;

public class LandingParty implements GameObject, Positionable {
    private int x;

    private int y;

    private int military;

    private int science;

    private int engineers;

    private int oxygen;

    public LandingParty(int x, int y, int military, int science, int engineers) {
        this.x = x;
        this.y = y;
        this.military = military;
        this.science = science;
        this.engineers = engineers;
        oxygen = 100;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(JGEngine engine, World world) {

    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        engine.drawImage(camera.getXCoord(x), camera.getYCoord(y), "awayteam");
        //JGRectangle rect = GameLogger.getInstance().getStatusMessagesRect();

        //final JGFont font = GameLogger.getInstance().getFont();
        /*engine.drawString("Landing team status:", rect.x, rect.y, -1, font, JGColor.white);
        engine.drawString("scientists: " + science, rect.x, rect.y + font.getSize(), -1, font, JGColor.white);
        engine.drawString("engineers: " + engineers, rect.x, rect.y + font.getSize() * 2, -1, font, JGColor.white);
        engine.drawString("military: " + military, rect.x, rect.y + font.getSize() * 3, -1, font, JGColor.white);
        engine.drawString("Remaining oxygen: " + oxygen, rect.x, rect.y + font.getSize() * 4, -1, font, JGColor.white);

        engine.drawString(String.format("Coordinates : (%d, %d)", x, y), rect.x, rect.y + font.getSize() * 5, -1, font, JGColor.white);*/
        GameLogger.getInstance().addStatusMessage("Landing team status:");
        GameLogger.getInstance().addStatusMessage("scientists: " + science);
        GameLogger.getInstance().addStatusMessage("engineers: " + engineers);
        GameLogger.getInstance().addStatusMessage("military: " + military);
        GameLogger.getInstance().addStatusMessage("Remaining oxygen: " + oxygen);
        GameLogger.getInstance().addStatusMessage(String.format("Current coordinates (%d, %d)", x, y));
    }

    public void consumeOxygen() {
        //todo: depend on team size?
        oxygen--;
    }

    public void refillOxygen() {
        oxygen = 100;
    }

    public int getOxygen() {
        return oxygen;
    }
}
