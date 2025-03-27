package io.github.lightman314.lightmanscurrency.common.text;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public final class TextEntry {

    private final Supplier<String> key;
    public String getKey() { return this.key.get(); }
    public TextEntry(@Nonnull String key) { this.key = () -> key; }
    public TextEntry(@Nonnull Supplier<String> key) { this.key = Suppliers.memoize(key::get); }

    @Nonnull
    public MutableComponent get(Object... objects) { return Component.translatable(this.getKey(),objects);}
    public MutableComponent getWithStyle(ChatFormatting... format) { return Component.translatable(this.getKey()).withStyle(format); }
    public List<Component> getAsList(Object... objects) { return Lists.newArrayList(this.get(objects)); }
    public List<Component> getAsListWithStyle(ChatFormatting... format) { return Lists.newArrayList(this.getWithStyle(format)); }
    public void tooltip(@Nonnull List<Component> tooltip, Object... objects) { tooltip.add(this.get(objects)); }
    @Nonnull
    public IconData icon(Object... objects) { return IconData.of(this.get(objects)); }

    //Vanilla Objects
    public static TextEntry item(@Nonnull RegistryObject<? extends ItemLike> item) { return new TextEntry(() -> item.get().asItem().getDescriptionId()); }
    public static TextEntry block(@Nonnull RegistryObject<? extends Block> block) { return new TextEntry(() -> block.get().getDescriptionId()); }
    public static TextEntry enchantment(@Nonnull RegistryObject<? extends Enchantment> enchantment) { return new TextEntry(() -> enchantment.get().getDescriptionId()); }
    public static TextEntry gamerule(@Nonnull String ruleKey) { return new TextEntry("gamerule." + ruleKey); }
    public static TextEntry profession(@Nonnull RegistryObject<? extends VillagerProfession> profession) { return new TextEntry(() -> {
        ResourceLocation id = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession.get());
        return "entity.minecraft.villager." + id.getNamespace() + "." + id.getPath();
    });}
    public static TextEntry creativeTab(@Nonnull String modid, @Nonnull String name) { return new TextEntry("itemGroup." + modid + "." + name); }
    public static TextEntry keyBind(@Nonnull String modid, @Nonnull String name) { return new TextEntry("key." + modid + "." + name); }
    public static TextEntry sound(@Nonnull String modid, @Nonnull String name) { return new TextEntry(modid + ".subtitle." + name); }
    public static TextEntry tooltip(@Nonnull String modid, @Nonnull String key) { return new TextEntry("tooltip." + modid + "." + key); }
    public static TextEntry gui(@Nonnull String modid, @Nonnull String key) { return new TextEntry("gui." + modid + "." + key); }
    public static TextEntry button(@Nonnull String modid, @Nonnull String key) { return new TextEntry("button." + modid + "." + key); }
    public static TextEntry widget(@Nonnull String modid, @Nonnull String key) { return new TextEntry("widget." + modid + "." + key); }
    public static TextEntry message(@Nonnull String modid, @Nonnull String key) { return new TextEntry("message." + modid + "." + key); }
    public static TextEntry blurb(@Nonnull String modid, @Nonnull String key) { return new TextEntry("blurb." + modid + "." + key); }
    public static TextEntry command(@Nonnull String modid, @Nonnull String key) { return new TextEntry("command." + modid + "." + key); }
    public static TextEntry argument(@Nonnull String key) { return new TextEntry("command.argument." + key); }

    //Custom Inputs
    public static TextEntry description(@Nonnull TextEntry parent) { return extend(parent,"desc"); }
    public static TextEntry plural(@Nonnull TextEntry parent) { return extend(parent,"plural"); }
    public static TextEntry initial(@Nonnull TextEntry parent) { return extend(parent,"initial"); }
    public static TextEntry tradeRule(@Nonnull TradeRuleType<?> type) { return new TextEntry(TradeRule.translationKeyOfType(type.type)); }
    public static TextEntry tradeRuleMessage(@Nonnull TradeRuleType<?> type, @Nonnull String message) { return new TextEntry(TradeRule.translationKeyOfType(type.type) + "." + message); }
    public static TextEntry notification(@Nonnull NotificationType<?> type) { return notification(type.type); }
    public static TextEntry notification(@Nonnull ResourceLocation type) { return new TextEntry("notification." + type.getNamespace() + "." + type.getPath()); }
    public static TextEntry notification(@Nonnull NotificationType<?> type, @Nonnull String extra) { return notification(type.type,extra); }
    public static TextEntry notification(@Nonnull ResourceLocation type, @Nonnull String extra) { return new TextEntry("notification." + type.getNamespace() + "." + type.getPath() + "." + extra); }

    public static TextEntry chain(@Nonnull String chain) { return new TextEntry("lightmanscurrency.money.chain." + chain); }
    public static TextEntry chainDisplay(@Nonnull String chain) { return new TextEntry("lightmanscurrency.money.chain." + chain + ".display"); }
    public static TextEntry chainDisplayWordy(@Nonnull String chain) { return new TextEntry("lightmanscurrency.money.chain." + chain + ".display.wordy"); }
    public static TextEntry lcStat(@Nonnull StatKey<?,?> statKey) { return new TextEntry(StatType.getTranslationKey(statKey.key)); }

    public static TextEntry reiGroup(@Nonnull String modid, @Nonnull String type) { return new TextEntry("rei." + modid + ".group." + type); }
    public static TextEntry jeiInfo(@Nonnull String modid, @Nonnull String type) { return new TextEntry("jei." + modid + ".info." + type); }
    public static TextEntry curiosSlot(@Nonnull String type) { return new TextEntry("curios.identifier." + type); }

    public static TextEntry extend(@Nonnull TextEntry parent, @Nonnull String extra) { return new TextEntry(() -> parent.getKey() + "." + extra); }

    @Override
    public String toString() { return this.get().getString(); }

}
