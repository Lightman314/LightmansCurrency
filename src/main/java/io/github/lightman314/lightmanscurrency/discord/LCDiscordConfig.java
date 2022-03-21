package io.github.lightman314.lightmanscurrency.discord;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class LCDiscordConfig {

	//Currency Configs
	public final ForgeConfigSpec.ConfigValue<String> currencyChannel;
	public final ForgeConfigSpec.ConfigValue<String> currencyCommandPrefix;
	
	private LCDiscordConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Discord bot settings. Requires lightmansdiscord v1.0.5.0+ to use.").push("lightmanscurrency");
		
		this.currencyChannel = builder
				.comment("The channel where users can run the currency commands and where currency related announcements will be made.")
				.define("channel", "000000000000000000");
		this.currencyCommandPrefix = builder
				.comment("Prefix for currency commands.")
				.define("prefix", "!");
		
		builder.pop();
	}
	
	public static final ForgeConfigSpec discordSpec;
	public static final LCDiscordConfig DISCORD;
	
	static
	{
		//Discord
		final Pair<LCDiscordConfig,ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(LCDiscordConfig::new);
		discordSpec = serverPair.getRight();
		DISCORD = serverPair.getLeft();
	}
	
}
