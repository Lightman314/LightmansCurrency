package io.github.lightman314.lightmanscurrency.common.traders.item.ticket;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TicketSaleData {

    public static final Codec<TicketSaleData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.optionalFieldOf("recipe").forGetter(d -> Optional.ofNullable(d.recipe)),
            Codec.STRING.fieldOf("code").forGetter(d -> d.code),
            Codec.INT.fieldOf("durability").forGetter(d -> d.durability))
            .apply(builder,TicketSaleData::new));

    public static final StreamCodec<ByteBuf,TicketSaleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),d -> Optional.ofNullable(d.recipe),
            ByteBufCodecs.STRING_UTF8,d -> d.code,
            ByteBufCodecs.INT,d -> d.durability,
            TicketSaleData::new);

    private TicketItemTrade trade;
    private int index;

    public TicketSaleData(TicketItemTrade trade, int index) {
        this.trade = trade;
        this.index = index;
    }
    private TicketSaleData(Optional<ResourceLocation> recipe,String code,int durability)
    {
        this.recipe = recipe.orElse(null);
        this.code = code;
        this.durability = durability;
    }

    protected final void copyFrom(TicketSaleData data) {
        this.recipe = data.recipe;
        this.code = data.code;
        this.durability = data.durability;
    }

    @Nullable
    ResourceLocation recipe = null;

    @Nullable
    public ResourceLocation getRecipe() { return this.recipe; }

    public void setRecipe(@Nullable ResourceLocation recipe) {
        if(!Objects.equals(this.recipe,recipe))
        {
            this.recipe = recipe;
            this.trade.setChanged();
        }
    }

    public void onSellItemChanged() {
        if (this.isPotentiallyRecipeMode() && this.tryGetRecipe() == null) {
            List<RecipeHolder<TicketStationRecipe>> allRecipes = this.getMatchingRecipes();
            if (allRecipes.isEmpty())
                return;
            this.setRecipe(allRecipes.getFirst().id());
        }
    }

    TicketStationRecipe.ExtraData getData() { return new TicketStationRecipe.ExtraData(this.code, this.durability); }

    String code = "";

    public String getCode() { return this.code; }

    public boolean setCode(String couponCode) {

        if (couponCode.length() > 16)
            couponCode = couponCode.substring(0, 16);
        if (TicketRecipe.CODE_INPUT_PREDICATE.test(couponCode)) {
            this.code = couponCode;
            this.trade.setChanged();
            return true;
        }
        return false;
    }

    int durability = 0;

    public int getDurability() {
        return this.durability;
    }

    public void setDurability(int durability) {
        if(this.durability != durability)
        {
            this.durability = durability;
            this.trade.setChanged();
        }
    }

    public List<RecipeHolder<TicketStationRecipe>> getMatchingRecipes() {
        ItemStack sellItem = trade.getActualItem(this.index);
        Level level = LightmansCurrency.getProxy().safeGetDummyLevel();
        if (level != null) {
            List<RecipeHolder<TicketStationRecipe>> list = new ArrayList<>(TicketStationMenu.getAllRecipes(level).stream().filter(r ->
                    r.value().matchesTicketKioskSellItem(sellItem)).toList());
            //Figure out if we should add an empty recipe as the default value, as some recipes may conflict with normal material sales
            for (RecipeHolder<TicketStationRecipe> recipe : list) {
                if (recipe.value().allowIgnoreKioskRecipe()) {
                    list.addFirst(new RecipeHolder<>(null, null));
                    return list;
                }
            }
            return list;
        }
        return ImmutableList.of();
    }

    @Nullable
    public TicketStationRecipe tryGetRecipe() {
        if (this.recipe == null || !this.isPotentiallyRecipeMode())
            return null;
        Level level = LightmansCurrency.getProxy().safeGetDummyLevel();
        if (level != null) {
            for (RecipeHolder<TicketStationRecipe> holder : TicketStationMenu.getAllRecipes(level)) {
                if (holder.id().equals(this.recipe)) {
                    //Don't return the trade if it doesn't match our required item
                    if (!holder.value().matchesTicketKioskSellItem(trade.getActualItem(this.index)))
                        return null;
                    return holder.value();
                }
            }
        }
        return null;
    }

    public boolean requestingCodeInput() {
        TicketStationRecipe recipe = this.tryGetRecipe();
        return recipe != null && recipe.requiredCodeInput();
    }

    public boolean requestingDurabilityInput() {
        TicketStationRecipe recipe = this.tryGetRecipe();
        return recipe != null && recipe.requiredDurabilityInput();
    }

    public ItemStack getCraftingResult(boolean replaceName) {
        ItemStack sellItem = trade.getActualItem(this.index);
        if (!this.isRecipeMode())
            return sellItem;
        TicketStationRecipe recipe = this.tryGetRecipe();
        if (recipe != null) {
            ItemStack result = recipe.assembleWithKiosk(sellItem, this.getData());
            if (result.isEmpty())
                return result;
            result.setCount(sellItem.getCount());
            if (replaceName) {
                String customName = trade.getCustomName(this.index);
                if (!customName.isBlank())
                    result.set(DataComponents.CUSTOM_NAME, EasyText.literal(customName));
            }
            return result;
        }
        return sellItem;
    }

    public boolean isValid() {
        ItemStack sellItem = trade.getActualItem(this.index);
        TicketStationRecipe recipe = this.tryGetRecipe();
        if (recipe != null)
            return recipe.matchesTicketKioskSellItem(sellItem) && (recipe.validData(this.getData()));
        return sellItem.isEmpty() || InventoryUtil.ItemHasTag(sellItem, LCTags.Items.TICKET_MATERIAL);
    }

    public boolean isPotentiallyRecipeMode() {
        return !this.trade.isPurchase() && !this.getMatchingRecipes().isEmpty();
    }

    public boolean isRecipeMode() {
        return this.tryGetRecipe() != null;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (this.recipe != null)
            tag.putString("Recipe", this.recipe.toString());
        tag.putString("Code", this.code);
        tag.putInt("Durability", this.durability);
        return tag;
    }

    public void saveSettings(SavedSettingData.MutableNodeAccess node) {
        String prefix = "item_" + this.index + "_ticketdata_";
        if (this.recipe == null)
            node.setBooleanValue(prefix + "no_recipe", true);
        else
            node.setStringValue(prefix + "recipe", this.recipe.toString());
        node.setStringValue(prefix + "code", this.code);
        node.setIntValue(prefix + "durability", this.durability);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Recipe"))
            this.recipe = VersionUtil.parseResource(tag.getString("Recipe"));
        this.code = tag.getString("Code");
        this.durability = tag.getInt("Durability");
    }

    public void loadSettings(SavedSettingData.NodeAccess node) {
        String prefix = "item_" + this.index + "_ticketdata_";
        if (node.getBooleanValue(prefix + "no_recipe"))
            this.recipe = null;
        this.recipe = VersionUtil.parseResource(node.getStringValue(prefix + "recipe"));
        this.code = node.getStringValue(prefix + "code");
        this.durability = node.getIntValue(prefix + "durability");
    }

}
