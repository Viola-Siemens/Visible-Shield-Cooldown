package com.hexagram2021.visible_shield_cd.client.config;

import com.google.common.collect.Maps;
import com.hexagram2021.visible_shield_cd.client.VisibleShieldCooldownClient;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
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

	private static final Color NONE = new Color(0, 0, 0, 0);

	@SerialEntry
	private int colorCount = 5;

	@SerialEntry
	private Color customColor0 = Color.RED;
	@SerialEntry
	private Color customColor1 = Color.ORANGE;
	@SerialEntry
	private Color customColor2 = Color.YELLOW;
	@SerialEntry
	private Color customColor3 = Color.GREEN;
	@SerialEntry
	private Color customColor4 = Color.CYAN;
	@SerialEntry
	private Color customColor5 = NONE;
	@SerialEntry
	private Color customColor6 = NONE;
	@SerialEntry
	private Color customColor7 = NONE;

	private final Color[] customColors = {
			this.customColor0, this.customColor1, this.customColor2, this.customColor3,
			this.customColor4, this.customColor5, this.customColor6, this.customColor7
	};

	public static final ConfigClassHandler<VisibleShieldCooldownConfig> INSTANCE = ConfigClassHandler.createBuilder(VisibleShieldCooldownConfig.class)
			.id(new ResourceLocation(MODID, "client_config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("visible-shield-cooldown-client.json")).build())
			.build();

	@SuppressWarnings("deprecation")
	public static Screen makeScreen(Screen parent) {
		return YetAnotherConfigLib.create(INSTANCE, (defaults, config, builder) -> builder
				.title(Component.translatable("config.visible_shield_cd.title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.visible_shield_cd.title"))
						.option(Option.createBuilder(String.class)
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
						.option(Option.createBuilder(int.class)
								.name(Component.translatable("config.visible_shield_cd.option.colorCount"))
								.binding(
										5,
										() -> config.colorCount,
										value -> config.colorCount = value
								)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 8).step(1))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 0))
								.binding(
										Color.RED,
										() -> config.customColors[0],
										value -> config.customColors[0] = config.customColor0 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 1))
								.binding(
										Color.ORANGE,
										() -> config.customColors[1],
										value -> config.customColors[1] = config.customColor1 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 2))
								.binding(
										Color.YELLOW,
										() -> config.customColors[2],
										value -> config.customColors[2] = config.customColor2 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 3))
								.binding(
										Color.GREEN,
										() -> config.customColors[3],
										value -> config.customColors[3] = config.customColor3 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 4))
								.binding(
										Color.CYAN,
										() -> config.customColors[4],
										value -> config.customColors[4] = config.customColor4 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 5))
								.binding(
										NONE,
										() -> config.customColors[5],
										value -> config.customColors[5] = config.customColor5 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 6))
								.binding(
										NONE,
										() -> config.customColors[6],
										value -> config.customColors[6] = config.customColor6 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.option(Option.createBuilder(Color.class)
								.name(Component.translatable("config.visible_shield_cd.option.customColors", 7))
								.binding(
										NONE,
										() -> config.customColors[7],
										value -> config.customColors[7] = config.customColor7 = value
								)
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build()
						)
						.build()))
				.generateScreen(parent);
	}

	public RenderMode getRenderMode() {
		return RenderMode.byName(this.renderMode);
	}

	float getCustomA(float remain) {
		for(int i = 1; i < this.colorCount; ++i) {
			float rate = remain * this.colorCount / i;
			if(rate < 1.0F) {
				return this.customColors[i - 1].getAlpha();
				//return Mth.lerp(rate, this.customColors[i - 1].getAlpha(), this.customColors[i].getAlpha());
			}
		}
		return this.customColors[this.colorCount - 1].getAlpha();
	}
	int getCustomR(float remain) {
		for(int i = 1; i < this.colorCount; ++i) {
			float rate = remain * this.colorCount / i;
			if(rate < 1.0F) {
				return this.customColors[i - 1].getRed();
				//return Mth.lerp(rate, this.customColors[i - 1].getRed(), this.customColors[i].getRed());
			}
		}
		return this.customColors[this.colorCount - 1].getRed();
	}
	int getCustomG(float remain) {
		for(int i = 1; i < this.colorCount; ++i) {
			float rate = remain * this.colorCount / i;
			if(rate < 1.0F) {
				return this.customColors[i - 1].getGreen();
				//return Mth.lerp(rate, this.customColors[i - 1].getGreen(), this.customColors[i].getGreen());
			}
		}
		return this.customColors[this.colorCount - 1].getGreen();
	}
	int getCustomB(float remain) {
		for(int i = 1; i < this.colorCount; ++i) {
			float rate = remain * this.colorCount / i;
			if(rate < 1.0F) {
				return this.customColors[i - 1].getBlue();
				//return Mth.lerp(rate, this.customColors[i - 1].getBlue(), this.customColors[i].getBlue());
			}
		}
		return this.customColors[this.colorCount - 1].getBlue();
	}
}
