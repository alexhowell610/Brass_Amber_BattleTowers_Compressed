package com.BrassAmber.ba_bt.block.block;

import com.BrassAmber.ba_bt.BrassAmberBattleTowers;
import com.BrassAmber.ba_bt.block.BTTileEntityTypes;

import com.BrassAmber.ba_bt.block.tileentity.GolemChestTileEntity;
import com.BrassAmber.ba_bt.block.tileentity.StoneChestTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;

import java.util.Random;

public class StoneChestBlock extends GolemChestBlock {


	public StoneChestBlock(BTChestType chestType, Properties properties) {
		super(chestType, properties, () -> {
			return BTTileEntityTypes.STONE_CHEST;
		});

	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return BTTileEntityTypes.STONE_CHEST.create();
	}

}