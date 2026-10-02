package io.github.lightman314.lightmanscurrency.api.proxy;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A class containing various methods that may be implemented differently on a client dist instead of the server dist
 */
public class LCProxy {

    private static LCProxy instance = new LCProxy();
    public static LCProxy get() { return instance; }
    protected static void setProxy(LCProxy proxy) { instance = Objects.requireNonNull(proxy); }

    @Nullable
    public Level getDummyLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.overworld();
        return null;
    }

    public boolean isSelf(Player player) { return false; }

    public List<GameProfile> getPlayerList(ISidedContext context) {
        if(context.isServer())
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null)
                return server.getPlayerList().getPlayers().stream().map(Player::getGameProfile).toList();
        }
        return List.of();
    }

    @Nullable
    public Player getLocalPlayer() { return null; }

    public long getTimeDesync() { return 0; }
    public void setTimeDesync(long offset) {}

}
