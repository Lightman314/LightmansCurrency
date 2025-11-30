package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list;

import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data.MutableChainData;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class MCLChainSubscreen extends MCLSubscreen {

    protected final MutableChainData chain;
    public MCLChainSubscreen(MCLSubscreen parentScreen, MutableChainData chain) { super(parentScreen); this.chain = chain; }
    public MCLChainSubscreen(MCLChainSubscreen parentScreen) { this(parentScreen,parentScreen.chain); }

    @Override
    protected void addTitleSections(List<Component> list) { list.add(this.chain.displayName.copy()); }

    @Override
    protected void screenTick() {
        if(this.getData().get(this.chain.getChain()) != this.chain)
            this.onClose();
    }

}
