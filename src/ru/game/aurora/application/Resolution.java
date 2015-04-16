package ru.game.aurora.application;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
* Created with IntelliJ IDEA.
* User: Egor.Smirnov
* Date: 09.01.14
* Time: 12:38
* To change this template use File | Settings | File Templates.
*/
public final class Resolution implements Comparable<Resolution>
{
    private final int width;
    private final int height;

    public Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Resolution(String propValue) {
        String[] split = propValue.split("[xX]");
        width = Integer.parseInt(split[0]);
        height = Integer.parseInt(split[1]);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

    public boolean isActive() {
        DisplayMode dm = Display.getDisplayMode();
        return dm.getWidth() == width && dm.getHeight() == height;
    }

    public int getTilesX() {
        return (int) Math.ceil(width / (float) AuroraGame.tileSize);
    }

    public int getTilesY() {
        return (int) Math.ceil((float) height / AuroraGame.tileSize);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int compareTo(Resolution o) {
        return (int) Math.signum(width * height - o.width * o.height);
    }
}
