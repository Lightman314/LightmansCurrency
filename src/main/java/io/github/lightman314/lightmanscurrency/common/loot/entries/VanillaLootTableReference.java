package io.github.lightman314.lightmanscurrency.common.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.core.ModLootPoolEntryTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VanillaLootTableReference extends LootPoolSingletonContainer {
    final ResourceLocation name;

    private VanillaLootTableReference(ResourceLocation name, int weight, int quality, LootItemCondition[] condition, LootItemFunction[] functions) {
        super(weight, quality, condition, functions);
        this.name = name;
    }

    public LootPoolEntryType getType() { return ModLootPoolEntryTypes.VANILLA_LOOT_TABLE.get(); }

    @SuppressWarnings("deprecation")
    public void createItemStack(@Nonnull Consumer<ItemStack> consumer, @Nonnull LootContext context) {
        LootTable table = context.getResolver().getLootTable(this.name);
        table.getRandomItemsRaw(context,consumer);
    }

    public void validate(ValidationContext context) {
        LootDataId<LootTable> table = new LootDataId<>(LootDataType.TABLE, this.name);
        if (context.hasVisitedElement(table)) {
            context.reportProblem("Table " + this.name + " is recursively called");
        } else {
            super.validate(context);
            //Validate child table if it's present, but if it's not present I'll assume that this is being validated during datagen where the vanilla table doesn't exist
            context.resolver().getElementOptional(table).ifPresent((t) ->
                t.validate(context.enterElement("->{" + this.name + "}", table))
            );
        }
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation name) {
        if(!BuiltInLootTables.all().contains(name))
            throw new IllegalArgumentException(name + " is not a valid vanilla loot table!");
        return simpleBuilder((weight, quality, conditions, functions) -> new VanillaLootTableReference(name, weight, quality, conditions, functions));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<VanillaLootTableReference> {
        public Serializer() {}

        public void serializeCustom(JsonObject json, VanillaLootTableReference entry, JsonSerializationContext context) {
            super.serializeCustom(json, entry, context);
            json.addProperty("name", entry.name.toString());
        }

        protected VanillaLootTableReference deserialize(JsonObject json, JsonDeserializationContext context, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
            ResourceLocation name = new ResourceLocation(GsonHelper.getAsString(json, "name"));
            if(!BuiltInLootTables.all().contains(name))
                throw new JsonSyntaxException(name + " is not a valid vanilla loot table!");
            return new VanillaLootTableReference(name, weight, quality, conditions, functions);
        }
    }
}
