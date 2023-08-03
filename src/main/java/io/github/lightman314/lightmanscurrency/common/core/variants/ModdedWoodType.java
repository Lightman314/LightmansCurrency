package io.github.lightman314.lightmanscurrency.common.core.variants;

import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

public class ModdedWoodType extends WoodType
{
    private final String mod;
    public ModdedWoodType(@Nonnull String name, @Nonnull MaterialColor mapColor, @Nonnull String mod) { super(name, mapColor); this.mod = mod; }
    public ModdedWoodType(@Nonnull String name, @Nonnull MaterialColor mapColor, @Nonnull String mod, @Nonnull WoodData data) { super(name, mapColor, data); this.mod = mod; }
    @Nonnull
    @Override
    public String getModID() { return this.mod; }
    @Override
    public boolean isValid() { return ModList.get().isLoaded(this.mod); }
}