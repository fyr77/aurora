package ru.game.aurora.player.research.projects;

import ru.game.aurora.effects.BigMommyShotEffect;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.research.ResearchProjectDesc;
import ru.game.aurora.player.research.ResearchReport;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.StarSystem;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 22.03.13
 * Time: 12:14
 */
public class NuclearWeaponTestProject extends ResearchProjectDesc
{
    private static final long serialVersionUID = 4529206689105938478L;

    private BigMommyShotEffect effect;

    public NuclearWeaponTestProject() {
        super("'Big Mommy' weapons test", "Test of the most powerful weapon humanity can create against Obliterator. If even this enormous bomb, that can " +
                "crack Earth into pieces, won't be able to do any major damage, then Earth is completely defenceless against this threat.", "weapons_test");

        setReport(new ResearchReport(
                "obliterator_background",
                "Our most powerful weapon failed to inflict any major damage to alien structure. In fact, it failed to inflict any damage at all. As if it were just a flashlight, and not the devastating weapon."
        ));
    }

    private boolean isInRightSystem(World world)
    {
        StarSystem ss = world.getCurrentStarSystem();
        return ss != null && ss.getVariables().containsKey("quest.main.obliterator");
    }

    @Override
    public void update(World world, int scientists) {
        if (isCompleted()) {
            return;
        }
        StarSystem ss = world.getCurrentStarSystem();
        if (!isInRightSystem(world)) {
            return;
        }

        // calculate background location with Obliterator sprite
        int x = (int) ss.getBackground().getXCoordPoint(world.getCamera(), 200, -1);
        int y = (int) ss.getBackground().getYCoordPoint(world.getCamera(), 100, -1);
        effect = new BigMommyShotEffect(world.getPlayer().getShip(), x, y, world.getCamera(), 100);
        ss.addEffect(effect);



        world.getGlobalVariables().put("quest.main.weapon_test_done", null);
        world.getGlobalVariables().put("earth.special_dialog", "dialogs/quest/main/weapon_test_report.json");
        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(
                "Leaked information - nuclear tests in space!"
                , "Breaking news! According to information provided by our private source in UNS, Aurora project ships are loaded with" +
                " powerful nuclear weapons. Possibly, they are conducting some kind of weapon tests in space, which directly violates Clean Space Act, signed by all UNS participants. \n Aurora " +
                "project representatives have not made any comments on the situation yet."
                , "news"
        ));
    }

    @Override
    public String getStatusString(World world, int scientists) {
        if (!isInRightSystem(world)) {
            return "Should be within same star system as Obliterator";
        }
        return "Done";
    }

    @Override
    public boolean isCompleted() {
        return effect != null && effect.isOver();
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public int getScore() {
        return 10;
    }
}
