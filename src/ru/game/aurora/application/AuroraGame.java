/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:05
 */
package ru.game.aurora.application;

import jgame.JGColor;
import jgame.JGPoint;
import jgame.platform.JGEngine;
import ru.game.aurora.world.World;

public class AuroraGame extends JGEngine
{

    private World world;

    /** Application constructor.
     * @param size window size
     * */
	public AuroraGame(JGPoint size)
    {
        initEngine(size.x,size.y);
    }

	/** Applet constructor. */
	public AuroraGame()
    {
        initEngineApplet();
    }

    @Override
	public void initCanvas() {
		// we set the background colour to same colour as the splash background
		setCanvasSettings(20,15,64,64, JGColor.black,new JGColor(255,246,199),null);
	}

    @Override
    public void initGame() {
        setFrameRate(30,2);
        defineMedia("sprites.tbl");
        GameLogger.init(this);
        world = new World();
        setBGColor(JGColor.black);
    }

    @Override
    public void doFrame()
    {
        world.update(this);
    }

    @Override
    public void paintFrame()
    {
        world.draw(this);
    }

    public static void main(String[] args)
    {
        new AuroraGame(new JGPoint(1024, 768));
    }
}
