package com.hexagram2021.visible_shield_cd.mixin;

import com.hexagram2021.visible_shield_cd.network.VSCPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
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
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUUID(this.player.getUUID());
		buf.writeId(BuiltInRegistries.ITEM, item);
		buf.writeInt(time);
		this.player.server.getPlayerList().getPlayers().forEach(player -> {
			if(!player.equals(this.player)) {
				ServerPlayNetworking.send(player, VSCPackets.VSC_OTHER_PLAYER_COOLDOWN, buf);
			}
		});
	}
	@Inject(method = {"onCooldownEnded"}, at = @At(value = "TAIL"))
	private void visible_shield_cd$sendEndToOtherPlayers(Item item, CallbackInfo ci) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUUID(this.player.getUUID());
		buf.writeId(BuiltInRegistries.ITEM, item);
		buf.writeInt(0);
		this.player.server.getPlayerList().getPlayers().forEach(player -> {
			if(!player.equals(this.player)) {
				ServerPlayNetworking.send(player, VSCPackets.VSC_OTHER_PLAYER_COOLDOWN, buf);
			}
		});
	}
}
