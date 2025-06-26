package io.github.lightman314.lightmanscurrency.api.traders.settings;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderSettingsNode<T extends TraderData> extends SettingsNode {

    public final T trader;
    public TraderSettingsNode(String key, T trader) { this(key,trader,0); }
    public TraderSettingsNode(String key, T trader, int priority) {
        super(key,trader,priority);
        this.trader = trader;
    }

    protected final HolderLookup.Provider registryAccess() { return this.trader.registryAccess(); }

    protected final void settingChangeNotification(Notification notification) { this.trader.pushLocalNotification(notification); }

    protected final boolean hasPermission(Player player, String permission) { return this.trader.hasPermission(player,permission); }
    protected final int getPermissionLevel(Player player, String permission) { return this.trader.getPermissionLevel(player,permission); }

}
