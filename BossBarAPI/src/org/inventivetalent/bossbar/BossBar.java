package org.inventivetalent.bossbar;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface BossBar {

	/**
	 * @return The players which can see the BossBar
	 */
	Collection<? extends Player> getPlayers();

	/**
	 * Add a player to this BossBar
	 *
	 * @param player {@link Player} to add
	 */
	void addPlayer(Player player);

	/**
	 * Remove a player from this BossBar
	 *
	 * @param player {@link Player} to remove
	 */
	void removePlayer(Player player);

	/**
	 * @return the {@link org.inventivetalent.bossbar.BossBarAPI.Color}
	 */
	BossBarAPI.Color getColor();

	/**
	 * @param color the new {@link org.inventivetalent.bossbar.BossBarAPI.Color}
	 */
	void setColor(BossBarAPI.Color color);

	/**
	 * @return the {@link org.inventivetalent.bossbar.BossBarAPI.Style}
	 */
	BossBarAPI.Style getStyle();

	/**
	 * @param style the new {@link org.inventivetalent.bossbar.BossBarAPI.Style}
	 */
	void setStyle(BossBarAPI.Style style);

	/**
	 * Modify a property
	 *
	 * @param property {@link org.inventivetalent.bossbar.BossBarAPI.Property}
	 * @param flag     whether to enable the property
	 */
	void setProperty(BossBarAPI.Property property, boolean flag);

	/**
	 * @return the message
	 */
	String getMessage();

	/**
	 * @param flag whether the BossBar is visible
	 */
	void setVisible(boolean flag);

	/**
	 * @return whether the BossBar is visible
	 */
	boolean isVisible();

	/**
	 * @return the progress (0.0 - 1.0)
	 */
	float getProgress();

	/**
	 * @param progress the new progress (0.0 - 1.0)
	 */
	void setProgress(float progress);

	@Deprecated
	float getMaxHealth();

	@Deprecated
	void setHealth(float percentage);

	@Deprecated
	float getHealth();

	@Deprecated
	void setMessage(String message);

	@Deprecated
	Player getReceiver();

	@Deprecated
	Location getLocation();

	@Deprecated
	void updateMovement();
}
