package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.menus.containers.SuppliedItemContainer;
import io.github.lightman314.lightmanscurrency.integration.curios.wallet.CuriosWalletHandler;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.CPacketCreativeWalletEdit;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WalletCapability {

	@Deprecated
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}

	@Nullable
	public static IWalletHandler lazyGetWalletHandler(@Nonnull final Entity entity) {
		LazyOptional<IWalletHandler> optional = entity.getCapability(CurrencyCapabilities.WALLET);
		if(optional.isPresent())
			return optional.orElseThrow(() -> new RuntimeException("Unexpected error occurred!"));
		return null;
	}

	@Nonnull
	public static Container getWalletContainer(@Nonnull final Entity entity) {
		return new SuppliedItemContainer(() -> {
			IWalletHandler handler = lazyGetWalletHandler(entity);
			if(handler != null)
				return new WalletInteractable(handler);
			return null;
		});
	}

	/**
	 * Gets the Wallet Handler used for wallet rendering.
	 * Creates a Curios Wallet Handler if curios is installed
	 * to allow rendering of the wallet on non-player entities
	 * that have a wallet Curios slot.
	 */
	@Nullable
	public static IWalletHandler getRenderWalletHandler(@Nonnull final Entity entity) {
		if(LightmansCurrency.isCuriosLoaded() && entity instanceof LivingEntity le)
			return new CuriosWalletHandler(le);
		return lazyGetWalletHandler(entity);
	}

	@Nonnull
	public static MoneyView getWalletMoney(@Nonnull final Entity entity) {
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
		if(walletHandler != null)
		{
			ItemStack wallet = walletHandler.getWallet();
			IMoneyViewer moneyViewer = CapabilityMoneyViewer.getCapability(wallet);
			if(moneyViewer == null)
				return MoneyView.empty();
			return moneyViewer.getStoredMoney();
		}
		return MoneyView.builder().build();
	}

	public static ICapabilityProvider createProvider(final Player playerEntity)
	{
		return new Provider(playerEntity);
	}
	
	private static class Provider implements ICapabilitySerializable<Tag>{
		final LazyOptional<IWalletHandler> optional;
		final IWalletHandler handler;
		Provider(final Player playerEntity)
		{
			this.handler = new WalletHandler(playerEntity);
			this.optional = LazyOptional.of(() -> this.handler);
		}
		
		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
			return CurrencyCapabilities.WALLET.orEmpty(capability, this.optional);
		}

		@Override
		public Tag serializeNBT() {
			return this.handler.save();
		}

		@Override
		public void deserializeNBT(Tag tag) {
			if(tag instanceof CompoundTag compound)
				this.handler.load(compound);
		}
		
	}

	private static class WalletInteractable implements SuppliedItemContainer.IItemInteractable
	{
		private final IWalletHandler walletHandler;
		WalletInteractable(@Nonnull IWalletHandler handler) { this.walletHandler = handler; }
		@Nonnull
		@Override
		public ItemStack getItem() { return this.walletHandler.getWallet(); }
		@Override
		public void setItem(@Nonnull ItemStack item) {
			this.walletHandler.setWallet(item);
			if (this.walletHandler.entity().level.isClientSide && this.walletHandler.entity() instanceof Player player && player.isCreative())
			{
				//Send an update packet when edited on the client in creative mode
				new CPacketCreativeWalletEdit(this.getItem().copy()).send();
			}
		}
	}
	
}
