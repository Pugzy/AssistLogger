package tc.oc.occ.assistlogger;

import org.bukkit.plugin.java.JavaPlugin;

public class AssistLogger extends JavaPlugin {

  private AssistConfig config;

  @Override
  public void onEnable() {
    this.saveDefaultConfig();
    this.reloadConfig();

    this.config = new AssistConfig(getConfig());

    this.registerListeners();
  }

  private void registerListeners() {
    this.getServer().getPluginManager().registerEvents(new DeathListener(this), this);
  }

  public void reloadAppConfig() {
    this.reloadConfig();
    config.reload(getConfig());
  }

  public AssistConfig getAppConfig() {
    return config;
  }
}
