/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:06
 */
package ru.game.aurora.world;

import de.lessvoid.nifty.screen.Screen;

public interface Room extends GameObject {

    public void enter(World world);


    public Screen getGUI();
}
