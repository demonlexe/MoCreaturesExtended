/*
 * GNU GENERAL PUBLIC LICENSE Version 3
 */
package drzhark.mocreatures.network.command;

import drzhark.mocreatures.entity.tameable.MoCPetData;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.tameable.IMoCTameable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandMoCTP extends CommandBase {

    private static final List<String> commands = new ArrayList<>();
    private static final List<String> aliases = new ArrayList<>();

    static {
        commands.add("/moctp <entityid> <playername>");
        commands.add("/moctp <petname> <playername>");
        aliases.add("moctp");
        //tabCompletionStrings.add("moctp");
    }

    @Override
    public String getName() {
        return "moctp";
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender par1ICommandSender) {
        return "commands.moctp.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        int petId;
        if (args.length == 0) {
            sender.sendMessage(new TextComponentTranslation(TextFormatting.RED + "Error" + TextFormatting.WHITE
                    + ": You must enter a valid entity ID."));
            return;
        }
        if (!(sender instanceof EntityPlayer)) {
            return;
        }
        try {
            petId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            petId = -1;
        }
        String playername = sender.getName();
        EntityPlayer player = (EntityPlayer) sender;//FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playername);
        // search for tamed entity in mocreatures.dat
        MoCPetData ownerPetData = MoCreatures.instance.mapData.getPetData(player.getUniqueID());
        if (ownerPetData != null) {
            for (int i = 0; i < ownerPetData.getTamedList().tagCount(); i++) {
                NBTTagCompound nbt = ownerPetData.getTamedList().getCompoundTagAt(i);
                if (nbt.hasKey("PetId") && nbt.getInteger("PetId") == petId) {
                    String petName = nbt.getString("Name");
                    WorldServer world = DimensionManager.getWorld(nbt.getInteger("Dimension"));
                    if (!teleportLoadedPet(world, player, petId, petName, sender)) {
                        double posX = nbt.getTagList("Pos", 6).getDoubleAt(0);
                        double posY = nbt.getTagList("Pos", 6).getDoubleAt(1);
                        double posZ = nbt.getTagList("Pos", 6).getDoubleAt(2);
                        int x = MathHelper.floor(posX);
                        int y = MathHelper.floor(posY);
                        int z = MathHelper.floor(posZ);
                        sender.sendMessage(new TextComponentTranslation("Found unloaded pet " + TextFormatting.GREEN
                                + nbt.getString("id") + TextFormatting.WHITE + " with name " + TextFormatting.AQUA + nbt.getString("Name")
                                + TextFormatting.WHITE + " at location " + TextFormatting.LIGHT_PURPLE + x + TextFormatting.WHITE + ", "
                                + TextFormatting.LIGHT_PURPLE + y + TextFormatting.WHITE + ", " + TextFormatting.LIGHT_PURPLE + z
                                + TextFormatting.WHITE + " with Pet ID " + TextFormatting.BLUE + nbt.getInteger("PetId")));
                        boolean result = teleportLoadedPet(world, player, petId, petName, sender); // attempt to TP again
                        if (!result) {
                            sender.sendMessage(new TextComponentTranslation("Unable to transfer entity ID " + TextFormatting.GREEN
                                    + petId + TextFormatting.WHITE + ". It may only be transferred to " + TextFormatting.AQUA
                                    + player.getName()));
                        }
                    }
                    break;
                }
            }
        } else {
            sender.sendMessage(new TextComponentTranslation("Tamed entity could not be located."));
        }
    }

    /**
     * Returns a sorted list of all possible commands for the given
     * ICommandSender.
     */
    protected List<String> getSortedPossibleCommands(ICommandSender par1ICommandSender) {
        Collections.sort(CommandMoCTP.commands);
        return CommandMoCTP.commands;
    }

    public boolean teleportLoadedPet(WorldServer world, EntityPlayer player, int petId, String petName, ICommandSender par1ICommandSender) {
        for (int j = 0; j < world.loadedEntityList.size(); j++) {
            Entity entity = world.loadedEntityList.get(j);
            // search for entities that are MoCEntityAnimal's
            if (IMoCTameable.class.isAssignableFrom(entity.getClass()) && !((IMoCTameable) entity).getPetName().equals("")
                    && ((IMoCTameable) entity).getOwnerPetId() == petId) {
                // grab the entity data
                NBTTagCompound compound = new NBTTagCompound();
                entity.writeToNBT(compound);
                if (!compound.isEmpty() && !compound.getString("Owner").isEmpty()) {
                    String owner = compound.getString("Owner");
                    String name = compound.getString("Name");
                    if (!owner.isEmpty() && owner.equalsIgnoreCase(player.getName())) {
                        // check if in same dimension
                        if (entity.dimension == player.dimension) {
                            entity.setPosition(player.posX, player.posY, player.posZ);
                        } else if (!player.world.isRemote)// transfer entity to player dimension
                        {
                            Entity newEntity = EntityList.newEntity(entity.getClass(), player.world);
                            if (newEntity != null) {
                                MoCTools.copyDataFromOld(newEntity, entity); // transfer all existing data to our new entity
                                newEntity.setPosition(player.posX, player.posY, player.posZ);
                                DimensionManager.getWorld(player.dimension).spawnEntity(newEntity);
                            }
                            if (entity.getRidingEntity() != null) {
                                entity.getRidingEntity().dismountRidingEntity();
                            }
                            entity.isDead = true;
                            world.resetUpdateEntityTick();
                            DimensionManager.getWorld(player.dimension).resetUpdateEntityTick();
                        }
                        par1ICommandSender.sendMessage(new TextComponentTranslation(TextFormatting.GREEN + name + TextFormatting.WHITE
                                + " has been tp'd to location " + Math.round(player.posX) + ", " + Math.round(player.posY) + ", "
                                + Math.round(player.posZ) + " in dimension " + player.dimension));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void sendCommandHelp(ICommandSender sender) {
        sender.sendMessage(new TextComponentTranslation("§2Listing MoCreatures commands"));
        for (String command : commands) {
            sender.sendMessage(new TextComponentTranslation(command));
        }
    }
}
