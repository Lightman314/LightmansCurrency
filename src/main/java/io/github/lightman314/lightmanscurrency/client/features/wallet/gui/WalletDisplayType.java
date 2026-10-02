package io.github.lightman314.lightmanscurrency.client.features.wallet.gui;

public enum WalletDisplayType {
    ITEMS_WIDE,ITEMS_NARROW,TEXT;
    public boolean isText() { return this == TEXT; }
    public boolean isItem() { return !this.isText(); }
    public int getItemOffset() { return this == ITEMS_NARROW ? 9 : 17; }
}