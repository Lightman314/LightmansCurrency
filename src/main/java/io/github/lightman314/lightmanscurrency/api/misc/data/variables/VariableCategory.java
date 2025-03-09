package io.github.lightman314.lightmanscurrency.api.misc.data.variables;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;

public class VariableCategory {

    public static final VariableCategory EMPTY = new VariableCategory(EasyText.literal("NULL"),"null");

    private final Component name;
    public Component getName() { return this.name; }
    private final String key;
    public String getKey() { return this.key; }
    public VariableCategory(Component name, String key) {
        this.name = name;
        this.key = key;
    }

}