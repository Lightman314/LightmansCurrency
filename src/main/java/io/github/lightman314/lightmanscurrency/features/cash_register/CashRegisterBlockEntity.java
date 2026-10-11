package io.github.lightman314.lightmanscurrency.features.cash_register;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.AbstractTabbedCustomerMenu;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.*;

public class CashRegisterBlockEntity extends EasyBlockEntity.Synced implements TraderSource.Multi {

    public static final TextEntry MESSAGE_NOT_LINKED = TextEntry.message(LCApi.MODID,"cash_register.not_linked");

    @Nullable
    private Component name;
    private List<BlockPos> traderPositions = new ArrayList<>();

    public CashRegisterBlockEntity(BlockPos worldPosition, BlockState blockState) { this(LCBlockEntities.CASH_REGISTER.get(),worldPosition,blockState); }
    protected CashRegisterBlockEntity(BlockEntityType<?> type,BlockPos worldPosition, BlockState blockState) {
        super(type,worldPosition,blockState);
    }

    public List<BlockPos> getTraderPositions() { return Collections.unmodifiableList(this.traderPositions); }
    public void setTraderPositions(List<BlockPos> set) {
        if(this.traderPositions.equals(set))
            return;
        this.traderPositions = new ArrayList<>(set);
        this.sendPacket(FancyPacketMap.map().setList("traderPositions",LCFancyPacketTypes.BLOCK_POS,List.copyOf(this.traderPositions)));
    }

    @Override
    public void writeInitialPacket(FancyPacketMap.Mutable packet, Player player) {
        packet.setList("traderPositions",LCFancyPacketTypes.BLOCK_POS,List.copyOf(this.traderPositions));
        if(this.name != null)
            packet.setText("name",this.name);
    }

    @Override
    public void handleSyncPacket(FancyPacketMap packet) {
        if(packet.contains("traderPositions"))
            this.traderPositions = packet.getList("traderPositions",LCFancyPacketTypes.BLOCK_POS);
        if(packet.contains("name"))
            this.name = packet.getText("name");
    }

    @Override
    @Nullable
    public Component getNameOverride() {
        if(this.name != null)
            return this.name;
        return null;
    }

    @Override
    public boolean forceSearch() { return this.traderPositions.size() > 1; }

    @Override
    public List<TraderData> getTraders() {
        if(this.level == null)
            return List.of();
        List<TraderData> traders = new ArrayList<>();
        for(BlockPos pos : Set.copyOf(this.traderPositions)) {
            if(this.level.getBlockEntity(pos) instanceof TraderBlockEntity be) {
                TraderData trader = be.getTrader();
                if(trader != null && !traders.contains(trader))
                    traders.add(trader);
            }
        }
        return traders;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<BlockPos> list = output.list("traderPositions",BlockPos.CODEC);
        for(BlockPos p : this.traderPositions)
            list.add(p);
        if(this.name != null)
            output.store("name",ComponentSerialization.CODEC,this.name);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.traderPositions = new ArrayList<>();
        ValueInput.TypedInputList<BlockPos> list = input.listOrEmpty("traderPositions",BlockPos.CODEC);
        this.traderPositions.addAll(list.stream().toList());
        this.name = input.read("name",ComponentSerialization.CODEC).orElse(null);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        if(this.name != null)
            components.set(DataComponents.CUSTOM_NAME,this.name);
        components.set(LCDataComponents.TRADER_POSITIONS,new TraderPositionSet(this.traderPositions));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        TraderPositionSet positions = components.getOrDefault(LCDataComponents.TRADER_POSITIONS, TraderPositionSet.EMPTY);
        this.traderPositions = new ArrayList<>(positions.positions());
        this.name = components.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("traderPositions");
        output.discard("name");
    }

    public void tryOpenMenu(Player player) {
        if(this.getTraders().isEmpty())
            player.sendSystemMessage(MESSAGE_NOT_LINKED.get());
        else
            player.openMenu(AbstractTabbedCustomerMenu.blockEntityProvider(this.worldPosition));
    }

}