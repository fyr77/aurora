/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:39
 */
package ru.game.aurora.npc;

import ru.game.aurora.dialog.Dialog;

import java.io.Serializable;

/**
 * Class for NPCs: unique starship capitans, world rulers etc
 */
public class NPC implements Serializable
{
    private static final long serialVersionUID = -6874203345072978912L;

    private final Dialog customDialog;

    public NPC(Dialog customDialog) {
        this.customDialog = customDialog;
    }

    public Dialog getCustomDialog() {
        return customDialog;
    }
}
