package io.github.lightman314.lightmanscurrency.common.money;

public class CoinValueHolder {

    private CoinValue value = CoinValue.EMPTY;
    public CoinValue getValue() { return this.value; }
    public void setValue(CoinValue value) { this.value = value; }

}
