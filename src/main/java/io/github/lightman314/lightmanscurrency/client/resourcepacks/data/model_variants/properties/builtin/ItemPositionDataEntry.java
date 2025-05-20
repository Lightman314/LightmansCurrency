package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionData;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.RotationHandler;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperty;
import io.github.lightman314.lightmanscurrency.datagen.client.builders.ItemPositionBuilder;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ItemPositionDataEntry {

    public static final VariantProperty<ItemPositionDataEntry> PROPERTY = new ItemPositionDataProperty();

    public abstract ItemPositionData get();
    public abstract JsonElement write();
    public static ItemPositionDataEntry create(ResourceLocation positionDataID) { return new IDEntry(positionDataID); }
    public static ItemPositionDataEntry create(ItemPositionData data) { return new InstanceEntry(data); }
    public static ItemPositionDataEntry create(ItemPositionBuilder data) { return new BuilderEntry(data); }

    private static class ItemPositionDataProperty extends VariantProperty<ItemPositionDataEntry>
    {

        private ItemPositionDataProperty() {}

        @Override
        public ItemPositionDataEntry parse(JsonElement element) throws JsonSyntaxException, ResourceLocationException {
            String elementName = this.getID().toString();
            if(element.isJsonPrimitive())
            {
                ResourceLocation dataID = VersionUtil.parseResource(GsonHelper.convertToString(element,elementName));
                return new IDEntry(dataID);
            }
            else
                return new InstanceEntry(ItemPositionData.parse(GsonHelper.convertToJsonObject(element, elementName)));
        }

        @Override
        public JsonElement write(Object value) {
            if(value instanceof ItemPositionDataEntry data)
                return data.write();
            else
                throw new IllegalArgumentException("Value must be an ItemPositionDataEntry element!");
        }

    }

    private static class IDEntry extends ItemPositionDataEntry
    {
        private final ResourceLocation positionDataID;
        public IDEntry(ResourceLocation id) { this.positionDataID = id; }

        @Override
        public ItemPositionData get() { return ItemPositionManager.getDataOrEmpty(this.positionDataID); }
        @Override
        public JsonElement write() { return new JsonPrimitive(this.positionDataID.toString()); }
    }

    private static class InstanceEntry extends ItemPositionDataEntry
    {
        private final ItemPositionData data;
        public InstanceEntry(ItemPositionData data) { this.data = data; }

        @Override
        public ItemPositionData get() { return this.data; }
        @Override
        public JsonElement write() {
            ItemPositionBuilder builder = ItemPositionBuilder.builder();
            for(int i = 0; i < this.data.getEntryCount(); ++i)
            {
                ItemPositionData.PositionEntry entry = this.data.safeGetEntry(i);
                builder.withEntry(entry.position())
                        .withExtraCount(entry.extraCount())
                        .withExtraOffset(entry.extraOffset())
                        .withScale(entry.scale())
                        .withMinLight(entry.minLight())
                        .withRotationType(RotationHandler.getRotationType(entry.rotationHandler()));
            }
            return builder.write();
        }
    }

    private static class BuilderEntry extends ItemPositionDataEntry
    {
        private final ItemPositionBuilder builder;
        private final Supplier<ItemPositionData> dataSource;
        public BuilderEntry(ItemPositionBuilder builder) { this.builder = builder; this.dataSource = Suppliers.memoize(() -> ItemPositionData.parse(this.builder.write())); }

        @Override
        public ItemPositionData get() { return this.dataSource.get(); }
        @Override
        public JsonElement write() { return this.builder.write(); }
    }

}