package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.SafeEjectionAPI;
import io.github.lightman314.lightmanscurrency.api.ejection.builtin.BasicEjectionData;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxSaveData;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TaxBlockEntity extends EasyBlockEntity {

    private long taxEntryID = -1;
    private boolean validBreak = false;

    public final TaxEntry getTaxEntry() { return TaxSaveData.GetTaxEntry(this.taxEntryID, this.isClient()); }

    public TaxBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.TAX_BLOCK.get(), pos, state); }
    protected TaxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    public void initialize(Player owner) {
        this.taxEntryID = TaxSaveData.CreateAndRegister(this, owner);
        this.setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public void flagAsValidBreak() { this.validBreak = true; }

    public List<ItemStack> getContents(boolean dropBlock)
    {
        List<ItemStack> drops = new ArrayList<>();
        //Drop the block (if applicable)
        if(dropBlock)
            drops.add(new ItemStack(this.getBlockState().getBlock()));
        //Drop stored money
        TaxEntry entry = this.getTaxEntry();
        if(entry != null)
        {
            //Add stored money
            for(MoneyValue value : entry.getStoredMoney().allValues())
            {
                List<ItemStack> items = value.onBlockBroken(entry.getOwner());
                if(items != null)
                    drops.addAll(items);
            }
            entry.clearStoredMoney();
        }
        return drops;
    }

    public void onRemove() {
        TaxEntry entry = this.getTaxEntry();
        if(entry != null)
        {
            //Eject the tax contents for safekeeping
            if(!this.validBreak)
            {
                EjectionData data = new BasicEjectionData(entry.getOwner(),this.getContents(true),entry.getName());
                SafeEjectionAPI.getApi().handleEjection(this.level,this.worldPosition,data);
                this.validBreak = true;
            }
            TaxSaveData.RemoveEntry(this.taxEntryID);
        }
    }

    @Override
    public void onLoad() {
        if(this.isClient()) //Request update packet on client so that the block entity renderer knows the tax entry area.
            BlockEntityUtil.requestUpdatePacket(this);
        else
        {
            TaxEntry entry = this.getTaxEntry();
            if(entry != null)
                entry.moveCenter(WorldPosition.ofBE(this));
        }
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
        compound.putLong("EntryID", this.taxEntryID);
        super.saveAdditional(compound,lookup);
    }

    @Override
    public void loadAdditional(@Nonnull CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
        super.loadAdditional(compound,lookup);
        if(compound.contains("EntryID"))
            this.taxEntryID = compound.getLong("EntryID");
    }

}
