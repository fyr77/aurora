/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 05.06.13
 * Time: 14:48
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;


public class StoryScreenController implements ScreenController
{
    private StoryScreen story;

    private Element imagePanel;

    private Element textPanel;

    public void setStory(StoryScreen story) {
        this.story = story;
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

    public void nextPage()
    {

    }

    public void prevPage()
    {

    }
}
