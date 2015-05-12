package ru.game.aurora.world;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores reputation with races
 */
public class Reputation implements Serializable
{

    private static class RepKey implements Serializable
    {
        private static final long serialVersionUID = -2140654393593028053L;

        final String raceFrom;

        final String raceTo;

        private RepKey(String raceFrom, String raceTo) {
            this.raceFrom = raceFrom;
            this.raceTo = raceTo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RepKey repKey = (RepKey) o;

            return !(raceFrom != null ? !raceFrom.equals(repKey.raceFrom) : repKey.raceFrom != null) && !(raceTo != null ? !raceTo.equals(repKey.raceTo) : repKey.raceTo != null);

        }

        @Override
        public int hashCode() {
            int result = raceFrom != null ? raceFrom.hashCode() : 0;
            result = 31 * result + (raceTo != null ? raceTo.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "RepKey{" +
                    "raceFrom='" + raceFrom + '\'' +
                    ", raceTo='" + raceTo + '\'' +
                    '}';
        }
    }

    private static final long serialVersionUID = 1L;

    private final Map<RepKey, Integer> entries = new HashMap<>();

    public static final int HOSTILE_REPUTATION = 0;

    public static final int RIVAL_REPUTATION = 2;

    public static final int NEUTRAL_REPUTATION = 4;

    public static final int FRIENDLY_REPUTATION = 7;

    public static final int DEFAULT_REPUTATION = 5;


    public void setReputation(String name, String name2, int value)
    {
        if (name2.equals(HumanityGenerator.NAME)) {
            final int oldRep = getReputation(name, name2);
            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "logging.relation_changed"), name, oldRep, value));
        }

        entries.put(new RepKey(name, name2), value);
    }

    public int getReputation(String name, String name2)
    {
        if (name.equals(name2)) {
            return FRIENDLY_REPUTATION;
        }
        Integer i = entries.get(new RepKey(name, name2));
        return i != null ? i : DEFAULT_REPUTATION;
    }

    public void updateReputation(String name, String name2, int delta)
    {
        final int oldRep = getReputation(name, name2);
        final int newRep = oldRep + delta;
        setReputation(name, name2, newRep);
    }

    public boolean isHostile(String name, String name2)
    {
        return getReputation(name, name2) < RIVAL_REPUTATION;
    }

    public boolean isRival(String name, String name2)
    {
        int rep = getReputation(name, name2);
        return rep >= RIVAL_REPUTATION && rep < NEUTRAL_REPUTATION;
    }

    public boolean isNeutral(String name, String name2)
    {
        int rep = getReputation(name, name2);
        return rep >= NEUTRAL_REPUTATION && rep < FRIENDLY_REPUTATION;
    }

    public boolean isFriendly(String name, String name2)
    {
        return getReputation(name, name2) >= FRIENDLY_REPUTATION;
    }

    public void setHostile(String name, String name2)
    {
        setReputation(name, name2, HOSTILE_REPUTATION);
        setReputation(name2, name, HOSTILE_REPUTATION);
    }

    public Reputation copy()
    {
        Reputation rep = new Reputation();
        rep.entries.putAll(entries);
        return rep;
    }

    public void merge(Reputation other)
    {
        for (Map.Entry<RepKey, Integer> e : other.entries.entrySet()) {
            Integer val = entries.get(e.getKey());
            if (val == null) {
                val = DEFAULT_REPUTATION;
            }
            entries.put(e.getKey(), val + e.getValue() / 2);
        }
    }
}
