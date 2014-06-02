/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 04.12.12
 * Time: 13:09
 */
package ru.game.aurora.world.planet;

/**
 * Types for planet atmosphere
 */
public enum PlanetAtmosphere {
    /**
     * No atmosphere, oxygen is spent by landing party, increased chance of party members dying on attack due to suit breaches
     */
    NO_ATMOSPHERE {
        public String descriptionKey() {
            return "no_atmosphere";
        }
    }
    /**
     * Passive atmosphere. Oxygen is spent, but no negative penalties applied to wounded.
     */
    , PASSIVE_ATMOSPHERE {
        public String descriptionKey() {
            return "passive";
        }
    }

    /**
     * Breathable earth-like atmosphere. Oxygen is not spent, no negative penalties
     */
    , BREATHABLE_ATMOSPHERE {
        public String descriptionKey() {
            return "breathable";
        }
    }

    /**
     * Aggressive atmosphere. Oxygen is spent by landing party, increased chance of party members dying on attack due to suit breaches.
     */
    , AGGRESSIVE_ATMOSPHERE {
        public String descriptionKey() {
            return "aggressive";
        }
    };

    public abstract String descriptionKey();
}
