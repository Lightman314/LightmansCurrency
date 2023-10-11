package io.github.lightman314.lightmanscurrency.integration.ftbchunks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.data.ChunkTeamDataImpl;
import dev.ftb.mods.ftbchunks.net.SendGeneralDataPacket;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LCFTBChunksIntegration {

    public static void setup() { MinecraftForge.EVENT_BUS.register(LCFTBChunksIntegration.class); }

    public static boolean canBuyClaims() { return Config.SERVER.ftbChunksAllowClaimPurchase.get(); }
    public static boolean canBuyForceload() { return Config.SERVER.ftbChunksAllowForceloadPurchase.get(); }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> lcftbcommand =
                Commands.literal("lcftb")
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.literal("buy")
                            .then(Commands.literal("claim")
                                .executes(c -> tryBuyClaim(c,1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(c -> tryBuyClaim(c, IntegerArgumentType.getInteger(c, "count")))))
                            .then(Commands.literal("forceload")
                                    .executes(c -> tryBuyForceload(c, 1))
                                    .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                            .executes(c -> tryBuyForceload(c, IntegerArgumentType.getInteger(c, "count"))))))
                        .then(Commands.literal("info")
                                .executes(LCFTBChunksIntegration::info));

        dispatcher.register(lcftbcommand);

    }

    private static int info(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException
    {
        CommandSourceStack source = commandContext.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ChunkTeamData data = FTBChunksAPI.api().getManager().getOrCreateData(player);
        if(data == null)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.fail.nodata"));
            return 0;
        }
        else
        {
            int count = 0;
            if(canBuyClaims())
            {
                EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.claims.info", data.getExtraClaimChunks(), Config.SERVER.ftbChunksMaxClaimCount.get()), false);
                EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.info.price", Config.SERVER.ftbChunksClaimPrice.get().getComponent(EasyText.translatable("command.lightmanscurrency.lcftb.invalid_price"))), false);
                count++;
            }
            if(canBuyForceload())
            {
                EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.forceload.info", data.getExtraForceLoadChunks(), Config.SERVER.ftbChunksMaxForceloadCount.get()), false);
                EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.info.price", Config.SERVER.ftbChunksForceloadPrice.get().getComponent(EasyText.translatable("command.lightmanscurrency.lcftb.invalid_price"))), false);
                count++;
            }
            if(count <= 0)
                EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.info.disabled.all"));
            return count;
        }
    }

    private static int tryBuyClaim(CommandContext<CommandSourceStack> commandContext, int count) throws CommandSyntaxException
    {
        CommandSourceStack source = commandContext.getSource();
        if(!canBuyClaims())
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.claims.fail.disabled"));
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        ChunkTeamData personalData = FTBChunksAPI.api().getManager().getPersonalData(player.getUUID());
        if(personalData == null)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.fail.nodata"));
            return 0;
        }
        else
        {
            CoinValue price = Config.SERVER.ftbChunksClaimPrice.get();
            int boughtCount = 0;
            boolean hitLimit = false;
            boolean invalidPrice = !price.hasAny();
            for(; boughtCount < count; ++boughtCount)
            {
                if(invalidPrice)
                    break;
                int allowedToBuy = Config.SERVER.ftbChunksMaxClaimCount.get() - personalData.getExtraClaimChunks() - boughtCount;
                if(allowedToBuy <= 0)
                {
                    hitLimit = true;
                    break;
                }
                if(!MoneyUtil.ProcessPayment(null, player, price))
                    break;
            }
            if(boughtCount > 0)
            {
                personalData.setExtraClaimChunks(personalData.getExtraClaimChunks() + boughtCount);
                setDataChanged(personalData, player);
                if(hitLimit)
                    EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.claims.limit_reached", Config.SERVER.ftbChunksMaxClaimCount.get()), true);
                EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.claims.success", boughtCount), true);
                return boughtCount;
            }
            else
            {
                if(invalidPrice)
                    EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.fail.invalid_price"));
                else if(hitLimit)
                    EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.claims.limit_reached", Config.SERVER.ftbChunksMaxClaimCount.get()));
                else
                    EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.claims.fail.cantafford"));
                return 0;
            }
        }
    }

    private static int tryBuyForceload(CommandContext<CommandSourceStack> commandContext, int count) throws CommandSyntaxException
    {
        CommandSourceStack source = commandContext.getSource();
        if(!canBuyForceload())
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.forceload.fail.disabled"));
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        ChunkTeamData data = FTBChunksAPI.api().getManager().getOrCreateData(player);
        if(data == null)
        {
            EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.fail.nodata"));
            return 0;
        }
        else
        {
            CoinValue price = Config.SERVER.ftbChunksForceloadPrice.get();
            int boughtCount = 0;
            boolean hitLimit = false;
            boolean invalidPrice = !price.hasAny();
            for(; boughtCount < count; ++boughtCount)
            {
                if(invalidPrice)
                    break;
                int allowedToBuy = Config.SERVER.ftbChunksMaxForceloadCount.get() - data.getExtraForceLoadChunks() - boughtCount;
                if(allowedToBuy <= 0)
                {
                    hitLimit = true;
                    break;
                }
                if(!MoneyUtil.ProcessPayment(null, player, price))
                    break;
            }
            if(boughtCount > 0)
            {
                data.setExtraForceLoadChunks(data.getExtraForceLoadChunks() + boughtCount);
                setDataChanged(data, player);
                if(hitLimit)
                    EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.forceload.limit_reached", Config.SERVER.ftbChunksMaxClaimCount.get()), true);
                EasyText.sendCommandSucess(source, EasyText.translatable("command.lightmanscurrency.lcftb.forceload.success", boughtCount), true);
                return boughtCount;
            }
            else
            {
                if(invalidPrice)
                    EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.fail.invalid_price"));
                else if(hitLimit)
                    EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.forceload.limit_reached", Config.SERVER.ftbChunksMaxClaimCount.get()));
                else
                    EasyText.sendCommandFail(source, EasyText.translatable("command.lightmanscurrency.lcftb.forceload.fail.cantafford"));
                return 0;
            }
        }
    }

    private static void setDataChanged(ChunkTeamData data, ServerPlayer player)
    {
        if(data instanceof ChunkTeamDataImpl d)
            d.markDirty();
        ChunkTeamData teamData = FTBChunksAPI.api().getManager().getOrCreateData(player);
        if(teamData instanceof ChunkTeamDataImpl d2)
            d2.updateLimits();
        SendGeneralDataPacket.send(teamData, player);
    }

}
