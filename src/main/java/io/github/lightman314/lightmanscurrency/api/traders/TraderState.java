package io.github.lightman314.lightmanscurrency.api.traders;

public enum TraderState {

    /**
     * Normal Trader Status<br>
     * Means that the trader is still placed within the world, and should be removed if the block is missing/destroyed
     */
    NORMAL(true,true, false),
    /**
     * Item Trader Status<br>
     * Means that the trader was picked up by a player as an item, and should be hidden from the terminal unless the item with the relevant data was destroyed
     */
    HELD_AS_ITEM(false,false, true),
    /**
     * Removed Trader Status<br>
     * Means that the trader was destroyed by an admin, and they explicitly requested that the Trader Data not be deleted<br>
     * Used to create something similar to a Persistent Trader without saving it to a json file or forcing creative mode
     */
    ADMIN_HELD_AS_ITEM(false,true, true),
    /**
     * Mod Compat Trader Status<br>
     * Means that the trader is still within the world, but not in a way that my mod can properly detect<br>
     * Any mods that manually trigger this state are responsible for deleting &amp; ejecting the traders data if the machine (i.e. Create Contraption) that contains the block is destroyed
     */
    MOVED_BY_MACHINE(false,true, true),
    /**
     * Ejected Status<br>
     * Means that the trader was broken and/or found missing, but can be recovered via the {@link io.github.lightman314.lightmanscurrency.common.menus.EjectionRecoveryMenu EjectionRecoveryMenu} and/or the <code>/lcadmin traderdata recover</code> command
     */
    EJECTED(false,false, true),
    /**
     * Persistent Trader Status<br>
     * Means that this trade was loaded from the <code>PersistentTraders.json</code> config file<br>
     * Traders in this state cannot have their state changed via {@link TraderData#setState}
     */
    PERSISTENT(false,true, false);


    public final boolean validateWorldPosition;
    public final boolean allowAccess;
    public final boolean allowRecovery;
    TraderState(boolean validateWorldPosition, boolean allowAccess, boolean allowRecovery) {
        this.validateWorldPosition = validateWorldPosition;
        this.allowAccess = allowAccess;
        this.allowRecovery = allowRecovery;
    }

}
