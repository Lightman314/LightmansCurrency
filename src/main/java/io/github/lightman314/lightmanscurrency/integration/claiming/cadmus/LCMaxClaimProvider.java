package io.github.lightman314.lightmanscurrency.integration.claiming.cadmus;

import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProvider;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import io.github.lightman314.lightmanscurrency.integration.claiming.bonus_data.LCBonusClaimData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class LCMaxClaimProvider implements MaxClaimProvider {

    public static final LCMaxClaimProvider INSTANCE = new LCMaxClaimProvider();
    public static final ResourceLocation CLAIM_PRIVIDER_TYPE = VersionUtil.lcResource("claim_provider");

    private static ResourceLocation lastSelected = Cadmus.DEFAULT_ID;

    public static void register() {
        MaxClaimProviderApi.API.register(CLAIM_PRIVIDER_TYPE, INSTANCE);
        lastSelected = MaxClaimProviderApi.API.getSelectedId();
        MaxClaimProviderApi.API.setSelected(CLAIM_PRIVIDER_TYPE);
    }

    private static MaxClaimProvider getLastSelected() { return MaxClaimProviderApi.API.get(lastSelected); }

    private LCMaxClaimProvider() { }

    @Override
    public void calculate(String id, MinecraftServer server) {
        MaxClaimProvider previous = getLastSelected();
        if(previous != null)
            previous.calculate(id, server);
    }

    @Override
    public void removeTeam(String id, MinecraftServer server) {
        MaxClaimProvider previous = getLastSelected();
        if(previous != null)
            previous.removeTeam(id, server);
    }

    @Override
    public int getMaxClaims(String id, MinecraftServer server, Player player) {
        MaxClaimProvider previous = getLastSelected();
        int defaultAmount = previous != null ? previous.getMaxClaims(id, server, player) : 0;
        return defaultAmount + LCBonusClaimData.getBonusClaimsFor(player);
    }

    @Override
    public int getMaxChunkLoaded(String id, MinecraftServer server, Player player) {
        MaxClaimProvider previous = getLastSelected();
        int defaultAmount = previous != null ? previous.getMaxChunkLoaded(id, server, player) : 0;
        return defaultAmount + LCBonusClaimData.getBonusChunkLoadsFor(player);
    }

}
