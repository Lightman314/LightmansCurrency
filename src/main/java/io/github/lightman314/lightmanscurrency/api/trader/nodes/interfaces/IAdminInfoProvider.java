package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import net.minecraft.network.chat.Component;

public interface IAdminInfoProvider {

    void displayAdminListData(Builder builder);

    interface Builder {
        default void accept(Component line) { this.accept(0,line); }
        void accept(int priority,Component line);
    }

}
