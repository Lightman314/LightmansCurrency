package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.function.BiConsumer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.settings.item.*;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ItemTraderSettings extends Settings {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	
	private static final String UPDATE_INPUT_SIDE = "updateInputSide";
	private static final String UPDATE_OUTPUT_SIDE = "updateOutputSide";
	private static final String UPDATE_INPUT_LIMIT = "updateInputLimit";
	private static final String UPDATE_OUTPUT_LIMIT = "updateOutputLimit";
	
	public enum ItemHandlerSettings
	{
		DISABLED,
		INPUT_ONLY,
		OUTPUT_ONLY,
		INPUT_AND_OUTPUT
	}
	
	public ItemTraderSettings(ITrader trader, IMarkDirty marker, BiConsumer<ResourceLocation,CompoundTag> sendToServer) { super(marker, sendToServer, TYPE); this.trader = trader;}
	
	private final ITrader trader;
	
	protected final boolean hasPermission(Player player, String permission) { return this.trader.getCoreSettings().hasPermission(player, permission); }
	
	protected final int getPermissionLevel(Player player, String permission) { return this.trader.getCoreSettings().getPermissionLevel(player, permission); }
	
	DirectionalSettings enabledInputSides = new DirectionalSettings();
	public DirectionalSettings getInputSides() { return this.enabledInputSides; }
	DirectionalSettings enabledOutputSides = new DirectionalSettings();
	public DirectionalSettings getOutputSides() { return this.enabledOutputSides; }
	boolean limitInputs = true;
	public boolean limitInputsToSales() { return this.limitInputs; }
	public void setLimitInputsToSales(boolean limitInputs) { this.limitInputs = limitInputs; }
	boolean limitOutputs = true;
	public boolean limitOutputsToPurchases() { return this.limitOutputs; }
	public void setLimitOutputsToPurchases(boolean limitOutputs) { this.limitOutputs = limitOutputs; }
	
	public ItemHandlerSettings getHandlerSetting(Direction side)
	{
		if(this.enabledInputSides.get(side))
		{
			if(this.enabledOutputSides.get(side))
				return ItemHandlerSettings.INPUT_AND_OUTPUT;
			else
				return ItemHandlerSettings.INPUT_ONLY;
		}
		else if(this.enabledOutputSides.get(side))
			return ItemHandlerSettings.OUTPUT_ONLY;
		return ItemHandlerSettings.DISABLED;
	}
	
	public CompoundTag toggleInputSide(Player requestor, Direction side)
	{
		if(!this.hasPermission(requestor, Permissions.ItemTrader.EXTERNAL_INPUTS))
		{
			PermissionWarning(requestor, "toggle external input side", Permissions.ItemTrader.EXTERNAL_INPUTS);
			return null;
		}
		this.enabledInputSides.set(side, !this.enabledInputSides.get(side));
		boolean newValue = this.enabledInputSides.get(side);
		this.trader.getCoreSettings().logger.LogSettingsChange(requestor, "inputSide." + side.toString(), newValue);
		CompoundTag updateInfo = this.initUpdateInfo(UPDATE_INPUT_SIDE);
		updateInfo.putInt("side", side.get3DDataValue());
		updateInfo.putBoolean("enabled", newValue);
		return updateInfo;
	}
	
	public CompoundTag toggleOutputSide(Player requestor, Direction side)
	{
		if(!this.hasPermission(requestor, Permissions.ItemTrader.EXTERNAL_INPUTS))
		{
			PermissionWarning(requestor, "toggle external output side", Permissions.ItemTrader.EXTERNAL_INPUTS);
			return null;
		}
		this.enabledOutputSides.set(side, !this.enabledOutputSides.get(side));
		boolean newValue = this.enabledOutputSides.get(side);
		this.trader.getCoreSettings().logger.LogSettingsChange(requestor, "outputSide." + side.toString(), newValue);
		CompoundTag updateInfo = this.initUpdateInfo(UPDATE_OUTPUT_SIDE);
		updateInfo.putInt("side", side.get3DDataValue());
		updateInfo.putBoolean("enabled", newValue);
		return updateInfo;
	}
	
	public CompoundTag toggleInputLimit(Player requestor)
	{
		if(!this.hasPermission(requestor, Permissions.ItemTrader.EXTERNAL_INPUTS))
		{
			PermissionWarning(requestor, "toggle external output side", Permissions.ItemTrader.EXTERNAL_INPUTS);
			return null;
		}
		this.limitInputs = !this.limitInputs;
		this.trader.getCoreSettings().logger.LogSettingsChange(requestor, "inputLimit", this.limitInputs);
		CompoundTag updateInfo = this.initUpdateInfo(UPDATE_INPUT_LIMIT);
		updateInfo.putBoolean("limited", this.limitInputs);
		return updateInfo;
	}
	
	public CompoundTag toggleOutputLimit(Player requestor)
	{
		if(!this.hasPermission(requestor, Permissions.ItemTrader.EXTERNAL_INPUTS))
		{
			PermissionWarning(requestor, "toggle external output side", Permissions.ItemTrader.EXTERNAL_INPUTS);
			return null;
		}
		this.limitOutputs = !this.limitOutputs;
		this.trader.getCoreSettings().logger.LogSettingsChange(requestor, "outputLimit", this.limitOutputs);
		CompoundTag updateInfo = this.initUpdateInfo(UPDATE_OUTPUT_LIMIT);
		updateInfo.putBoolean("limited", this.limitOutputs);
		return updateInfo;
	}
	
	@Override
	public void changeSetting(Player requestor, CompoundTag updateInfo) {
		if(this.isUpdateType(updateInfo, UPDATE_INPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(updateInfo.getInt("side"));
			boolean newValue = updateInfo.getBoolean("enabled");
			if(newValue != this.enabledInputSides.get(side))
			{
				CompoundTag result = this.toggleInputSide(requestor, side);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_OUTPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(updateInfo.getInt("side"));
			boolean newValue = updateInfo.getBoolean("enabled");
			if(newValue != this.enabledOutputSides.get(side))
			{
				CompoundTag result = this.toggleOutputSide(requestor, side);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_INPUT_LIMIT))
		{
			boolean newValue = updateInfo.getBoolean("limited");
			if(newValue != this.limitInputs)
			{
				CompoundTag result = this.toggleInputLimit(requestor);
				if(result != null)
					this.markDirty();
			}
		}
		else if(this.isUpdateType(updateInfo, UPDATE_OUTPUT_LIMIT))
		{
			boolean newValue = updateInfo.getBoolean("limited");
			if(newValue != this.limitOutputs)
			{
				CompoundTag result = this.toggleOutputLimit(requestor);
				if(result != null)
					this.markDirty();
			}
		}
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		
		compound.put("InputSides", this.enabledInputSides.save(new CompoundTag()));
		compound.put("OutputSides", this.enabledOutputSides.save(new CompoundTag()));
		compound.putBoolean("LimitInputs", this.limitInputs);
		compound.putBoolean("LimitOutputs", this.limitOutputs);
		
		return compound;
		
	}
	
	public void load(CompoundTag compound)
	{
		this.enabledInputSides.load(compound.getCompound("InputSides"));
		this.enabledOutputSides.load(compound.getCompound("OutputSides"));
		this.limitInputs = compound.getBoolean("LimitInputs");
		this.limitOutputs = compound.getBoolean("LimitOutputs");
	}
	
	@Override
	protected void initSettingsTabs() {
		this.addTab(ItemInputTab.INSTANCE);
	}
	
	
}
