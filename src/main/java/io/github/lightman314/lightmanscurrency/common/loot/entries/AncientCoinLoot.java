package io.github.lightman314.lightmanscurrency.common.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.core.ModLootPoolEntryTypes;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class AncientCoinLoot extends LootPoolSingletonContainer {

    private final AncientCoinType type;
    protected AncientCoinLoot(@Nonnull AncientCoinType type, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.type = type;
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> consumer, @Nonnull LootContext lootContext) { consumer.accept(this.type.asItem()); }

    @Nonnull
    @Override
    public LootPoolEntryType getType() { return ModLootPoolEntryTypes.ANCIENT_COIN_TYPE.get(); }

    public static LootPoolSingletonContainer.Builder<?> ancientCoin(@Nonnull AncientCoinType type) {
        return simpleBuilder((p_79583_, p_79584_, p_79585_, p_79586_) -> new AncientCoinLoot(type, p_79583_, p_79584_, p_79585_, p_79586_));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<AncientCoinLoot>
    {
        @Override
        public void serializeCustom(@Nonnull JsonObject json, @Nonnull AncientCoinLoot entry, @Nonnull JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("coin",entry.type.toString());
        }
        @Nonnull
        @Override
        protected AncientCoinLoot deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, int weight, int quality, @Nonnull LootItemCondition[] conditions, @Nonnull LootItemFunction[] functions) {
            AncientCoinType type = EnumUtil.enumFromString(GsonHelper.getAsString(json,"coin"),AncientCoinType.values(),null);
            if(type == null)
                throw new JsonSyntaxException(GsonHelper.getAsString(json,"coin") + " is not a valid ancient coin type!");
            return new AncientCoinLoot(type,weight,quality,conditions,functions);
        }
    }

}
