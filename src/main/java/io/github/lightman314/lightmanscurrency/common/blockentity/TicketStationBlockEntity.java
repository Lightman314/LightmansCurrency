package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public class TicketStationBlockEntity extends EasyBlockEntity {

	private final LCItemStackHandler storage = new LCItemStackHandler(2);
	public LCItemStackHandler getStorage() { return this.storage; }

	public TicketStationRecipeInput getRecipeInput(TicketStationRecipe.ExtraData data) { return new TicketStationRecipeInput(this.storage,data); }
	
	public TicketStationBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.TICKET_MACHINE.get(), pos, state);
		this.storage.withListener(this::setChanged);
	}

	@Override
	protected void saveAdditional(CompoundTag tag,DataContext<Tag> context) {
        tag.put("Items",context.write(this.storage::serializeNBT));
		super.saveAdditional(tag,context);
	}

	@Override
	protected void loadAdditional(CompoundTag tag,DataContext<Tag> context) {
        this.storage.safeLoad(tag,"Items",context);
		super.loadAdditional(tag,context);
	}
	
}
