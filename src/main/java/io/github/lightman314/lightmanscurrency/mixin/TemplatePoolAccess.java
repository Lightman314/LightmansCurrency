package io.github.lightman314.lightmanscurrency.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

//Mostly copied from Immersive Engineering
//See LCVillages class for primary usage

@Mixin(StructureTemplatePool.class)
public interface TemplatePoolAccess {

    @Accessor
    List<Pair<StructurePoolElement,Integer>> getRawTemplates();

    @Accessor
    @Final
    @Mutable
    void setRawTemplates(List<Pair<StructurePoolElement,Integer>> newElements);

    @Accessor
    ObjectArrayList<StructurePoolElement> getTemplates();

}