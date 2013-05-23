package ru.game.aurora.player.earth;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.ui.ListWithIconAndDescrScreen;
import ru.game.aurora.world.World;

import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.03.13
 * Time: 14:51
 */

public class EarthScreen extends ListWithIconAndDescrScreen
{
    private static final long serialVersionUID = 8371537687377256327L;

    private EarthState state;

    private static final int PM_TAB = 0;

    private static final int UPGRADE_TAB = 1;

    private int currentTab;

    private static final String[] tabs = {"Messages", "Shipyard"};

    @Override
    public void enter(World world) {
        super.enter(world);
        state = world.getPlayer().getEarthState();
    }

    @Override
    public Screen getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (currentTab == PM_TAB) {
            maxIdx = state.getMessages().size();
        }

        if (container.getInput().isKeyPressed(Input.KEY_TAB)) {
            currentTab++;
            if (currentTab > UPGRADE_TAB) {
                currentTab = 0;
            }
        }

        super.update(container, world);
    }

    private void drawPMTab(Graphics graphics, Camera camera)
    {
        strings.clear();
        // iterate in reverse order
        for (ListIterator<PrivateMessage> iter = state.getMessages().listIterator(state.getMessages().size()); iter.hasPrevious();) {
            PrivateMessage res = iter.previous();
            strings.add(res.getName());
        }

        String icon = null;
        String descr = null;
        if (!state.getMessages().isEmpty()) {
            PrivateMessage pm = state.getMessages().get(state.getMessages().size() - 1 - currentIdx);
            icon = pm.getIcon();
            descr = pm.getText();
        }

        drawTabbed(graphics, camera, tabs, currentTab, icon, descr);
    }

    private void drawShipyardTab(Graphics graphics, Camera camera)
    {
        strings.clear();
        drawTabbed(graphics, camera, tabs, currentTab, null, "");
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        switch (currentTab) {
            case PM_TAB:
                drawPMTab(graphics, camera);
                break;
            case UPGRADE_TAB:
                drawShipyardTab(graphics, camera);
                break;
        }

        GameLogger.getInstance().addStatusMessage("Up/down to select item");
        GameLogger.getInstance().addStatusMessage("TAB to switch tabs");
    }
}
