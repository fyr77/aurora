/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 08.06.13
 * Time: 22:27
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.WindowClosedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.render.RenderFont;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchState;
import ru.game.aurora.world.World;


public class EarthProgressScreenController implements ScreenController {
    private World world;

    private ListBox results;

    private Element total;

    public EarthProgressScreenController(World world) {
        this.world = world;
    }

    /**
     * Given 2 strings returns a combined string like 'text1.........text2', filling dots to a fixed width in pixels
     */
    private String getLine(String text1, String text2) {
        final RenderFont font = results.getElement().getRenderer(TextRenderer.class).getFont();
        int text1Width = font.getWidth(text1);
        int text2Width = font.getWidth(text2);
        int dotWidth = font.getWidth(".");

        int totalTextWidth = results.getWidth();

        int dotCount = (totalTextWidth - text1Width - text2Width) / dotWidth;

        StringBuilder sb = new StringBuilder(text1);
        for (int i = 0; i < dotCount; ++i) {
            sb.append('.');
        }
        sb.append(text2);
        return sb.toString();
    }


    @Override
    public void bind(Nifty nifty, Screen screen) {
        results = screen.findNiftyControl("itemsList", ListBox.class);
        total = screen.findElementByName("totalText");

    }

    @Override
    public void onStartScreen() {
        results.clear();
        int totalScore = 0;
        final ResearchState researchState = world.getPlayer().getResearchState();

        int i = researchState.getGeodata().getRaw();
        totalScore += i;
        results.addItem(getLine("Raw geodata", String.valueOf(i)));

        i = researchState.getGeodata().getProcessed() * 2;
        totalScore += i;
        results.addItem(getLine("Processed geodata", String.valueOf(i)));

        results.addItem(getLine("Astro data", String.valueOf(researchState.getProcessedAstroData())));


        for (ResearchProjectDesc desc : researchState.getCompletedProjects()) {
            totalScore += desc.getScore();
            results.addItem(getLine(desc.getName(), String.valueOf(desc.getScore())));
        }

        final int lostCrewMembers = world.getPlayer().getShip().getLostCrewMembers();

        if (lostCrewMembers > 0) {
            results.addItem(getLine("Crew members lost", String.valueOf(lostCrewMembers) + " (" + String.valueOf(-10 * lostCrewMembers) + ")"));
            totalScore -= 10 * lostCrewMembers;
        }

        total.getRenderer(TextRenderer.class).setText("Total: " + totalScore);
    }

    @Override
    public void onEndScreen() {

    }

    @NiftyEventSubscriber(id = "earth_progress_window")
    public void onCloseWindow(String id, WindowClosedEvent event) {
        GUI.getInstance().popAndSetScreen();
    }

    public void closeScreen() {
        GUI.getInstance().popAndSetScreen();
    }
}
