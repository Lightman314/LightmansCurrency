package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.variant;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ModelVariantButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.DefaultModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.variant.VariantSelectMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class VariantSelectScreen<T extends VariantSelectMenu> extends EasyMenuScreen<T> implements IScrollable {

    private List<Pair<ResourceLocation,ModelVariant>> availableVariants;
    public VariantSelectScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory);
        this.onVariantTargetChanged();
    }

    protected int getWidth() { return 9; }
    protected int getHeight() { return 4; }

    protected final void onVariantTargetChanged()
    {
        //Reset the variant being previewed
        this.viewingVariant = null;
        if(!this.hasValidTarget())
        {
            this.availableVariants = ImmutableList.of();
            return;
        }
        boolean creative = Minecraft.getInstance().player.isCreative();
        //Collect possible variants
        List<Pair<ResourceLocation,ModelVariant>> temp = new ArrayList<>();
        temp.add(Pair.of(null, this.buildDefaultVariant()));
        for(ResourceLocation id : this.getValidVariants())
        {
            if(creative || !LCConfig.SERVER.variantBlacklist.matches(id))
            {
                ModelVariant variant = ModelVariantDataManager.getVariant(id);
                if(variant != null && (creative || !variant.getOrDefault(VariantProperties.HIDDEN)))
                    temp.add(Pair.of(id,variant));
            }
        }
        temp.sort(ModelVariant.COMPARATOR);
        this.availableVariants = ImmutableList.copyOf(temp);
    }

    protected abstract boolean hasValidTarget();
    protected abstract List<ResourceLocation> getValidVariants();
    protected abstract DefaultModelVariant buildDefaultVariant();
    protected abstract ItemLike getTargetAsItem();

    private int scroll = 0;
    private ResourceLocation selectedVariant = null;
    private Pair<ResourceLocation,ModelVariant> viewingVariant = null;

    @Override
    protected void initialize(ScreenArea screenArea) {
        this.addChild(ScrollListener.builder().listener(this).area(screenArea).build());
        this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(169,87))
                .height(18 * this.getHeight())
                .scrollable(this)
                .build());

        int relativeIndex = 0;
        for(int y = 0; y < this.getHeight(); ++y)
        {
            int yPos = 103 + (18 * y);
            for(int x = 0; x < this.getWidth(); ++x)
            {
                int xPos = 7 + (18 * x);
                final int i = relativeIndex++;
                this.addChild(ModelVariantButton.builder()
                        .target(this::getTargetAsItem)
                        .position(screenArea.pos.offset(xPos,yPos))
                        .selected(this::selectedVariant)
                        .viewing(this::viewingVariant)
                        .pressAction(() -> this.selectVariant(i))
                        .source(this.variantSource(i))
                        .screen(this)
                        .build());
            }
        }

        this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(83,43))
                .text(LCText.BUTTON_VARIANT_SELECT)
                .width(92)
                .pressAction(this::setSelectedVariant)
                .addon(EasyAddonHelper.activeCheck(this::canSelectCurrentVariant))
                .build());

    }

    protected abstract ResourceLocation getBackgroundTexture();

    @Override
    protected void renderBG(EasyGuiGraphics gui) {

        //Render background
        gui.renderNormalBackground(this.getBackgroundTexture(),this);

        //Render Title
        gui.drawString(LCText.GUI_VARIANT_MENU.get(),8,93,0x404040);

        //Cache selected variant locally so that we don't have to constantly query the block entity data several times per frame
        this.selectedVariant = this.menu.getSelectedVariant();
        if(this.viewingVariant == null && this.hasValidTarget())
        {
            //Get selected variant from list
            for(int i = 0; this.viewingVariant == null && i < this.availableVariants.size(); ++i)
            {
                Pair<ResourceLocation,ModelVariant> entry = this.availableVariants.get(i);
                if(Objects.equals(entry.getFirst(),this.selectedVariant))
                    this.viewingVariant = entry;
            }
            if(this.viewingVariant == null)
                this.viewingVariant = this.availableVariants.getFirst();
        }

        //Render viewed variant in the preview area
        if(this.viewingVariant != null) //Null check for safety
        {

            ModelVariant variant = this.viewingVariant.getSecond();
            //Render the Variants name
            gui.drawString(variant.getName(),8, 7,0x404040);

            //Render the large variant preview
            ItemStack item = variant.getItemIcon();
            if(item == null)
            {
                item = new ItemStack(this.getTargetAsItem());
                if(this.viewingVariant.getFirst() != null)
                    item.set(ModDataComponents.MODEL_VARIANT,this.viewingVariant.getFirst());
            }
            gui.renderScaledItem(item, ScreenPosition.of(8,18), 70f/16f);
        }

    }

    private Supplier<Pair<ResourceLocation,ModelVariant>> variantSource(int relativeIndex)
    {
        return () -> {
            int index = relativeIndex + (this.scroll * this.getWidth());
            if(index < 0 || index >= this.availableVariants.size())
                return null;
            return this.availableVariants.get(index);
        };
    }

    @Nullable
    private ResourceLocation selectedVariant() { return this.selectedVariant; }
    @Nullable
    private ResourceLocation viewingVariant() { return this.viewingVariant == null ? null : this.viewingVariant.getFirst(); }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(9 * 3, this.getWidth(), this.availableVariants.size()); }

    private void selectVariant(int relativeIndex)
    {
        int index = relativeIndex + (this.scroll * this.getWidth());
        if(index < 0 || index >= this.availableVariants.size())
            return;
        this.viewingVariant = this.availableVariants.get(index);
    }

    private boolean canSelectCurrentVariant()
    {
        if(this.viewingVariant == null)
            return false;
        return !Objects.equals(this.selectedVariant,this.viewingVariant.getFirst());
    }

    private void setSelectedVariant()
    {
        if(this.viewingVariant == null)
            return;
        this.menu.SetVariant(this.viewingVariant.getFirst());
    }

}
