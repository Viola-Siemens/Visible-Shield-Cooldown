package com.hexagram2021.visible_shield_cd.client;

import com.hexagram2021.visible_shield_cd.network.VSCPackets;
import net.fabricmc.api.ClientModInitializer;

public class VisibleShieldCooldownClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VSCPackets.initClient();
	}
}
