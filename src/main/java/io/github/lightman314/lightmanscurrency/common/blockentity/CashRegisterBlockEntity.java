package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CashRegisterBlockEntity extends EasyBlockEntity implements ITraderSource{
	
	List<BlockPos> positions = new ArrayList<>();
	public List<BlockPos> traderPositions() { return new ArrayList<>(this.positions); }
	Component customTitle = null;
	
	public CashRegisterBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.CASH_REGISTER.get(), pos, state);
	}
	
	public void loadDataFromItems(ItemStack stack)
	{
		if(stack == null || !stack.hasTag())
			return;
		CompoundTag tag = stack.getTag();
		//Copy positions from the item
		readPositions(tag);
		//Copy name from the item
		if(stack.hasCustomHoverName())
			this.customTitle = stack.getHoverName();
	}

	public void setPositions(List<BlockPos> positions)
	{
		this.positions = new ArrayList<>(positions);
		if(this.isServer())
			BlockEntityUtil.sendUpdatePacket(this);
	}

	public void OpenContainer(Player player)
	{
		MenuProvider provider = TraderData.getTraderMenuProvider(this.worldPosition, BlockEntityValidator.of(this));
		if(!(player instanceof ServerPlayer))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
			return;
		}
		if(!this.getTraders().isEmpty())
			NetworkHooks.openScreen((ServerPlayer)player, provider, this.worldPosition);
		else
			player.sendSystemMessage(LCText.MESSAGE_CASH_REGISTER_NOT_LINKED.get());
	}
	
	@Override
	public boolean isSingleTrader() { return false; }
	
	@Nonnull
	@Override
	public List<TraderData> getTraders() {
		List<TraderData> traders = new ArrayList<>();
		for (BlockPos position : this.positions) {
			if (this.level.getBlockEntity(position) instanceof TraderBlockEntity<?> be) {
				TraderData trader = be.getTraderData();
				if (trader != null && trader.allowAccess())
					traders.add(trader);
			}
		}
		return traders;
	}

	@Nullable
	@Override
	public Component getCustomTitle() { return this.customTitle; }

	public void setCustomTitle(@Nullable Component customTitle)
	{
		this.customTitle = customTitle;
		if(this.isServer())
			BlockEntityUtil.sendUpdatePacket(this);
	}

	//Only show the search bar if we actually expect for there to be more than 1 trader linked to this machine
	@Override
	public boolean showSearchBox() { return this.positions.size() > 1; }

	@Override
	public void saveAdditional(@Nonnull CompoundTag compound)
	{
		
		ListTag storageList = new ListTag();
		for (BlockPos thisPos : positions) {
			CompoundTag thisEntry = new CompoundTag();
			thisEntry.putInt("x", thisPos.getX());
			thisEntry.putInt("y", thisPos.getY());
			thisEntry.putInt("z", thisPos.getZ());
			storageList.add(thisEntry);
		}
		
		if(!storageList.isEmpty())
			compound.put("TraderPos", storageList);

		if(this.customTitle != null)
			compound.putString("CustomName",Component.Serializer.toJson(this.customTitle));
		
		super.saveAdditional(compound);
	}
	
	@Override
	public void load(@Nonnull CompoundTag compound)
	{
		
		this.readPositions(compound);

		if(compound.contains("CustomName"))
			this.customTitle = Component.Serializer.fromJson(compound.getString("CustomName"));
		else
			this.customTitle = null;
		
		super.load(compound);
		
	}
	
	private void readPositions(CompoundTag compound)
	{
		if(compound.contains("TraderPos"))
		{
			this.positions = new ArrayList<>();
			ListTag storageList = compound.getList("TraderPos", Tag.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundTag thisEntry = storageList.getCompound(i);
				if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
				{
					BlockPos thisPos = new BlockPos(thisEntry.getInt("x"), thisEntry.getInt("y"), thisEntry.getInt("z"));
					this.positions.add(thisPos);
				}
			}
		}
	}
	
}
