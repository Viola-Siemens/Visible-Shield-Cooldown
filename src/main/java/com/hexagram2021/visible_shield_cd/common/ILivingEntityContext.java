package com.hexagram2021.visible_shield_cd.common;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface ILivingEntityContext {
	void visible_shield_cooldown$setLivingEntity(@Nullable LivingEntity living);
	@Nullable
	LivingEntity visible_shield_cooldown$getLivingEntity();
}
