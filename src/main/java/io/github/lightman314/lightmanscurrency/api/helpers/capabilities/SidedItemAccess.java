package io.github.lightman314.lightmanscurrency.api.helpers.capabilities;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;

/**
 * Set of {@link ItemAccess} and {@link ISidedContext} for use with side-sensitive Item Capabilities<br>
 * Implements all methods included within the {@linkplain ItemAccess} interface, but cannot directly implement due to conflicting
 */
public record SidedItemAccess(ItemAccess access, ISidedContext context) implements ItemAccess, ISidedContext {

    @Override
    public boolean isClient() { return this.context.isClient(); }

    public static SidedItemAccess forPlayerInteraction(Player player, InteractionHand hand, ISidedContext context) { return new SidedItemAccess(ItemAccess.forPlayerInteraction(player,hand),context); }
    public static SidedItemAccess forInfiniteMaterials(Player player, ItemStack contents, ISidedContext context) { return new SidedItemAccess(ItemAccess.forInfiniteMaterials(player,contents),context); }
    public static SidedItemAccess forPlayerCursor(Player player, AbstractContainerMenu menu, ISidedContext context) { return new SidedItemAccess(ItemAccess.forPlayerCursor(player,menu),context); }
    public static SidedItemAccess forPlayerSlot(Player player,int slot,ISidedContext context) { return new SidedItemAccess(ItemAccess.forPlayerSlot(player,slot),context); }
    public static SidedItemAccess forHandlerIndex(ResourceHandler<ItemResource> handler,int index,ISidedContext context) { return new SidedItemAccess(ItemAccess.forHandlerIndex(handler,index),context); }
    public static SidedItemAccess forHandlerIndexStrict(ResourceHandler<ItemResource> handler,int index,ISidedContext context) { return new SidedItemAccess(ItemAccess.forHandlerIndexStrict(handler,index),context); }
    public static SidedItemAccess forStack(ItemStack stack,ISidedContext context) { return new SidedItemAccess(ItemAccess.forStack(stack),context); }

    @Nullable
    public <T> T getSidedCapability(ItemCapability<T,SidedItemAccess> capability) { return this.access.getResource().toStack().getCapability(capability,this); }

    @Override
    public ItemResource getResource() { return this.access.getResource(); }
    @Override
    public int getAmount() { return this.access.getAmount(); }
    @Override
    public int insert(ItemResource resource,int amount,TransactionContext transaction) { return this.access.insert(resource,amount,transaction); }
    @Override
    public int extract(ItemResource resource,int amount,TransactionContext transaction) { return this.access.extract(resource,amount,transaction); }

}