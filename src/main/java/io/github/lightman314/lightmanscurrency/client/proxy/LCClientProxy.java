package io.github.lightman314.lightmanscurrency.client.proxy;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.proxy.LCProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class LCClientProxy extends LCProxy {

    public static void initialize() { setProxy(new LCClientProxy()); }

    private long timeDesync = 0;
    private final Minecraft mc;

    private LCClientProxy() { this.mc = Minecraft.getInstance(); }

    @Nullable
    @Override
    public Level getDummyLevel() {
        Level level = this.mc.level;
        if(level == null)
            return super.getDummyLevel();
        return level;
    }

    @Override
    public boolean isSelf(Player player) { return player == this.mc.player; }

    @Override
    public List<GameProfile> getPlayerList(ISidedContext context) {
        if(context.isClient())
        {
            List<GameProfile> list = new ArrayList<>();
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if(connection != null)
                list.addAll(connection.getListedOnlinePlayers().stream().map(PlayerInfo::getProfile).toList());
            return list;
        }
        return super.getPlayerList(context);
    }

    @Nullable
    @Override
    public Player getLocalPlayer() { return this.mc.player; }

    @Override
    public void setTimeDesync(long offset) { this.timeDesync = offset; }
    @Override
    public long getTimeDesync() { return this.timeDesync; }

}