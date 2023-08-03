package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxSaveData;
import io.github.lightman314.lightmanscurrency.common.taxes.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
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
    }

    public void onValidBreak()
    {
        this.validBreak = true;
        InventoryUtil.dumpContents(this.level, this.worldPosition, this.collectDrops());
        TaxSaveData.RemoveEntry(this.taxEntryID);
    }

    private List<ItemStack> collectDrops()
    {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this.getBlockState().getBlock()));
        //TODO drop stored coins from the entry
        return drops;
    }

    public void onRemove() {
        TaxEntry entry = this.getTaxEntry();
        if(entry != null)
        {
            //Eject the tax contents for safekeeping
            if(!this.validBreak)
            {
                EjectionSaveData.HandleEjectionData(this.level, this.worldPosition, EjectionData.create(this.level, this.worldPosition, this.getBlockState(), IDumpable.preCollected(this.collectDrops(), entry.getName(), entry.getOwner())));
                this.validBreak = true;
                TaxSaveData.RemoveEntry(this.taxEntryID);
            }
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
    protected void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putLong("EntryID", this.taxEntryID);
    }

    @Override
    public void load(@Nonnull CompoundTag compound) {
        super.load(compound);
        if(compound.contains("EntryID"))
            this.taxEntryID = compound.getLong("EntryID");
    }

}