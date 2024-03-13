package com.hexagram2021.visible_shield_cd.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.UUID;

import static com.hexagram2021.visible_shield_cd.VisibleShieldCooldown.MODID;

public final class VSCPackets {
	public static final ResourceLocation VSC_OTHER_PLAYER_COOLDOWN = new ResourceLocation(MODID, "update_cd");

	public static void init() {

	}
	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(VSC_OTHER_PLAYER_COOLDOWN, (client, handler, buf, responseSender) -> {
			if(client.level != null) {
				UUID uuid = buf.readUUID();
				Item item = buf.readById(BuiltInRegistries.ITEM);
				int time = buf.readInt();
				Player player = client.level.getPlayerByUUID(uuid);
				if(item != null && player != null) {
					player.getCooldowns().addCooldown(item, time);
				}
			}
		});
	}

	private VSCPackets() {
	}
}
