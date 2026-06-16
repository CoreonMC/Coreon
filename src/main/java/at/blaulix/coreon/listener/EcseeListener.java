package at.blaulix.coreon.listener;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.handler.EcseeHandler;
import at.blaulix.coreon.util.Playerdata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listener für EcSee-GUIs: synchronisiert und persistiert Änderungen am EnderChest
 * sowohl für online als auch für offline Spieler. Aktualisiert offene GUIs, wenn
 * der Zielspieler sein EnderChest schließt.
 */
public class EcseeListener implements Listener {

    private final Coreon plugin;

    /**
     * Konstruktor.
     *
     * @param plugin Plugin-Instanz
     */
    public EcseeListener(Coreon plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // When the target player closes their own ender chest, refresh all open
    // EcSee GUIs that are watching them so admins see the current state.
    // -------------------------------------------------------------------------
    /**
     * Aktualisiert alle offenen EcSee-GUIs, die den Zielspieler beobachten, wenn
     * dieser sein EnderChest schließt.
     *
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onEnderChestClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player target)) return;

        // Was this player's own ender chest?
        if (!event.getInventory().equals(target.getEnderChest())) return;

        String onlineTitle = EcseeHandler.TITLE_PREFIX + target.getName();

        // Find all players who have this target's EcSee GUI open and refresh it
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getOpenInventory() == null) continue;
            String viewerTitle = viewer.getOpenInventory().getTitle();
            if (!viewerTitle.startsWith(EcseeHandler.TITLE_PREFIX)) continue;

            // Match by online title or UUID suffix
            boolean isWatchingTarget = viewerTitle.equals(onlineTitle)
                    || (viewerTitle.contains(EcseeHandler.UUID_SEPARATOR)
                        && viewerTitle.contains(target.getUniqueId().toString()));

            if (!isWatchingTarget) continue;

            // Refresh the GUI with the target's current ender chest contents
            Inventory gui = viewer.getOpenInventory().getTopInventory();
            ItemStack[] current = target.getEnderChest().getContents();
            for (int i = 0; i < 27; i++) {
                gui.setItem(i, i < current.length ? current[i] : null);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Click
    // -------------------------------------------------------------------------
    /**
     * Behandelt Klicks in EcSee-GUIs und synchronisiert Änderungen entsprechend.
     *
     * @param event InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(EcseeHandler.TITLE_PREFIX)) return;

        Inventory topInv = event.getView().getTopInventory();
        boolean inTop = event.getRawSlot() < topInv.getSize();
        boolean shiftFromBottom = event.isShiftClick() && event.getRawSlot() >= topInv.getSize();

        if (!inTop && !shiftFromBottom) return;

        if (shiftFromBottom) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            int targetSlot = -1;
            for (int i = 0; i < topInv.getSize(); i++) {
                ItemStack existing = topInv.getItem(i);
                if (existing == null || existing.getType().isAir()) {
                    targetSlot = i;
                    break;
                }
            }
            if (targetSlot == -1) return;

            topInv.setItem(targetSlot, clicked.clone());
            event.setCurrentItem(null);

            plugin.getServer().getScheduler().runTask(plugin, () ->
                    handleChange(topInv, title)
            );
        } else {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    handleChange(topInv, title)
            );
        }
    }

    // -------------------------------------------------------------------------
    // Drag
    // -------------------------------------------------------------------------
    /**
     * Behandelt Drag-Events in EcSee-GUIs und synchronisiert Änderungen entsprechend.
     *
     * @param event InventoryDragEvent
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(EcseeHandler.TITLE_PREFIX)) return;

        Inventory topInv = event.getView().getTopInventory();
        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleChange(topInv, title)
        );
    }

    // -------------------------------------------------------------------------
    // Central change handler
    // -------------------------------------------------------------------------
    /**
     * Zentrale Handler-Methode, die entscheidet ob Änderungen an ein online Ziel
     * weitergereicht oder in YAML gespeichert werden.
     *
     * @param gui   Inventory mit Änderungen
     * @param title Titel des GUIs (kann UUID enthalten)
     */
    private void handleChange(Inventory gui, String title) {
        if (title.contains(EcseeHandler.UUID_SEPARATOR)) {
            String uuid = title.substring(title.indexOf(EcseeHandler.UUID_SEPARATOR)
                    + EcseeHandler.UUID_SEPARATOR.length());
            Player nowOnline = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (nowOnline != null && nowOnline.isOnline()) {
                syncToOnlinePlayer(gui, nowOnline);
            } else {
                saveToYaml(gui, uuid);
            }
        } else {
            String targetName = title.substring(EcseeHandler.TITLE_PREFIX.length());
            Player target = plugin.getServer().getPlayer(targetName);
            if (target != null && target.isOnline()) {
                syncToOnlinePlayer(gui, target);
            }
        }
    }

    /**
     * Synchronisiert die GUI-Inhalte mit dem EnderChest eines online Spielers
     * und persistiert die Änderungen zur Sicherheit.
     *
     * @param gui    Inventory
     * @param target Zielspieler (online)
     */
    private void syncToOnlinePlayer(Inventory gui, Player target) {
        ItemStack[] contents = new ItemStack[27];
        for (int i = 0; i < 27; i++) contents[i] = gui.getItem(i);
        target.getEnderChest().setContents(contents);
        target.updateInventory();
        Playerdata.saveOfflineEnderChest(target.getUniqueId().toString(), contents);
    }

    /**
     * Speichert die EnderChest-Inhalte eines offline Spielers in die YAML-Datei.
     *
     * @param gui  Inventory
     * @param uuid UUID des Zielspielers als String
     */
    private void saveToYaml(Inventory gui, String uuid) {
        ItemStack[] contents = new ItemStack[27];
        for (int i = 0; i < 27; i++) contents[i] = gui.getItem(i);
        Playerdata.saveOfflineEnderChest(uuid, contents);
    }
}
