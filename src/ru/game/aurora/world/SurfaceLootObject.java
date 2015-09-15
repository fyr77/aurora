package ru.game.aurora.world;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.planet.InventoryItem;
import ru.game.aurora.world.planet.LandingParty;

public class SurfaceLootObject extends BaseGameObject {

    private static final long serialVersionUID = 1L;
    
    protected boolean pickedUp = false;
    
    public SurfaceLootObject(int x, int y, String spriteName) {
        super(x, y, spriteName);
    }

    public SurfaceLootObject(int x, int y) {
        super(x, y);
    }
    
    @Override
    public boolean interact(World world) {
        return interact(world, null, getLootItem());
    }

    @Override
    public String getInteractMessage() {
        return Localization.getText("gui", "surface.pick_up");
    }
    
    protected boolean interact(World world, String pickUpMessage, InventoryItem lootItem) {
        if(!checkInteractOverweight()) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "surface.overweight"));
            return false;
        }
        
        if(pickUpMessage == null) {
            pickUpMessage = String.format(Localization.getText("gui", "surface.picked_up"), getName()); //default pick up message
        }
        
        GameLogger.getInstance().logMessage(pickUpMessage);
        world.getPlayer().getLandingParty().pickUp(lootItem, 1);
        pickedUp = true;

        return true;
    }
    
    protected boolean checkInteractOverweight() {
        LandingParty party = World.getWorld().getPlayer().getLandingParty();
        return getInteractWeightChange() <= (party.getMaxWeight() - party.getInventoryWeight());
    }
    
    protected int getInteractWeightChange() {
        return 1;
    }
    
    protected InventoryItem getLootItem() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isAlive() {
        return !pickedUp;
    }
}
