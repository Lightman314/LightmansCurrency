package io.github.lightman314.lightmanscurrency.api.misc.blockentity;

import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EasyBlockEntity extends BlockEntity implements IClientTracker, IVariantSupportingBlockEntity {

    public final RegistryAccess registryAccess() { return this.level.registryAccess(); }

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if(this.currentVariant != null)
            tag.putString("Variant",this.currentVariant.toString());
        if(this.variantLocked)
            tag.putBoolean("VariantLocked",this.variantLocked);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains("Variant"))
            this.currentVariant = VersionUtil.parseResource(tag.getString("Variant"));
        else if(tag.contains("NoVariant"))
            this.currentVariant = null;
        if(tag.contains("VariantLocked"))
            this.variantLocked = tag.getBoolean("VariantLocked");
        super.loadAdditional(tag, registries);
    }

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

}
