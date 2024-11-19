package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.TallRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockValidator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ATMBlock extends TallRotatableBlock{
	
	public ATMBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Nonnull
	@Override
	public InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			if(QuarantineAPI.IsDimensionQuarantined(level))
				EasyText.sendMessage(player,LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
			else
			{
				MenuValidator validator = BlockValidator.of(pos, this);
				player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ATMMenu(id, inventory, validator), EasyText.empty()), EasyMenu.encoder(validator));
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_ATM);
		super.appendHoverText(stack, context, tooltip, flagIn);
	}
	
	
}
