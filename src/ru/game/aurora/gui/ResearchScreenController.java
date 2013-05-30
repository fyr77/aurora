/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 28.05.13
 * Time: 16:14
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.world.World;

public class ResearchScreenController implements ScreenController
{
    private World world;

    private ListBox researchListBox;

    public ResearchScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {
    }


    public void closeScreen()
    {
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }
}
