package com.BrassAmber.ba_bt.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BTEvents {

    @SubscribeEvent
    public static void onDeath(PlayerEvent.Clone event) {
        Player player = event.getPlayer();
        if (player.level.isClientSide()) {
            MusicManager musicManager = ((ClientLevel) player.level).minecraft.getMusicManager();
            if (event.isWasDeath()) {
                musicManager.stopPlaying();
            }
        }
    }

}