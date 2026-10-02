package me.onlyjordon.capehider;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CapeHider extends JavaPlugin implements Listener {

    private final Map<UUID, URL> originalCapes = new ConcurrentHashMap<>();
    private final Set<UUID> hiddenCapes = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("togglecape").setExecutor(new CommandToggleCape(this));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hiddenCapes.contains(player.getUniqueId())) {
                restoreCape(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("capehider.bypass")) return;

        PlayerTextures textures = player.getPlayerProfile().getTextures();
        if (textures != null && textures.getCape() != null) {
            originalCapes.put(player.getUniqueId(), textures.getCape());
        }
        Bukkit.getScheduler().runTaskLater(this, () -> hideCape(player), 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        hiddenCapes.remove(uuid);
        originalCapes.remove(uuid);
    }

    public void hideCape(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        PlayerTextures textures = profile.getTextures();
        if (textures == null) return;

        if (!originalCapes.containsKey(player.getUniqueId()) && textures.getCape() != null) {
            originalCapes.put(player.getUniqueId(), textures.getCape());
        }
        textures.setCape(null);
        profile.setTextures(textures);
        player.setPlayerProfile(profile);
        hiddenCapes.add(player.getUniqueId());
    }

    public void restoreCape(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        PlayerTextures textures = profile.getTextures();
        if (textures == null) return;

        URL capeUrl = originalCapes.get(player.getUniqueId());
        textures.setCape(capeUrl);
        profile.setTextures(textures);
        player.setPlayerProfile(profile);
        hiddenCapes.remove(player.getUniqueId());
    }

    public boolean toggleCape(Player player) {
        if (hiddenCapes.contains(player.getUniqueId())) {
            restoreCape(player);
            return true;
        } else {
            hideCape(player);
            return false;
        }
    }

    public boolean isCapeHidden(Player player) {
        return hiddenCapes.contains(player.getUniqueId());
    }
}
