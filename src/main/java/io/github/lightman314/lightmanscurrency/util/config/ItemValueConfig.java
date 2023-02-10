package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemValueConfig implements Supplier<Item> {

    private final ForgeConfigSpec.ConfigValue<String> baseConfig;
    private final Supplier<Item> defaultSupplier;
    private final Supplier<ForgeConfigSpec> specSupplier;
    private final Predicate<Item> isAllowed;

    private Item cachedItem = null;
    private ItemValueConfig(ForgeConfigSpec.ConfigValue<String> baseConfig, Supplier<Item> defaultSupplier, Predicate<Item> isAllowed, Supplier<ForgeConfigSpec> specSupplier) {
        this.baseConfig = baseConfig;
        this.defaultSupplier = defaultSupplier;
        this.specSupplier = specSupplier;
        this.isAllowed = isAllowed;
        //Register to the mod event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReloaded);
    }

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
        try{ this.cachedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));
        } catch(Throwable t) { LightmansCurrency.LogDebug("Error loading item from config value."); }
        if(this.cachedItem == null)
            this.cachedItem = this.defaultSupplier.get();
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

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, ResourceLocation defaultItem, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, convertDefault(defaultItem), specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, ResourceLocation defaultItem, Supplier<Item> defaultItemSupplier, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, defaultItemSupplier, i -> true, specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, ResourceLocation defaultItem, Predicate<Item> itemAllowed, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, convertDefault(defaultItem), itemAllowed, specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, ResourceLocation defaultItem, Supplier<Item> defaultItemSupplier, Predicate<Item> itemAllowed, Supplier<ForgeConfigSpec> specSupplier) {
        ForgeConfigSpec.ConfigValue<String> baseConfig = builder.define(path, defaultItem.toString(), ItemValueConfig::IsValidInput);
        return new ItemValueConfig(baseConfig, defaultItemSupplier, itemAllowed, specSupplier);
    }

}