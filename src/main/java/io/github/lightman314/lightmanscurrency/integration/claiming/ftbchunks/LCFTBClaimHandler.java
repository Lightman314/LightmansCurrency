package io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks;

import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.ChunkTeamDataImpl;
import dev.ftb.mods.ftbchunks.net.SendGeneralDataPacket;
import io.github.lightman314.lightmanscurrency.integration.claiming.IClaimPurchaseHandler;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class LCFTBClaimHandler implements IClaimPurchaseHandler {

    public static final IClaimPurchaseHandler INSTANCE = new LCFTBClaimHandler();

    @Override
    public boolean canBuyClaims(@Nonnull ServerPlayer player) { return IClaimPurchaseHandler.super.canBuyClaims(player) && FTBChunksAPI.api().getManager().getPersonalData(player.getUUID()) != null; }
    @Override
    public boolean canBuyForceload(@Nonnull ServerPlayer player) { return IClaimPurchaseHandler.super.canBuyForceload(player) && FTBChunksAPI.api().getManager().getPersonalData(player.getUUID()) != null; }

    @Override
    public int getCurrentBonusClaims(@Nonnull ServerPlayer player) {
        ChunkTeamData data = FTBChunksAPI.api().getManager().getOrCreateData(player);
        if(data == null)
            return data.getExtraClaimChunks();
        return 0;
    }

    @Override
    public int getCurrentBonusForceloadChunks(@Nonnull ServerPlayer player) {
        ChunkTeamData data = FTBChunksAPI.api().getManager().getOrCreateData(player);
        if(data == null)
            return data.getExtraForceLoadChunks();
        return 0;
    }

    @Override
    public void addBonusClaims(@Nonnull ServerPlayer player, int addAmount) {
        ChunkTeamData personalData = FTBChunksAPI.api().getManager().getPersonalData(player.getUUID());
        personalData.setExtraClaimChunks(personalData.getExtraClaimChunks() + addAmount);
        setDataChanged(personalData, player);
    }

    @Override
    public void addBonusForceloadChunks(@Nonnull ServerPlayer player, int addAmount) {
        ChunkTeamData personalData = FTBChunksAPI.api().getManager().getPersonalData(player.getUUID());
        personalData.setExtraForceLoadChunks(personalData.getExtraForceLoadChunks() + addAmount);
        setDataChanged(personalData, player);
    }

    private static void setDataChanged(ChunkTeamData data, ServerPlayer player)
    {
        if(data instanceof ChunkTeamDataImpl d)
            d.markDirty();
        ChunkTeamData teamData = FTBChunksAPI.api().getManager().getOrCreateData(player);
        if(teamData instanceof ChunkTeamDataImpl d2)
            d2.updateLimits();
        SendGeneralDataPacket.send(teamData, player);
    }
}
