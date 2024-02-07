package io.github.lightman314.lightmanscurrency.common.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.core.ModLootPoolEntryTypes;
import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ConfigLoot extends LootPoolSingletonContainer {

    private final ConfigItemTier tier;

    protected ConfigLoot(ConfigItemTier tier, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions)
    {
        super(weight, quality, conditions, functions);
        this.tier = tier;
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> consumer, @Nonnull LootContext context) { consumer.accept(new ItemStack(tier.getItem())); }

    @Nonnull
    @Override
    public LootPoolEntryType getType() { return ModLootPoolEntryTypes.LOOT_TIER_TYPE.get(); }

    public static LootPoolSingletonContainer.Builder<?> lootTableTier(@Nonnull ConfigItemTier tier) {
        return simpleBuilder((p_79583_, p_79584_, p_79585_, p_79586_) -> new ConfigLoot(tier, p_79583_, p_79584_, p_79585_, p_79586_));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ConfigLoot>
    {

        @Override
        public void serializeCustom(@Nonnull JsonObject json, @Nonnull ConfigLoot entry, @Nonnull JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("tier", entry.tier.tier);
        }

        @Nonnull
        @Override
        protected ConfigLoot deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, int weight, int quality, @Nonnull LootItemCondition[] conditions, @Nonnull LootItemFunction[] functions) {
            int tierNum = GsonHelper.getAsInt(json, "tier");
            ConfigItemTier tier = ConfigItemTier.get(tierNum);
            if(tier == null)
                throw new JsonSyntaxException("Tier must be a valid number between 1 and 6!");
            return new ConfigLoot(tier, weight, quality, conditions, functions);
        }
    }

}
