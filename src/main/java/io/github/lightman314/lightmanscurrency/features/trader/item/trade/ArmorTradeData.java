package io.github.lightman314.lightmanscurrency.features.trader.item.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ArmorTradeData extends ItemTradeData {

    public static final MapCodec<ArmorTradeData> MAP_CODEC = buildCodec(ArmorTradeData::new);
    public static final Codec<ArmorTradeData> CODEC = MAP_CODEC.codec();

    public static final TradeDataType<ArmorTradeData> TYPE = new TradeDataType<>(MAP_CODEC);

    @Nullable
    private EquipmentSlot slot;
    public ArmorTradeData() { super(); }
    public ArmorTradeData(TradePrice price,List<TradeItem> sellItems,Map<TradeRuleType<?>,TradeRule> ruleMap) { super(price,sellItems,ruleMap); }

    protected EquipmentSlot getEquipmentSlot() {
        if(this.slot != null)
            return this.slot;
        if(this.getHolder() != null) {
            int index = this.getHolder().getTrades().indexOf(this);
            if(index >= 0)
            {
                this.slot = switch (Math.abs(index) % 4) {
                    case 1 -> EquipmentSlot.CHEST;
                    case 2 -> EquipmentSlot.LEGS;
                    case 3 -> EquipmentSlot.FEET;
                    default -> EquipmentSlot.HEAD;
                };
            }
            else
                this.slot = EquipmentSlot.OFFHAND;
            return this.slot;
        }
        return EquipmentSlot.OFFHAND;
    }

    @Override
    @Nullable
    public Identifier getBackgroundOverride() {
        return switch (this.getEquipmentSlot()) {
            case HEAD -> Identifier.withDefaultNamespace("container/slot/helmet");
            case CHEST -> Identifier.withDefaultNamespace("container/slot/chestplate");
            case LEGS -> Identifier.withDefaultNamespace("container/slot/leggings");
            case FEET -> Identifier.withDefaultNamespace("container/slot/boots");
            default -> null;
        };
    }

    @Override
    protected ItemResource mutateSellItem(ItemResource resource) {
        //Check if it can be equipped in the required slot
        Equippable e = resource.get(DataComponents.EQUIPPABLE);
        if(e != null && e.slot() == this.getEquipmentSlot())
            return resource;
        return ItemResource.EMPTY;
    }

}