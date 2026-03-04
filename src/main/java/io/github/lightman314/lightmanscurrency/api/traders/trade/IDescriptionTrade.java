package io.github.lightman314.lightmanscurrency.api.traders.trade;

public interface IDescriptionTrade {

    DescriptionData getDescriptionData();

    default String getDescription() { return this.getDescriptionData().description; }

    default String getTooltip() { return this.getDescriptionData().tooltip; }

}
