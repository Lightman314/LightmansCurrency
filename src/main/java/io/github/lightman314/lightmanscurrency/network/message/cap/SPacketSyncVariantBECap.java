package io.github.lightman314.lightmanscurrency.network.message.cap;

import io.github.lightman314.lightmanscurrency.api.capability.variant.CapabilityVariantData;
import io.github.lightman314.lightmanscurrency.api.variants.block.builtin.VariantDataStorageAttachment;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class SPacketSyncVariantBECap extends ServerToClientPacket {

    public static final Handler<SPacketSyncVariantBECap> HANDLER = new H();

    private final BlockPos pos;
    @Nullable
    private final ResourceLocation variantID;
    private final boolean locked;
    public SPacketSyncVariantBECap(BlockPos pos, @Nullable ResourceLocation variantID, boolean locked)
    {
        this.pos = pos;
        this.variantID = variantID;
        this.locked = locked;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.variantID != null);
        if(this.variantID != null)
            buffer.writeResourceLocation(this.variantID);
        buffer.writeBoolean(this.locked);
    }

    private static class H extends Handler<SPacketSyncVariantBECap>
    {

        @Override
        public SPacketSyncVariantBECap decode(FriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            ResourceLocation variantID = null;
            if(buffer.readBoolean())
                variantID = buffer.readResourceLocation();
            return new SPacketSyncVariantBECap(pos,variantID,buffer.readBoolean());
        }

        @Override
        protected void handle(SPacketSyncVariantBECap message, Player player) {
            if(player != null)
            {
                BlockEntity be = player.level().getBlockEntity(message.pos);
                if(be != null)
                {
                    be.getCapability(CapabilityVariantData.CAPABILITY).ifPresent((data) -> {
                        if (data instanceof VariantDataStorageAttachment d)
                            data.setVariant(message.variantID, message.locked);
                    });
                }
            }
        }

    }

}
