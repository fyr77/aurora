package ru.game.aurora.world;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import ru.game.aurora.application.Camera;

public interface IDrawable {
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world);
}
