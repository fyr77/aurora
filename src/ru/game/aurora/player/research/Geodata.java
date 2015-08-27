/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:16
 */
package ru.game.aurora.player.research;

import ru.game.aurora.world.World;

import java.io.Serializable;

/**
 * Contains info about current geodata research.
 * There are 'raw' and 'processed' geodata.
 * <p/>
 * Raw geodata is data from sensors received while scanning and scouting planets. Each explored planet tile add 1 point to raw geodata.
 * Processed geodata is result of raw data processing - such as maps, cataloged probing results, some outputs created from that.
 * <p/>
 * Raw geodata is converted to processed by scientists performing 'Cartography' task.
 * Processed geodata gives more Research points when returning science results to Earth.
 */
public class Geodata implements Serializable {
    private static final long serialVersionUID = 8595341321476584280L;

    private int raw = 0;

    private int processed = 0;

    public static double getAmountMultiplier(World world) {
        return Double.parseDouble((String) world.getGlobalVariable("geodataResearchMultiplier", "1.0"));
    }

    public static double getPriceMultiplier(World world) {
        return Double.parseDouble((String) world.getGlobalVariable("geodataPriceMultiplier", "1.0"));
    }

    public void addRawData(int amount) {
        raw += amount;
    }

    public void process(int amount) {
        int realAmount = Math.min(amount, raw);
        raw -= realAmount;
        processed += realAmount;
    }

    public int dumpAndGetVictoryPoints(World world) {
        int result = (int) ((raw + 2 * processed) * getAmountMultiplier(world));
        raw = 0;
        processed = 0;
        return result;
    }

    public int getRaw() {
        return raw;
    }

    public int getProcessed() {
        return processed;
    }
}
