package io.github.lightman314.lightmanscurrency.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class LCMixinPlugin implements IMixinConfigPlugin {

    //Build our own logger for this, as using LightmansCurrency class will cause undesired classes to be loaded prematurely
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onLoad(String s) { }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClass, String mixinClass) {
        try {
            if(mixinClass.contains("compat"))
            {
                String[] splits = mixinClass.split("\\.");
                String modid = splits[splits.length - 2];
                LOGGER.debug("Compat mixin detected. Checking if '" + modid + "' is loaded!");
                boolean loaded = FMLLoader.getLoadingModList().getMods().stream().anyMatch(mod -> mod.getModId().equals(modid));
                if(loaded)
                    LOGGER.debug("Mod was loaded. Applying mixin.");
                else
                    LOGGER.debug("Mod was not loaded. Will not apply the mixin.");
                return loaded;
            }
            else
                return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) { }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) { }
    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) { }

}
