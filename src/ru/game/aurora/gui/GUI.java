/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 17:03
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;

public class GUI {
    private Nifty nifty;

    private static GUI instance;

    public static void init(Nifty n) {
        instance = new GUI(n);
    }

    public Nifty getNifty() {
        return nifty;
    }

    private GUI(Nifty n) {
        this.nifty = n;
    }

    public static GUI getInstance() {
        return instance;
    }

    public void addScreen(Screen screen) {
        nifty.addScreen(screen.getScreenId(), screen);
    }

    public void setScreen(String id) {
        nifty.gotoScreen(id);
    }
}
