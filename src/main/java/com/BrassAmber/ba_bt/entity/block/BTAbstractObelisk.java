package com.BrassAmber.ba_bt.entity.block;

import com.BrassAmber.ba_bt.BrassAmberBattleTowers;
import com.BrassAmber.ba_bt.block.block.BTSpawnerBlock;
import com.BrassAmber.ba_bt.block.blockentity.GolemChestBlockEntity;
import com.BrassAmber.ba_bt.block.blockentity.TowerChestBlockEntity;
import com.BrassAmber.ba_bt.block.blockentity.spawner.BTAbstractSpawnerBlockEntity;
import com.BrassAmber.ba_bt.entity.hostile.golem.BTAbstractGolem;
import com.BrassAmber.ba_bt.item.item.ResonanceStoneItem;
import com.BrassAmber.ba_bt.util.BTStatics;
import com.BrassAmber.ba_bt.util.BTUtil;
import com.BrassAmber.ba_bt.util.GolemType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.BrassAmber.ba_bt.util.BTLoot.getLootTable;
import static com.BrassAmber.ba_bt.util.BTStatics.*;
import static com.BrassAmber.ba_bt.util.BTUtil.distanceTo2D;
import static com.BrassAmber.ba_bt.util.BTUtil.doNoOutputPostionedCommand;

@SuppressWarnings("DanglingJavadoc")
public class BTAbstractObelisk extends Entity {
    // Parameters that must be saved
    private static final EntityDataAccessor<Integer> SPAWNERS_DESTROYED = SynchedEntityData.defineId(BTAbstractObelisk.class, EntityDataSerializers.INT);

    @SuppressWarnings("FieldMayBeFinal")
    private List<BlockPos> CHESTS = new ArrayList<>(9);
    private List<List<BlockPos>> SPAWNERS;
    private List<Integer> spawnerAmounts;
    private List<Integer> keySpawnerAmounts;
    protected List<EntityType<?>> towerMobs;

    //Other Parameters
    private boolean initialized;
    private boolean clientInitialized;
    protected int checkLayer;
    protected int currentFloorY;
    protected boolean doServerInit;
    private boolean doCheck;

    protected MusicManager music;
    protected int musicDistance;
    protected int towerRange;

    protected GolemType golemType;
    private boolean justSpawnedKey;

    public Music TOWER_MUSIC;
    public Music BOSS_MUSIC;

    // Data Strings
    private final String towerName = "Tower";
    private final String spawnersDestroyedName = "SpawnersDestroyed";
    private final String chestsFoundName = "ChestsFound?";

    private final String spawnersName = "Spawners";
    private final String chestsName = "TowerChests";

    protected boolean musicPlaying;
    protected boolean canCheck;
    private boolean golemSpawned = false;
    private Class<? extends Entity> specialEnemy;
    private boolean chestsFound;
    public boolean hasPlayer;

    protected int floorDistance;
    protected Block chestBlock;
    protected Block golemChestBlock;
    protected Block spawnerBlock;
    protected Block spawnerFillBlock;
    protected Block woolBlock;
    protected List<List<Integer>> perFloorData;
    protected List<Integer> floorData;
    protected GolemChestBlockEntity golemChest;

    public BTAbstractObelisk(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.initialized = false;
        this.clientInitialized = false;
        this.checkLayer = 1;
        this.blocksBuilding = true;
        this.musicPlaying = false;
        this.doServerInit = true;
        this.doCheck = true;
        this.hasPlayer = false;
        this.canCheck = true;
        this.musicDistance = 0;
        this.towerRange = 0;
    }

    public BTAbstractObelisk(GolemType golemType, Level level) {
        this(GolemType.getObeliskFor(golemType), level);
        this.golemType = golemType;
        this.chestsFound = false;
    }

    public void initialize() {
        if (!this.level.isClientSide()) {
            if (this.doServerInit) {
                BrassAmberBattleTowers.LOGGER.info("Obelisk of type " + this.golemType + " at " + this.blockPosition());
                if (!this.chestsFound ) {
                    this.spawnerAmounts = towerSpawnerAmounts.get(GolemType.getNumForType(this.golemType));
                    this.SPAWNERS = Arrays.asList(new ArrayList<>(this.spawnerAmounts.get(0)), new ArrayList<>(this.spawnerAmounts.get(1)),
                            new ArrayList<>(this.spawnerAmounts.get(2)), new ArrayList<>(this.spawnerAmounts.get(3)),
                            new ArrayList<>(this.spawnerAmounts.get(4)), new ArrayList<>(this.spawnerAmounts.get(5)),
                            new ArrayList<>(this.spawnerAmounts.get(6)), new ArrayList<>(this.spawnerAmounts.get(7)));
                }

                int golemNum = GolemType.getNumForType(this.golemType);
                this.keySpawnerAmounts = towerChestUnlocking.get(golemNum);
                this.specialEnemy = GolemType.getSpecialEnemyClass(this.golemType);
                this.towerMobs = BTStatics.towerMobs.get(golemNum);
                this.perFloorData = towerSpawnerData.get(golemNum);
                this.floorData = this.perFloorData.get(0);
                this.serverInitialize();
                this.doServerInit = false;
                /**BrassAmberBattleTowers.LOGGER.info(this.golemType + " " + this.chestBlock + " " +
                        this.spawnerBlock + " " + this.BOSS_MUSIC + " " + this.TOWER_MUSIC + " " + this.woolBlock);
                **/
            }
            if (!this.chestsFound ) {
                this.findChestsAndSpawners(this.level);
            } else {
                this.initialized = true;
            }
        }
    }

    public void serverInitialize() {}

    public void findChestsAndSpawners(Level level) {
        // Monoliths are always centered on their floor
        BlockPos center = this.getOnPos();
        int nextFloorY;
        if (this.checkLayer == 8) {
            nextFloorY = this.currentFloorY + this.floorDistance + this.floorDistance;
        } else {
            nextFloorY = this.currentFloorY + this.floorDistance;
        }


        // BrassAmberBattleTowers.LOGGER.info("Floor y: " + this.currentFloorY + " Next y: " + nextFloorY);

        // Get corners of tower area.
        BlockPos corner = center.offset(-15, 0, -15);
        BlockPos oppositeCorner = center.offset(15, 0, 15);
        BlockPos toCheck;
        int spawnersSet = 0;

        // Certain tower have obelisks at the 'top' instead of the bottom -ie ocean, and we need to make sure we use the
        // correct lower number of currentFloorY and nextFloorY
        int bottomOfFloor = Math.min(this.currentFloorY, nextFloorY);
        int topOfFloor = Math.max(this.currentFloorY, nextFloorY);

        // Check all blocks, not needed for land technically (could just check inside tower) but will be useful for
        // Nether/End Towers
        for (int x = corner.getX(); x < oppositeCorner.getX(); x++) {
            for (int z = corner.getZ(); z < oppositeCorner.getZ(); z++) {
                for (int y = bottomOfFloor; y <= topOfFloor; y++) {
                    toCheck = new BlockPos(x, y, z);
                    // This is here to avoid unnecessary variable passing to checkPos()
                    if (level.getBlockState(toCheck).getBlock() == this.woolBlock) {
                        // BrassAmberBattleTowers.LOGGER.info(toCheck + " " + this.level.getBlockState(toCheck));
                        spawnersSet = this.setSpawnerBlock(toCheck, this.checkLayer, level, spawnersSet);
                    }
                    this.checkPos(toCheck, level);
                    this.extraCheck(toCheck, level);
                }
            }
        }

        // In case a chest has previously been removed (tower partially completed) fill empty spot with null value
        // Ensures that Index of chest in chest-list matches the floor its on
        // I.E. chest for floor 6 is ID 5 (-1 for list index) even when the chests on floors 3 and 4 are missing
        if (this.CHESTS.size() != this.checkLayer) {
            this.CHESTS.add(null);
        }

        if (this.checkLayer == 8) {
            this.initialized = true;
            this.chestsFound = true;
        }
        else {
            this.checkLayer += 1;
            if (this.checkLayer % 2 == 0 && this.checkLayer < 8) {
                this.floorData = this.perFloorData.get(this.checkLayer / 2);
            }
            this.currentFloorY = nextFloorY;
        }

    }

    protected int setSpawnerBlock(BlockPos pos, int floor, Level level, int spawnersSet) {
        if (spawnersSet < this.spawnerAmounts.get(floor-1)) {
            level.setBlock(pos, this.spawnerBlock.defaultBlockState(), 2);
            // BrassAmberBattleTowers.LOGGER.info("Set Spawner at: " +pos + " floor: " + floor + " Spawners: " + spawnersSet);
            return spawnersSet + 1;
        } else {
            level.setBlock(pos, this.spawnerFillBlock.defaultBlockState(), 2);
            return spawnersSet;
        }
    }

    public void checkPos(BlockPos toCheck, Level level) {
        try {
            Block block = level.getBlockState(toCheck).getBlock();
            if (block == this.chestBlock) {
                this.CHESTS.add(toCheck);
                // BrassAmberBattleTowers.LOGGER.info("Found chest");
            } else if (block == this.spawnerBlock || block == Blocks.SPAWNER) {
                this.SPAWNERS.get(this.checkLayer-1).add(toCheck);
                BlockEntity entity = this.level.getBlockEntity(toCheck);
                if (entity instanceof BTAbstractSpawnerBlockEntity btspawnerEntity) {
                    btspawnerEntity.getSpawner().setBtSpawnData(
                            this.towerMobs.get(this.random.nextInt(this.towerMobs.size())),
                            this.floorData.get(0), this.floorData.get(1), this.floorData.get(2),
                            this.floorData.get(3), this.floorData.get(4), this.floorData.get(5)
                    );
                }
                // BrassAmberBattleTowers.LOGGER.info("Found spawner: " + this.checkLayer + " " + this.spawnersFound);
                // BrassAmberBattleTowers.LOGGER.info(this.SPAWNERS.get(this.checkLayer-1).size());
            } else if (block == this.golemChestBlock) {
                this.golemChest = (GolemChestBlockEntity) level.getBlockEntity(toCheck);
                // BrassAmberBattleTowers.LOGGER.info("Found Golem Chest");
            }
        } catch (Exception e) {
            BrassAmberBattleTowers.LOGGER.info("Exception in Obelisk class, not a chest or spawner: " + level.getBlockState(toCheck).getBlock());
            e.printStackTrace();

        }
    }


    public void extraCheck(BlockPos toUpdate, Level level) {}

    @Override
    public void tick() {
        super.tick();

        if (this.level.isClientSide()) {
            ClientLevel client = (ClientLevel)this.level;
            if (!this.clientInitialized) {
                Minecraft mc = Minecraft.getInstance();
                this.music = mc.getMusicManager();
                this.clientInitialized = true;
            }
            if (client.players().size() == 0) {
                return;
            }

            // Make sure we have a player within range.
            boolean hasClientPlayer = client.hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), 100D);
            boolean playerInTowerRange;
            boolean playerInMusicRange;

            if (hasClientPlayer) {
                //noinspection ConstantConditions
                playerInTowerRange = BTUtil.distanceTo2D(this, client.getNearestPlayer(this, 100D)) <= this.towerRange;
                //noinspection ConstantConditions
                playerInMusicRange = BTUtil.distanceTo2D(this, client.getNearestPlayer(this, 100D)) < this.musicDistance;
            } else {
                playerInTowerRange = false;
                playerInMusicRange = false;
            }

            boolean tryPlayMusic = false;

            if (this.BOSS_MUSIC == null) {
                tryPlayMusic = playerInTowerRange;
            } else {
                // BrassAmberBattleTowers.LOGGER.info("Player: " + true + "  In Music Range: " + playerInMusicRange + " Tower music playing?: " + this.musicPlaying);
                if (!this.music.isPlayingMusic(this.BOSS_MUSIC)) {
                    tryPlayMusic = playerInTowerRange;
                }

            }

            if (tryPlayMusic) {
                if (playerInMusicRange && !this.music.isPlayingMusic(this.TOWER_MUSIC)) {
                    this.music.stopPlaying();
                    this.music.nextSongDelay = this.TOWER_MUSIC.getMinDelay();
                    this.music.startPlaying(this.TOWER_MUSIC);
                    this.musicPlaying = true;
                }
            } else {
                if (this.musicPlaying) {
                    this.music.nextSongDelay = 500;
                    this.music.stopPlaying();
                    this.musicPlaying = false;
                }
            }
            return;
        }

        if (!this.initialized) {
            // BrassAmberBattleTowers.LOGGER.info("Finding Chests for layer: " + this.checkLayer + "  At block level: " + this.currentFloorY);
            this.initialize();
            return;
        }

        if (this.doCheck) {
            try {
                List<?> list = this.level.getEntitiesOfClass(BTMonolith.class, this.getBoundingBox().inflate(15, 110, 15));
                this.canCheck = list.size() != 0;
                if (!this.canCheck) {
                    try {
                        List<?> list2 = this.level.getEntitiesOfClass(BTAbstractGolem.class, this.getBoundingBox().inflate(15, 110, 15));
                        this.canCheck = list2.size() != 0;
                        if (!this.golemSpawned) {
                            this.golemSpawned = true;
                        }
                    } catch (Exception f) {
                        BrassAmberBattleTowers.LOGGER.error("Exception finding Golem: " + f);
                    }
                }
            } catch (Exception e) {
                BrassAmberBattleTowers.LOGGER.error("Exception finding Monolith: " + e);
            }
        }


        if (this.canCheck) {
            List<ServerPlayer> players = Objects.requireNonNull(this.level.getServer()).getPlayerList().getPlayers();
            List<Boolean> playersClose = new ArrayList<>();
            for (ServerPlayer player : players
            ) {
                if (distanceTo2D(this, player) < this.towerRange) {
                    playersClose.add(Boolean.TRUE);
                    // BrassAmberBattleTowers.LOGGER.info("Player " +  distanceTo2D(this, player) + " blocks away");
                } else {
                    playersClose.add(Boolean.FALSE);
                }
            }

            this.hasPlayer = Collections.frequency(playersClose, Boolean.TRUE) > 0;

            int timeCheck = (this.random.nextInt(2) + 4) * 10;

            if (this.tickCount % timeCheck == 0) {
                List<? extends Entity> specialEnemies = this.level.getEntitiesOfClass(this.specialEnemy, this.getBoundingBox().inflate(15, 110, 15));
                if (specialEnemies.size() < 10) {
                    int floor = this.blockPosition().getY() + this.random.nextInt(8) * 11;
                    int x = this.blockPosition().getX() + this.random.nextInt(24) - 12;
                    int y = floor + this.random.nextInt(9);
                    int z = this.blockPosition().getZ() + this.random.nextInt(24) - 12;

                    ServerLevel serverWorld = (ServerLevel) this.level;

                    switch (this.golemType) {
                        case LAND -> this.spawnSpecialEnemy(serverWorld, new BlockPos(x, y, z),
                                0D, 11.5D, true);
                        case OCEAN -> this.spawnSpecialEnemy(serverWorld, new BlockPos(x, y, z),
                                12.5D, 17.5D, false);
                        case CORE -> this.spawnSpecialEnemy(serverWorld, new BlockPos(x, y, z),
                                12.5D, 17.5D, true);
                    }
                }
            }

            if (this.tickCount % 20 == 0 && this.hasPlayer) {
                // BrassAmberBattleTowers.LOGGER.info("Checking Spawners");
                this.checkSpawners(this.level);
                // BrassAmberBattleTowers.LOGGER.info(this.towerEffect + " effect ");
            }
        }
    }

    protected void spawnSpecialEnemy(ServerLevel serverWorld, BlockPos spawn, double lowerRadiusBound,
                                     double upperRadiusBound, boolean checkOnGround) {
        // BrassAmberBattleTowers.LOGGER.info("Trying to spawn cultist at: " + spawn);
        double distance = BTUtil.distanceTo2D(this, spawn.getX(), spawn.getZ());
        boolean canSpawn = SpawnPlacements.checkSpawnRules(GolemType.getSpecialEnemyType(this.golemType), serverWorld, MobSpawnType.STRUCTURE, spawn, this.random);
        boolean acceptableDistance = lowerRadiusBound < distance && distance < upperRadiusBound;
        boolean onGround;
        if (checkOnGround) {
            onGround = !serverWorld.getBlockState(spawn.below()).isAir() && serverWorld.getBlockState(spawn).isAir();
        } else {
            onGround = true;
        }

        if (canSpawn && acceptableDistance && onGround && serverWorld.getBlockState(spawn.above()).isAir()) {
            Entity entity = GolemType.getSpecialEnemy(this.golemType, serverWorld);
            if (entity instanceof Mob mob) {
                mob.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
                mob.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.TRIGGERED, null, null);
                serverWorld.addFreshEntity(entity);
                // BrassAmberBattleTowers.LOGGER.info("Success");
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void checkSpawners(Level level) {
        // Make sure there are chests && spawners in the tower (tower has not been cleared)
        BlockPos chestPos;
        if (this.SPAWNERS.size() == 0 || this.CHESTS.size() == 0) {
            this.doCheck = false;
            this.canCheck = false;
        } else {
            // Main loop to iterate over each 'floor' contained in the spawners list
            for (int i = 0; i < this.SPAWNERS.size(); i++) {
                if (this.SPAWNERS.get(i).size() == 0) {
                    // If no spawners left on the floor unlock the chest.
                    chestPos = this.CHESTS.get(i);
                    if (chestPos != null && level.getBlockEntity(chestPos) instanceof TowerChestBlockEntity chest) {
                        if (!chest.isUnlocked()) {
                            chest.setUnlocked(true);
                            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(chestPos)).withOptionalRandomSeed(this.random.nextLong());
                            assert chest != null: "BTObelisk: Not a BTChest";
                            BTUtil.btFill(getLootTable(GolemType.getNumForType(this.golemType), i), chest, lootcontext$builder.create(LootContextParamSets.CHEST));
                            this.chestUnlockingSound(level);
                            this.CHESTS.set(i, null);
                        }
                    }
                } else {
                    List<BlockPos> poss = this.SPAWNERS.get(i);
                    //noinspection ForLoopReplaceableByForEach
                    for (int x = 0; x < poss.size(); x++) {
                        BlockPos blockPos = poss.get(x);
                        if (!(level.getBlockState(blockPos).getBlock() instanceof BTSpawnerBlock)) {
                            this.SPAWNERS.get(i).remove(blockPos);
                            this.setSpawnersDestroyed(this.getSpawnersDestroyed() + 1);
                            BrassAmberBattleTowers.LOGGER.info("Spawners Destroyed: " + this.getSpawnersDestroyed());

                            if (this.justSpawnedKey) {
                                this.justSpawnedKey = false;
                            }
                        }
                    }
                    if (this.keySpawnerAmounts.contains(this.getSpawnersDestroyed()) && !justSpawnedKey) {
                        if (this.CHESTS.get(i) != null && level.getBlockEntity(this.CHESTS.get(i)) instanceof TowerChestBlockEntity chest) {
                            // chest.setLootTable(BrassAmberBattleTowers.locate("chests/" + GolemType.getNameForNum(this.getTower())+ "_tower/" + (i+1) + "key"), this.random.nextLong());
                            chest.setItem(13, GolemType.getKeyFor(this.golemType).getDefaultInstance());
                        }
                        else if (this.CHESTS.get(i) != null) {
                            doNoOutputPostionedCommand(this, "give @p ba_bt:" + GolemType.getKeyFor(this.golemType).getRegistryName(), new Vec3(this.blockPosition().getX(), this.blockPosition().getY() + (11 * i), this.blockPosition().getZ()));
                            this.CHESTS.set(i, null);
                        }
                        this.justSpawnedKey = true;
                    }
                }
            }
        }

    }

    private void chestUnlockingSound(Level level) {
        List<ServerPlayer> players = Objects.requireNonNull(level.getServer()).getPlayerList().getPlayers();
        for (ServerPlayer player: players) {
            if (BTUtil.distanceTo2D(this, player) < 30) {
                level.playSound(null, player.blockPosition(), SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1f, 1.5f);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPAWNERS_DESTROYED, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (!this.level.isClientSide()) {
            BrassAmberBattleTowers.LOGGER.info("Reading obelisk data");
            // BrassAmberBattleTowers.LOGGER.info("Reading obelisk data " + tag);
            this.golemType = GolemType.getTypeForName(tag.getString(towerName));
            this.setSpawnersDestroyed(tag.getInt(spawnersDestroyedName));
            this.chestsFound = tag.getBoolean(chestsFoundName);

            BlockPos checkPos;

            this.spawnerAmounts = towerSpawnerAmounts.get(GolemType.getNumForType(this.golemType));
            this.SPAWNERS = Arrays.asList(new ArrayList<>(this.spawnerAmounts.get(0)), new ArrayList<>(this.spawnerAmounts.get(1)),
                    new ArrayList<>(this.spawnerAmounts.get(2)), new ArrayList<>(this.spawnerAmounts.get(3)),
                    new ArrayList<>(this.spawnerAmounts.get(4)), new ArrayList<>(this.spawnerAmounts.get(5)),
                    new ArrayList<>(this.spawnerAmounts.get(6)), new ArrayList<>(this.spawnerAmounts.get(7)));


            ListTag spawners = tag.getList(this.spawnersName, 9);
            ListTag spawnerFloor = spawners.getList(0);
            ListTag spawnerXTag = spawners.getList(1);
            ListTag spawnerYTag = spawners.getList(2);
            ListTag spawnerZTag = spawners.getList(3);

            // BrassAmberBattleTowers.LOGGER.info("spawner floors: " + spawnerFloor);

            int floor;
            for (int i = 0; i < spawnerFloor.size(); i++) {
                floor = (int) spawnerFloor.getDouble(i);
                checkPos = new BlockPos(spawnerXTag.getDouble(i), spawnerYTag.getDouble(i), spawnerZTag.getDouble(i));
                if (checkPos.getY() != 0) {
                    this.SPAWNERS.get(floor).add(checkPos);
                }
            }
            // BrassAmberBattleTowers.LOGGER.info("Spawners: " + this.SPAWNERS);


            if (this.chestsFound) {
                ListTag chests = tag.getList(chestsName, 9);
                ListTag chestXTag = chests.getList(0);
                ListTag chestYTag = chests.getList(1);
                ListTag chestZTag = chests.getList(2);
                for (int i = 0; i < chestXTag.size(); i++) {
                    checkPos = new BlockPos(chestXTag.getDouble(i), chestYTag.getDouble(i), chestZTag.getDouble(i));
                    if (checkPos.getY() == 0) {
                        this.CHESTS.add(null);
                    } else {
                        this.CHESTS.add(checkPos);
                    }
                }
                // BrassAmberBattleTowers.LOGGER.info("Chests: " + chests);
                // BrassAmberBattleTowers.LOGGER.info("Chests: " + this.CHESTS);


            }

            if (this.chestsFound && this.SPAWNERS.get(7).isEmpty()) {
                BrassAmberBattleTowers.LOGGER.info("No Spawners in data: Needs to be initialized");
                this.doServerInit = true;
                this.chestsFound = false;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        if (this.level.isClientSide()) {
            ((ClientLevel) this.level).minecraft.getMusicManager().stopPlaying();
        } else {
            BrassAmberBattleTowers.LOGGER.info("Setting obelisk data");
            tag.putString(towerName, this.golemType.getSerializedName());
            tag.putInt(spawnersDestroyedName, this.getSpawnersDestroyed());
            tag.putBoolean(chestsFoundName, this.chestsFound);

            double[] chestX = new double[this.CHESTS.size()];
            double[] chestY = new double[this.CHESTS.size()];
            double[] chestZ = new double[this.CHESTS.size()];
            int i = 0;
            for (BlockPos chestPos: this.CHESTS) {
                if (chestPos == null) {
                    chestX[i] = 0;
                    chestY[i] = 0;
                    chestZ[i] = 0;
                } else {
                    chestX[i] = chestPos.getX();
                    chestY[i] = chestPos.getY();
                    chestZ[i] = chestPos.getZ();
                }
                i++;
            }

            ListTag chestXTag = this.newDoubleList(chestX);
            ListTag chestYTag = this.newDoubleList(chestY);
            ListTag chestZTag = this.newDoubleList(chestZ);

            ListTag chests = new ListTag();
            chests.add(0, chestXTag);
            chests.add(1, chestYTag);
            chests.add(2, chestZTag);
            tag.put(chestsName, chests);

            int spawnerSize = towerChestUnlocking.get(GolemType.getNumForType(this.golemType)).size() -1;
            int chestNum = towerChestUnlocking.get(GolemType.getNumForType(this.golemType)).get(spawnerSize);

            double[] spawnerFloor = new double[chestNum];
            double[] spawnerX = new double[chestNum];
            double[] spawnerY = new double[chestNum];
            double[] spawnerZ = new double[chestNum];

            i = 0;
            int f = 0;
            for (List<BlockPos> posList: this.SPAWNERS) {
                for (BlockPos spawnerPos: posList) {
                    spawnerFloor[f] = i;
                    spawnerX[f] = spawnerPos.getX();
                    spawnerY[f] = spawnerPos.getY();
                    spawnerZ[f] = spawnerPos.getZ();
                    f++;
                }
                i++;
            }

            ListTag spawnerFloorTag = this.newDoubleList(spawnerFloor);
            ListTag spawnerXTag = this.newDoubleList(spawnerX);
            ListTag spawnerYTag = this.newDoubleList(spawnerY);
            ListTag spawnerZTag = this.newDoubleList(spawnerZ);

            ListTag Spawners = new ListTag();
            Spawners.addTag(0, spawnerFloorTag);
            Spawners.addTag(1, spawnerXTag);
            Spawners.addTag(2, spawnerYTag);
            Spawners.addTag(3, spawnerZTag);
            tag.put(spawnersName, Spawners);
            // BrassAmberBattleTowers.LOGGER.info("Tag: " + tag.get(spawnersName));
            // BrassAmberBattleTowers.LOGGER.info("Tag: " + tag);

        }


    }
    /*************************************** Characteristics & Properties *******************************************/

    /**
     * Called when a user uses the creative pick block button on this entity.
     * @return An ItemStack to add to the player's inventory, empty ItemStack if nothing should be added.
     * (Empty ItemStack is an ItemStack of '(Item) null')
     */
    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack((Item) null);
    }


    /**
     * {@link PushReaction.BLOCK} is the valid option for an entity to stop piston interaction
     *
     * Used in: {@link PistonTileEntity.moveCollidedEntities method}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     * (Like arrows and stuff.)
     */
    @Override
    public boolean isPickable() {
        return this.isAlive();
    }

    /**
     * Block movement through this entity
     */
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    /***************************************************** Breaking *************************************************/

    /**
     * Called by the /kill command.
     */
    @Override
    public void kill() {
        Player player = this.level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 50, EntitySelector.NO_SPECTATORS);

        if (player != null && player.isCreative()) {
            BrassAmberBattleTowers.LOGGER.info("Item: " + player.getItemInHand(InteractionHand.MAIN_HAND).getItem());
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.CLAY_BALL) {
                this.remove(RemovalReason.KILLED);
            } else {
                // Do nothing to prevent people deleting a Monolith by accident.
                BrassAmberBattleTowers.LOGGER.info("Used the /kill command. However, an Obelisk has been saved at: " + Math.round(this.getX()) + "X " + Math.round(this.getY()) + "Y " + Math.round(this.getZ()) + "Z.");
            }
        }
        else {
            // Do nothing to prevent people deleting a Monolith by accident.
            BrassAmberBattleTowers.LOGGER.info("Used the /kill command. However, an Obelisk has been saved at: " + Math.round(this.getX()) + "X " + Math.round(this.getY()) + "Y " + Math.round(this.getZ()) + "Z.");
        }

    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!(source.getMsgId().equals("player"))) {
            return false;
        } else {
            if (this.isAlive() && !this.level.isClientSide() && source.isCreativePlayer()) {
                Player player = (Player) source.getEntity();
                if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.CLAY_BALL) {
                    this.remove(RemovalReason.KILLED);
                }
            }
            return true;
        }
    }

    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getItem() instanceof ResonanceStoneItem stoneItem) {
            stoneItem.addEnchantment(itemInHand);
            return InteractionResult.SUCCESS;
        } else {
            return super.interact(player, hand);
        }
    }

    @Override
    public MobCategory getClassification(boolean forSpawnCount) {
        return MobCategory.AMBIENT;
    }

    /************************************************** DATA SET/GET **************************************************/

    public void setSpawnersDestroyed(int num) {
        this.entityData.set(SPAWNERS_DESTROYED, num);
    }

    public int getSpawnersDestroyed() {
        return this.entityData.get(SPAWNERS_DESTROYED);
    }

    /************************************************** COMMANDS **************************************************/



    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
