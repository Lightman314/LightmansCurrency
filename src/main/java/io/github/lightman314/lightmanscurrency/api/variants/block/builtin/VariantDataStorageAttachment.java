package io.github.lightman314.lightmanscurrency.api.variants.block.builtin;

import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Neoforge Data Attachment that can be added to a block entity to give it the ability to store IVariantBlockData without a mandatory
 */
public class VariantDataStorageAttachment implements IVariantDataStorage {

    public static final Supplier<AttachmentType<VariantDataStorageAttachment>> TYPE = ModAttachmentTypes.VARIANT_BLOCK_DATA;
    public static final IAttachmentSerializer<CompoundTag, VariantDataStorageAttachment> SERIALIZER = new Serializer();
    public static final AttachmentSyncHandler<VariantDataStorageAttachment> SYNC_HANDLER = new SyncHandler();
    @Nullable
    private ResourceLocation variant = null;
    private boolean locked = false;
    private final IAttachmentHolder parent;
    public VariantDataStorageAttachment(IAttachmentHolder parent) { this.parent = parent; }

    @Nullable
    @Override
    public ResourceLocation getCurrentVariant() { return this.variant; }
    @Override
    public boolean isVariantLocked() { return this.locked; }
    @Override
    public void setVariant(@Nullable ResourceLocation variant, boolean locked) {
        this.variant = variant;
        this.locked = locked;
        this.parent.setData(TYPE,this);
    }

    private CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        if(this.variant != null)
            tag.putString("Variant",this.variant.toString());
        if(this.locked)
            tag.putBoolean("Locked",true);
        return tag;
    }

    private void read(CompoundTag tag) {
        if(tag.contains("Variant"))
            this.variant = VersionUtil.parseResource(tag.getString("Variant"));
        else
            this.variant = null;
        this.locked = tag.getBoolean("Locked");
    }

    private static class Serializer implements IAttachmentSerializer<CompoundTag, VariantDataStorageAttachment>
    {
        @Override
        public VariantDataStorageAttachment read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            VariantDataStorageAttachment a;
            if(holder instanceof BlockEntity be)
                a = new VariantDataStorageAttachment(be);
            else
                a = new VariantDataStorageAttachment(null);
            a.read(tag);
            return a;
        }

        @Override
        @Nullable
        public CompoundTag write(VariantDataStorageAttachment attachment, HolderLookup.Provider provider) {
            if(attachment.variant != null || attachment.locked)
                return attachment.write();
            return null;
        }
    }

    private static class SyncHandler implements AttachmentSyncHandler<VariantDataStorageAttachment>
    {
        @Override
        public void write(RegistryFriendlyByteBuf buffer, VariantDataStorageAttachment data, boolean initialSync) {
            boolean hasVariant = data.variant != null;
            buffer.writeBoolean(hasVariant);
            if(hasVariant)
                buffer.writeResourceLocation(data.variant);
            buffer.writeBoolean(data.locked);
        }
        @Override
        @Nullable
        public VariantDataStorageAttachment read(IAttachmentHolder holder, RegistryFriendlyByteBuf buffer, @Nullable VariantDataStorageAttachment oldData) {
            VariantDataStorageAttachment data = oldData == null ? new VariantDataStorageAttachment(holder) : oldData;
            boolean hasVariant = buffer.readBoolean();
            if(hasVariant)
                data.variant = buffer.readResourceLocation();
            else
                data.variant = null;
            data.locked = buffer.readBoolean();
            //Reset to null if no data is present
            if(data.variant == null && !data.locked)
                return null;
            return data;
        }
    }

}