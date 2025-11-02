package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.trade_mod;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.config.VillagerTradeModsOption;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.ConfiguredTradeMod;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.VillagerTradeMods;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VillagerTradeModConfigScreen extends ConfiguredTradeModConfigScreen {

    private static VillagerTradeModConfigScreen activeScreen;
    private final VillagerTradeModsOption option;
    private final Consumer<Object> changeHandler;
    private List<VillagerProfession> professions = null;
    private VillagerProfession selectedProfession = null;
    private String getSelectedProfessionKey() {
        if(this.selectedProfession == null)
            this.collectVillagerTypes();
        return ForgeRegistries.VILLAGER_PROFESSIONS.getKey(this.selectedProfession).toString();
    }
    public VillagerTradeModConfigScreen(Screen parentScreen, ConfigFileOption file, VillagerTradeModsOption option, Consumer<Object> changeHandler) {
        super(parentScreen, file, option, VillagerTradeModConfigScreen::getSelectedMod, VillagerTradeModConfigScreen::handleModChange, false);
        this.option = option;
        this.changeHandler = changeHandler;
    }

    private DropdownWidget professionSelection;

    @Override
    protected void initialize(ScreenArea screenArea) {
        activeScreen = this;
        //Add Dropdown to select villager type
        this.collectVillagerTypes();

        this.professionSelection = this.addChild(DropdownWidget.builder()
                .position(screenArea.centerX() - 75,screenArea.centerY() - 110)
                .width(150)
                .options(this.professions.stream().map(VillagerTradeModConfigScreen::getProfessionName).toList())
                .selected(this.professions.indexOf(this.selectedProfession))
                .selectAction(this::changeProfession)
                .build());

        super.initialize(screenArea);
    }

    @Override
    protected void renderAdditionalBG(EasyGuiGraphics gui) {
        super.renderAdditionalBG(gui);
        if(this.professionSelection != null)
            TextRenderUtil.drawCenteredText(gui, LCText.CONFIG_TRADE_MOD_PROFESSION.get(),this.professionSelection.getArea().centerX(),this.professionSelection.getY() - 10,0xFFFFFF,true);
    }

    //Copied from Villager#getTypeName
    private static Component getProfessionName(VillagerProfession profession)
    {
        ResourceLocation profID = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
        return EasyText.translatable(EntityType.VILLAGER.getDescriptionId() + "." + (!"minecraft".equals(profID.getNamespace()) ? profID.getNamespace() + '.' : "") + profID.getPath());
    }

    private void collectVillagerTypes()
    {
        if(this.professions == null)
        {
            List<VillagerProfession> temp = new ArrayList<>();
            for(VillagerProfession p : ForgeRegistries.VILLAGER_PROFESSIONS)
            {
                if(allowProfessionSelection(p))
                    temp.add(p);
            }
            this.professions = ImmutableList.copyOf(temp);
            this.selectedProfession = this.professions.get(0);
        }
    }

    private static boolean allowProfessionSelection(VillagerProfession profession)
    {
        return profession != VillagerProfession.NITWIT && profession != VillagerProfession.NONE;
    }

    private void changeProfession(int newIndex)
    {
        VillagerProfession profession = this.professions.get(newIndex);
        if(profession != this.selectedProfession)
        {
            this.selectedProfession = profession;
            //Trigger the same code as though the option was changed through a third-party source
            this.onDataChanged(this.option);
        }
    }

    private static ConfiguredTradeMod getEmptyMod() { return new ConfiguredTradeMod(Pair.of(null,null), ImmutableMap.of()); }

    private static ConfiguredTradeMod getSelectedMod() {
        if(activeScreen == null)
            return getEmptyMod();
        else
        {
            VillagerTradeMods mods = activeScreen.option.get();
            return mods.getModMap().getOrDefault(activeScreen.getSelectedProfessionKey(),getEmptyMod());
        }
    }

    private static void handleModChange(Object newMod)
    {
        if(activeScreen != null && newMod instanceof ConfiguredTradeMod mod)
        {
            VillagerTradeMods mods = activeScreen.option.get();
            String professionKey = activeScreen.getSelectedProfessionKey();
            Map<String,ConfiguredTradeMod> data = new HashMap<>(mods.getModMap());
            if(mod.isEmpty() && data.containsKey(professionKey))
            {
                data.remove(professionKey);
                activeScreen.changeHandler.accept(new VillagerTradeMods(data));
            }
            else
            {
                data.put(professionKey,mod);
                activeScreen.changeHandler.accept(new VillagerTradeMods(data));
            }
        }
    }

    @Override
    protected void afterClose() {
        super.afterClose();
        activeScreen = null;
    }
}