package io.github.lightman314.lightmanscurrency.api.traders.attachments;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
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

    public void modifyDefaultPermissions(Map<String,Integer> defaultPermissions) { }

    public void handleSettingsChange(Player player, LazyPacketData message) { }

    public record TraderAttachmentType<T extends TraderAttachment>(ResourceLocation id, Function<TraderData,T> builder) {
        public T build(TraderData trader) { return this.builder.apply(trader); }
        @Override
        public int hashCode() { return this.id.hashCode(); }
        @Override
        public String toString() { return this.id.toString(); }
    }

}