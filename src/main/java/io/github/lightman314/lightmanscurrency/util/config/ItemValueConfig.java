package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemValueConfig implements Supplier<ItemLike> {

    private final ForgeConfigSpec.ConfigValue<String> baseConfig;
    private final Supplier<Item> defaultSupplier;
    private final Supplier<ForgeConfigSpec> specSupplier;
    private Predicate<Item> isAllowed = i -> true;

    private Item cachedItem = null;
    private ItemValueConfig(ForgeConfigSpec.ConfigValue<String> baseConfig, Supplier<Item> defaultSupplier, Supplier<ForgeConfigSpec> specSupplier) {
        this.baseConfig = baseConfig;
        this.defaultSupplier = defaultSupplier;
        this.specSupplier = specSupplier;
        //Register to the mod event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReloaded);
    }

    public ItemValueConfig withCheck(@Nonnull Predicate<Item> isAllowed) { this.isAllowed = isAllowed; return this; }

    public void onConfigReloaded(ModConfigEvent event)
    {
        if(event.getConfig().getSpec() == this.specSupplier.get())
            this.cachedItem = null;
    }

    @Override
    public Item get() {
        if(this.cachedItem != null)
            return this.cachedItem;
        String itemID = this.baseConfig.get();
        try{
            if(itemID.isBlank())
                this.cachedItem = Items.AIR;
            else
                this.cachedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));
        } catch(Throwable t) { LightmansCurrency.LogDebug("Error loading item from config value."); }
        if(this.cachedItem == null)
        {
            this.cachedItem = this.defaultSupplier.get();
            LightmansCurrency.LogWarning("Could not load an item from a Config Input of \"" + itemID + "\". Assuming default value!");
        }

        //Test the predicate on each get, just in case the requirement has been updated.
        return this.isAllowed.test(this.cachedItem) ? this.cachedItem : this.defaultSupplier.get();
    }

    private static Supplier<Item> convertDefault(ResourceLocation defaultItem) { return () -> ForgeRegistries.ITEMS.getValue(defaultItem); }

    private static boolean IsValidInput(Object o) {
        if(o instanceof String s)
        {
            try{
                return ForgeRegistries.ITEMS.getValue(new ResourceLocation(s)) != null;
            } catch(Throwable ignored) {}
        }
        return false;
    }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, String defaultItem, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, new ResourceLocation(defaultItem), specSupplier); }
    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, ResourceLocation defaultItem, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, convertDefault(defaultItem), specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, ResourceLocation defaultItem, Supplier<Item> defaultItemSupplier, Supplier<ForgeConfigSpec> specSupplier) {
        ForgeConfigSpec.ConfigValue<String> baseConfig = builder.define(path, defaultItem.toString(), ItemValueConfig::IsValidInput);
        return new ItemValueConfig(baseConfig, defaultItemSupplier, specSupplier);
    }

}