package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class CashRegisterItem extends BlockItem{
		
	private static final SoundEvent soundEffect = SoundEvents.EXPERIENCE_ORB_PICKUP;
	
	public CashRegisterItem(Block block, Properties properties)
	{
		super(block, properties.stacksTo(1));
	}
	
	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		
		BlockPos lookPos = context.getClickedPos();
		Level level = context.getLevel();
		if(level.getBlockState(lookPos).getBlock() instanceof ITraderBlock block)
		{
			BlockEntity blockEntity = block.getBlockEntity(level.getBlockState(lookPos), level, lookPos);
			if(!HasEntity(context.getItemInHand(), blockEntity) && blockEntity instanceof TraderBlockEntity)
			{
				AddEntity(context.getItemInHand(), blockEntity);

				if(level.isClientSide)
				{
					level.playSound(context.getPlayer(), blockEntity.getBlockPos(), soundEffect, SoundSource.NEUTRAL, 1f, 0f);
				}

				return InteractionResult.SUCCESS;
			}
			else if(blockEntity instanceof TraderBlockEntity) //Return even if we have the entity to prevent any accidental placements.
			{
				if(level.isClientSide)
				{
					level.playSound(context.getPlayer(), blockEntity.getBlockPos(), soundEffect, SoundSource.NEUTRAL, 1f, 1.35f);
				}
				return InteractionResult.SUCCESS;
			}
		}

		return super.useOn(context);
		
	}
	
	private boolean HasEntity(ItemStack stack, BlockEntity blockEntity)
	{
		
		//Get the tag

		if(!stack.has(ModDataComponents.CASH_REGISTER_TRADER_POSITIONS))
			return false;

		return stack.get(ModDataComponents.CASH_REGISTER_TRADER_POSITIONS).stream().anyMatch(p -> p.equals(blockEntity.getBlockPos()));
		
	}
	
	private void AddEntity(@Nonnull ItemStack stack, BlockEntity blockEntity)
	{
		//Get the current list (or an empty list if not present)
		List<BlockPos> editableList = new ArrayList<>(getNonnullData(stack));

		//Add the positoin to it
		editableList.add(blockEntity.getBlockPos());

		//Put the modified result back into the stack
		stack.set(ModDataComponents.CASH_REGISTER_TRADER_POSITIONS, ImmutableList.copyOf(editableList));
		
	}

	@Nonnull
	public static List<BlockPos> getNonnullData(@Nonnull ItemStack stack)
	{
		return stack.has(ModDataComponents.CASH_REGISTER_TRADER_POSITIONS) ? stack.get(ModDataComponents.CASH_REGISTER_TRADER_POSITIONS) : new ArrayList<>();
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		super.appendHoverText(stack,  context,  tooltip,  flagIn);
		List<BlockPos> data = getNonnullData(stack);

		tooltip.addAll(LCText.TOOLTIP_CASH_REGISTER.get());

		tooltip.add(LCText.TOOLTIP_CASH_REGISTER_INFO.get(data.size()));
		
		if(!Screen.hasShiftDown() || data.isEmpty())
			tooltip.add(LCText.TOOLTIP_CASH_REGISTER_INSTRUCTIONS.get());
		
		if(Screen.hasShiftDown())
		{
			//Display details of the registered blocks
			for(int i = 0; i < data.size(); i++)
			{
				BlockPos pos = data.get(i);
				tooltip.add(LCText.TOOLTIP_CASH_REGISTER_DETAILS.get(i + 1,pos.getX(), pos.getY(), pos.getZ()));
			}
		}
		else if(!data.isEmpty())
			tooltip.add(LCText.TOOLTIP_CASH_REGISTER_HOLD_SHIFT.get().withStyle(ChatFormatting.YELLOW));
	}
	
}
