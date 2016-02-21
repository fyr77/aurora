package ru.game.aurora.steam;

import com.codedisaster.steamworks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by User on 21.02.2016.
 * Handles all interaction with Steam Achievements API
 */
public class AchievementManager implements SteamUserStatsCallback {

    private final long appId;

    private SteamUserStats statsAPI;

    private volatile boolean isInitialized = false;

    private static final Logger logger = LoggerFactory.getLogger(AchievementManager.class);

    private static AchievementManager instance;

    public static AchievementManager getInstance() {
        return instance;
    }

    public static void init(long appId) {
        logger.info("Creating AchievementManager");
        instance = new AchievementManager(appId);
    }

    public static void term() {
        logger.info("Terminating AchievementManager");
        instance.statsAPI.dispose();
    }

    private AchievementManager(long appId) {
        this.appId = appId;
        requestStats();
    }

    /**
     * Loads statistics from Steam servers
     */
    private void requestStats() {
        if (!SteamAPI.isSteamRunning()) {
            logger.warn("Steam API is not running, can not request stats");
            return;
        }

        logger.info("Requesting stats from steam");
        statsAPI = new SteamUserStats(this);
        statsAPI.requestCurrentStats();
    }

    /**
     * Wipes all data and achievements
     */
    public void resetStats() {
        logger.warn("Resetting Steam stats!!!");
        statsAPI.resetAllStats(true);
    }

    public void achievementUnlocked(String name) {
        if (!isInitialized) {
            logger.warn("Steam API not initialized, can not record unlock of achievement " + name);
            return;
        }
        logger.info("Unlocking achievement " + name);
        statsAPI.setAchievement(name);

        logger.info("Sending stats to server");
        statsAPI.storeStats();
    }

    /////////////// callbacks for steam api ///////////////////

    @Override
    public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
        if (gameId != this.appId) {
            // according to documentation we may receive stats for other games too, ignore it
            return;
        }
        logger.info("Steam user stats received");
        isInitialized = true;
    }

    @Override
    public void onUserStatsStored(long gameId, SteamResult result) {
        if (gameId != this.appId) {
            return;
        }
    }

    @Override
    public void onUserStatsUnloaded(SteamID steamIDUser) {

    }

    @Override
    public void onUserAchievementStored(long gameId, boolean isGroupAchievement, String achievementName, int curProgress, int maxProgress) {
        if (gameId != this.appId) {
            return;
        }
        logger.info("onUserAchievementStored called for " + achievementName);
    }

    @Override
    public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {

    }

    @Override
    public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries) {

    }

    @Override
    public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) {

    }
}
