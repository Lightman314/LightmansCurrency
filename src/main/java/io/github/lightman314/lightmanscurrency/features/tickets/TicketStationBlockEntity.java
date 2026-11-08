package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.helpers.RecipeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketRecipeData;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;

public class TicketStationBlockEntity extends EasyBlockEntity implements MenuProvider {

    private Component name = null;
    public Component getName() { return name == null ? this.getBlockState().getBlock().getName() : this.name; }

    private final TicketStationStorage storage = new TicketStationStorage(this);
    public final NormalItemStorage getStorage() { return this.storage; }

    public TicketStationBlockEntity(BlockPos worldPosition,BlockState blockState) { this(LCBlockEntities.TICKET_STATION.get(),worldPosition,blockState); }
    protected TicketStationBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
        this.storage.setListener(this::setChanged);
    }

    public TicketStationRecipeInput getRecipeInput() { return this.getRecipeInput(TicketRecipeData.DEFAULT); }
    public TicketStationRecipeInput getRecipeInput(TicketRecipeData data) { return new TicketStationRecipeInput(this,this.storage,data); }

    public boolean isValidModifier(ItemStack item) {
        RecipeMap recipeMap = RecipeHelper.getRecipeMap(this.getLevel());
        return recipeMap.byType(LCRecipeTypes.TICKET_STATION.get()).stream().anyMatch(holder -> holder.value().validModifier(item));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.storage.serialize(output,"storage");
        output.storeNullable("name",ComponentSerialization.CODEC,this.name);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storage.deserialize(input,"storage");
        this.name = input.read("name",ComponentSerialization.CODEC).orElse(this.name);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.name = components.get(DataComponents.CUSTOM_NAME);
        this.storage.copyFrom(components.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).allItemsCopyStream().toList());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        if(this.name != null)
            components.set(DataComponents.CUSTOM_NAME,this.name);
        if(!this.storage.isEmpty())
            components.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(this.storage.getContents()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("name");
        output.discard("storage");
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        for(int i = 0; i < this.storage.size(); ++i)
            Block.popResource(this.level,pos,this.storage.getStack(i));
        this.storage.clear();
    }

    @Override
    public Component getDisplayName() { return this.name == null ? this.getBlockName() : this.name; }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TicketStationMenu(containerId,player,this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.getBlockPos());
    }

}