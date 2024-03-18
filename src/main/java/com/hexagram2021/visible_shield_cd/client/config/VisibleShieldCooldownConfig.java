package com.hexagram2021.visible_shield_cd.client.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hexagram2021.visible_shield_cd.client.VisibleShieldCooldownClient;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.hexagram2021.visible_shield_cd.VisibleShieldCooldown.MODID;

public final class VisibleShieldCooldownConfig {
	public enum RenderMode implements StringRepresentable {
		DISABLE(
				remain -> 1.0F,
				remain -> 1.0F,
				remain -> 1.0F,
				remain -> 1.0F
		),
		LINEAR(
				remain -> 1.0F,
				remain -> 1.0F - remain * (1.0F - remain * remain),
				remain -> remain * (1.0F + remain * (1.0F - remain)),
				remain -> remain * remain
		),
		DISPERSED(
				remain -> 1.0F,
				remain -> remain < 0.25F ? 1.0F : remain < 0.75F ? 0.0F : 0.125F,
				remain -> remain >= 0.75F ? 1.0F : remain >= 0.25F ? 0.5F : 0.0F,
				remain -> remain >= 0.25F ? 0.25F : 0.0F
		),
		SMOOTH_DISPERSED(
				remain -> 1.0F,
				remain -> (float)Mth.clamp(1.0D - factSmooth(remain, 10, 0.25F) - factSmooth(1.0F - remain, 10, 0.25F) * 0.125D, 0.0D, 1.0D),
				remain -> (float)(factSmooth(remain, 10, 0.25F) + factSmooth(remain, 10, 0.75F)) * 0.5F,
				remain -> (float)factSmooth(remain, 8, 0.25F) * 0.25F
		),
		CUSTOM(
				remain -> VisibleShieldCooldownClient.getConfig().getCustomA(remain) / 255.0F,
				remain -> VisibleShieldCooldownClient.getConfig().getCustomR(remain) / 255.0F,
				remain -> VisibleShieldCooldownClient.getConfig().getCustomG(remain) / 255.0F,
				remain -> VisibleShieldCooldownClient.getConfig().getCustomB(remain) / 255.0F
		);

		private static final Map<String, RenderMode> BY_NAME;

		private final FloatUnaryOperator getA;
		private final FloatUnaryOperator getR;
		private final FloatUnaryOperator getG;
		private final FloatUnaryOperator getB;

		RenderMode(FloatUnaryOperator getA, FloatUnaryOperator getR, FloatUnaryOperator getG, FloatUnaryOperator getB) {
			this.getA = getA;
			this.getR = getR;
			this.getG = getG;
			this.getB = getB;
		}

		public float a(float x) {
			return this.getA.apply(x);
		}
		public float r(float x) {
			return this.getR.apply(x);
		}
		public float g(float x) {
			return this.getG.apply(x);
		}
		public float b(float x) {
			return this.getB.apply(x);
		}

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}

		public static RenderMode byName(String name) {
			return BY_NAME.getOrDefault(name, SMOOTH_DISPERSED);
		}

		static {
			BY_NAME = Maps.newHashMap();
			for(RenderMode mode: RenderMode.values()) {
				BY_NAME.put(mode.getSerializedName(), mode);
			}
		}

		static double factSmooth(double x, double k, double s) {
			double dp = Math.pow(x, k);
			double dn = Math.pow(1.0D - x, k);
			double as = Math.pow(s / (1.0D - s), k - 1.0D);
			return dp / (dp + as * dn);
		}
	}

	@SerialEntry
	private String renderMode = RenderMode.SMOOTH_DISPERSED.getSerializedName();

	@SerialEntry
	private List<Color> customColors = Lists.newArrayList(
			Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN
	);

	public static final ConfigClassHandler<VisibleShieldCooldownConfig> INSTANCE = ConfigClassHandler.createBuilder(VisibleShieldCooldownConfig.class)
			.id(new ResourceLocation(MODID, "client_config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("visible-shield-cooldown-client.json")).build())
			.build();

	public static Screen makeScreen(Screen parent) {
		return YetAnotherConfigLib.create(INSTANCE, (defaults, config, builder) -> builder
				.title(Component.translatable("config.visible_shield_cd.title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.visible_shield_cd.title"))
						.option(Option.<String>createBuilder()
								.name(Component.translatable("config.visible_shield_cd.option.renderMode"))
								.description(OptionDescription.of(Component.translatable("config.visible_shield_cd.option.renderMode.desc")))
								.binding(
										RenderMode.SMOOTH_DISPERSED.getSerializedName(),
										() -> config.renderMode,
										value -> config.renderMode = value
								)
								.controller(opt -> DropdownStringControllerBuilder.create(opt)
										.values(Arrays.stream(RenderMode.values()).map(RenderMode::getSerializedName).toList()))
								.build())
						.option(ListOption.<Color>createBuilder()
								.name(Component.translatable("config.visible_shield_cd.option.customColors"))
								.binding(
										List.of(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN),
										() -> config.customColors,
										value -> config.customColors = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.initial(Color.BLACK)
								.build())
						.build()))
				.generateScreen(parent);
	}

	public RenderMode getRenderMode() {
		return RenderMode.byName(this.renderMode);
	}

	float getCustomA(float remain) {
		int index = Mth.floor(remain * this.customColors.size());
		if(index >= this.customColors.size()) {
			return this.customColors.get(this.customColors.size() - 1).getAlpha();
		}
		return this.customColors.get(index).getAlpha();
	}
	int getCustomR(float remain) {
		int index = Mth.floor(remain * this.customColors.size());
		if(index >= this.customColors.size()) {
			return this.customColors.get(this.customColors.size() - 1).getRed();
		}
		return this.customColors.get(index).getRed();
	}
	int getCustomG(float remain) {
		int index = Mth.floor(remain * this.customColors.size());
		if(index >= this.customColors.size()) {
			return this.customColors.get(this.customColors.size() - 1).getGreen();
		}
		return this.customColors.get(index).getGreen();
	}
	int getCustomB(float remain) {
		int index = Mth.floor(remain * this.customColors.size());
		if(index >= this.customColors.size()) {
			return this.customColors.get(this.customColors.size() - 1).getBlue();
		}
		return this.customColors.get(index).getBlue();
	}
}
