package io.github.lightman314.lightmanscurrency.integration.claiming.flan;

import io.github.flemmli97.flan.api.ClaimHandler;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.integration.claiming.IClaimPurchaseHandler;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class LCFlanClaimHandler implements IClaimPurchaseHandler {

    public static final IClaimPurchaseHandler INSTANCE = new LCFlanClaimHandler();

    private LCFlanClaimHandler() {}

    @Override
    public boolean canBuyForceload(@Nonnull ServerPlayer player) { return false; }

    @Override
    public int getCurrentBonusClaims(@Nonnull ServerPlayer player) {
        IPlayerData data = ClaimHandler.getPlayerData(player);
        if(data != null)
            return data.getAdditionalClaims() / LCConfig.SERVER.flanClaimingBlocksPerChunk.get();
        return 0;
    }

    @Override
    public int getCurrentBonusForceloadChunks(@Nonnull ServerPlayer player) { return 0; }

    @Override
    public void addBonusClaims(@Nonnull ServerPlayer player, int addAmount) {
        IPlayerData data = ClaimHandler.getPlayerData(player);
        if(data != null)
        {
            int addBlocks = addAmount * LCConfig.SERVER.flanClaimingBlocksPerChunk.get();
            data.setAdditionalClaims(data.getAdditionalClaims() + addBlocks);
        }
    }

    @Override
    public void addBonusForceloadChunks(@Nonnull ServerPlayer player, int addAmount) { LightmansCurrency.LogError("Cannot buy forceload chunks with Flan!"); }

}