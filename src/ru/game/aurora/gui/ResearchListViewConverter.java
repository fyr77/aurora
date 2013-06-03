/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 03.06.13
 * Time: 16:42
 */
package ru.game.aurora.gui;


import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchProjectState;

public class ResearchListViewConverter implements ListBox.ListBoxViewConverter
{
    private String getText(Object obj)
    {
        ResearchProjectDesc researchProjectDesc;
        if (ResearchProjectDesc.class.isAssignableFrom(obj.getClass())) {
            return ((ResearchProjectDesc) obj).getName();
        } else if (ResearchProjectState.class.isAssignableFrom(obj.getClass())) {
            ResearchProjectState rps = (ResearchProjectState) obj;
            return rps.desc.getName() + ", " + rps.scientists + " scientists";
        } else {
            throw new IllegalStateException("research screen can not show research item of class " + obj.getClass());
        }

    }

    @Override
    public void display(Element listBoxItem, Object obj) {
        Label l = listBoxItem.findNiftyControl("#name_text", Label.class);
        l.setText(getText(obj));
    }

    @Override
    public int getWidth(Element element, Object item) {
        final Element text = element.findElementByName("#name_text");
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        return 64 + ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(getText(item)));
    }
}
