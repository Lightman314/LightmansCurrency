package io.github.lightman314.lightmanscurrency.mixin.plugin;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class LCMixinPlugin implements IMixinConfigPlugin {

    //Build our own logger for this, as using LightmansCurrency class will cause undesired classes to be loaded prematurely
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, Predicate<String>> extraTests = new HashMap<>();
    private final Predicate<String> defaultTest = this::isModLoaded;

    public LCMixinPlugin() {
        this.extraTests.put("create", modid -> this.isModMajorVersion(modid,6));
    }

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
                return this.extraTests.getOrDefault(modid,this.defaultTest).test(modid);
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

    @Nullable
    private ModInfo getMod(String modid)
    {
        List<ModInfo> mods = FMLLoader.getLoadingModList().getMods().stream().filter(mod -> mod.getModId().equals(modid)).toList();
        if(mods.isEmpty())
            return null;
        return mods.getFirst();
    }

    private boolean isModLoaded(String modid) {
        LOGGER.debug("Compat mixin detected. Checking if '" + modid + "' is loaded!");
        boolean loaded = FMLLoader.getLoadingModList().getMods().stream().anyMatch(mod -> mod.getModId().equals(modid));
        if(loaded)
            LOGGER.debug(modid + " was loaded. Applying mixin.");
        else
            LOGGER.debug(modid + " was not loaded. Will not apply the mixin.");
        return loaded;
    }

    private boolean isModMajorVersion(String modid, int majorVersion) {
        LOGGER.debug("Compat mixin detected. Checking if '" + modid + "' is loaded and v" + majorVersion + " or newer!");
        ModInfo mod = this.getMod(modid);
        if(mod == null)
        {
            LOGGER.debug(modid + " was not loaded. Will not apply the mixin.");
            return false;
        }
        int actualMajorVersion = mod.getVersion().getMajorVersion();
        boolean passed = actualMajorVersion >= majorVersion;
        if(passed)
            LOGGER.debug(actualMajorVersion + " is newer than " + majorVersion + ". Applying mixin.");
        else
            LOGGER.debug(actualMajorVersion + " is older than " + majorVersion + ". Will not apply mixin.");
        return actualMajorVersion >= majorVersion;
    }

}
