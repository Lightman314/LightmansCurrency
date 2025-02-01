package io.github.lightman314.lightmanscurrency.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class DebugCommands {

    private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.structure.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {

        LiteralArgumentBuilder<CommandSourceStack> placeStructureCommand =
                Commands.literal("lcdebug")
                        .requires(stack -> stack.hasPermission(2) && stack.isPlayer())
                        .then(Commands.literal("placeStructure")
                                .then(Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE))
                                        .executes(DebugCommands::placeStructure))
                        );

        dispatcher.register(placeStructureCommand);

    }

    private static int placeStructure(CommandContext<CommandSourceStack> commandContext)
    {
        try {
            CommandSourceStack stack = commandContext.getSource();
            ServerLevel level = stack.getLevel();
            Holder.Reference<Structure> holder = ResourceKeyArgument.getStructure(commandContext,"structure");
            Structure structure = holder.value();
            ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
            BlockPos pos = BlockPos.containing(stack.getPosition());
            StructureStart start = structure.generate(stack.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), new ChunkPos(pos), 0, level, (p_214580_) -> {
                return true;
            });
            if (!start.isValid()) {
                throw ERROR_STRUCTURE_FAILED.create();
            } else {
                BoundingBox area = start.getBoundingBox();
                ChunkPos $$8 = new ChunkPos(SectionPos.blockToSectionCoord(area.minX()), SectionPos.blockToSectionCoord(area.minZ()));
                ChunkPos $$9 = new ChunkPos(SectionPos.blockToSectionCoord(area.maxX()), SectionPos.blockToSectionCoord(area.maxZ()));
                checkLoaded(level, $$8, $$9);
                ChunkPos.rangeClosed($$8, $$9).forEach((p_289290_) -> {
                    start.placeInChunk(level, level.structureManager(), chunkGenerator, level.getRandom(), new BoundingBox(p_289290_.getMinBlockX(), level.getMinBuildHeight(), p_289290_.getMinBlockZ(), p_289290_.getMaxBlockX(), level.getMaxBuildHeight(), p_289290_.getMaxBlockZ()), p_289290_);
                });
                String structureID = holder.key().location().toString();
                stack.sendSuccess(() -> {
                    return Component.translatable("commands.place.structure.success", structureID, pos.getX(), pos.getY(), pos.getZ());
                }, true);
                return 1;
            }
        } catch (Throwable t) {
            LightmansCurrency.LogError("Error placing structure.", t);
            return 0;
        }
    }

    private static void checkLoaded(ServerLevel level, ChunkPos pos1, ChunkPos pos2) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed(pos1, pos2).anyMatch((p_214542_) -> {
            return !level.isLoaded(p_214542_.getWorldPosition());
        })) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }

}
