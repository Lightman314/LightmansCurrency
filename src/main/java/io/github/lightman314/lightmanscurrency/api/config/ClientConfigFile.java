package io.github.lightman314.lightmanscurrency.api.config;

import net.minecraft.resources.ResourceLocation;

public abstract class ClientConfigFile extends ConfigFile {
    protected ClientConfigFile(ResourceLocation fileID, String fileName) { super(fileID,fileName); }
    protected ClientConfigFile(ResourceLocation fileID, String fileName, LoadPhase loadPhase) { super(fileID, fileName, loadPhase); }
    @Override
    public final boolean isClientOnly() { return true; }
}
