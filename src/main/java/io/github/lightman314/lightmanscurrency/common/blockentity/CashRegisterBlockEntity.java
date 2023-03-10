package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;

public class CashRegisterBlockEntity extends EasyBlockEntity implements ITraderSource{
	
	List<BlockPos> positions = new ArrayList<>();
	
	public CashRegisterBlockEntity()
	{
		super(ModBlockEntities.CASH_REGISTER.get());
	}
	
	public void loadDataFromItems(CompoundNBT itemTag)
	{
		if(itemTag == null)
			return;
		readPositions(itemTag);
		BlockEntityUtil.sendUpdatePacket(this);
	}
	
	public void OpenContainer(PlayerEntity player)
	{
		INamedContainerProvider provider = TraderData.getTraderMenuProvider(this.worldPosition);
		if(!(player instanceof ServerPlayerEntity))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
			return;
		}
		if(this.getTraders().size() > 0)
			NetworkHooks.openGui((ServerPlayerEntity)player, provider, this.worldPosition);
		else
			player.sendMessage(EasyText.translatable("message.lightmanscurrency.cash_register.notlinked"), new UUID(0,0));
	}
	
	@Override
	public boolean isSingleTrader() { return false; }
	
	@Override
	public @Nonnull List<TraderData> getTraders() {
		List<TraderData> traders = new ArrayList<>();
		for (BlockPos position : this.positions) {
			TileEntity be = this.level.getBlockEntity(position);
			if (be instanceof TraderBlockEntity<?>) {
				TraderData trader = ((TraderBlockEntity<?>) be).getTraderData();
				if (trader != null)
					traders.add(trader);
			}
		}
		return traders;
	}
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);
		ListNBT storageList = new ListNBT();
		for (BlockPos thisPos : positions) {
			CompoundNBT thisEntry = new CompoundNBT();
			thisEntry.putInt("x", thisPos.getX());
			thisEntry.putInt("y", thisPos.getY());
			thisEntry.putInt("z", thisPos.getZ());
			storageList.add(thisEntry);
		}
		
		if(storageList.size() > 0)
		{
			compound.put("TraderPos", storageList);
		}
		return compound;
	}
	
	@Override
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{

		super.load(state, compound);
		
		this.readPositions(compound);
		
	}
	
	private void readPositions(CompoundNBT compound)
	{
		if(compound.contains("TraderPos"))
		{
			this.positions = new ArrayList<>();
			ListNBT storageList = compound.getList("TraderPos", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < storageList.size(); i++)
			{
				CompoundNBT thisEntry = storageList.getCompound(i);
				if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
				{
					BlockPos thisPos = new BlockPos(thisEntry.getInt("x"), thisEntry.getInt("y"), thisEntry.getInt("z"));
					this.positions.add(thisPos);
				}
			}
		}
	}
	
}