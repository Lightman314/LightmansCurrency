package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.BooleanPermission;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public abstract class InputTraderData extends TraderData {

	public static MutableComponent getFacingName(Direction side) { return EasyText.translatable("gui.lightmanscurrency.settings.side." + side.toString().toLowerCase()); }

	public final ImmutableList<Direction> ignoreSides;
	private final Map<Direction,Boolean> inputSides = new HashMap<>();
	private final Map<Direction,Boolean> outputSides = new HashMap<>();

	@Override
	protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) {
		defaultValues.put(Permissions.InputTrader.EXTERNAL_INPUTS, 1);
	}

	protected InputTraderData(ResourceLocation type) { this(type, ImmutableList.of()); }
	protected InputTraderData(ResourceLocation type, ImmutableList<Direction> ignoreSides) { super(type); this.ignoreSides = ignoreSides; }
	protected InputTraderData(ResourceLocation type, Level level, BlockPos pos) { this(type, level, pos, ImmutableList.of()); }
	protected InputTraderData(ResourceLocation type, Level level, BlockPos pos, ImmutableList<Direction> ignoreSides) {
		super(type, level, pos);
		this.ignoreSides = ignoreSides;
	}

	public boolean allowInputSide(Direction side) {
		if(this.ignoreSides.contains(side))
			return false;
		return this.inputSides.getOrDefault(side, false);
	}

	public final boolean hasInputSide() {
		for(Direction side : Direction.values())
		{
			if(this.allowInputSide(side))
				return true;
		}
		return false;
	}

	public boolean allowOutputSide(Direction side) {
		if(this.ignoreSides.contains(side))
			return false;
		return this.outputSides.getOrDefault(side, false);
	}

	public final boolean hasOutputSide() {
		for(Direction side : Direction.values())
		{
			if(this.allowOutputSide(side))
				return true;
		}
		return false;
	}

	public void setInputSide(Player player, Direction side, boolean value) {
		if(this.hasPermission(player, Permissions.InputTrader.EXTERNAL_INPUTS) && value != this.allowInputSide(side))
		{
			if(this.ignoreSides.contains(side))
				return;
			this.inputSides.put(side, value);
			this.markDirty(this::saveInputSides);

			if(player != null)
				this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Input-" + getFacingName(side).getString(), String.valueOf(this.allowInputSide(side))));
		}
	}

	public void setOutputSide(Player player, Direction side, boolean value) {
		if(this.hasPermission(player, Permissions.InputTrader.EXTERNAL_INPUTS) && value != this.allowOutputSide(side))
		{
			if(this.ignoreSides.contains(side))
				return;
			this.outputSides.put(side, value);
			this.markDirty(this::saveOutputSides);

			if(player != null)
				this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Output-" + getFacingName(side).getString(), String.valueOf(this.allowOutputSide(side))));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		this.saveInputSides(compound);
		this.saveOutputSides(compound);
	}

	protected final void saveInputSides(CompoundTag compound) {
		CompoundTag tag = new CompoundTag();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			tag.putBoolean(side.toString(), this.allowInputSide(side));
		}
		compound.put("InputSides", tag);
	}

	protected final void saveOutputSides(CompoundTag compound) {
		CompoundTag tag = new CompoundTag();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			tag.putBoolean(side.toString(), this.allowOutputSide(side));
		}
		compound.put("OutputSides", tag);
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {
		if(compound.contains("InputSides"))
		{
			this.inputSides.clear();
			CompoundTag tag = compound.getCompound("InputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(tag.contains(side.toString()))
					this.inputSides.put(side, tag.getBoolean(side.toString()));
			}
		}

		if(compound.contains("OutputSides"))
		{
			this.outputSides.clear();
			CompoundTag tag = compound.getCompound("OutputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(tag.contains(side.toString()))
					this.outputSides.put(side, tag.getBoolean(side.toString()));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public abstract IconData inputSettingsTabIcon();
	@OnlyIn(Dist.CLIENT)
	public abstract MutableComponent inputSettingsTabTooltip();
	@OnlyIn(Dist.CLIENT)
	public List<? extends InputTabAddon> inputSettingsAddons() { return ImmutableList.of(); }

	@Override
	public void handleSettingsChange(@Nonnull Player player, @Nonnull LazyPacketData message) {
		super.handleSettingsChange(player, message);

		if(message.contains("SetInputSide"))
		{
			boolean newValue = message.getBoolean("SetInputSide");
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setInputSide(player, side, newValue);
		}
		if(message.contains("SetOutputSide"))
		{
			boolean newValue = message.getBoolean("SetOutputSide");
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setOutputSide(player, side, newValue);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	@Deprecated(since = "2.1.2.4")
	public void receiveNetworkMessage(@Nonnull Player player, @Nonnull CompoundTag message)
	{
		super.receiveNetworkMessage(player, message);

		if(message.contains("SetInputSide"))
		{
			boolean newValue = message.getBoolean("SetInputSide");
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setInputSide(player, side, newValue);
		}
		if(message.contains("SetOutputSide"))
		{
			boolean newValue = message.getBoolean("SetOutputSide");
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setOutputSide(player, side, newValue);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addSettingsTabs(TraderSettingsClientTab tab, List<SettingsSubTab> tabs) { tabs.add(new InputTab(tab)); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addPermissionOptions(List<PermissionOption> options) { options.add(BooleanPermission.of(Permissions.InputTrader.EXTERNAL_INPUTS)); }

	@Deprecated
	protected final void loadOldInputSides(CompoundTag compound) {
		this.inputSides.clear();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			if(compound.contains(side.toString()))
				this.inputSides.put(side, compound.getBoolean(side.toString()));
		}
	}

	@Deprecated
	protected final void loadOldOutputSides(CompoundTag compound) {
		this.outputSides.clear();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			if(compound.contains(side.toString()))
				this.outputSides.put(side, compound.getBoolean(side.toString()));
		}
	}

}