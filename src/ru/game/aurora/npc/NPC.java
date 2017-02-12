/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:39
 */
package ru.game.aurora.npc;

import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.music.MusicDialogListener;

import java.io.Serializable;

/**
 * Class for NPCs: unique starship capitans, world rulers etc
 */
public class NPC implements Serializable
{
    private static final long serialVersionUID = -6874203345072978912L;

    private final Dialog customDialog;

    private String customMusicId;
    private MusicDialogListener musicListener;

    public NPC(Dialog customDialog) {
        this.customDialog = customDialog;
    }

    public Dialog getCustomDialog() {
        return customDialog;
    }

    public void setCustomPlaylist(String customMusicId){
        this.customMusicId = customMusicId;
    }

    public String getCustomMusicId(){
        return this.customMusicId;
    }

    public void addMusicListener(MusicDialogListener musicListener){
        this.musicListener = musicListener;
    }

    public MusicDialogListener getMusicListener() {
        return this.musicListener;
    }
}
