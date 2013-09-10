/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.12.12
 * Time: 12:59
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.ui.ListWithIconAndDescrScreen;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;


public class LandingPartyEquipScreen extends ListWithIconAndDescrScreen {

    private static final long serialVersionUID = 4481758557250417847L;
    /*
        Military
        Engineers
        Scientists
        Weapon
         */
    private boolean isBeforeLanding;

    public LandingPartyEquipScreen(boolean isBeforeLanding) {
        maxIdx = 4;
        this.isBeforeLanding = isBeforeLanding;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            world.setCurrentRoom(previousRoom);
            if (isBeforeLanding) {
                previousRoom.enter(world);
            }
        }

        super.update(container, world);

        final Ship ship = world.getPlayer().getShip();
        final LandingParty party = world.getPlayer().getLandingParty();

        final int maxCapacity = 10;


        if (container.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            switch (currentIdx) {
                case 0:
                    if (party.getMilitary() < ship.getMilitary() && party.getTotalMembers() < maxCapacity) {
                        party.setMilitary(party.getMilitary() + 1);
                    }
                    break;
                case 1:
                    if (party.getEngineers() < ship.getEngineers() && party.getTotalMembers() < maxCapacity) {
                        party.setEngineers(party.getEngineers() + 1);
                    }
                    break;
                case 2:
                    if (party.getScience() < ship.getScientists() && party.getTotalMembers() < maxCapacity) {
                        party.setScience(party.getScience() + 1);
                    }
                    break;
            }
        }

        if (container.getInput().isKeyPressed(Input.KEY_LEFT)) {
            switch (currentIdx) {
                case 0:
                    if (party.getMilitary() > 0 && party.getTotalMembers() > 1) {
                        party.setMilitary(party.getMilitary() - 1);
                    }
                    break;
                case 1:
                    if (party.getEngineers() > 0 && party.getTotalMembers() > 1) {
                        party.setEngineers(party.getEngineers() - 1);
                    }
                    break;
                case 2:
                    if (party.getScience() > 0 && party.getTotalMembers() > 1) {
                        party.setScience(party.getScience() - 1);
                    }
                    break;
            }
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {


        strings.clear();
        LandingParty party = world.getPlayer().getLandingParty();
        if (party == null) {
            party = new LandingParty(0, 0, ResourceManager.getInstance().getLandingPartyWeapons().getEntity("assault"), 1, 1, 1);
            world.getPlayer().setLandingParty(party);
        }
        strings.add("Military: " + party.getMilitary());
        strings.add("Engineers: " + party.getEngineers());
        strings.add("Scientist: " + party.getScience());
        strings.add("Weapons: " + party.getWeapon().getName());

        String text = null;
        String icon = null;

        switch (currentIdx) {
            case 0:
                text = "Military personnel is responsible for protecting other crew members from hostile animals and aliens and is most useful in combat";
                break;
            case 1:
                text = "Engineers are responsible for controlling mining and excavation equipment";
                break;
            case 2:
                text = "Scientists are required for collecting samples and conducting field research";
                break;
            case 3:
                text = party.getWeapon().getName();
                break;
        }

        draw(graphics, camera, "Landing party status", icon, text);
    }

}
