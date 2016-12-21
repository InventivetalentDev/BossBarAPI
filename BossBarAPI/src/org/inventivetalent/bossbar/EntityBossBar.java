package org.inventivetalent.bossbar;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.bossbar.reflection.ClassBuilder;
import org.inventivetalent.bossbar.reflection.NMSClass;
import org.inventivetalent.reflection.minecraft.DataWatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static org.inventivetalent.reflection.minecraft.DataWatcher.V1_9.ValueType.*;

public class EntityBossBar extends BukkitRunnable implements BossBar {

	protected static int ENTITY_DISTANCE = 32;

	protected final int  ID;
	protected final UUID uuid;

	protected final Player receiver;
	protected       String message;
	protected       float  health;
	protected       float  healthMinus;
	protected float minHealth = 1;

	protected Location location;
	protected World    world;
	protected boolean visible = false;
	protected Object dataWatcher;

	protected EntityBossBar(Player player, String message, float percentage, int timeout, float minHealth) {
		this.ID = new Random().nextInt();
		this.uuid = UUID.randomUUID();

		this.receiver = player;
		this.message = message;
		this.health = percentage / 100F * this.getMaxHealth();
		this.minHealth = minHealth;
		this.world = player.getWorld();
		this.location = this.makeLocation(player.getLocation());

		if (percentage <= minHealth) {
			BossBarAPI.removeBar(player);
		}

		if (timeout > 0) {
			this.healthMinus = this.getMaxHealth() / timeout;
			this.runTaskTimer(BossBarPlugin.instance, 20, 20);
		}
	}

	protected Location makeLocation(Location base) {
		return base.getDirection().multiply(ENTITY_DISTANCE).add(base.toVector()).toLocation(this.world);
	}

	@Override
	public Player getReceiver() {
		return receiver;
	}

	@Override
	public float getMaxHealth() {
		return 300;
	}

	@Override
	public void setHealth(float percentage) {
		this.health = percentage / 100F * this.getMaxHealth();
		if (this.health <= this.minHealth) {
			BossBarAPI.removeBar(this.receiver);
		} else {
			this.sendMetadata();
		}
	}

	@Override
	public float getHealth() {
		return health;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
		if (this.isVisible()) {
			this.sendMetadata();
		}
	}

	@Override
	public Collection<? extends Player> getPlayers() {
		return Collections.singletonList(getReceiver());
	}

	@Override
	public void addPlayer(Player player) {
	}

	@Override
	public void removePlayer(Player player) {
		setVisible(false);
	}

	@Override
	public BossBarAPI.Color getColor() {
		return null;
	}

	@Override
	public void setColor(BossBarAPI.Color color) {
	}

	@Override
	public BossBarAPI.Style getStyle() {
		return null;
	}

	@Override
	public void setStyle(BossBarAPI.Style style) {
	}

	@Override
	public void setProperty(BossBarAPI.Property property, boolean flag) {
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public void run() {
		this.health -= this.healthMinus;
		if (this.health <= this.minHealth) {
			BossBarAPI.removeBar(this.receiver);
		} else {
			this.sendMetadata();
		}
	}

	@Override
	public void setVisible(boolean flag) {
		if (flag == this.visible) { return; }
		if (flag) {
			this.spawn();
		} else {
			this.destroy();
		}
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public void setProgress(float progress) {
		setHealth(progress * 100);
	}

	@Override
	public float getProgress() {
		return getHealth() / 100;
	}

	@Override
	public void updateMovement() {
		if (!this.visible) { return; }
		this.location = this.makeLocation(this.receiver.getLocation());
		try {
			Object packet = ClassBuilder.buildTeleportPacket(this.ID, this.getLocation(), false, false);
			BossBarAPI.sendPacket(this.receiver, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void updateDataWatcher() {
		if (this.dataWatcher == null) {
			try {
				this.dataWatcher = DataWatcher.newDataWatcher(null);
				DataWatcher.setValue(this.dataWatcher, 17, ENTITY_WITHER_a, new Integer(0));
				DataWatcher.setValue(this.dataWatcher, 18, ENTITY_WIHER_b, new Integer(0));
				DataWatcher.setValue(this.dataWatcher, 19, ENTITY_WITHER_c, new Integer(0));

				DataWatcher.setValue(this.dataWatcher, 20, ENTITY_WITHER_bw, new Integer(1000));// Invulnerable time (1000 = very small)
				DataWatcher.setValue(this.dataWatcher, 0, ENTITY_FLAG, Byte.valueOf((byte) (0 | 1 << 5)));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		try {
			DataWatcher.setValue(this.dataWatcher, 6, ENTITY_LIVING_HEALTH, this.health);

			DataWatcher.setValue(this.dataWatcher, 10, ENTITY_NAME, this.message);
			DataWatcher.setValue(this.dataWatcher, 2, ENTITY_NAME, this.message);

			DataWatcher.setValue(this.dataWatcher, 11, ENTITY_NAME_VISIBLE, (byte) 1);
			DataWatcher.setValue(this.dataWatcher, 3, ENTITY_NAME_VISIBLE, (byte) 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void sendMetadata() {
		this.updateDataWatcher();
		try {
			Object metaPacket = ClassBuilder.buildNameMetadataPacket(this.ID, this.dataWatcher, 2, 3, this.message);
			BossBarAPI.sendPacket(this.receiver, metaPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void spawn() {
		try {
			this.updateMovement();
			this.updateDataWatcher();
			Object packet = ClassBuilder.buildWitherSpawnPacket(this.ID, this.uuid, this.getLocation(), this.dataWatcher);
			BossBarAPI.sendPacket(this.receiver, packet);
			this.visible = true;
			this.sendMetadata();
			this.updateMovement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void destroy() {
		try {
			this.cancel();
		} catch (IllegalStateException e) {
		}
		try {
			Object packet = NMSClass.PacketPlayOutEntityDestroy.getConstructor(int[].class).newInstance(new int[] { this.ID });
			BossBarAPI.sendPacket(this.receiver, packet);
			this.visible = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
