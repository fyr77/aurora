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
import ru.game.aurora.util.Pair;
import ru.game.aurora.world.World;

import java.util.LinkedList;
import java.util.List;


public class EarthProgressScreenController implements ScreenController {
    private final World world;

    private ListBox results;

    private Element total;

    private RenderFont listFont;

    private final List<Pair<String, String>> data = new LinkedList<>();

    private int totalScore;

    private Element myWindow;

    public EarthProgressScreenController(World world) {
        this.world = world;
    }

    /**
     * Given 2 strings returns a combined string like 'text1.........text2', filling dots to a fixed width in pixels
     */
    private String getLine(String text1, String text2) {
        int text2Width = listFont.getWidth(text2);

        StringBuilder sb = new StringBuilder(" ");
        sb.append(text1);
        while (listFont.getWidth(sb.toString()) + text2Width + 20 < results.getElement().getWidth()) {
            sb.append('.');
        }
        sb.append(text2);
        return sb.toString();
    }


    @Override
    public void bind(Nifty nifty, Screen screen) {
        results = screen.findNiftyControl("itemsList", ListBox.class);
        total = screen.findElementByName("totalText");
        listFont = results.getElement().findElementByName("#child-root").getElements().get(0).getRenderer(TextRenderer.class).getFont();
        myWindow = screen.findElementByName("earth_progress_window");
    }

    public void updateStats() {
        data.clear();
        totalScore = 0;
        final ResearchState researchState = world.getPlayer().getResearchState();

        int i = researchState.getGeodata().getRaw();
        totalScore += i;
        data.add(new Pair<>("Raw geodata", String.valueOf(i)));

        i = researchState.getGeodata().getProcessed() * 2;
        totalScore += i;
        data.add(new Pair<>("Processed geodata", String.valueOf(i)));

        data.add(new Pair<>("Astro data", String.valueOf(researchState.getProcessedAstroData())));
        totalScore += researchState.getProcessedAstroData();

        for (ResearchProjectDesc desc : researchState.getCompletedProjects()) {
            totalScore += desc.getScore();
            data.add(new Pair<>(desc.getName(), String.valueOf(desc.getScore())));
        }

        final int lostCrewMembers = world.getPlayer().getShip().getLostCrewMembers();

        if (lostCrewMembers > 0) {
            data.add(new Pair<>("Crew members lost", String.valueOf(lostCrewMembers) + " (" + String.valueOf(-10 * lostCrewMembers) + ")"));
            totalScore -= 10 * lostCrewMembers;
        }

    }

    @Override
    public void onStartScreen() {
        myWindow.setVisible(true);
        total.getRenderer(TextRenderer.class).setText("Total: " + totalScore);
        results.clear();
        for (Pair<String, String> p : data) {
            results.addItem(getLine(p.getKey(), p.getVal()));
        }
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
