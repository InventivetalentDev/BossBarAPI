package org.inventivetalent.bossbar;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.apihelper.APIManager;

public class BossBarPlugin extends JavaPlugin {

	protected static Plugin instance = JavaPlugin.getProvidingPlugin(BossBarPlugin.class);
	BossBarAPI apiInstance = new BossBarAPI();

	@Override
	public void onLoad() {
		APIManager.registerAPI(apiInstance, this);
	}

	@Override
	public void onEnable() {
		APIManager.initAPI(BossBarAPI.class);
	}

}
