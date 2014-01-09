package ru.game.aurora.application;

/**
* Created with IntelliJ IDEA.
* User: Egor.Smirnov
* Date: 09.01.14
* Time: 12:38
* To change this template use File | Settings | File Templates.
*/
public final class Resolution implements Comparable<Resolution>
{
    private int width;
    private int height;

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
        return AuroraGame.tilesX * AuroraGame.tileSize == width && AuroraGame.tilesY * AuroraGame.tileSize == height;
    }

    public int getTilesX() {
        return width / AuroraGame.tileSize;
    }

    public int getTilesY() {
        return height / AuroraGame.tileSize;
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
