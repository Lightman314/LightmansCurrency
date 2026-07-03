package io.github.lightman314.lightmanscurrency.features.wallet;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketCreativeWalletUpdate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.world.inventory.StackCopySlot;

public class WalletSlot extends StackCopySlot {

    private final Entity entity;
    public WalletSlot(Entity entity, int x, int y) {
        super(0, x, y);
        this.entity = entity;
        this.setBackground(LCApi.id("container/slot/wallet"));
    }

    @Override
    public int getMaxStackSize() { return 1; }

    @Override
    public boolean mayPlace(ItemStack itemStack) { return WalletItem.isWallet(itemStack); }

    @Override
    protected ItemStack getStackCopy() { return this.entity.getData(LCDataAttachments.WALLET).getWallet().copy(); }

    @Override
    protected void setStackCopy(ItemStack stack) {
        this.entity.getData(LCDataAttachments.WALLET).setWallet(stack.copy());
        if(this.entity instanceof Player player && player.isCreative() && player.level().isClientSide())
            new CPacketCreativeWalletUpdate(stack).send();
    }

}