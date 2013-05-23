/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 17:03
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;

public class GUI
{
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
       /*
        Screen mainScreen = new ScreenBuilder("main", this) {{
            layer(new LayerBuilder("layer") {{
                childLayoutCenter();
                panel(new PanelBuilder() {{
                    height(percentage(40));
                }
                });
                panel(new PanelBuilder() {{
                    id("panel");
                    childLayoutVertical();
                    height(percentage(25));
                    width(percentage(80));
                    control(new ButtonBuilder("continue_button", "Continue") {{

                        interactOnClick("continueGame()");
                    }});
                    control(new ButtonBuilder("new_game_button", "New Game") {{
                        interactOnClick("newGame()");
                    }});
                    control(new ButtonBuilder("exit_button", "Exit") {{
                        interactOnClick("exitGame()");
                    }});
                }});
            }});
        }}.build(nifty);


        nifty.addScreen("mainScreen", mainScreen);
        nifty.gotoScreen("mainScreen");
        nifty.getCurrentScreen().findElementByName("panel").findElementByName("continue_button").disable();
         */
    }

    public static GUI getInstance() {
        return instance;
    }

    public void addScreen(Screen screen)
    {
        nifty.addScreen(screen.getScreenId(), screen);
    }

    public void setScreen(String id)
    {
        nifty.gotoScreen(id);
    }
}
