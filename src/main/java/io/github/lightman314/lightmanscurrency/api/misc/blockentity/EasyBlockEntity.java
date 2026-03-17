package io.github.lightman314.lightmanscurrency.api.misc.blockentity;

import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.IBuilderProvider;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.network.message.lazy.BPacketLazyBlockEntity;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class EasyBlockEntity extends BlockEntity implements IClientTracker, IVariantDataStorage, IBuilderProvider {

    @Override
    public final HolderLookup.Provider registryAccess() { return this.level.registryAccess(); }

    public final DataContext<Tag> dataContext() { return DataContext.createNBT(this.registryAccess()); }

    @Nullable
    private ResourceLocation currentVariant = null;
    @Nullable
    @Override
    public ResourceLocation getCurrentVariant() { return this.currentVariant; }
    private boolean variantLocked = false;
    @Override
    public boolean isVariantLocked() { return this.variantLocked; }

    @Override
    public void setVariant(@Nullable ResourceLocation variant, boolean variantLocked) {
        this.currentVariant = variant;
        this.variantLocked = variantLocked;
        this.setChanged();
        if(this.isServer())
            BlockEntityUtil.sendUpdatePacket(this,this.saveVariantPacket());
    }

    public EasyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public boolean isClient() { return this.level == null || this.level.isClientSide; }

    protected final CompoundTag saveVariantPacket()
    {
        CompoundTag tag = new CompoundTag();
        if(this.currentVariant != null)
            tag.putString("Variant",this.currentVariant.toString());
        else
            tag.putBoolean("NoVariant",true);
        tag.putBoolean("VariantLocked",this.variantLocked);
        return tag;
    }

    @Override
    protected final void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        if(this.currentVariant != null)
            tag.putString("Variant",this.currentVariant.toString());
        if(this.variantLocked)
            tag.putBoolean("VariantLocked",this.variantLocked);
        this.saveAdditional(tag,DataContext.createNBT(lookup));
    }

    protected void saveAdditional(CompoundTag tag, DataContext<Tag> context) {}

    @Override
    protected final void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Variant"))
        {
            try {
                this.currentVariant = ResourceLocation.parse(tag.getString("Variant"));
            } catch (ResourceLocationException ignored) {}
        }
        else if(tag.contains("NoVariant"))
            this.currentVariant = null;
        if(tag.contains("VariantLocked"))
            this.variantLocked = tag.getBoolean("VariantLocked");
        super.loadAdditional(tag,lookup);
        this.loadAdditional(tag,DataContext.createNBT(lookup));
    }

    protected void loadAdditional(CompoundTag tag,DataContext<Tag> context) {}

    @Override
    public void onLoad() {
        if(this.currentVariant != null)
        {
            BlockState state = this.getBlockState();
            if(VariantProvider.getVariantBlock(state.getBlock()) != null && !state.getValue(IVariantBlock.VARIANT))
                this.level.setBlockAndUpdate(this.worldPosition,state.setValue(IVariantBlock.VARIANT,true));
        }
        if(this.isClient())
            BlockEntityUtil.requestUpdatePacket(this);
        super.onLoad();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) { return this.saveWithoutMetadata(lookup); }

    public final void sendPacket(LazyPacketData.Builder message) {
        BPacketLazyBlockEntity packet = new BPacketLazyBlockEntity(this.worldPosition,message.build());
        if(this.isClient())
            packet.sendToServer();
        else if(this.level instanceof ServerLevel sl)
            packet.sendToPlayersTrackingChunk(sl,new ChunkPos(this.worldPosition));
    }

    public void handleMessage(Player player,LazyPacketData message) {}
}
