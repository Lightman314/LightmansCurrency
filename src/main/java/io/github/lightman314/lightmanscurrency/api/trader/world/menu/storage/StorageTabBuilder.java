package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage;

import net.minecraft.resources.Identifier;

import java.util.function.Function;

public interface StorageTabBuilder {

    /**
     * The menu in the process of being opened.
     */
    TraderStorageMenu menu();

    /**
     * Adds the tab to the menu, making it
     * @param factory A function that will create the tab from the trader's menu
     */
    default void addTab(Function<TraderStorageMenu,TraderStorageTab> factory) { this.addTab(factory.apply(this.menu())); }
    void addTab(TraderStorageTab tab);
    void removeTab(Identifier tabKey);
    boolean hasTab(Identifier tabKey);

}