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
        StringBuilder sb = new StringBuilder("Landing team status:\n");
        sb.append("    Science: ").append(science).append('\n');
        sb.append("    Engineers: ").append(engineers).append('\n');
        sb.append("    Military: ").append(military).append('\n');
        sb.append("Remaining oxygen: ").append(oxygen).append('\n');
        GameLogger.getInstance().addStatusString(sb.toString());
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {
        engine.drawImage(camera.getXCoord(x), camera.getYCoord(y), "awayteam");
    }
}
