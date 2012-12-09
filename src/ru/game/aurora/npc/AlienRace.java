/**
 * User: jedi-philosopher
 * Date: 09.12.12
 * Time: 18:34
 */
package ru.game.aurora.npc;

public class AlienRace
{
    private String name;

    /**
     * 0-3 - hate: will attack on sight
     * 4-6 - dislike: will not attack, but also will not communicate
     * 7-9 - neutral: will communicate, can occasionally help
     * 10-12 - like: will help and easily share information
     */
    private int relationToPlayer;
}
