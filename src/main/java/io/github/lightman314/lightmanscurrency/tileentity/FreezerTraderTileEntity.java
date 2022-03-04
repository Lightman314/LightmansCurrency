package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class FreezerTraderTileEntity extends ItemTraderTileEntity{

	/** The current angle of the door (between 0 and 1) */
	private float doorAngle;
	/** The angle of the door last tick */
	private float prevDoorAngle;
	
	public FreezerTraderTileEntity()
	{
		super(ModTileEntities.FREEZER_TRADER);
	}
	
	public FreezerTraderTileEntity(int tradeCount)
	{
		super(ModTileEntities.FREEZER_TRADER, tradeCount);
	}
	
	public float getDoorAngle(float partialTicks) {
		return MathHelper.lerp(partialTicks, this.prevDoorAngle, this.doorAngle);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		//this.userCount = this.storageContainers.size();
		int userCount = this.getUserCount();
		//LightmansCurrency.LOGGER.info("Freezer Usercount: " + userCount);
		
		this.prevDoorAngle = this.doorAngle;
		//Play the opening sound
		if (userCount > 0 && this.doorAngle == 0.0F) {
			this.world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
		}
		
		if(userCount > 0 && this.doorAngle < 1f)
		{
			this.doorAngle += 0.1f;
		}
		else if(userCount == 0 && doorAngle > 0f)
		{
			this.doorAngle -= 0.1f;
			if (this.doorAngle < 0.5F && this.prevDoorAngle >= 0.5F) {
				this.world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
		if(this.doorAngle > 1f)
			this.doorAngle = 1f;
		else if(this.doorAngle < 0f)
			this.doorAngle = 0f;
		
	}

}
