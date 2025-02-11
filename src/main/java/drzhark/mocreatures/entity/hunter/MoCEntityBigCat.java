/*
 * GNU GENERAL PUBLIC LICENSE Version 3
 */
package drzhark.mocreatures.entity.hunter;

import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.ai.EntityAIFollowAdult;
import drzhark.mocreatures.entity.ai.EntityAIFollowOwnerPlayer;
import drzhark.mocreatures.entity.ai.EntityAIHunt;
import drzhark.mocreatures.entity.ai.EntityAIWanderMoC2;
import drzhark.mocreatures.entity.inventory.MoCAnimalChest;
import drzhark.mocreatures.entity.tameable.MoCEntityTameableAnimal;
import drzhark.mocreatures.entity.tameable.MoCPetData;
import drzhark.mocreatures.init.MoCItems;
import drzhark.mocreatures.init.MoCSoundEvents;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSaddle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class MoCEntityBigCat extends MoCEntityTameableAnimal {

    private static final DataParameter<Boolean> RIDEABLE = EntityDataManager.createKey(MoCEntityBigCat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_AMULET = EntityDataManager.createKey(MoCEntityBigCat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(MoCEntityBigCat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> GHOST = EntityDataManager.createKey(MoCEntityBigCat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHESTED = EntityDataManager.createKey(MoCEntityBigCat.class, DataSerializers.BOOLEAN);
    public int mouthCounter;
    public int tailCounter;
    public int wingFlapCounter;
    public MoCAnimalChest localchest;
    public ItemStack localstack;
    protected String chestName = "BigCatChest";
    private int tCounter;
    private float fTransparency;

    public MoCEntityBigCat(World world) {
        super(world);
        setAge(45);
        setSize(1.4F, 1.3F);
        setAdult(this.rand.nextInt(4) != 0);
        stepHeight = 1.0F;
        experienceValue = 5;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(4, new EntityAIFollowAdult(this, 1.0D));
        this.tasks.addTask(5, new EntityAIFollowOwnerPlayer(this, 1D, 2F, 10F));
        this.tasks.addTask(2, new EntityAIWanderMoC2(this, 0.8D, 30));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        //this.targetTasks.addTask(2, new EntityAIHunt<>(this, EntityAnimal.class, false));
        this.targetTasks.addTask(3, new EntityAIHunt<>(this, EntityPlayer.class, false));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
    }

    @Override
    public void selectType() {
        if (getIsAdult()) {
            setAge(getMaxAge());
        }
    }

    @Override
    public double getCustomSpeed() {
        return 2D;
    }

    /**
     * Initializes datawatchers for entity. Each datawatcher is used to sync
     * server data to client.
     */
    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(RIDEABLE, Boolean.FALSE);
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(GHOST, Boolean.FALSE);
        this.dataManager.register(HAS_AMULET, Boolean.FALSE);
        this.dataManager.register(CHESTED, Boolean.FALSE);
    }

    public boolean getHasAmulet() {
        return this.dataManager.get(HAS_AMULET);
    }

    public void setHasAmulet(boolean flag) {
        this.dataManager.set(HAS_AMULET, flag);
    }

    @Override
    public boolean getIsSitting() {
        return this.dataManager.get(SITTING);
    }

    @Override
    public boolean getIsRideable() {
        return this.dataManager.get(RIDEABLE);
    }

    public boolean getIsChested() {
        return this.dataManager.get(CHESTED);
    }

    public void setIsChested(boolean flag) {
        this.dataManager.set(CHESTED, flag);
    }

    @Override
    public boolean getIsGhost() {
        return this.dataManager.get(GHOST);
    }

    public void setIsGhost(boolean flag) {
        this.dataManager.set(GHOST, flag);
    }

    public void setSitting(boolean flag) {
        this.dataManager.set(SITTING, flag);
    }

    public void setRideable(boolean flag) {
        this.dataManager.set(RIDEABLE, flag);
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return experienceValue;
    }

    // Method used for receiving damage from another source
    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        Entity entity = damagesource.getTrueSource();
        if ((this.isBeingRidden()) && (entity == this.getRidingEntity())) {
            return false;
        }

        if (super.attackEntityFrom(damagesource, i)) {
            if (entity != null && getIsTamed() && entity instanceof EntityPlayer) {
                return false;
            }
            if (entity != this && entity instanceof EntityLivingBase && (this.world.getDifficulty() != EnumDifficulty.PEACEFUL)) {
                setAttackTarget((EntityLivingBase) entity);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        openMouth();
        if (getIsAdult()) {
            return MoCSoundEvents.ENTITY_LION_DEATH;
        } else {
            return MoCSoundEvents.ENTITY_LION_DEATH_BABY;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        openMouth();
        if (getIsAdult()) {
            return MoCSoundEvents.ENTITY_LION_HURT;
        } else {
            return MoCSoundEvents.ENTITY_LION_HURT_BABY;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        openMouth();
        if (getIsAdult()) {
            return MoCSoundEvents.ENTITY_LION_AMBIENT;
        } else {
            return MoCSoundEvents.ENTITY_LION_AMBIENT_BABY;
        }
    }

    @Override
    public void onDeath(DamageSource damagesource) {
        if (!this.world.isRemote) {
            if (getHasAmulet()) {
                MoCTools.dropCustomItem(this, this.world, new ItemStack(MoCItems.medallion, 1));
                setHasAmulet(false);
            }

            if (getIsTamed() && !getIsGhost() && this.rand.nextInt(4) == 0) {
                this.spawnGhost();
            }
        }
        super.onDeath(damagesource);
    }

    public void spawnGhost() {
        try {
            EntityLiving templiving = (EntityLiving) EntityList.createEntityByIDFromName(new ResourceLocation(this.getClazzString().toLowerCase()), this.world);
            if (templiving instanceof MoCEntityBigCat) {
                MoCEntityBigCat ghost = (MoCEntityBigCat) templiving;
                ghost.setPosition(this.posX, this.posY, this.posZ);
                this.world.spawnEntity(ghost);
                MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_GENERIC_MAGIC_ENCHANTED);
                ghost.setOwnerId(this.getOwnerId());
                ghost.setTamed(true);
                EntityPlayer entityplayer = this.world.getClosestPlayerToEntity(this, 24D);
                if (entityplayer != null) {
                    MoCTools.tameWithName(entityplayer, ghost);
                }

                ghost.setAdult(false);
                ghost.setAge(1);
                ghost.setType(this.getType());
                ghost.selectType();
                ghost.setIsGhost(true);

            }
        } catch (Exception ignored) {
        }

    }

    @Override
    public void onLivingUpdate() {

        super.onLivingUpdate();

        if (!this.world.isRemote) {
            setSprinting(this.getAttackTarget() != null);
        }

        if (this.world.isRemote) //animation counters
        {
            if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
                this.mouthCounter = 0;
            }

            if (this.rand.nextInt(250) == 0) {
                moveTail();
            }

            if (this.tailCounter > 0 && ++this.tailCounter > 10 && this.rand.nextInt(15) == 0) {
                this.tailCounter = 0;
            }
        } else //server stuff
        {
            if (getIsGhost() && getAge() > 0 && getAge() < 10 && this.rand.nextInt(5) == 0) {
                setAge(getAge() + 1);
                if (getAge() == 9) {
                    setAge(getMaxAge());
                    setAdult(true);
                }
            }

            if (!getIsGhost() && getAge() < 10) {
                this.setDead();
            }
            /*if (getHasEaten() && rand.nextInt(300) == 0)
            {
                setEaten(false);
            }*/
        }

        if (!this.world.isRemote && isFlyer() && isOnAir()) {
            float myFlyingSpeed = MoCTools.getMyMovementSpeed(this);
            int wingFlapFreq = (int) (25 - (myFlyingSpeed * 10));
            if (!this.isBeingRidden() || wingFlapFreq < 5) {
                wingFlapFreq = 5;
            }
            if (this.rand.nextInt(wingFlapFreq) == 0) {
                wingFlap();
            }
        }

        if (isFlyer()) {
            if (this.wingFlapCounter > 0 && ++this.wingFlapCounter > 20) {
                this.wingFlapCounter = 0;
            }
            if (!this.world.isRemote && this.wingFlapCounter == 5) {
                MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_GENERIC_WING_FLAP);
            }
        }

        if ((this.rand.nextInt(300) == 0) && (this.getHealth() <= getMaxHealth()) && (this.deathTime == 0) && !this.world.isRemote) {
            this.setHealth(getHealth() + 1);
        }

        if ((this.deathTime == 0) && !isMovementCeased()) {
            EntityItem entityitem = getClosestItem(this, 12D, Items.PORKCHOP, Items.FISH);
            if (entityitem != null) {
                float f = entityitem.getDistance(this);
                if (f > 2.0F) {
                    setPathToEntity(entityitem, f);
                }
                if (f < 2.0F && this.deathTime == 0) {
                    entityitem.setDead();
                    this.setHealth(getMaxHealth());
                    setHasEaten(true);
                    MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_GENERIC_EAT);
                }
            }
        }
    }

    @Override
    public boolean readytoBreed() {
        return !this.getIsGhost() && super.readytoBreed();
    }

    public void wingFlap() {
        if (this.world.isRemote) {
            return;
        }

        if (this.wingFlapCounter == 0) {
            this.wingFlapCounter = 1;
            MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 3),
                    new TargetPoint(this.world.provider.getDimensionType().getId(), this.posX, this.posY, this.posZ, 64));
        }
    }

    @Override
    public boolean isNotScared() {
        return getIsAdult() || getAge() > 80;
    }

    @Override
    public boolean isReadyToHunt() {
        return getIsAdult() && !this.isMovementCeased();
    }

    @Override
    public boolean isReadyToFollowOwnerPlayer() { return !this.isMovementCeased(); }

    @Override
    public void updatePassenger(Entity passenger) {
        double dist = getSizeFactor() * (0.1D);
        double newPosX = this.posX + (dist * Math.sin(this.renderYawOffset / 57.29578F));
        double newPosZ = this.posZ - (dist * Math.cos(this.renderYawOffset / 57.29578F));
        passenger.setPosition(newPosX, this.posY + getMountedYOffset() + passenger.getYOffset(), newPosZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("Saddle", getIsRideable());
        nbttagcompound.setBoolean("Sitting", getIsSitting());
        nbttagcompound.setBoolean("Chested", getIsChested());
        nbttagcompound.setBoolean("Ghost", getIsGhost());
        nbttagcompound.setBoolean("Amulet", getHasAmulet());
        if (getIsChested() && this.localchest != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.localchest.getSizeInventory(); i++) {
                // grab the current item stack
                this.localstack = this.localchest.getStackInSlot(i);
                if (!this.localstack.isEmpty()) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte("Slot", (byte) i);
                    this.localstack.writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }
            nbttagcompound.setTag("Items", nbttaglist);
        }

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        setRideable(nbttagcompound.getBoolean("Saddle"));
        setSitting(nbttagcompound.getBoolean("Sitting"));
        setIsChested(nbttagcompound.getBoolean("Chested"));
        setIsGhost(nbttagcompound.getBoolean("Ghost"));
        setHasAmulet(nbttagcompound.getBoolean("Amulet"));
        if (getIsChested()) {
            NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
            this.localchest = new MoCAnimalChest("BigCatChest", 18);
            for (int i = 0; i < nbttaglist.tagCount(); i++) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 0xff;
                if (j < this.localchest.getSizeInventory()) {
                    this.localchest.setInventorySlotContents(j, new ItemStack(nbttagcompound1));
                }
            }
        }

    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        final Boolean tameResult = this.processTameInteract(player, hand);
        if (tameResult != null) {
            return tameResult;
        }

        final ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && !getIsTamed() && getHasEaten() && !getIsAdult() && (stack.getItem() == MoCItems.medallion)) {
            if (!this.world.isRemote) {
                setHasAmulet(true);
                MoCTools.tameWithName(player, this);
            }
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            return true;
        }

        if (!stack.isEmpty() && getIsTamed() && !getHasAmulet() && (stack.getItem() == MoCItems.medallion)) {
            if (!this.world.isRemote) {
                setHasAmulet(true);
            }
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            return true;
        }

        if (!stack.isEmpty() && getIsTamed() && (stack.getItem() == MoCItems.whip)) {
            setSitting(!getIsSitting());
            setIsJumping(false);
            getNavigator().clearPath();
            setAttackTarget(null);
            return true;
        }
        if (!stack.isEmpty() && getIsTamed() && (MoCTools.isItemEdibleforCarnivores(stack.getItem()))) {
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            this.setHealth(getMaxHealth());
            MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_GENERIC_EAT);
            setIsHunting(false);
            setHasEaten(true);
            return true;
        }
        if (!stack.isEmpty() && getIsTamed() && !getIsRideable() && (getAge() > 80)
                && (stack.getItem() instanceof ItemSaddle || stack.getItem() == MoCItems.horsesaddle)) {
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            setRideable(true);
            return true;
        }

        if (!stack.isEmpty() && this.getIsGhost() && this.getIsTamed() && stack.getItem() == MoCItems.amuletghost) {

            player.setHeldItem(hand, ItemStack.EMPTY);
            if (!this.world.isRemote) {
                MoCPetData petData = MoCreatures.instance.mapData.getPetData(this.getOwnerId());
                if (petData != null) {
                    petData.setInAmulet(this.getOwnerPetId(), true);
                }
                this.dropMyStuff();
                MoCTools.dropAmulet(this, 3, player);
                this.isDead = true;
            }

            return true;

        }

        if (!stack.isEmpty() && getIsTamed() && getIsAdult() && !getIsChested() && (stack.getItem() == Item.getItemFromBlock(Blocks.CHEST))) {
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            setIsChested(true);
            MoCTools.playCustomSound(this, SoundEvents.ENTITY_CHICKEN_EGG);
            return true;
        }

        if (getIsChested() && player.isSneaking()) {
            if (this.localchest == null) {
                this.localchest = new MoCAnimalChest(this.chestName, 18);
            }
            if (!this.world.isRemote) {
                player.displayGUIChest(this.localchest);
            }
            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    public float getSizeFactor() {
        return getAge() * 0.01F;
    }

    @Override
    public void fall(float f, float f1) {
        if (isFlyer()) {
            return;
        }
        float i = (float) (Math.ceil(f - 3F) / 2F);
        if (!this.world.isRemote && (i > 0)) {
            i /= 2;
            if (i > 1F) {
                attackEntityFrom(DamageSource.FALL, i);
            }
            if ((this.isBeingRidden()) && (i > 1F)) {
                for (Entity entity : this.getRecursivePassengers()) {
                    entity.attackEntityFrom(DamageSource.FALL, i);
                }
            }
            IBlockState iblockstate = this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.2D - (double) this.prevRotationYaw, this.posZ));
            Block block = iblockstate.getBlock();

            if (iblockstate.getMaterial() != Material.AIR && !this.isSilent()) {
                SoundType soundtype = block.getSoundType(iblockstate, world, new BlockPos(this.posX, this.posY - 0.2D - (double) this.prevRotationYaw, this.posZ), this);
                this.world.playSound(null, this.posX, this.posY, this.posZ, soundtype.getStepSound(), this.getSoundCategory(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
            }
        }
    }

    private void openMouth() {
        this.mouthCounter = 1;
    }

    public boolean hasMane() {
        return false;
    }

    @Override
    public int getTalkInterval() {
        return 400;
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    public boolean hasSaberTeeth() {
        return false;
    }

    @Override
    public void performAnimation(int animationType) {
        if (animationType == 0) //tail animation
        {
            //setPoisoning(true);
        } else if (animationType == 3) //wing flap
        {
            this.wingFlapCounter = 1;
        }
    }

    @Override
    public void makeEntityJump() {
        if (this.isFlyer()) {
            wingFlap();
        }
        super.makeEntityJump();
    }

    @Override
    public void dropMyStuff() {
        if (!this.world.isRemote) {
            dropArmor();
            MoCTools.dropSaddle(this, this.world);

            if (getIsChested()) {
                MoCTools.dropInventory(this, this.localchest);
                MoCTools.dropCustomItem(this, this.world, new ItemStack(Blocks.CHEST, 1));
                setIsChested(false);
            }
        }
    }

    public boolean getHasStinger() {
        return false;
    }

    @Override
    public double getMountedYOffset() {
        double Yfactor = ((0.0833D * this.getAge()) - 2.5D) / 10D;
        return this.height * Yfactor;
    }

    public float tFloat() {

        if (++this.tCounter > 30) {
            this.tCounter = 0;
            this.fTransparency = (this.rand.nextFloat() * (0.4F - 0.2F) + 0.15F);
        }

        if (this.getAge() < 10) {
            return 0F;
        }
        return this.fTransparency;
    }

    @Override
    public int nameYOffset() {
        return (int) (((0.445D * this.getAge()) + 15D) * -1);
    }

    @Override
    public boolean rideableEntity() {
        return true;
    }

    @Override
    public float getAIMoveSpeed() {
        if (isSprinting()) {
            return 0.37F;
        }
        return 0.18F;
    }
}

//would be nice
//lying down
//manticore sounds, drops
//cheetahs
//hand swing when attacking
//more hybrids
//jaguars
//lynx / bobcats