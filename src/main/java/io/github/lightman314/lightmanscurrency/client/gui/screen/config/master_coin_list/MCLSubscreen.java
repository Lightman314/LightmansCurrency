package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.MasterCoinListConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MCLSubscreen extends ConfigScreen {

    private final MutableMasterCoinList root;
    public MCLSubscreen(MutableMasterCoinList rootScreen, Screen parentScreen)
    {
        super(parentScreen);
        this.root = rootScreen;
    }
    public MCLSubscreen(MCLSubscreen parentScreen)
    {
        super(parentScreen);
        this.root = parentScreen.root;
    }

    @Nullable
    public final MutableChainData createChain(String chainKey) { return this.root.createChain(chainKey); }
    public final Map<String,MutableChainData> getData() { return this.root.getData(); }
    protected final boolean canEdit() { return MasterCoinListConfigOption.INSTANCE.canEdit(this.minecraft); }

    @Override
    protected final List<Component> getTitleSections() {
        List<Component> list = new ArrayList<>();
        list.add(LCText.CONFIG_MCL_LABEL.get());
        this.addTitleSections(list);
        return list;
    }

    protected abstract void addTitleSections(List<Component> list);

}
