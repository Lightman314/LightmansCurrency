package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.TallRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.EasyMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class ATMBlock extends TallRotatableBlock{

	public ATMBlock(Properties properties)
	{
		super(properties);
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
	{
		if(player instanceof ServerPlayer sp)
		{
			MenuValidator validator = BlockValidator.of(pos, this);
			NetworkHooks.openGui(sp, new SimpleMenuProvider((id, inventory, p) -> new ATMMenu(id, inventory, validator), EasyText.empty()), EasyMenu.encoder(validator));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.ATM);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}


}