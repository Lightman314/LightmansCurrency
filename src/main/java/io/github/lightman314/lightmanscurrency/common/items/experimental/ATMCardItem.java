package io.github.lightman314.lightmanscurrency.common.items.experimental;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.ATMCardMenu;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class ATMCardItem extends Item {

    public ATMCardItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {

        tooltip.add(LCText.TOOLTIP_BETA.getWithStyle(ChatFormatting.RED));

        ATMCardData data = stack.getOrDefault(ModDataComponents.ATM_CARD_DATA, ATMCardData.EMPTY);
        if(data.bankReference().isEmpty())
            tooltip.add(LCText.TOOLTIP_ATM_CARD_NOT_LINKED.getWithStyle(ChatFormatting.GRAY));
        else
        {
            BankReference reference = data.getBankReference(true);
            IBankAccount account = reference.get();
            if(account == null)
                tooltip.add(LCText.TOOLTIP_ATM_CARD_NOT_LINKED.getWithStyle(ChatFormatting.GRAY));
            else
            {
                tooltip.add(LCText.TOOLTIP_ATM_CARD_ACCOUNT.get(account.getName().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
                if(!account.isCardValid(data.validation()))
                    tooltip.add(LCText.TOOLTIP_ATM_CARD_LINK_INVALID.getWithStyle(ChatFormatting.RED));
                else
                {
                    tooltip.add(account.getBalanceText().withStyle(ChatFormatting.GRAY));
                    //Usage tooltip
                    tooltip.addAll(TooltipHelper.splitTooltips(LCText.TOOLTIP_PAYMENT_CARD_USAGE.get(),ChatFormatting.GRAY));
                }
            }

        }
        if(data.locked())
            tooltip.add(LCText.TOOLTIP_ATM_CARD_ACCOUNT_LOCKED.getWithStyle(ChatFormatting.GRAY));
    }

    @Override
    public void verifyComponentsAfterLoad(@Nonnull ItemStack stack) {
        if(!stack.has(ModDataComponents.ATM_CARD_DATA))
            stack.set(ModDataComponents.ATM_CARD_DATA, ATMCardData.EMPTY);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        if(!level.isClientSide)
        {
            if(QuarantineAPI.IsDimensionQuarantined(level))
            {
                EasyText.sendMessage(player,LCText.MESSAGE_DIMENSION_QUARANTINED_BANK.getWithStyle(ChatFormatting.GOLD));
                return InteractionResultHolder.success(card);
            }
            int inventorySlot = -1;
            for(int i = 0; i < player.getInventory().getContainerSize() && inventorySlot < 0; ++i)
            {
                ItemStack stack = player.getInventory().getItem(i);
                if(stack == card)
                    inventorySlot = i;
            }
            if(inventorySlot >= 0)
            {
                final int slot = inventorySlot;
                ATMCardData data = card.getOrDefault(ModDataComponents.ATM_CARD_DATA, ATMCardData.EMPTY);
                if(data.locked() && !LCAdminMode.isAdminPlayer(player))
                {
                    player.sendSystemMessage(LCText.MESSAGE_ATM_CARD_LOCKED.get());
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS_NO_ITEM_USED,card);
                }
                player.openMenu(ATMCardMenu.getProvider(inventorySlot), b -> b.writeInt(slot));
            }
        }
        return InteractionResultHolder.success(card);
    }

}
