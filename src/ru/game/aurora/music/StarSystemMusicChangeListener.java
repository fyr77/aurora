package ru.game.aurora.music;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Faction;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Plays a race-specific musical theme when entering their homeworld starsystem
 */
public class StarSystemMusicChangeListener extends GameEventListener
{
    @Override
    public boolean onPlayerEnterStarSystem(World world, StarSystem ss) {
        for (Faction faction : world.getFactions().values()) {
            if (!(faction instanceof AlienRace)) {
                continue;
            }
            AlienRace race = (AlienRace) faction;
            if (race.getHomeworld() == ss && race.getMusic() != null) {
                race.getMusic().play();
            }
        }
        return false;
    }

    @Override
    public boolean onPlayerLeftStarSystem(World world, StarSystem ss) {
        if (!Playlist.getCurrentPlaylist().getId().equals(Playlist.DEFAULT_PLAYLIST)) {
            ResourceManager.getInstance().getPlaylist(Playlist.DEFAULT_PLAYLIST).play();
        }
        return false;
    }
}
