package io.github.lightman314.lightmanscurrency.api.traders.attachments;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderAttachment {

    protected final TraderData trader;
    protected TraderAttachment(TraderData trader) { this.trader = trader; }

    protected final void markDirty() { this.trader.markAttachmentDirty(this.getType()); }

    public abstract TraderAttachmentType<?> getType();

    public abstract CompoundTag save();
    public abstract void load(CompoundTag tag);

    @OnlyIn(Dist.CLIENT)
    public void addSettingsTabs(TraderSettingsClientTab tab, Consumer<SettingsSubTab> consumer) { }
    public void modifyDefaultPermissions(Map<String,Integer> defaultPermissions) { }
    @OnlyIn(Dist.CLIENT)
    public void addPermissionOptions(Consumer<PermissionOption> consumer) { }

    public void handleSettingsChange(Player player, LazyPacketData message) { }

    public record TraderAttachmentType<T extends TraderAttachment>(ResourceLocation id, Function<TraderData,T> builder) {
        public T build(TraderData trader) { return this.builder.apply(trader); }
        @Override
        public int hashCode() { return this.id.hashCode(); }
        @Override
        public String toString() { return this.id.toString(); }
    }

}