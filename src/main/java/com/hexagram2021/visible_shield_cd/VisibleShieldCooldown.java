package com.hexagram2021.visible_shield_cd;

import com.hexagram2021.visible_shield_cd.network.VSCPackets;
import net.fabricmc.api.ModInitializer;

public class VisibleShieldCooldown implements ModInitializer {
	public static final String MODID = "visible_shield_cd";

	@Override
	public void onInitialize() {
		VSCPackets.init();
	}
}