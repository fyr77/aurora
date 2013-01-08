/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.SimpleTextAreaModel;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.player.research.ResearchScreen;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapScreen;

public class GalaxyMapWidget extends BoxLayout {
    private Button mapButton;

    private Button researchButton;

    private SimpleTextAreaModel textAreaModel = new SimpleTextAreaModel();

    private World world;

    private final GalaxyMapScreen galaxyMapScreen = new GalaxyMapScreen();

    public GalaxyMapWidget(final World world) {
        super(Direction.VERTICAL);
        setTheme("");
        this.world = world;

        TextArea statusText = new TextArea();
        statusText.setModel(textAreaModel);
        add(statusText);


        mapButton = new Button("View Map");
        mapButton.setTheme("button");
        //  mapButton.setPosition(900, 150);
        mapButton.setFocusKeyEnabled(false);
        mapButton.setCanAcceptKeyboardFocus(false);
        mapButton.addCallback(new Runnable() {
            @Override
            public void run() {
                galaxyMapScreen.enter(world);
                world.setCurrentRoom(galaxyMapScreen);
            }
        });
        add(mapButton);


        researchButton = new Button("Research Screen");
        researchButton.setTheme("button");
        //researchButton.setPosition(900, 250);
        researchButton.setFocusKeyEnabled(false);
        researchButton.setCanAcceptKeyboardFocus(false);
        researchButton.addCallback(new Runnable() {
            @Override
            public void run() {
                ResearchScreen rs = new ResearchScreen();
                rs.enter(world);
                world.setCurrentRoom(rs);
            }
        });
        add(researchButton);
    }

    @Override
    protected void layout() {
        super.layout();

        final Rectangle sidePanelRect = GUI.getInstance().getSidePanelRect();
        setPosition((int) sidePanelRect.getX(), (int) sidePanelRect.getY());
        setSize((int) sidePanelRect.getWidth(), (int) sidePanelRect.getHeight());

        mapButton.adjustSize();
        researchButton.adjustSize();
    }

    public void update() {
        StringBuilder sb = new StringBuilder("Ship status:\n");
        final Ship ship = world.getPlayer().getShip();
        sb.append("\t Scientists: ").append(ship.getScientists()).append("\n");
        sb.append("\t Engineers: ").append(ship.getEngineers()).append("\n");
        sb.append("\t Military: ").append(ship.getMilitary()).append("\n");
        sb.append("Resources: ").append(world.getPlayer().getResourceUnits()).append("\n");
        sb.append("Ship coordinates: [").append(ship.getX()).append(", ").append(ship.getY()).append("]");
        textAreaModel.setText(sb.toString());
    }
}
