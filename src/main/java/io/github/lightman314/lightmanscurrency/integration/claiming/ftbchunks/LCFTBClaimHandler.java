package io.github.lightman314.lightmanscurrency.integration.claiming.ftbchunks;

import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.FTBChunksTeamData;
import dev.ftb.mods.ftbchunks.net.SendGeneralDataPacket;
import io.github.lightman314.lightmanscurrency.integration.claiming.IClaimPurchaseHandler;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

public class LCFTBClaimHandler implements IClaimPurchaseHandler {

    public static final IClaimPurchaseHandler INSTANCE = new LCFTBClaimHandler();

    private LCFTBClaimHandler() {}

    @Override
    public boolean canBuyClaims(@Nonnull ServerPlayer player) { return IClaimPurchaseHandler.super.canBuyClaims(player) && FTBChunksAPI.getManager().getPersonalData(player.getUUID()) != null; }
    @Override
    public boolean canBuyForceload(@Nonnull ServerPlayer player) { return IClaimPurchaseHandler.super.canBuyForceload(player) && FTBChunksAPI.getManager().getPersonalData(player.getUUID()) != null; }

    @Override
    public int getCurrentBonusClaims(@Nonnull ServerPlayer player) {
        FTBChunksTeamData data = FTBChunksAPI.getManager().getPersonalData(player);
        if(data == null)
            return data.getExtraClaimChunks();
        return 0;
    }

    @Override
    public int getCurrentBonusForceloadChunks(@Nonnull ServerPlayer player) {
        FTBChunksTeamData data = FTBChunksAPI.getManager().getPersonalData(player);
        if(data == null)
            return data.getExtraForceLoadChunks();
        return 0;
    }

    @Override
    public void addBonusClaims(@Nonnull ServerPlayer player, int addAmount) {
        FTBChunksTeamData data = FTBChunksAPI.getManager().getPersonalData(player.getUUID());
        data.extraClaimChunks += addAmount;
        setDataChanged(data, player);
    }

    @Override
    public void addBonusForceloadChunks(@Nonnull ServerPlayer player, int addAmount) {
        FTBChunksTeamData data = FTBChunksAPI.getManager().getPersonalData(player.getUUID());
        data.extraForceLoadChunks += addAmount;
        setDataChanged(data, player);
    }

    private static void setDataChanged(FTBChunksTeamData data, ServerPlayer player)
    {
        data.save();
        FTBChunksTeamData teamData = FTBChunksAPI.getManager().getData(player);
        teamData.updateLimits();
        SendGeneralDataPacket.send(teamData, player);
    }
}