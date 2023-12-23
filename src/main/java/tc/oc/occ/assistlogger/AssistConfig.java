package tc.oc.occ.assistlogger;

import org.bukkit.configuration.Configuration;

public class AssistConfig {

  private boolean enabled;
  private String storagePath;

  public AssistConfig(Configuration config) {
    reload(config);
  }

  public void reload(Configuration config) {
    this.enabled = config.getBoolean("enabled");
    this.storagePath = config.getString("storage-path", null);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getStoragePath() {
    return storagePath;
  }
}
