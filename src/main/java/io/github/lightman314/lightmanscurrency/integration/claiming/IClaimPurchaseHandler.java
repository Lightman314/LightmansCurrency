package io.github.lightman314.lightmanscurrency.integration.claiming;

import io.github.lightman314.lightmanscurrency.Config;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public interface IClaimPurchaseHandler {

    default boolean canBuyClaims(@Nonnull ServerPlayer player) { return Config.SERVER.claimingAllowClaimPurchase.get(); }
    default boolean canBuyForceload(@Nonnull ServerPlayer player) { return Config.SERVER.claimingAllowForceloadPurchase.get(); }
    int getCurrentBonusClaims(@Nonnull ServerPlayer player);
    int getCurrentBonusForceloadChunks(@Nonnull ServerPlayer player);
    void addBonusClaims(@Nonnull ServerPlayer player, int addAmount);
    void addBonusForceloadChunks(@Nonnull ServerPlayer player, int addAmount);


}
