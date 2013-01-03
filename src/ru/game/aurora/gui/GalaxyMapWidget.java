/**
 * User: jedi-philosopher
 * Date: 03.01.13
 * Time: 16:45
 */
package ru.game.aurora.gui;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;
import ru.game.aurora.player.research.ResearchScreen;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.GalaxyMapScreen;

public class GalaxyMapWidget extends Widget {
    private Button mapButton;

    private Button researchButton;

    private World world;

    private final GalaxyMapScreen galaxyMapScreen = new GalaxyMapScreen();

    public GalaxyMapWidget(final World world) {
        setTheme("");
        this.world = world;
        mapButton = new Button("View Map");
        mapButton.setTheme("button");
        mapButton.setPosition(800, 30);
        mapButton.setFocusKeyEnabled(false);
        mapButton.addCallback(new Runnable() {
            @Override
            public void run() {
                world.setCurrentRoom(galaxyMapScreen);
                galaxyMapScreen.enter(world);
            }
        });
        add(mapButton);


        researchButton = new Button("Research Screen");
        researchButton.setTheme("button");
        researchButton.setPosition(800, 60);
        researchButton.setFocusKeyEnabled(false);
        researchButton.addCallback(new Runnable() {
            @Override
            public void run() {
                ResearchScreen rs = new ResearchScreen();
                world.setCurrentRoom(rs);
                rs.enter(world);
            }
        });
        add(researchButton);

    }

    @Override
    protected void layout() {
        super.layout();
        mapButton.adjustSize();
        researchButton.adjustSize();
    }
}
