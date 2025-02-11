/*
 * GNU GENERAL PUBLIC LICENSE Version 3
 */
package drzhark.mocreatures.dimension.chunk;

import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.dimension.worldgen.MoCWorldGenPortal;
import drzhark.mocreatures.dimension.worldgen.MoCWorldGenTower;
import drzhark.mocreatures.init.MoCBlocks;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;
import java.util.Random;

public class MoCChunkGeneratorWyvernLair implements IChunkGenerator {
    protected static final IBlockState WYVERN_STONE = MoCBlocks.wyvstone.getDefaultState();
    protected static final IBlockState WYVERN_DIRT = MoCBlocks.wyvdirt.getDefaultState();
    protected static final IBlockState WYVERN_GRASS = MoCBlocks.wyvgrass.getDefaultState();
    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    /**
     * RNG.
     */
    private final Random rand;
    /**
     * Reference to the World object.
     */
    private final World world;
    /**
     * are map structures going to be generated (e.g. strongholds)
     */
    private final boolean mapFeaturesEnabled;
    private final WorldGenEndIsland endIslands = new WorldGenEndIsland();
    /**
     * A NoiseGeneratorOctaves used in generating terrain
     */
    public NoiseGeneratorOctaves noiseGen5;
    /**
     * A NoiseGeneratorOctaves used in generating terrain
     */
    public NoiseGeneratorOctaves noiseGen6;
    double[] pnr;
    double[] ar;
    double[] br;
    private NoiseGeneratorOctaves lperlinNoise1;
    private NoiseGeneratorOctaves lperlinNoise2;
    private NoiseGeneratorOctaves perlinNoise1;
    //private final MapGenEndCity endCityGen = new MapGenEndCity(this);
    private NoiseGeneratorSimplex islandNoise;
    private double[] buffer;
    /**
     * The biomes that are used to generate the chunk
     */
    private Biome[] biomesForGeneration;
    // temporary variables used during event handling
    private int chunkX = 0;
    private int chunkZ = 0;
    private boolean towerDone = false;
    private boolean portalDone = false;

    public MoCChunkGeneratorWyvernLair(World worldIn, boolean mapFeaturesEnabledIn, long seed) {
        this.world = worldIn;
        this.mapFeaturesEnabled = mapFeaturesEnabledIn;
        this.rand = new Random(seed);
        this.lperlinNoise1 = new NoiseGeneratorOctaves(this.rand, 16);
        this.lperlinNoise2 = new NoiseGeneratorOctaves(this.rand, 16);
        this.perlinNoise1 = new NoiseGeneratorOctaves(this.rand, 8);
        this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
        this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
        this.islandNoise = new NoiseGeneratorSimplex(this.rand);

        InitNoiseGensEvent.ContextEnd ctx = new InitNoiseGensEvent.ContextEnd(lperlinNoise1, lperlinNoise2, perlinNoise1, noiseGen5, noiseGen6, islandNoise);
        ctx = TerrainGen.getModdedNoiseGenerators(worldIn, this.rand, ctx);
        this.lperlinNoise1 = ctx.getLPerlin1();
        this.lperlinNoise2 = ctx.getLPerlin2();
        this.perlinNoise1 = ctx.getPerlin();
        this.noiseGen5 = ctx.getDepth();
        this.noiseGen6 = ctx.getScale();
        this.islandNoise = ctx.getIsland();
    }

    /**
     * Generates a bare-bones chunk of nothing but stone or ocean blocks, formed, but featureless.
     */
    public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
        int i = 2;
        int j = 3;
        int k = 33;
        int l = 3;
        this.buffer = this.getHeights(this.buffer, x * 2, 0, z * 2, 3, 33, 3);

        for (int i1 = 0; i1 < 2; ++i1) {
            for (int j1 = 0; j1 < 2; ++j1) {
                for (int k1 = 0; k1 < 32; ++k1) {
                    double d0 = 0.25D;
                    double d1 = this.buffer[((i1) * 3 + j1) * 33 + k1];
                    double d2 = this.buffer[((i1) * 3 + j1 + 1) * 33 + k1];
                    double d3 = this.buffer[((i1 + 1) * 3 + j1) * 33 + k1];
                    double d4 = this.buffer[((i1 + 1) * 3 + j1 + 1) * 33 + k1];
                    double d5 = (this.buffer[((i1) * 3 + j1) * 33 + k1 + 1] - d1) * 0.25D;
                    double d6 = (this.buffer[((i1) * 3 + j1 + 1) * 33 + k1 + 1] - d2) * 0.25D;
                    double d7 = (this.buffer[((i1 + 1) * 3 + j1) * 33 + k1 + 1] - d3) * 0.25D;
                    double d8 = (this.buffer[((i1 + 1) * 3 + j1 + 1) * 33 + k1 + 1] - d4) * 0.25D;

                    for (int l1 = 0; l1 < 4; ++l1) {
                        double d9 = 0.125D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.125D;
                        double d13 = (d4 - d2) * 0.125D;

                        for (int i2 = 0; i2 < 8; ++i2) {
                            double d14 = 0.125D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * 0.125D;

                            for (int j2 = 0; j2 < 8; ++j2) {
                                IBlockState iblockstate = AIR;

                                if (d15 > 0.0D) {
                                    iblockstate = WYVERN_STONE;
                                }

                                int k2 = i2 + i1 * 8;
                                int l2 = l1 + k1 * 4;
                                int i3 = j2 + j1 * 8;
                                primer.setBlockState(k2, l2, i3, iblockstate);
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void buildSurfaces(ChunkPrimer primer) {
        if (!ForgeEventFactory.onReplaceBiomeBlocks(this, this.chunkX, this.chunkZ, primer, this.world)) return;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                byte b0 = 5;
                int k = -1;

                for (int l = 127; l >= 0; --l) {
                    IBlockState iblockstate2 = primer.getBlockState(i, l, j);

                    if (iblockstate2.getMaterial() == Material.AIR) {
                        k = -1;
                    } else if (iblockstate2.getBlock() == WYVERN_STONE.getBlock()) {
                        if (k == -1) {
                            k = b0;
                            primer.setBlockState(i, l, j, WYVERN_GRASS);
                        } else if (k > 0) {
                            --k;
                            primer.setBlockState(i, l, j, WYVERN_DIRT);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        this.chunkX = x;
        this.chunkZ = z;
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.setBlocksInChunk(x, z, chunkprimer);
        this.buildSurfaces(chunkprimer);

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte) Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private float getIslandHeightValue(int p_185960_1_, int p_185960_2_, int p_185960_3_, int p_185960_4_) {
        float f = (float) (p_185960_1_ * 2 + p_185960_3_);
        float f1 = (float) (p_185960_2_ * 2 + p_185960_4_);
        float f2 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * 8.0F;

        if (f2 > 80.0F) {
            f2 = 80.0F;
        }

        if (f2 < -100.0F) {
            f2 = -100.0F;
        }

        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                long k = p_185960_1_ + i;
                long l = p_185960_2_ + j;

                if (k * k + l * l > 4096L && this.islandNoise.getValue((double) k, (double) l) < -0.8999999761581421D) {
                    float f3 = (MathHelper.abs((float) k) * 3439.0F + MathHelper.abs((float) l) * 147.0F) % 13.0F + 9.0F;
                    f = (float) (p_185960_3_ - i * 2);
                    f1 = (float) (p_185960_4_ - j * 2);
                    float f4 = 100.0F - MathHelper.sqrt(f * f + f1 * f1) * f3;

                    if (f4 > 80.0F) {
                        f4 = 80.0F;
                    }

                    if (f4 < -100.0F) {
                        f4 = -100.0F;
                    }

                    if (f4 > f2) {
                        f2 = f4;
                    }
                }
            }
        }

        return f2;
    }

    private double[] getHeights(double[] p_185963_1_, int p_185963_2_, int p_185963_3_, int p_185963_4_, int p_185963_5_, int p_185963_6_, int p_185963_7_) {
        ChunkGeneratorEvent.InitNoiseField event = new ChunkGeneratorEvent.InitNoiseField(this, p_185963_1_, p_185963_2_, p_185963_3_, p_185963_4_, p_185963_5_, p_185963_6_, p_185963_7_);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) return event.getNoisefield();

        if (p_185963_1_ == null) {
            p_185963_1_ = new double[p_185963_5_ * p_185963_6_ * p_185963_7_];
        }

        double d0 = 684.412D;
        double d1 = 684.412D;
        d0 = d0 * 2.0D;
        this.pnr = this.perlinNoise1.generateNoiseOctaves(this.pnr, p_185963_2_, p_185963_3_, p_185963_4_, p_185963_5_, p_185963_6_, p_185963_7_, d0 / 80.0D, 4.277575000000001D, d0 / 80.0D);
        this.ar = this.lperlinNoise1.generateNoiseOctaves(this.ar, p_185963_2_, p_185963_3_, p_185963_4_, p_185963_5_, p_185963_6_, p_185963_7_, d0, 684.412D, d0);
        this.br = this.lperlinNoise2.generateNoiseOctaves(this.br, p_185963_2_, p_185963_3_, p_185963_4_, p_185963_5_, p_185963_6_, p_185963_7_, d0, 684.412D, d0);
        int i = p_185963_2_ / 2;
        int j = p_185963_4_ / 2;
        int k = 0;

        for (int l = 0; l < p_185963_5_; ++l) {
            for (int i1 = 0; i1 < p_185963_7_; ++i1) {
                float f = this.getIslandHeightValue(i, j, l, i1);

                for (int j1 = 0; j1 < p_185963_6_; ++j1) {
                    double d2 = this.ar[k] / 512.0D;
                    double d3 = this.br[k] / 512.0D;
                    double d5 = (this.pnr[k] / 10.0D + 1.0D) / 2.0D;
                    double d4;

                    if (d5 < 0.0D) {
                        d4 = d2;
                    } else if (d5 > 1.0D) {
                        d4 = d3;
                    } else {
                        d4 = d2 + (d3 - d2) * d5;
                    }

                    d4 = d4 - 8.0D;
                    d4 = d4 + (double) f;
                    int k1 = 2;

                    if (j1 > p_185963_6_ / 2 - k1) {
                        double d6 = (float) (j1 - (p_185963_6_ / 2 - k1)) / 64.0F;
                        d6 = MathHelper.clamp(d6, 0.0D, 1.0D);
                        d4 = d4 * (1.0D - d6) + -3000.0D * d6;
                    }

                    k1 = 8;

                    if (j1 < k1) {
                        double d7 = (float) (k1 - j1) / ((float) k1 - 1.0F);
                        d4 = d4 * (1.0D - d7) + -30.0D * d7;
                    }

                    p_185963_1_[k] = d4;
                    ++k;
                }
            }
        }

        return p_185963_1_;
    }

    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);

        int var4 = x * 16;
        int var5 = z * 16;
        BlockPos blockpos = new BlockPos(var4 + 16, 0, var5 + 16);
        Biome var6 = this.world.getBiome(blockpos);
        boolean var11 = false;

        int var12;
        int var13;
        int var14;

        if (!var11 && this.rand.nextInt(2) == 0) {
            var12 = var4 + this.rand.nextInt(16) + 8;
            var13 = this.rand.nextInt(128);
            var14 = var5 + this.rand.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, new BlockPos(var12, var13, var14));
        }

        if (!var11 && this.rand.nextInt(8) == 0) {
            var12 = var4 + this.rand.nextInt(16) + 8;
            var13 = this.rand.nextInt(this.rand.nextInt(120) + 8);
            var14 = var5 + this.rand.nextInt(16) + 8;

            if (var13 < 63 || this.rand.nextInt(10) == 0) {
                (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, new BlockPos(var12, var13, var14));
            }
        }

        var6.decorate(this.world, this.rand, new BlockPos(var4, 0, var5));

        if (x == 0 && z == 0 && !this.portalDone) {
            createPortal(this.world, this.rand);
        }

        //if (x != 0 && z != 0 && !this.towerDone) {
        //    generateTower(this.world, this.rand, (int) (this.world.rand.nextGaussian() * 64), (int) (this.world.rand.nextGaussian() * 64));
        //}

        MoCTools.performCustomWorldGenSpawning(this.world, var6, var4 + 8, var5 + 8, 16, 16, this.rand, this.world.getBiome(blockpos).getSpawnableList(EnumCreatureType.CREATURE), EntityLiving.SpawnPlacementType.ON_GROUND);

        ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);
        BlockFalling.fallInstantly = false;
    }

    public void generateTower(World par1World, Random par2Random, int par3, int par4) {
        MoCWorldGenTower myTower = new MoCWorldGenTower(Blocks.GRASS, Blocks.DOUBLE_STONE_SLAB, Blocks.LAPIS_ORE);
        if (!this.towerDone) {
            int randPosX = par3 + par2Random.nextInt(16) + 8;
            int randPosZ = par4 + par2Random.nextInt(16) + 8;
            this.towerDone = myTower.generate(par1World, par2Random, new BlockPos(randPosX, 61, randPosZ));
        }
    }

    public void createPortal(World par1World, Random par2Random) {
        MoCWorldGenPortal myPortal = new MoCWorldGenPortal(Blocks.QUARTZ_BLOCK, 2, Blocks.QUARTZ_STAIRS, 0, Blocks.QUARTZ_BLOCK, 1, Blocks.QUARTZ_BLOCK, 0);
        for (int i = 0; i < 16; i++) {
            if (!this.portalDone) {
                int randPosY = 56 + i;
                this.portalDone = myPortal.generate(par1World, par2Random, new BlockPos(0, randPosY, 0));
            }
        }
    }

    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }
}