package io.github.lightman314.lightmanscurrency.api.variants.block.builtin;

import io.github.lightman314.lightmanscurrency.api.capability.variant.CapabilityVariantData;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.network.message.cap.SPacketSyncVariantBECap;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

/**
 * Neoforge Data Attachment that can be added to a block entity to give it the ability to store IVariantBlockData without a mandatory
 */
public class VariantDataStorageAttachment implements IVariantDataStorage, ICapabilitySerializable<CompoundTag> {

    private final LazyOptional<IVariantDataStorage> optional = LazyOptional.of(() -> this);
    @Nullable
    private ResourceLocation variant = null;
    private boolean locked = false;
    private final BlockEntity parent;
    public VariantDataStorageAttachment(BlockEntity parent) { this.parent = parent; }

    @Nullable
    @Override
    public ResourceLocation getCurrentVariant() { return this.variant; }
    @Override
    public boolean isVariantLocked() { return this.locked; }
    @Override
    public void setVariant(@Nullable ResourceLocation variant, boolean locked) {
        this.variant = variant;
        this.locked = locked;
        this.parent.setChanged();
        if(!this.parent.getLevel().isClientSide)
            new SPacketSyncVariantBECap(this.parent.getBlockPos(),this.variant,this.locked).sendToTarget(PacketDistributor.TRACKING_CHUNK.with(() -> this.parent.getLevel().getChunkAt(this.parent.getBlockPos())));
    }

    public void syncWith(ServerPlayer player)
    {
        new SPacketSyncVariantBECap(this.parent.getBlockPos(),this.variant,this.locked).sendTo(player);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if(this.variant != null)
            tag.putString("Variant",this.variant.toString());
        if(this.locked)
            tag.putBoolean("Locked",true);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if(tag.contains("Variant"))
            this.variant = VersionUtil.parseResource(tag.getString("Variant"));
        else
            this.variant = null;
        this.locked = tag.getBoolean("Locked");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        return CapabilityVariantData.CAPABILITY.orEmpty(capability,this.optional);
    }
}