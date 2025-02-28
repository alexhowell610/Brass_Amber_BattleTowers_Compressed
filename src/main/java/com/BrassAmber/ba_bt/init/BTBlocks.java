package com.BrassAmber.ba_bt.init;

import com.BrassAmber.ba_bt.BrassAmberBattleTowers;
import com.BrassAmber.ba_bt.block.block.*;
import com.BrassAmber.ba_bt.block.block.GolemChestBlock.BTChestType;
import com.BrassAmber.ba_bt.block.block.TowerChestBlock;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class BTBlocks {
	private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
		return false;
	}

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BrassAmberBattleTowers.MOD_ID);

	public static final RegistryObject<Block> LAND_GOLEM_CHEST = BLOCKS.register("land_golem_chest",
			() -> new GolemChestBlock(
					BTChestType.GOLEM, BTBlockEntityTypes.LAND_GOLEM_CHEST::get,
					Block.Properties.of(Material.STONE).strength(2.5F).sound(SoundType.STONE).noOcclusion().explosionResistance(1200.0F))
	);
	public static final RegistryObject<Block> LAND_CHEST = BLOCKS.register("land_chest",
			() -> new TowerChestBlock(
					BTChestType.TOWER, BTBlockEntityTypes.LAND_CHEST::get,
					Block.Properties.of(Material.STONE).strength(2.5F, 1200.0F).sound(SoundType.STONE).noOcclusion().explosionResistance(6.0F))
	);

	public static final RegistryObject<Block> OCEAN_GOLEM_CHEST = BLOCKS.register("ocean_golem_chest",
			() -> new GolemChestBlock(
					BTChestType.GOLEM, BTBlockEntityTypes.OCEAN_GOLEM_CHEST::get,
					Block.Properties.of(Material.STONE).strength(2.5F).sound(SoundType.STONE).noOcclusion().explosionResistance(1200.0F))
	);
	public static final RegistryObject<Block> OCEAN_CHEST = BLOCKS.register("ocean_chest",
			() -> new TowerChestBlock(
					BTChestType.TOWER, BTBlockEntityTypes.OCEAN_CHEST::get,
					Block.Properties.of(Material.STONE).strength(2.5F, 1200.0F).sound(SoundType.STONE).noOcclusion().explosionResistance(6.0F))
	);

	public static final RegistryObject<Block> PLATINUM_BLOCK = BLOCKS.register("platinum_block",
			() -> new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).requiresCorrectToolForDrops()
					.strength(4.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> PLATINUM_TILES = BLOCKS.register("platinum_tiles",
			() -> new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).requiresCorrectToolForDrops()
					.strength(4.0F, 6.0F).sound(SoundType.METAL)));

	public static final RegistryObject<Block> TAB_ICON = BLOCKS.register("tab_icon",
			() -> new TabIconBlock(Block.Properties.of(Material.STONE).strength(-1.0F, 3600000.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> BT_LAND_SPAWNER = BLOCKS.register("bt_land_spawner",
			() -> new BTSpawnerBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));

	public static final RegistryObject<Block> BT_OCEAN_SPAWNER = BLOCKS.register("bt_ocean_spawner",
			() -> new BTSpawnerBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));

	public static final RegistryObject<Block> BT_CORE_SPAWNER = BLOCKS.register("bt_core_spawner",
			() -> new BTSpawnerBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));

	public static final RegistryObject<Block> BT_NETHER_SPAWNER = BLOCKS.register("bt_nether_spawner",
			() -> new BTSpawnerBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));

	public static final RegistryObject<Block> BT_END_SPAWNER = BLOCKS.register("bt_end_spawner",
			() -> new BTSpawnerBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));

	public static final RegistryObject<Block> BT_SKY_SPAWNER = BLOCKS.register("bt_sky_spawner",
			() -> new BTSpawnerBlock(Block.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion()));

	public static final RegistryObject<Block> BT_AIR_FILL = BLOCKS.register("bt_air_fill",
			() -> new BTBlockingAirBlock(BlockBehaviour.Properties.of(Material.STRUCTURAL_AIR).noCollission().noOcclusion().noDrops().air()));









}
