package io.github.lightman314.lightmanscurrency.common.items.experimental;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.items.colored.ColoredItem;
import io.github.lightman314.lightmanscurrency.common.menus.ATMCardMenu;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ATMCardItem extends Item implements ColoredItem {

    public ATMCardItem(Properties properties) { super(properties); }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag tag) {
        return CapabilityMoneyHandler.createProvider(new ATMCardMoneyHandler(stack));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {

        tooltip.add(LCText.TOOLTIP_BETA.getWithStyle(ChatFormatting.RED));

        CompoundTag tag = stack.getTag();
        if(tag == null || !tag.contains("BankAccount"))
            tooltip.add(LCText.TOOLTIP_ATM_CARD_NOT_LINKED.getWithStyle(ChatFormatting.GRAY));
        else
        {
            BankReference reference = BankReference.load(tag.getCompound("BankAccount")).flagAsClient();
            IBankAccount account = reference.get();
            if(account == null)
                tooltip.add(LCText.TOOLTIP_ATM_CARD_NOT_LINKED.getWithStyle(ChatFormatting.GRAY));
            else
            {
                tooltip.add(LCText.TOOLTIP_ATM_CARD_ACCOUNT.get(account.getName().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
                int validation = tag.contains("AccountValidation") ? tag.getInt("AccountValidation") : -1;
                if(!account.isCardValid(validation))
                    tooltip.add(LCText.TOOLTIP_ATM_CARD_LINK_INVALID.getWithStyle(ChatFormatting.RED));
                else
                    tooltip.add(account.getBalanceText().withStyle(ChatFormatting.GRAY));
            }
            if(tag.getBoolean("CardLocked"))
                tooltip.add(LCText.TOOLTIP_ATM_CARD_ACCOUNT_LOCKED.getWithStyle(ChatFormatting.GRAY));
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        if(!level.isClientSide && player instanceof ServerPlayer sp)
        {
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
                CompoundTag tag = card.getOrCreateTag();
                if(tag.getBoolean("CardLocked") && !LCAdminMode.isAdminPlayer(player))
                {
                    player.sendSystemMessage(LCText.MESSAGE_ATM_CARD_LOCKED.get());
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS,card);
                }
                NetworkHooks.openScreen(sp, ATMCardMenu.getProvider(inventorySlot), b -> b.writeInt(slot));
            }
        }
        return InteractionResultHolder.success(card);
    }

}