package io.github.lightman314.lightmanscurrency.integration.claiming.bonus_data;

import io.github.lightman314.lightmanscurrency.integration.claiming.IClaimPurchaseHandler;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class LCBonusClaimHandler implements IClaimPurchaseHandler {

    public static final IClaimPurchaseHandler INSTANCE = new LCBonusClaimHandler();

    private LCBonusClaimHandler() {}
    @Override
    public int getCurrentBonusClaims(@Nonnull ServerPlayer player) { return LCBonusClaimData.getBonusClaimsFor(player); }
    @Override
    public int getCurrentBonusForceloadChunks(@Nonnull ServerPlayer player) { return LCBonusClaimData.getBonusChunkLoadsFor(player); }
    @Override
    public void addBonusClaims(@Nonnull ServerPlayer player, int addAmount) { LCBonusClaimData.addBonusClaimsFor(player, addAmount); }
    @Override
    public void addBonusForceloadChunks(@Nonnull ServerPlayer player, int addAmount) { LCBonusClaimData.addBonusChunkLoadsFor(player, addAmount); }
}
