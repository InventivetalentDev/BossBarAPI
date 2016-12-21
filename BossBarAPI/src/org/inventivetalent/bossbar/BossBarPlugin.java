package org.inventivetalent.bossbar;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.apihelper.APIManager;

public class BossBarPlugin extends JavaPlugin {

	protected static Plugin instance;
	BossBarAPI apiInstance = new BossBarAPI();

	@Override
	public void onLoad() {
		APIManager.registerAPI(apiInstance, this);
	}

	@Override
	public void onEnable() {
		instance = this;

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}
		} catch (Exception e) {
		}

		APIManager.initAPI(BossBarAPI.class);
	}

}
