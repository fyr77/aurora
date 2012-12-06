/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 06.12.12
 * Time: 16:16
 */
package ru.game.aurora.player.research;

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
public class Geodata {
    private int raw = 0;

    private int processed = 0;

    public void addRawData(int amount) {
        raw += amount;
    }

    public void process(int amount) {
        int realAmount = Math.min(amount, raw);
        raw -= realAmount;
        processed += realAmount;
    }

    public int dumpAndGetVictoryPoints() {
        int result = raw + 2 * processed;
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
