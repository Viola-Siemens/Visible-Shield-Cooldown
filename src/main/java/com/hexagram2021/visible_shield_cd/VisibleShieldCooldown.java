package com.hexagram2021.visible_shield_cd;

import com.hexagram2021.visible_shield_cd.network.VSCPackets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class VisibleShieldCooldown implements ModInitializer {
	public static final String MODID = "visible_shield_cd";

	@Override
	public void onInitialize() {
		VSCPackets.init();
	}

	public static void addCoolDownToPlayer(Player player, Item item, int time) {
		player.getCooldowns().addCooldown(item, time);
	}
}