package tc.oc.occ.assistlogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.pgm.api.match.event.MatchFinishEvent;
import tc.oc.pgm.api.match.event.MatchStartEvent;
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent;
import tc.oc.pgm.damagehistory.DamageEntry;
import tc.oc.pgm.damagehistory.DamageHistoryMatchModule;

public class DeathListener implements Listener {

  private final AssistLogger plugin;
  private File dataFolder;
  private FileWriter writer;
  private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

  public DeathListener(AssistLogger assistLogger) {
    this.plugin = assistLogger;
    this.dataFolder = createStorageFolder();
  }

  public File createStorageFolder() {
    String configPath = plugin.getAppConfig().getStoragePath();
    String storagePath =
        (configPath != null)
            ? configPath
            : plugin.getDataFolder().getPath() + File.separator + "data";

    File dataFolder = new File(storagePath);
    // Ensure the plugin folder exists
    if (!dataFolder.exists()) {
      dataFolder.mkdirs();
    }

    return dataFolder;
  }

  @EventHandler
  public void onMatchStart(MatchStartEvent event) {
    plugin.reloadAppConfig();
    this.dataFolder = createStorageFolder();

    if (!plugin.getAppConfig().isEnabled()) return;

    try {
      if (this.writer != null) this.writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    File file =
        new File(
            dataFolder,
            "match-" + event.getMatch().getId() + "-" + Instant.now().toEpochMilli() + ".csv");
    try {
      this.writer = new FileWriter(file);
      this.writer.write("instant,player,killer,damages\r\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @EventHandler
  public void onMatchEnd(MatchFinishEvent event) {
    if (!plugin.getAppConfig().isEnabled()) return;

    try {
      this.writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @EventHandler
  public void onPlayerDeath(MatchPlayerDeathEvent event) {
    if (!plugin.getAppConfig().isEnabled()) return;

    DamageHistoryMatchModule dhm = event.getMatch().getModule(DamageHistoryMatchModule.class);
    if (dhm == null) return;

    Deque<DamageEntry> damageHistory = dhm.getDamageHistory(event.getPlayer());

    LinkedHashMap<String, Double> collect =
        damageHistory.stream()
            .collect(
                Collectors.groupingBy(
                    damageEntry ->
                        damageEntry.getDamager() != null
                            ? damageEntry.getDamager().getNameLegacy()
                            : "$unknown",
                    LinkedHashMap::new,
                    Collectors.mapping(
                        DamageEntry::getDamage, Collectors.reducing(0d, Double::sum))));

    String player = event.getPlayer().getNameLegacy();
    String instant = Instant.now().toString();
    String killer = event.getKiller() != null ? event.getKiller().getNameLegacy() : "";
    StringJoiner damageValues = new StringJoiner(",");
    int killerIndex = -1;
    int index = 0;

    for (Map.Entry<String, Double> entry : collect.entrySet()) {
      damageValues.add(decimalFormat.format(entry.getValue()));
      if (killerIndex == -1 && Objects.equals(entry.getKey(), killer)) {
        killerIndex = index;
      }
      index++;
    }

    try {
      this.writer.write(instant + "," + player + "," + killerIndex + "," + damageValues + "\r\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
