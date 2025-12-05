package io.github.lightman314.lightmanscurrency.api.variants.block.builtin;

import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.network.message.cap.SPacketSyncVariantChunkCap;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VariantChunkDataStorageAttachment implements ICapabilitySerializable<ListTag>, IClientTracker {

    public static final Capability<VariantChunkDataStorageAttachment> CAP = CapabilityManager.get(new CapabilityToken<>(){});

    private final LazyOptional<VariantChunkDataStorageAttachment> optional = LazyOptional.of(() -> this);
    private final LevelChunk parent;
    private final Map<BlockPos,DataHolder> data = new HashMap<>();
    public VariantChunkDataStorageAttachment(LevelChunk parent) { this.parent = parent; }

    @Override
    public boolean isClient() { return this.parent.getLevel().isClientSide; }

    public IVariantDataStorage getData(BlockPos pos) { return new DataWrapper(this,pos); }

    private DataHolder getOrCreateData(BlockPos pos)
    {
        if(!this.data.containsKey(pos))
            return new DataHolder();
        return this.data.get(pos);
    }

    private void setChanged() {
        //Send sync packet to the client
        if(this.isServer())
            new SPacketSyncVariantChunkCap(this.parent.getPos(),this.copyData()).sendToTarget(PacketDistributor.TRACKING_CHUNK.with(() -> this.parent));
    }

    public void loadData(Map<BlockPos,DataHolder> data)
    {
        if(this.isServer())
            return;
        this.data.clear();
        data.forEach((pos,entry) -> this.data.put(pos,entry.copy()));
    }

    private Map<BlockPos,DataHolder> copyData()
    {
        Map<BlockPos,DataHolder> result = new HashMap<>();
        this.data.forEach((pos,entry) ->
            result.put(pos,entry.copy()));
        return result;
    }

    public void syncWith(ServerPlayer player)
    {
        new SPacketSyncVariantChunkCap(this.parent.getPos(),this.copyData()).sendTo(player);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        return CAP.orEmpty(capability,this.optional);
    }

    @Override
    public ListTag serializeNBT() {
        ListTag tag = new ListTag();
        this.data.forEach((pos,data) -> {
            CompoundTag entry = data.write();
            entry.put("pos",TagUtil.saveBlockPos(pos));
            tag.add(entry);
        });
        return tag;
    }

    @Override
    public void deserializeNBT(ListTag tag) {
        this.data.clear();
        for(int i = 0; i < tag.size(); ++i)
        {
            CompoundTag entry = tag.getCompound(i);
            DataHolder data = DataHolder.read(tag.getCompound(i));
            BlockPos pos = TagUtil.loadBlockPos(entry.getCompound("pos"));
            this.data.put(pos,data);
        }
    }

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

    public static class DataHolder
    {
        @Nullable
        public ResourceLocation variantID;
        public boolean locked = false;

        public DataHolder() { }

        private boolean shouldSave() { return this.variantID != null || this.locked; }

        DataHolder copy() {
            DataHolder copy = new DataHolder();
            copy.variantID = this.variantID;
            copy.locked = this.locked;
            return copy;
        }

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

        public void encode(FriendlyByteBuf buffer)
        {
            boolean hasVariant = this.variantID != null;
            buffer.writeBoolean(hasVariant);
            if(hasVariant)
                buffer.writeResourceLocation(this.variantID);
            buffer.writeBoolean(this.locked);
        }

        public static DataHolder decode(FriendlyByteBuf buffer)
        {
            DataHolder result = new DataHolder();
            if(buffer.readBoolean())
                result.variantID = buffer.readResourceLocation();
            result.locked = buffer.readBoolean();
            return result;
        }

    }

}