/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 13.06.13
 * Time: 22:02
 */
package ru.game.aurora.gui;


import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.slick2d.render.image.ImageSlickRenderImage;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.world.World;

import java.util.List;
import java.util.ListIterator;

public class EarthScreenController implements ScreenController {
    private TabGroup tg;

    private World world;

    private Element messagesList;

    public EarthScreenController(World world) {
        this.world = world;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        tg = screen.findNiftyControl("research_tabs", TabGroup.class);
        messagesList = screen.findElementByName("messages_list");
    }

    @Override
    public void onStartScreen() {
        ListBox l = messagesList.findNiftyControl("itemsList", ListBox.class);
        l.clear();
        List<PrivateMessage> privateMessages = world.getPlayer().getEarthState().getMessages();
        for (ListIterator listIterator = privateMessages.listIterator(privateMessages.size()); listIterator.hasPrevious(); ) {
            l.addItem(listIterator.previous());
        }
    }

    @Override
    public void onEndScreen() {

    }

    public void closeScreen() {
        GUI.getInstance().getNifty().gotoScreen(GUI.getInstance().popScreen());
    }

    @NiftyEventSubscriber(id = "itemsList")
    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent event) {
        Element imagePanel = messagesList.findElementByName("selectedItemImg");

        TextRenderer tr = messagesList.findElementByName("selectedItemText").getRenderer(TextRenderer.class);
        if (event.getSelection().isEmpty()) {
            tr.setText("<No item selected>");
            imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage("no_image"))));
            return;
        }
        PrivateMessage pm = (PrivateMessage) event.getSelection().get(0);
        tr.setText(pm.getText());
        imagePanel.getRenderer(ImageRenderer.class).setImage(new NiftyImage(GUI.getInstance().getNifty().getRenderEngine(), new ImageSlickRenderImage(ResourceManager.getInstance().getImage(pm.getIcon()))));
        messagesList.layoutElements();
    }

}
