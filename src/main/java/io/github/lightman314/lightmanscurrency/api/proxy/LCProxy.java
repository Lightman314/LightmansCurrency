package io.github.lightman314.lightmanscurrency.api.proxy;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class containing various methods that may be implemented differently on a client dist instead of the server dist
 */
public class LCProxy {

    private static LCProxy instance = new LCProxy();
    public static LCProxy get() { return instance; }
    protected static void setProxy(LCProxy proxy) { instance = Objects.requireNonNull(instance); }

    @Nullable
    public Level getDummyLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.overworld();
        return null;
    }

    public List<GameProfile> getPlayerList(ISidedContext context) {
        List<GameProfile> profiles = new ArrayList<>();
        if(context.isServer())
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null)
                profiles.addAll(server.getPlayerList().getPlayers().stream().map(Player::getGameProfile).toList());
        }
        return profiles;
    }

}
