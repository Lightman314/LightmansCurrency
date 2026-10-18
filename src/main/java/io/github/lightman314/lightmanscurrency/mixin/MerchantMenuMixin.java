package io.github.lightman314.lightmanscurrency.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lightman314.lightmanscurrency.mixin_helpers.MerchantMenuHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin extends AbstractContainerMenu {

    protected MerchantMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Shadow
    @Final
    private Merchant trader;
    @Shadow
    @Final
    private MerchantContainer tradeContainer;
    @Shadow
    public abstract MerchantOffers getOffers();
    @Unique
    @Nullable
    private Player lightmanscurrency$getPlayer() { return this.trader.getTradingPlayer(); }

    @WrapMethod(method = "tryMoveItems")
    private void tryMoveItemsWrapper(int newTradeIndex,Operation<Void> original) {
        boolean validIndex = newTradeIndex >= 0 && newTradeIndex < this.getOffers().size();
        //Eject money from the slots back into your wallet
        if(validIndex)
            MerchantMenuHelper.ejectMoneyIntoWallet(this.lightmanscurrency$getPlayer(),this.tradeContainer,false);
        //Perform vanilla behaviour
        original.call(newTradeIndex);
        //Now try to move coins from your wallet back into any empty slots
        if(validIndex) {
            MerchantOffer offer = this.getOffers().get(newTradeIndex);
            MerchantMenuHelper.tryMoveItems(this.lightmanscurrency$getPlayer(),this.tradeContainer,offer);
        }
    }


    @WrapOperation(at = @At(value = "INVOKE",target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V"),method = "removed")
    private void placeInWalletBeforeInventory(Inventory instance, ItemStack itemStack, Operation<Void> original) {
        original.call(instance,MerchantMenuHelper.tryPickupCoin(instance.player,itemStack));
    }

}