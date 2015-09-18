package ru.game.aurora.gui;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import ru.game.aurora.world.IStateChangeListener;

/**
 * Screen that turns to black
 */
public class FadeOutScreenController implements ScreenController {
    private static IStateChangeListener listener;
    private Element panel;

    public static void makeFade(IStateChangeListener listener) {
        GUI.getInstance().pushCurrentScreen();
        FadeOutScreenController.listener = listener;
        GUI.getInstance().getNifty().gotoScreen("fade_out_screen");
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        panel = screen.findElementByName("fadePanel");
    }

    @Override
    public void onStartScreen() {
        panel.startEffect(EffectEventId.onCustom, new EndNotify() {
            @Override
            public void perform() {
                panel.startEffect(EffectEventId.onCustom, new EndNotify() {
                    @Override
                    public void perform() {
                        GUI.getInstance().popAndSetScreen();
                        if (listener != null) {
                            listener.stateChanged(null);
                        }
                    }
                }, "fadeIn");
            }
        }, "fadeOut");
    }

    @Override
    public void onEndScreen() {

    }
}
