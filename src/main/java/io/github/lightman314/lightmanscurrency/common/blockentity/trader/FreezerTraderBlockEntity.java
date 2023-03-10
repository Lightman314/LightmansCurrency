package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class FreezerTraderBlockEntity extends ItemTraderBlockEntity {

	/** The current angle of the door (between 0 and 1) */
	private float doorAngle;
	/** The angle of the door last tick */
	private float prevDoorAngle;
	
	public FreezerTraderBlockEntity()
	{
		super(ModBlockEntities.FREEZER_TRADER.get());
	}
	
	public FreezerTraderBlockEntity(int tradeCount)
	{
		super(ModBlockEntities.FREEZER_TRADER.get(), tradeCount);
	}
	
	public float getDoorAngle(float partialTicks) { return MathHelper.lerp(partialTicks, this.prevDoorAngle, this.doorAngle); }
	
	private static final float distancePerTick = 0.1f;
	
	@Override
	public void tick()
	{
		super.tick();

		if(!this.isClient())
			return;

		TraderData trader = this.getTraderData();
		if(trader != null)
		{
			int userCount = trader.getUserCount();
			
			this.prevDoorAngle = this.doorAngle;
			//Play the opening sound
			if (userCount > 0 && this.doorAngle == 0.0F) {
				this.level.playLocalSound(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 0.5d, this.worldPosition.getZ() + 0.5d, SoundEvents.CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				//this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
			}
			if(userCount > 0 && this.doorAngle < 1f)
			{
				this.doorAngle += distancePerTick;
			}
			else if(userCount <= 0 && doorAngle > 0f)
			{
				this.doorAngle -= distancePerTick;
				if (this.doorAngle < 0.5F && this.prevDoorAngle >= 0.5F) {
					this.level.playLocalSound(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 0.5d, this.worldPosition.getZ() + 0.5d, SoundEvents.CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				}
			}
			if(this.doorAngle > 1f)
				this.doorAngle = 1f;
			else if(this.doorAngle < 0f)
				this.doorAngle = 0f;
		}
	}

}