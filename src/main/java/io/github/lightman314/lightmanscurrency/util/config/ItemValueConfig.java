package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemValueConfig implements Supplier<Item>, IItemProvider {

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

    public void onConfigReloaded(ModConfig.ModConfigEvent event)
    {
        //Check if the config contains a matching path. If so, assume that this is the correct config.
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

    @Override
    public @Nonnull Item asItem() { return this.get(); }

    private static Supplier<Item> convertDefault(ResourceLocation defaultItem) { return () -> ForgeRegistries.ITEMS.getValue(defaultItem); }

    private static boolean IsValidInput(Object o) {
        if(o instanceof String)
        {
            String s = (String)o;
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
