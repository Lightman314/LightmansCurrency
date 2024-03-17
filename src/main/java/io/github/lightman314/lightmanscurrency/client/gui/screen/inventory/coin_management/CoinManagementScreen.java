package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.tabs.CoinManagementTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.tabs.SelectChainTab;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.CoinManagementMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.*;

public class CoinManagementScreen extends EasyMenuScreen<CoinManagementMenu> {

    private final Map<String,EditableChainData> editableDataMap = new HashMap<>();
    public Collection<EditableChainData> getEditableChains() { return this.editableDataMap.values(); }

    private int selectedTab = 0;
    private final List<CoinManagementTab> allTabs = ImmutableList.of(new SelectChainTab(this));
    private CoinManagementTab currentTab() { return this.allTabs.get(this.selectedTab); }
    private String selectedChain = "main";
    public EditableChainData getSelectedChain() { return this.editableDataMap.get(this.selectedChain); }

    public CoinManagementScreen(CoinManagementMenu menu, Inventory inventory, Component ignored) {
        super(menu, inventory);
        //Collect editable money data from local chain data cache
        for(ChainData data : CoinAPI.API.AllChainData())
            this.editableDataMap.put(data.chain, new EditableChainData(data));
    }

    @Override
    protected void initialize(ScreenArea screenArea) {
        //Create Tab Buttons
        screenArea = this.resize(screenArea.width,screenArea.height);

    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {
        this.currentTab().renderBG(gui);
    }

    @Override
    protected void renderAfterWidgets(@Nonnull EasyGuiGraphics gui) {
        this.currentTab().renderAfterWidgets(gui);
    }

    public void CreateChain(@Nonnull String chain, @Nonnull ItemLike coreCoin, @Nonnull String chainName)
    {
        if(this.editableDataMap.containsKey(chain))
            return;
        EditableChainData newChain = new EditableChainData(chain,coreCoin,chainName);
        this.editableDataMap.put(chain,newChain);
        this.selectedChain = chain;
    }

    public void DeleteChain(@Nonnull String chain) {
        if(chain.equals(CoinAPI.MAIN_CHAIN))
        {
            LightmansCurrency.LogWarning("Cannot delete the `main` chain no matter what!");
            return;
        }
        this.editableDataMap.remove(chain);
        if(this.selectedChain.equals(chain))
            this.selectedChain = CoinAPI.MAIN_CHAIN;
    }

    public void ChangeChainID(@Nonnull String oldID, @Nonnull String newID, boolean allowOverride) {
        if(this.editableDataMap.containsKey(newID) && !allowOverride)
        {
            LightmansCurrency.LogWarning("Attempted to change a chains id to another chain that already exists!");
            return;
        }
        if(oldID.equals(CoinAPI.MAIN_CHAIN))
        {
            LightmansCurrency.LogInfo("Cannot delete the `main` chain even by changing its id. It will be copied instead!");
            EditableChainData copy = this.editableDataMap.get(oldID).copy();
            this.editableDataMap.put(newID, copy);
        }
        else
        {
            EditableChainData data = this.editableDataMap.get(oldID);
            data.chainID = newID;
            this.editableDataMap.remove(oldID);
            this.editableDataMap.put(newID,data);
            if(this.selectedChain.equals(oldID))
                this.selectedChain = newID;
        }
    }

    public void SaveChanges() {
        if(EditableChainData.isSaveable(this.getEditableChains()))
        {

            JsonObject root = new JsonObject();
            JsonArray chainList = new JsonArray();
            //TODO write editable data to json
            root.add("Chains",chainList);
            Gson g = new GsonBuilder().create();
            String json = g.toJson(root);
            this.menu.SendMessageToServer(LazyPacketData.simpleString("SaveData",json));
        }
    }

}