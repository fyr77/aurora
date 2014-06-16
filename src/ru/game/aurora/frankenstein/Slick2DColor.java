package ru.game.aurora.frankenstein;

import org.newdawn.slick.Color;
import ru.game.frankenstein.FrankensteinColor;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 19.03.14
 * Time: 15:56
 */
public class Slick2DColor implements FrankensteinColor, Serializable {
    private static final long serialVersionUID = 7371175836249657696L;

    private Color color;

    public Slick2DColor(Color color) {
        this.color = color;
    }

    @Override
    public int getR() {
        return color.getRed();
    }

    @Override
    public int getG() {
        return color.getGreen();
    }

    @Override
    public int getB() {
        return color.getBlue();
    }

    @Override
    public int getAlpha() {
        return color.getAlpha();
    }

    @Override
    public String toString() {
        return color.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Slick2DColor that = (Slick2DColor) o;

        return color.equals(that.color);

    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }
}
