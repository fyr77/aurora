/**
 * User: jedi-philosopher
 * Date: 29.11.12
 * Time: 20:06
 */
package ru.game.aurora.world;

import de.matthiasmann.twl.Widget;

public interface Room extends GameObject {

    public void enter(World world);


    public Widget getGUI();
}
