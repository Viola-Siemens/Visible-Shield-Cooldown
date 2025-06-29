package com.hexagram2021.visible_shield_cd.network.payload;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.UUID;

import static com.hexagram2021.visible_shield_cd.VisibleShieldCooldown.MODID;

public record UpdatePlayerCooldownPayload(UUID uuid, Item item, int time) implements CustomPacketPayload {
	public static final ResourceLocation VSC_OTHER_PLAYER_COOLDOWN = new ResourceLocation(MODID, "update_cd");

	public static final StreamCodec<FriendlyByteBuf, UpdatePlayerCooldownPayload> STREAM_CODEC = CustomPacketPayload.codec(UpdatePlayerCooldownPayload::write, UpdatePlayerCooldownPayload::new);
	public static final Type<UpdatePlayerCooldownPayload> TYPE = CustomPacketPayload.createType(VSC_OTHER_PLAYER_COOLDOWN.toString());

	private UpdatePlayerCooldownPayload(FriendlyByteBuf buf) {
		this(buf.readUUID(), BuiltInRegistries.ITEM.get(buf.readResourceLocation()), buf.readVarInt());
	}

	private void write(FriendlyByteBuf buf) {
		buf.writeUUID(this.uuid);
		buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(this.item));
		buf.writeVarInt(this.time);
	}

	@Override
	public Type<UpdatePlayerCooldownPayload> type() {
		return TYPE;
	}
}
