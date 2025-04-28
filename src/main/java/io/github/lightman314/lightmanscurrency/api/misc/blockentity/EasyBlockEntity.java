package io.github.lightman314.lightmanscurrency.api.misc.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
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

    @Override
    public void setVariant(@Nullable ResourceLocation variant) {
        this.currentVariant = variant;
        this.setChanged();
        if(this.isServer())
            BlockEntityUtil.sendUpdatePacket(this);
    }

    public EasyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public boolean isClient() { return this.level == null || this.level.isClientSide; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if(this.currentVariant != null)
            tag.putString("Variant",this.currentVariant.toString());
        else
            tag.putBoolean("NoVariant",true);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains("Variant"))
            this.currentVariant = VersionUtil.parseResource(tag.getString("Variant"));
        else if(tag.contains("NoVariant"))
            this.currentVariant = null;
        super.loadAdditional(tag, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) { return this.saveWithoutMetadata(lookup); }

}
