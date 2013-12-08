package ru.game.aurora.tools;

import ru.game.aurora.tools.intro.IntroDialogEditorWindow;
import ru.game.aurora.tools.workspace.AuroraWorkspace;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 05.12.13
 * Time: 17:18
 */
public class AuroraToolsMain {
    public static void main(String[] args) {
        new IntroDialogEditorWindow(new AuroraWorkspace("")).setVisible(true);
    }
}
