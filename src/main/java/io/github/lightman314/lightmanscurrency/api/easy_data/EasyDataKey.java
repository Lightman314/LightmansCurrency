package io.github.lightman314.lightmanscurrency.api.easy_data;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.network.chat.Component;

public final class EasyDataKey {

    public final Component dataName;
    public final String tagKey;
    public final String simpleKey;
    public final DataCategory category;

    public EasyDataKey(Component dataName,String tagKey,String simpleKey,DataCategory category)
    {
        this.dataName = dataName;
        this.tagKey = tagKey;
        this.simpleKey = simpleKey;
        this.category = category;
    }
    private EasyDataKey(Builder builder) {
        this.dataName = builder.dataName;
        this.tagKey = builder.tagKey;
        this.simpleKey = builder.simpleKey;
        this.category = builder.category;
    }

    public static Builder builder(String simpleKey) { return new Builder(simpleKey); }

    public static class Builder {
        private Component dataName;
        private String tagKey;
        private final String simpleKey;
        private DataCategory category = DataCategory.NULL;

        private Builder(String simpleKey) { this.tagKey = simpleKey; this.simpleKey = simpleKey; this.dataName = TextEntry.dataName(LightmansCurrency.MODID,simpleKey).get(); }

        public Builder name(Component name) { this.dataName = name; return this; }
        public Builder tagKey(String tagKey) { this.tagKey = tagKey; return this; }
        public Builder category(DataCategory category) { this.category = category; return this; }

        public EasyDataKey build() { return new EasyDataKey(this); }

    }


}