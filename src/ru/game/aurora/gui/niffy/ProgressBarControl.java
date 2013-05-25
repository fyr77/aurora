package ru.game.aurora.gui.niffy;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 25.05.13
 * Time: 15:16
 */
public class ProgressBarControl implements Controller {
    private Element progressBarElement;
    private Element progressTextElement;

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties properties, Attributes attributes) {
        progressBarElement = element.findElementByName("progress");
        progressTextElement = element.findElementByName("progress-text");
    }

    @Override
    public void init(Properties properties, Attributes attributes) {
    }

    @Override
    public boolean inputEvent(NiftyInputEvent niftyInputEvent) {
        return false;
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onFocus(boolean b) {
    }

    public void setProgress(final float progressValue) {
        float progress = progressValue;
        if (progress < 0.0f) {
            progress = 0.0f;
        } else if (progress > 1.0f) {
            progress = 1.0f;
        }
        final int MIN_WIDTH = 32;
        int pixelWidth = (int) (MIN_WIDTH + (progressBarElement.getParent().getWidth() - MIN_WIDTH) * progress);
        progressBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
        progressBarElement.getParent().layoutElements();

        String progressText = String.format("%3.0f%%", progress * 100);
        progressTextElement.getRenderer(TextRenderer.class).setText(progressText);
    }
}