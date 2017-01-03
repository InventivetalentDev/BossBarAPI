package org.inventivetalent.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class PacketBossBar implements BossBar {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> PacketPlayOutBoss       = nmsClassResolver.resolveSilent("PacketPlayOutBoss");
	static Class<?> PacketPlayOutBossAction = nmsClassResolver.resolveSilent("PacketPlayOutBoss$Action");
	static Class<?> ChatSerializer          = nmsClassResolver.resolveSilent("ChatSerializer", "IChatBaseComponent$ChatSerializer");
	static Class<?> BossBattleBarColor      = nmsClassResolver.resolveSilent("BossBattle$BarColor");
	static Class<?> BossBattleBarStyle      = nmsClassResolver.resolveSilent("BossBattle$BarStyle");

	static FieldResolver PacketPlayOutBossFieldResolver = new FieldResolver(PacketPlayOutBoss);

	static MethodResolver ChatSerializerMethodResolver = new MethodResolver(ChatSerializer);

	private final UUID uuid;
	private Collection<Player> receivers = new ArrayList<>();
	private float            progress;
	private String           message;
	private BossBarAPI.Color color;
	private BossBarAPI.Style style;
	private boolean          visible;

	private boolean darkenSky;
	private boolean playMusic;
	private boolean createFog;

	protected PacketBossBar(String message, BossBarAPI.Color color, BossBarAPI.Style style, float progress, BossBarAPI.Property... properties) {
		this.uuid = UUID.randomUUID();

		this.color = color != null ? color : BossBarAPI.Color.PURPLE;
		this.style = style != null ? style : BossBarAPI.Style.PROGRESS;
		setMessage(message);
		setProgress(progress);

		for (BossBarAPI.Property property : properties) {
			setProperty(property, true);
		}
	}

	protected PacketBossBar(BaseComponent message, BossBarAPI.Color color, BossBarAPI.Style style, float progress, BossBarAPI.Property... properties) {
		this(ComponentSerializer.toString(message), color, style, progress, properties);
	}

	@Override
	public Collection<? extends Player> getPlayers() {
		return new ArrayList<>(this.receivers);
	}

	@Override
	public void addPlayer(Player player) {
		if (!this.receivers.contains(player)) {
			this.receivers.add(player);
			sendPacket(0, player);
			BossBarAPI.addBarForPlayer(player, this);
		}
	}

	@Override
	public void removePlayer(Player player) {
		if (this.receivers.contains(player)) {
			this.receivers.remove(player);
			sendPacket(1, player);
			BossBarAPI.removeBarForPlayer(player, this);
		}
	}

	@Override
	public BossBarAPI.Color getColor() {
		return this.color;
	}

	@Override
	public void setColor(BossBarAPI.Color color) {
		if (color == null) { throw new IllegalArgumentException("color cannot be null"); }
		if (color != this.color) {
			this.color = color;
			sendPacket(4, null);
		}
	}

	@Override
	public BossBarAPI.Style getStyle() {
		return this.style;
	}

	@Override
	public void setStyle(BossBarAPI.Style style) {
		if (style == null) { throw new IllegalArgumentException("style cannot be null"); }
		if (style != this.style) {
			this.style = style;
			sendPacket(4, null);
		}
	}

	@Override
	public void setProperty(BossBarAPI.Property property, boolean flag) {
		switch (property) {
			case DARKEN_SKY:
				darkenSky = flag;
				break;
			case PLAY_MUSIC:
				playMusic = flag;
				break;
			case CREATE_FOG:
				createFog = flag;
				break;
			default:
				break;
		}
		sendPacket(5, null);
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public void setMessage(String message) {
		if (message == null) { throw new IllegalArgumentException("message cannot be null"); }
		if (!message.startsWith("{") || !message.endsWith("}")) {
			throw new IllegalArgumentException("Invalid JSON");
		}
		if (!message.equals(this.message)) {
			this.message = message;
			sendPacket(3, null);
		}
	}

	@Override
	public float getProgress() {
		return progress;
	}

	@Override
	public void setProgress(float progress) {
		if (progress > 1) {
			progress = progress / 100F;
		}
		if (progress != this.progress) {
			this.progress = progress;
			sendPacket(2, null);
		}
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public void setVisible(boolean flag) {
		if (flag != this.visible) {
			this.visible = flag;
			sendPacket(flag ? 0 : 1, null);
		}
	}

	void sendPacket(int action, Player player) {
		try {
			Object packet = PacketPlayOutBoss.newInstance();
			PacketPlayOutBossFieldResolver.resolve("a").set(packet, this.uuid);
			PacketPlayOutBossFieldResolver.resolve("b").set(packet, PacketPlayOutBossAction.getEnumConstants()[action]);
			PacketPlayOutBossFieldResolver.resolve("c").set(packet, serialize(this.message));
			PacketPlayOutBossFieldResolver.resolve("d").set(packet, this.progress);
			PacketPlayOutBossFieldResolver.resolve("e").set(packet, BossBattleBarColor.getEnumConstants()[this.color.ordinal()]);
			PacketPlayOutBossFieldResolver.resolve("f").set(packet, BossBattleBarStyle.getEnumConstants()[this.style.ordinal()]);
			PacketPlayOutBossFieldResolver.resolve("g").set(packet, this.darkenSky);
			PacketPlayOutBossFieldResolver.resolve("h").set(packet, this.playMusic);
			PacketPlayOutBossFieldResolver.resolve("i").set(packet, this.createFog);

			if (player != null) {
				BossBarAPI.sendPacket(player, packet);
			} else {
				for (Player player1 : this.getPlayers()) {
					BossBarAPI.sendPacket(player1, packet);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	//Deprecated methods

	@Override
	public float getMaxHealth() {
		return 100F;
	}

	@Override
	public void setHealth(float percentage) {
		setProgress(percentage / 100F);
	}

	@Override
	public float getHealth() {
		return getProgress() * 100F;
	}

	@Override
	public Player getReceiver() {
		return null;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public void updateMovement() {
	}

	static Object serialize(String json) throws ReflectiveOperationException {
		return ChatSerializerMethodResolver.resolve(new ResolverQuery("a", String.class)).invoke(null, json);
	}
}
