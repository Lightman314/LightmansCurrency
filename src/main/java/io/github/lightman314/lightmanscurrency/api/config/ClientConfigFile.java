package io.github.lightman314.lightmanscurrency.api.config;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ClientConfigFile extends ConfigFile {
    @Deprecated(since = "2.2.5.1c")
    protected ClientConfigFile(String fileName) { super(fileName); }
    protected ClientConfigFile(ResourceLocation fileID, String fileName) { super(fileID,fileName); }
    @Deprecated(since = "2.2.5.1c")
    protected ClientConfigFile(String fileName, LoadPhase loadPhase) { super(fileName, loadPhase); }
    protected ClientConfigFile(ResourceLocation fileID, String fileName, LoadPhase loadPhase) { super(fileID, fileName, loadPhase); }
    @Override
    public boolean isClientOnly() { return true; }
}
