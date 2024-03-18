package com.hexagram2021.visible_shield_cd.client;

import com.hexagram2021.visible_shield_cd.VisibleShieldCooldown;
import com.hexagram2021.visible_shield_cd.client.config.VisibleShieldCooldownConfig;
import com.hexagram2021.visible_shield_cd.network.VSCPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class VisibleShieldCooldownClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VSCPackets.initClient();

		AttackEntityCallback.EVENT.register((Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) -> {
			if(entity instanceof Player targetPlayer && targetPlayer.isBlocking() && player.canDisableShield()) {
				Vec3 damageDirection = player.position().vectorTo(targetPlayer.position());
				Vec3 view = targetPlayer.calculateViewVector(0.0f, targetPlayer.getYHeadRot());
				if(new Vec3(damageDirection.x, 0.0, damageDirection.z).dot(view) < 0) {
					targetPlayer.stopUsingItem();
					VisibleShieldCooldown.addCoolDownToPlayer(targetPlayer, Items.SHIELD, 100);
				}
			}

			return InteractionResult.PASS;
		});

		VisibleShieldCooldownConfig.INSTANCE.load();
	}

	public static VisibleShieldCooldownConfig getConfig() {
		return VisibleShieldCooldownConfig.INSTANCE.instance();
	}
}
