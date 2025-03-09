package io.github.lightman314.lightmanscurrency.api.misc.data.variables;

public interface IVariableHost {

    void registerVariable(EasyVariable<?> variable);
    void markVariableChanged(EasyVariable<?> variable);

}