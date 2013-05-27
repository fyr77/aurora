/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 27.05.13
 * Time: 17:42
 */
package ru.game.aurora.gui.niffy;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;


public class ListScreenController implements ScreenController
{
    @Override
    public void bind(Nifty nifty, Screen screen) {
        ListBox listBox = (ListBox) screen.findElementByName("itemsList");
        listBox.addItem("Research 1");
        listBox.addItem("Research 2");
        listBox.addItem("Research 3");
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }
}
