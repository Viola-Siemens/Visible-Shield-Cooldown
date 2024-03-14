package com.hexagram2021.visible_shield_cd.client.compat;

import com.hexagram2021.visible_shield_cd.client.config.VisibleShieldCooldownConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class VSCModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return VisibleShieldCooldownConfig::makeScreen;
	}
}
