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
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class TextEntry {

    private final Supplier<String> key;
    public String getKey() { return this.key.get(); }
    public TextEntry(String key) { this.key = () -> key; }
    public TextEntry(Supplier<String> key) { this.key = Suppliers.memoize(key::get); }

    public MutableComponent get(Object... objects) { return Component.translatableEscape(this.getKey(),objects);}
    public MutableComponent getWithStyle(ChatFormatting... format) { return Component.translatableEscape(this.getKey()).withStyle(format); }
    public List<Component> getAsList(Object... objects) { return Lists.newArrayList(this.get(objects)); }
    public List<Component> getAsListWithStyle(ChatFormatting... format) { return Lists.newArrayList(this.getWithStyle(format)); }
    public void tooltip(List<Component> tooltip, Object... objects) { tooltip.add(this.get(objects)); }
    public IconData icon(Object... objects) { return IconData.of(this.get(objects)); }

    //Vanilla Objects
    public static TextEntry item(Supplier<? extends ItemLike> item) { return new TextEntry(() -> item.get().asItem().getDescriptionId()); }
    public static TextEntry block(Supplier<? extends Block> block) { return new TextEntry(() -> block.get().getDescriptionId()); }
    public static TextEntry enchantment(ResourceKey<? extends Enchantment> enchantment) { return new TextEntry("enchantment." + enchantment.location().getNamespace() + "." + enchantment.location().getPath()); }
    public static TextEntry gamerule(String ruleKey) { return new TextEntry("gamerule." + ruleKey); }
    public static TextEntry profession(Supplier<? extends VillagerProfession> profession) { return new TextEntry(() -> {
        ResourceLocation id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession.get());
        return "entity.minecraft.villager." + id.getNamespace() + "." + id.getPath();
    });}
    public static TextEntry creativeTab(String modid, String name) { return new TextEntry("itemGroup." + modid + "." + name); }
    public static TextEntry keyBind(String modid, String name) { return new TextEntry("key." + modid + "." + name); }
    public static TextEntry sound(String modid, String name) { return new TextEntry(modid + ".subtitle." + name); }
    public static TextEntry tooltip(String modid, String key) { return new TextEntry("tooltip." + modid + "." + key); }
    public static TextEntry gui(String modid, String key) { return new TextEntry("gui." + modid + "." + key); }
    public static TextEntry button(String modid, String key) { return new TextEntry("button." + modid + "." + key); }
    public static TextEntry widget(String modid, String key) { return new TextEntry("widget." + modid + "." + key); }
    public static TextEntry message(String modid, String key) { return new TextEntry("message." + modid + "." + key); }
    public static TextEntry blurb(String modid, String key) { return new TextEntry("blurb." + modid + "." + key); }
    public static TextEntry command(String modid, String key) { return new TextEntry("command." + modid + "." + key); }
    public static TextEntry argument(String key) { return new TextEntry("command.argument." + key); }

    //Custom Inputs
    public static TextEntry description(TextEntry parent) { return extend(parent,"desc"); }
    public static TextEntry plural(TextEntry parent) { return extend(parent,"plural"); }
    public static TextEntry initial(TextEntry parent) { return extend(parent,"initial"); }
    public static TextEntry tradeRule(TradeRuleType<?> type) { return new TextEntry(TradeRule.translationKeyOfType(type.type)); }
    public static TextEntry tradeRuleMessage(TradeRuleType<?> type, String message) { return new TextEntry(TradeRule.translationKeyOfType(type.type) + "." + message); }
    public static TextEntry notification(NotificationType<?> type) { return notification(type.type); }
    public static TextEntry notification(ResourceLocation type) { return new TextEntry("notification." + type.getNamespace() + "." + type.getPath()); }
    public static TextEntry notification(NotificationType<?> type, String extra) { return notification(type.type,extra); }
    public static TextEntry notification(ResourceLocation type, String extra) { return new TextEntry("notification." + type.getNamespace() + "." + type.getPath() + "." + extra); }

    public static TextEntry dataName(String modid, String key) { return new TextEntry("data." + modid + ".name." + key); }
    public static TextEntry dataCategory(String modid, String key) { return new TextEntry("data." + modid + ".category." + key); }

    public static TextEntry chain(String chain) { return new TextEntry("lightmanscurrency.money.chain." + chain); }
    public static TextEntry chainDisplay(String chain) { return new TextEntry("lightmanscurrency.money.chain." + chain + ".display"); }
    public static TextEntry chainDisplayWordy(String chain) { return new TextEntry("lightmanscurrency.money.chain." + chain + ".display.wordy"); }
    public static TextEntry lcStat(StatKey<?,?> statKey) { return new TextEntry(StatType.getTranslationKey(statKey.key)); }
    public static TextEntry blockVariant(String type) { return new TextEntry("lightmanscurrency.block_variant." + type); }
    public static TextEntry blockVariantModifier(String type) { return new TextEntry("lightmanscurrency.block_variant.modifier." + type); }
    public static List<TextEntry> blockVariantList(String type, int size) {
        List<TextEntry> list = new ArrayList<>();
        for(int i = 1; i <= size; ++i)
            list.add(blockVariant(type + "." + i));
        return list;
    }

    public static TextEntry reiGroup(String modid, String type) { return new TextEntry("rei." + modid + ".group." + type); }
    public static TextEntry jeiInfo(String modid, String type) { return new TextEntry("jei." + modid + ".info." + type); }
    public static TextEntry curiosSlot(String type) { return new TextEntry("curios.identifier." + type); }

    public static TextEntry extend(TextEntry parent, String extra) { return new TextEntry(() -> parent.getKey() + "." + extra); }

    @Override
    public String toString() { return this.get().getString(); }

}
