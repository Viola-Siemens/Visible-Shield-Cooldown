package com.hexagram2021.visible_shield_cd.mixin;

import com.hexagram2021.visible_shield_cd.network.payload.UpdatePlayerCooldownPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerItemCooldowns.class)
public class ServerItemCooldownsMixin {
	@Shadow @Final
	private ServerPlayer player;

	@Inject(method = {"onCooldownStarted"}, at = @At(value = "TAIL"))
	private void visible_shield_cd$sendStartToOtherPlayers(Item item, int time, CallbackInfo ci) {
		this.player.server.getPlayerList().getPlayers().forEach(player -> {
			if(!player.equals(this.player)) {
				ServerPlayNetworking.send(player, new UpdatePlayerCooldownPayload(this.player.getUUID(), item, time));
			}
		});
	}
	@Inject(method = {"onCooldownEnded"}, at = @At(value = "TAIL"))
	private void visible_shield_cd$sendEndToOtherPlayers(Item item, CallbackInfo ci) {
		this.player.server.getPlayerList().getPlayers().forEach(player -> {
			if(!player.equals(this.player)) {
				ServerPlayNetworking.send(player, new UpdatePlayerCooldownPayload(this.player.getUUID(), item, 0));
			}
		});
	}
}
