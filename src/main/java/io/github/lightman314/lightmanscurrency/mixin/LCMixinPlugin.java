package io.github.lightman314.lightmanscurrency.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Wouldn't need to do this if they had done what they said they would do...<br>
 * <a href="https://legacy.curseforge.com/minecraft/mc-mods/supplementaries/issues/928">Them saying they'd add a trade event to Red Merchants</a>
 */
public final class LCMixinPlugin implements IMixinConfigPlugin {

    public static final List<Predicate<String>> FILTERS = ImmutableList.of((mixin) -> !mixin.contains("supplementaries") || ModList.get().isLoaded("supplementaries"));

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FILTERS.stream().allMatch(f -> f.test(mixinClassName));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
