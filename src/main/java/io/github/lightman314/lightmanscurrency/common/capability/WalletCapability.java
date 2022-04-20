package io.github.lightman314.lightmanscurrency.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WalletCapability {
	
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}
	
	public static ICapabilityProvider createProvider(final Player playerEntity)
	{
		return new Provider(playerEntity);
	}
	
	public static class WalletHandler implements IWalletHandler
	{
		
		final LivingEntity entity;
		ItemStack backupWallet;
		final Container walletInventory;
		
		public WalletHandler() {
			this(null);
		}
		
		public WalletHandler(LivingEntity entity) {
			this.entity = entity;
			this.backupWallet = ItemStack.EMPTY;
			this.walletInventory = new SimpleContainer(1);
		}
		
		@Override
		public Container getInventory()
		{
			return this.walletInventory;
		}
		
		@Override
		public ItemStack getWallet() {
			return this.walletInventory.getItem(0);
		}

		@Override
		public void setWallet(ItemStack walletStack) {
			this.walletInventory.setItem(0, walletStack);
			if(!(walletStack.getItem() instanceof WalletItem) && !walletStack.isEmpty())
				LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");
		}

		@Override
		public LivingEntity getEntity() {
			return this.entity;
		}
		
		@Override
		public boolean isDirty() {
			return !InventoryUtil.ItemMatches(this.backupWallet, this.getWallet()) || this.backupWallet.getCount() != this.getWallet().getCount();
		}
		
		@Override
		public void clean() { this.backupWallet = this.getWallet().copy(); }
		
		@Override
		public Tag writeTag() {
			CompoundTag compound = new CompoundTag();
			CompoundTag walletItem = this.getWallet().save(new CompoundTag());
			compound.put("Wallet", walletItem);
			return compound;
		}
		
		@Override
		public void readTag(Tag tag)
		{
			if(tag instanceof CompoundTag)
			{
				CompoundTag compound = (CompoundTag)tag;
				ItemStack wallet = ItemStack.of(compound.getCompound("Wallet"));
				this.setWallet(wallet);
				this.clean();
			}
		}
		
	}
	
	public static class Provider implements ICapabilitySerializable<Tag>{
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
			return handler.writeTag();
		}

		@Override
		public void deserializeNBT(Tag tag) {
			handler.readTag(tag);
		}
		
	}
	
}
