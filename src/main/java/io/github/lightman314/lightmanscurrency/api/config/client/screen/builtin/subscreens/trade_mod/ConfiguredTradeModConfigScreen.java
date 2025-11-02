package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.trade_mod;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredTradeModOption;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.mods.ConfiguredTradeMod;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfiguredTradeModConfigScreen extends ConfigScreen {

    private final ConfigFileOption file;
    private final ConfigOption<?> option;
    private final Supplier<ConfiguredTradeMod> data;
    private final Consumer<Object> changeHandler;
    private final boolean requireDefaults;
    private boolean ignoreInputs = false;
    public ConfiguredTradeModConfigScreen(Screen parentScreen, ConfigFileOption file, ConfiguredTradeModOption option, Consumer<Object> changeHandler) { this(parentScreen,file,option,option,changeHandler,true); }
    protected ConfiguredTradeModConfigScreen(Screen parentScreen, ConfigFileOption file, ConfigOption<?> option, Supplier<ConfiguredTradeMod> data, Consumer<Object> changeHandler, boolean requireDefaults) {
        super(parentScreen);
        this.file = file;
        this.option = option;
        this.data = data;
        this.changeHandler = changeHandler;
        this.requireDefaults = requireDefaults;
    }

    private boolean canEdit() { return this.file.canEdit(this.minecraft); }

    private TextBoxWrapper<String> defaultCostInput;
    private TextBoxWrapper<String> defaultResultInput;

    private DropdownWidget regionalDropdown;
    private TextBoxWrapper<String> regionalCostInput;
    private TextBoxWrapper<String> regionalResultInput;

    private List<VillagerType> knownTypes = null;
    private VillagerType selectedType = null;

    @Override
    protected void initialize(ScreenArea screenArea) {
        this.option.addListener(this::onDataChanged);

        ConfiguredTradeMod mod = this.data.get();
        debugData(mod,"Opening Trade Mod Screen");
        int centerX = screenArea.centerX();
        int centerY = screenArea.centerY();
        //Text Inputs for Default Input/Output Items
        this.defaultCostInput = this.addChild(TextInputUtil.stringBuilder()
                .position(centerX - 100,centerY - 80)
                .width(200)
                .maxLength(100)
                .startingValue(itemAsString(mod.getDefaultReplacements().getFirst()))
                .handler(newVal -> this.onDefaultInputChanged(newVal,true))
                .wrap()
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());
        this.defaultResultInput = this.addChild(TextInputUtil.stringBuilder()
                .position(centerX -100,centerY - 40)
                .width(200)
                .maxLength(100)
                .startingValue(itemAsString(mod.getDefaultReplacements().getSecond()))
                .handler(newVal -> this.onDefaultInputChanged(newVal,false))
                .wrap()
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());

        this.collectVillagerTypes();
        //Dropdown for each regional replacement option
        this.regionalDropdown = this.addChild(DropdownWidget.builder()
                .position(centerX - 70,centerY - 5)
                .width(140)
                .options(this.knownTypes.stream().map(t -> (Component)EasyText.literal(BuiltInRegistries.VILLAGER_TYPE.getKey(t).toString())).toList())
                .selected(this.knownTypes.indexOf(this.selectedType))
                .selectAction(this::onRegionChanged)
                .build());

        Pair<Item,Item> regionalData = this.getRegionalData(mod);
        //Text Inputs for regional replacements
        this.regionalCostInput = this.addChild(TextInputUtil.stringBuilder()
                .position(centerX - 100,centerY + 20)
                .width(200)
                .maxLength(100)
                .startingValue(itemAsString(regionalData.getFirst()))
                .handler(newVal -> this.onRegionalInputChanged(newVal,true))
                .wrap()
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());
        this.regionalResultInput = this.addChild(TextInputUtil.stringBuilder()
                .position(centerX - 100,centerY + 60)
                .width(200)
                .maxLength(100)
                .startingValue(itemAsString(regionalData.getSecond()))
                .handler(newVal -> this.onRegionalInputChanged(newVal,false))
                .wrap()
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());

        //Back Button?
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(200)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

    }

    private void collectVillagerTypes()
    {
        if(this.knownTypes == null)
        {
            this.knownTypes = new ArrayList<>(BuiltInRegistries.VILLAGER_TYPE.stream().toList());
            this.selectedType = BuiltInRegistries.VILLAGER_TYPE.get(BuiltInRegistries.VILLAGER_TYPE.getDefaultKey());
            LightmansCurrency.LogDebug("Collected all " + this.knownTypes.size() + " known villager types!");
        }
    }

    protected void onDataChanged(ConfigOption<?> option)
    {
        this.ignoreInputs = true;
        //Re-define text box values
        ConfiguredTradeMod mod = this.data.get();
        debugData(mod,"Config Option Changed, Reloading Fields");
        if(this.defaultCostInput != null)
            this.defaultCostInput.setValue(itemAsString(mod.getDefaultReplacements().getFirst()));
        if(this.defaultResultInput != null)
            this.defaultResultInput.setValue(itemAsString(mod.getDefaultReplacements().getSecond()));
        Pair<Item,Item> regionalData = this.getRegionalData();
        if(this.regionalCostInput != null)
            this.regionalCostInput.setValue(itemAsString(regionalData.getFirst()));
        if(this.regionalResultInput != null)
            this.regionalResultInput.setValue(itemAsString(regionalData.getSecond()));

        this.ignoreInputs = false;
    }

    private static void debugData(ConfiguredTradeMod mod, String text)
    {
        StringBuilder builder = new StringBuilder();
        mod.write(builder);
        LightmansCurrency.LogDebug(text + "\nCurrent Value: " + builder);
    }

    private static void debugPair(Pair<Item,Item> value, String text)
    {
        LightmansCurrency.LogDebug(text + "\nCurrent Value: " + itemAsString(value.getFirst()) + ";" + itemAsString(value.getSecond()));
    }

    private static void debugPairChange(Pair<Item,Item> oldValue, Pair<Item,Item> newValue, String text)
    {
        LightmansCurrency.LogDebug(text + "\nOld Value: " + itemAsString(oldValue.getFirst()) + ";" + itemAsString(oldValue.getSecond()) + "\nNew Value: " + itemAsString(newValue.getFirst()) + ";" + newValue.getSecond());
    }

    private static String itemAsString(@Nullable Item item) {
        if(item == null)
            return "";
        return ForgeRegistries.ITEMS.getKey(item).toString();
    }

    private void onDefaultInputChanged(String newInput, boolean costInput)
    {
        if(this.ignoreInputs)
            return;
        ConfiguredTradeMod currentMod = this.data.get();
        Pair<Item,Item> defaultInputs = currentMod.getDefaultReplacements();
        try {
            if(newInput.isEmpty() && this.requireDefaults)
                return;
            Pair<Item,Item> newValue = modifyPair(defaultInputs,newInput,costInput);
            if(!Objects.equals(defaultInputs,newValue))
            {
                debugPairChange(defaultInputs,newValue,costInput ? "Default Cost Input Changed" : "Default Result Input Changed");
                this.changeHandler.accept(new ConfiguredTradeMod(newValue,currentMod.getRegionalReplacements()));
            }
        } catch (ConfigParsingException ignored) {}
    }

    private Pair<Item,Item> getRegionalData() { return this.getRegionalData(this.data.get()); }
    private Pair<Item,Item> getRegionalData(ConfiguredTradeMod mod)
    {
        if(this.selectedType == null)
            this.collectVillagerTypes();
        return mod.getRegionalReplacements().getOrDefault(BuiltInRegistries.VILLAGER_TYPE.getKey(this.selectedType).toString(),Pair.of(null,null));
    }

    private void onRegionChanged(int newRegion)
    {
        VillagerType newType = this.knownTypes.get(newRegion);
        if(newType != this.selectedType)
        {
            this.ignoreInputs = true;
            this.selectedType = newType;
            Pair<Item,Item> data = this.getRegionalData();
            //debugPair(data,"Region changed to " + BuiltInRegistries.VILLAGER_TYPE.getKey(this.selectedType));
            if(this.regionalCostInput != null)
                this.regionalCostInput.setValue(itemAsString(data.getFirst()));
            if(this.regionalResultInput != null)
                this.regionalResultInput.setValue(itemAsString(data.getSecond()));
            this.ignoreInputs = false;
        }
    }

    private void onRegionalInputChanged(String newInput, boolean costInput)
    {
        if(this.ignoreInputs)
            return;
        ConfiguredTradeMod mod = this.data.get();
        Map<String,Pair<Item,Item>> regionalData = new HashMap<>(mod.getRegionalReplacements());
        String entryKey = BuiltInRegistries.VILLAGER_TYPE.getKey(this.selectedType).toString();
        Pair<Item,Item> currentInputs = regionalData.getOrDefault(entryKey,Pair.of(null,null));
        try {
            Pair<Item,Item> newValue = modifyPair(currentInputs,newInput,costInput);
            if(!Objects.equals(currentInputs,newValue))
            {
                //Remove the entry
                if(newValue.getFirst() == null && newValue.getSecond() == null)
                {
                    //LightmansCurrency.LogDebug("Removed regional entry for " + entryKey);
                    regionalData.remove(entryKey);
                }
                else
                {
                    //debugPairChange(currentInputs,newValue,(costInput ? "Regional Cost Input Changed for " : " Regional Result Input Changed for ") + entryKey);
                    regionalData.put(entryKey,newValue);
                }
                this.changeHandler.accept(new ConfiguredTradeMod(mod.getDefaultReplacements(),regionalData));
            }
        } catch (ConfigParsingException ignored) { }

    }

    private Pair<Item,Item> modifyPair(Pair<Item,Item> currentValue, String textInput, boolean costInput) throws ConfigParsingException
    {
        try {
            Item newItem;
            //Require a : in there, otherwise a partial input of "light" parses as "minecraft:light"
            if(!textInput.contains(":"))
                newItem = null;
            else
            {
                ResourceLocation itemID = VersionUtil.parseResource(textInput);
                if(ForgeRegistries.ITEMS.containsKey(itemID))
                {
                    newItem = ForgeRegistries.ITEMS.getValue(itemID);
                    if(newItem == Items.AIR)
                        throw new ConfigParsingException("Cannot set the item to air!");
                }
                else
                    throw new ConfigParsingException(textInput + " is not a valid item!");
            }
            //Replace the pair
            if(costInput)
                return Pair.of(newItem,currentValue.getSecond());
            else
                return Pair.of(currentValue.getFirst(),newItem);
        } catch (ResourceLocationException e) { throw new ConfigParsingException(e); }
    }

    @Override
    protected List<Component> getTitleSections() {
        List<Component> list = new ArrayList<>();
        list.add(file.name());
        list.add(this.option.getDisplayName());
        return list;
    }

    @Override
    protected void renderAdditionalBG(EasyGuiGraphics gui) {
        //Render labels
        if(this.defaultCostInput != null)
            gui.drawShadowed(LCText.CONFIG_TRADE_MOD_COST.get(),this.defaultCostInput.getPosition().offset(2,-10),0xFFFFFF);
        if(this.defaultResultInput != null)
            gui.drawShadowed(LCText.CONFIG_TRADE_MOD_RESULT.get(),this.defaultResultInput.getPosition().offset(2,-10),0xFFFFFF);

        if(this.regionalDropdown != null)
            TextRenderUtil.drawCenteredText(gui,LCText.CONFIG_TRADE_MOD_REGION_ID.get(),this.regionalDropdown.getArea().centerX(),this.regionalDropdown.getY() - 10,0xFFFFFF,true);
        if(this.regionalCostInput != null)
            gui.drawShadowed(LCText.CONFIG_TRADE_MOD_COST_REGIONAL.get(),this.regionalCostInput.getPosition().offset(2,-10),0xFFFFFF);
        if(this.regionalResultInput != null)
            gui.drawShadowed(LCText.CONFIG_TRADE_MOD_RESULT_REGIONAL.get(),this.regionalResultInput.getPosition().offset(2,-10),0xFFFFFF);

    }

    @Override
    protected void afterClose() { this.option.removeListener(this::onDataChanged); }

}