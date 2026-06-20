package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class TradeCustomer {

    public static final TradeCustomer DISPLAY = new TradeCustomer(PlayerReference.NULL,CustomerType.DISPLAY);

    private final PlayerReference customer;
    private final CustomerType type;
    private TradeCustomer(PlayerReference customer,CustomerType type) {
        this.customer = customer;
        this.type = type;
    }

    public static TradeCustomer of(Player player) { return new TradeCustomer(PlayerReference.of(player),CustomerType.PLAYER); }

    public static TradeCustomer ofNPC(Entity entity) { return ofNPC(entity.getUUID(),entity.getName().getString()); }
    public static TradeCustomer ofNPC(UUID entityID,String name) { return ofNPC(PlayerReference.of(entityID,name)); }
    public static TradeCustomer ofNPC(PlayerReference entity) { return new TradeCustomer(entity,CustomerType.NPC); }

    public static TradeCustomer ofMachine(OwnerHolder owner) { return ofMachine(owner.getValidOwner()); }
    public static TradeCustomer ofMachine(Owner owner) { return ofMachine(owner.asPlayerReference()); }
    public static TradeCustomer ofMachine(UUID ownerID,String name) { return ofMachine(PlayerReference.of(ownerID,name)); }
    public static TradeCustomer ofMachine(PlayerReference owner) { return new TradeCustomer(owner,CustomerType.MACHINE); }

    public PlayerReference getPlayer() { return this.customer; }

    public boolean isPlayer() { return this.type == CustomerType.PLAYER; }
    public boolean isNPC() { return this.type == CustomerType.NPC; }
    public boolean isMachine() { return this.type == CustomerType.MACHINE; }
    public boolean isDisplay() { return this.type == CustomerType.DISPLAY; }

    public enum CustomerType { PLAYER,NPC,MACHINE, DISPLAY }

}