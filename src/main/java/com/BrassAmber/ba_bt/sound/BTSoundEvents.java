package com.BrassAmber.ba_bt.sound;

import com.BrassAmber.ba_bt.BrassAmberBattleTowers;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BTSoundEvents {

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BrassAmberBattleTowers.MOD_ID);

	public static final SoundEvent ENTITY_GOLEM_HURT = registerSoundEvent("entity.golem.hurt");
	public static final SoundEvent ENTITY_GOLEM_DEATH = registerSoundEvent("entity.golem.death");
	public static final SoundEvent ENTITY_GOLEM_CHARGE = registerSoundEvent("entity.golem.charge");
	public static final SoundEvent ENTITY_GOLEM_AWAKEN = registerSoundEvent("entity.golem.awaken");
	public static final SoundEvent ENTITY_GOLEM_AMBIENT = registerSoundEvent("entity.golem.ambient");
	public static final SoundEvent ENTITY_GOLEM_SPECIAL = registerSoundEvent("entity.golem.special");

	public static final SoundEvent MUSIC_LAND_GOLEM_FIGHT = registerSoundEvent("entity.golem.land.fight");

	public static final SoundEvent MONOLITH_SPAWN_GOLEM = registerSoundEvent("monolith.spawn.golem");

	public static final SoundEvent TOWER_BREAK_START = registerSoundEvent("tower.break.start");
	public static final SoundEvent TOWER_BREAK_CRUMBLE = registerSoundEvent("tower.break.crumble");

	public static final SoundEvent MUSIC_LAND_TOWER = registerSoundEvent("tower.ambient.music.land");
	public static final SoundEvent MUSIC_OCEAN_TOWER = registerSoundEvent("tower.ambient.music.ocean");
	public static final SoundEvent MUSIC_CORE_TOWER = registerSoundEvent("tower.ambient.music.core");
	public static final SoundEvent MUSIC_NETHER_TOWER = registerSoundEvent("tower.ambient.music.nether");
	public static final SoundEvent MUSIC_END_TOWER = registerSoundEvent("tower.ambient.music.end");
	public static final SoundEvent MUSIC_SKY_TOWER = registerSoundEvent("tower.ambient.music.sky");
	public static final SoundEvent MUSIC_CITY = registerSoundEvent("tower.ambient.music.city");

	public static final Music LAND_TOWER_MUSIC = new Music(MUSIC_LAND_TOWER, 7000, 8000, false);
	public static final Music LAND_GOLEM_FIGHT_MUSIC = new Music(MUSIC_LAND_GOLEM_FIGHT, 5000, 6000, false);
	public static final Music OCEAN_TOWER_MUSIC = new Music(MUSIC_OCEAN_TOWER, 8220, 9220, false);
	// TODO: Set music lengths correctly
	public static final Music CORE_TOWER_MUSIC = new Music(MUSIC_CORE_TOWER, 8220, 9220, false);
	public static final Music NETHER_TOWER_MUSIC = new Music(MUSIC_NETHER_TOWER, 8220, 9220, false);
	public static final Music END_TOWER_MUSIC = new Music(MUSIC_END_TOWER, 8220, 9220, false);
	public static final Music SKY_TOWER_MUSIC = new Music(MUSIC_SKY_TOWER, 8220, 9220, false);
	

	/**
	 * Helper method for registering all SoundEvents
	 */
	private static SoundEvent registerSoundEvent(String registryName) {
		SoundEvent soundEvent = new SoundEvent(BrassAmberBattleTowers.locate(registryName));
		SOUND_EVENTS.register(registryName, () -> soundEvent);
		return soundEvent;
	}

}
