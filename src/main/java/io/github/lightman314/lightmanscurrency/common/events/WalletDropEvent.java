package io.github.lightman314.lightmanscurrency.common.events;

import java.util.Collection;

import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class WalletDropEvent extends PlayerEvent{

	private final IWalletHandler walletHandler;
	private final ItemStack walletStack;
	public ItemStack getWalletStack() { return this.walletHandler == null ? this.walletStack : this.walletHandler.getWallet(); }
	public final DamageSource source;
	private Collection<ItemEntity> walletDrops;
	public Collection<ItemEntity> getDrops() { return this.walletDrops; }
	public void setDrops(Collection<ItemEntity> drops) { this.walletDrops = drops; }
	public final boolean keepWallet;
	public final int coinDropPercent;
	
	public WalletDropEvent(PlayerEntity player, IWalletHandler walletHandler, DamageSource source, Collection<ItemEntity> walletDrops, boolean keepWallet, int coinDropPercent)
	{
		super(player);
		this.walletHandler = walletHandler;
		this.walletStack = null;
		this.source = source;
		this.walletDrops = walletDrops;
		this.keepWallet = keepWallet;
		this.coinDropPercent = coinDropPercent;
	}
	
	public WalletDropEvent(PlayerEntity player, ItemStack walletStack, DamageSource source, Collection<ItemEntity> walletDrops, boolean keepWallet, int coinDropPercent)
	{
		super(player);
		this.walletHandler = null;
		this.walletStack = walletStack;
		this.source = source;
		this.walletDrops = walletDrops;
		this.keepWallet = keepWallet;
		this.coinDropPercent = coinDropPercent;
	}
	
}
