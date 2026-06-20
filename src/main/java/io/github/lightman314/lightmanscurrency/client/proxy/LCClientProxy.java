package io.github.lightman314.lightmanscurrency.client.proxy;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.proxy.LCProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class LCClientProxy extends LCProxy {

    public static void initialize() { setProxy(new LCClientProxy()); }

    private LCClientProxy() {}

    @Nullable
    @Override
    public Level getDummyLevel() {
        Level level = Minecraft.getInstance().level;
        if(level == null)
            return super.getDummyLevel();
        return level;
    }

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
}