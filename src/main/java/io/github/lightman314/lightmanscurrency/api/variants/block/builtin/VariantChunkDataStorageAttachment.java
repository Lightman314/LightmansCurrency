package io.github.lightman314.lightmanscurrency.api.variants.block.builtin;

import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VariantChunkDataStorageAttachment {

    public static final IAttachmentSerializer<ListTag,VariantChunkDataStorageAttachment> SERIALIZER = new Serializer();
    public static final AttachmentSyncHandler<VariantChunkDataStorageAttachment> SYNC_HANDLER = new SyncHandler();

    private final Map<BlockPos,DataHolder> data = new HashMap<>();
    private final IAttachmentHolder parent;
    public VariantChunkDataStorageAttachment(IAttachmentHolder parent) { this.parent = parent; }

    public IVariantDataStorage getData(BlockPos pos) { return new DataWrapper(this,pos); }

    private DataHolder getOrCreateData(BlockPos pos)
    {
        if(!this.data.containsKey(pos))
            return new DataHolder();
        return this.data.get(pos);
    }

    private void setChanged() { this.parent.setData(ModAttachmentTypes.VARIANT_CHUNK_DATA,this); }

    private record DataWrapper(VariantChunkDataStorageAttachment parent,BlockPos pos) implements IVariantDataStorage
    {
        @Nullable
        @Override
        public ResourceLocation getCurrentVariant() {
            DataHolder data = this.parent.data.get(this.pos);
            return data != null ? data.variantID : null;
        }
        @Override
        public boolean isVariantLocked() {
            DataHolder data = this.parent.data.get(this.pos);
            return data != null && data.locked;
        }
        @Override
        public void setVariant(@Nullable ResourceLocation variant, boolean locked) {
            DataHolder data = this.parent.getOrCreateData(this.pos);
            data.variantID = variant;
            data.locked = locked;
            if(data.shouldSave())
                this.parent.data.put(this.pos,data);
            else
                this.parent.data.remove(this.pos);
            this.parent.setChanged();
        }
    }

    private static class DataHolder
    {
        @Nullable
        private ResourceLocation variantID;
        private boolean locked = false;

        private DataHolder() {  }

        private boolean shouldSave() { return this.variantID != null || this.locked; }

        private CompoundTag write()
        {
            CompoundTag tag = new CompoundTag();
            if(this.variantID != null)
                tag.putString("variant",this.variantID.toString());
            if(this.locked)
                tag.putBoolean("locked",true);
            return tag;
        }

        private static DataHolder read(CompoundTag tag)
        {
            DataHolder result = new DataHolder();
            if(tag.contains("variant"))
                result.variantID = VersionUtil.parseResource(tag.getString("variant"));
            result.locked = tag.getBoolean("locked");
            return result;
        }

        private void encode(FriendlyByteBuf buffer)
        {
            boolean hasVariant = this.variantID != null;
            buffer.writeBoolean(hasVariant);
            if(hasVariant)
                buffer.writeResourceLocation(this.variantID);
            buffer.writeBoolean(this.locked);
        }

        private static DataHolder decode(FriendlyByteBuf buffer)
        {
            DataHolder result = new DataHolder();
            if(buffer.readBoolean())
                result.variantID = buffer.readResourceLocation();
            result.locked = buffer.readBoolean();
            return result;
        }

    }

    private static class Serializer implements IAttachmentSerializer<ListTag,VariantChunkDataStorageAttachment>
    {
        @Override
        @Nullable
        public ListTag write(VariantChunkDataStorageAttachment attachment, HolderLookup.Provider provider) {
            ListTag tag = new ListTag();
            attachment.data.forEach((pos,data) -> {
                CompoundTag entry = data.write();
                entry.put("pos",TagUtil.saveBlockPos(pos));
                tag.add(entry);
            });
            return tag.isEmpty() ? null : tag;
        }
        @Override
        public VariantChunkDataStorageAttachment read(IAttachmentHolder holder, ListTag tag, HolderLookup.Provider provider) {
            VariantChunkDataStorageAttachment result = new VariantChunkDataStorageAttachment(holder);
            for(int i = 0; i < tag.size(); ++i)
            {
                CompoundTag entry = tag.getCompound(i);
                DataHolder data = DataHolder.read(tag.getCompound(i));
                BlockPos pos = TagUtil.loadBlockPos(entry.getCompound("pos"));
                result.data.put(pos,data);
            }
            return result;
        }
    }

    private static class SyncHandler implements AttachmentSyncHandler<VariantChunkDataStorageAttachment>
    {

        @Override
        public void write(RegistryFriendlyByteBuf buffer, VariantChunkDataStorageAttachment data, boolean initialSync) {
            buffer.writeInt(data.data.size());
            data.data.forEach((pos,d) -> {
                buffer.writeBlockPos(pos);
                d.encode(buffer);
            });
        }
        @Override
        @Nullable
        public VariantChunkDataStorageAttachment read(IAttachmentHolder holder, RegistryFriendlyByteBuf buffer, @Nullable VariantChunkDataStorageAttachment oldData) {
            VariantChunkDataStorageAttachment data = oldData == null ? new VariantChunkDataStorageAttachment(holder) : oldData;
            data.data.clear();
            int count = buffer.readInt();
            for(int i = 0; i < count; ++i)
            {
                BlockPos pos = buffer.readBlockPos();
                DataHolder d = DataHolder.decode(buffer);
                data.data.put(pos,d);
            }
            return data;
        }
    }

}
