package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.CoreonHandler;
import at.blaulix.coreon.handler.SearchState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class CoreonListener implements Listener {

    private final CoreonHandler handler;

    public CoreonListener(CoreonHandler handler) {
        this.handler = handler;
    }

    // ─── Inventory Click ──────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String rawTitle = event.getView().getTitle();
        String title    = ChatColor.stripColor(rawTitle).toLowerCase(Locale.ROOT);

        if (!title.startsWith("coreon settings")
                && !title.startsWith("coreon search settings")
                && !title.startsWith("(de-)activation")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName()).toLowerCase(Locale.ROOT);

        // ── Main Settings GUI ────────────────────────────────────────────────
        if (title.startsWith("coreon settings")) {

            if (displayName.equals("exit")) {
                player.closeInventory(); return;
            }
            // Search compass OR Sort hopper → open search menu
            if (displayName.equals("search") || displayName.startsWith("sortierung:")) {
                handler.openSearchMenu(player); return;
            }
            // Module item → confirm screen
            String moduleName = displayName.trim();
            if (!moduleName.isEmpty()) {
                handler.deActiveSettings(moduleName, player);
            }
            return;
        }

        // ── Search Settings Menu ─────────────────────────────────────────────
        if (title.startsWith("coreon search settings")) {

            // Back
            if (displayName.equals("back")) {
                handler.coreonSettings(player); return;
            }
            // Execute search → ask for chat input
            if (displayName.contains("suchen")) {
                handler.enterSearchInput(player); return;
            }

            SearchState state = handler.getSearchState(player);
            boolean changed = false;

            // Match mode buttons
            if (displayName.contains("startswith")) {
                state.setMatchMode(SearchState.MatchMode.STARTSWITH); changed = true;
            } else if (displayName.contains("contains")) {
                state.setMatchMode(SearchState.MatchMode.CONTAINS);   changed = true;
            } else if (displayName.contains("endswith")) {
                state.setMatchMode(SearchState.MatchMode.ENDSWITH);   changed = true;
            }
            // Sort mode buttons
            else if (displayName.contains("a → z")) {
                state.setSortMode(SearchState.SortMode.AZ);             changed = true;
            } else if (displayName.contains("z → a")) {
                state.setSortMode(SearchState.SortMode.ZA);             changed = true;
            } else if (displayName.contains("enabled first")) {
                state.setSortMode(SearchState.SortMode.ENABLED_FIRST);  changed = true;
            } else if (displayName.contains("disabled first")) {
                state.setSortMode(SearchState.SortMode.DISABLED_FIRST); changed = true;
            }

            if (changed) {
                Inventory open = player.getOpenInventory().getTopInventory();
                handler.refreshSearchMenu(open, state);
                player.updateInventory();
            }
            return;
        }

        // ── Confirm/Cancel screen ────────────────────────────────────────────
        if (title.startsWith("(de-)activation")) {

            if (displayName.equals("confirm")) {
                String stripped = ChatColor.stripColor(rawTitle);
                String moduleName = stripped.substring("(de-)activation ".length()).trim().toLowerCase(Locale.ROOT);
                player.closeInventory();
                handler.changeActive(moduleName, player);
                handler.coreonSettings(player);
                return;
            }
            if (displayName.equals("cancel")) {
                handler.coreonSettings(player);
            }
        }
    }

    // ─── Chat Input ───────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!handler.isSearching(player)) return;
        event.setCancelled(true);
        String message = event.getMessage().trim();
        handler.getPlugin().getServer().getScheduler().runTask(
                handler.getPlugin(), () -> handler.handleSearchInput(player, message));
    }
}
