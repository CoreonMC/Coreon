package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.util.Formats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CoreonHandler {

    private final Coreon plugin;

    // Per-player search state (persists across opens)
    private final Map<UUID, SearchState> searchStates   = new HashMap<>();
    // Players currently waiting for chat input
    private final Set<UUID>              searchingPlayers = new HashSet<>();

    public CoreonHandler(Coreon plugin) {
        this.plugin = plugin;
    }

    public Coreon getPlugin() { return plugin; }

    // ─── Per-player SearchState ───────────────────────────────────────────────

    public SearchState getSearchState(Player player) {
        return searchStates.computeIfAbsent(player.getUniqueId(), k -> new SearchState());
    }

    // ─── Main Settings GUI ────────────────────────────────────────────────────

    public void coreonSettings(Player player) {
        if (plugin.getConfig().getConfigurationSection("modules") == null) {
            player.sendMessage("§cNo modules section found in config!"); return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, "§8Coreon Settings");
        loadSettings(inv, player);
        player.openInventory(inv);
    }

    // ─── Populate main settings inventory ────────────────────────────────────

    public void loadSettings(Inventory inv, Player player) {
        inv.clear();
        ConfigurationSection modules = plugin.getConfig().getConfigurationSection("modules");
        if (modules == null) return;

        SearchState state = getSearchState(player);

        // Collect + filter
        List<String> keys = new ArrayList<>(modules.getKeys(false));
        keys.removeIf(k -> !state.matches(k));

        // Sort
        switch (state.getSortMode()) {
            case AZ             -> Collections.sort(keys);
            case ZA             -> keys.sort(Comparator.reverseOrder());
            case ENABLED_FIRST  -> keys.sort(Comparator.comparingInt(k ->
                    modules.getBoolean(k) ? 0 : 1));
            case DISABLED_FIRST -> keys.sort(Comparator.comparingInt(k ->
                    modules.getBoolean(k) ? 1 : 0));
        }

        // Place module items
        int slot = 0;
        for (String key : keys) {
            boolean enabled = modules.getBoolean(key);
            ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((enabled ? "§a" : "§c") + Formats.capitalizeFirstChar(key));
                meta.setLore(Collections.singletonList(
                        enabled ? "§7Status: §aEnabled" : "§7Status: §cDisabled"));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // Bottom bar: slots 45-53
        inv.setItem(45, createExitItem());
        inv.setItem(49, createSearchItem(state));
        inv.setItem(53, createSortIndicator(state));
    }

    // ─── Search Menu (3 rows) ─────────────────────────────────────────────────
    // Layout:
    //  Row 0 (slots 0-2):  Startswith | Contains | Endswith
    //  Row 1 (slots 9-12): A→Z | Z→A | Enabled first | Disabled first
    //  Row 2 (slot 22):    🔍 Suchen   |  slot 18: Back

    public void openSearchMenu(Player player) {
        SearchState state = getSearchState(player);
        Inventory inv = Bukkit.createInventory(null, 27, "§8Coreon §7Search Settings");
        refreshSearchMenu(inv, state);
        player.openInventory(inv);
    }

    public void refreshSearchMenu(Inventory inv, SearchState state) {
        inv.clear();

        // ── Match mode row ──
        inv.setItem(0,  matchItem(SearchState.MatchMode.STARTSWITH, state, Material.ARROW,         "§bStartswith",     "§7Nur Module die §bbeginnen §7mit dem Begriff"));
        inv.setItem(1,  matchItem(SearchState.MatchMode.CONTAINS,   state, Material.PAPER,         "§bContains",       "§7Module die den Begriff §benthalten"));
        inv.setItem(2,  matchItem(SearchState.MatchMode.ENDSWITH,   state, Material.FEATHER,       "§bEndswith",       "§7Nur Module die §benden §7mit dem Begriff"));

        // ── Sort mode row ──
        inv.setItem(9,  sortItem(SearchState.SortMode.AZ,             state, Material.OAK_SIGN,   "§eA → Z",          "§7Alphabetisch aufsteigend"));
        inv.setItem(10, sortItem(SearchState.SortMode.ZA,             state, Material.DARK_OAK_SIGN, "§eZ → A",        "§7Alphabetisch absteigend"));
        inv.setItem(11, sortItem(SearchState.SortMode.ENABLED_FIRST,  state, Material.LIME_WOOL,  "§aEnabled first",  "§7Aktivierte Module zuerst"));
        inv.setItem(12, sortItem(SearchState.SortMode.DISABLED_FIRST, state, Material.RED_WOOL,   "§cDisabled first", "§7Deaktivierte Module zuerst"));

        // ── Back ──
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) { bm.setDisplayName("§7Back"); back.setItemMeta(bm); }
        inv.setItem(18, back);

        // ── Search / execute ──
        ItemStack go = new ItemStack(Material.NETHER_STAR);
        ItemMeta gm = go.getItemMeta();
        if (gm != null) {
            gm.setDisplayName("§a§lSuchen");
            gm.setLore(Arrays.asList(
                    "§7Aktueller Begriff: §b" + (state.getQuery().isEmpty() ? "§o(leer)" : state.getQuery()),
                    "§7Modus: §b" + matchLabel(state.getMatchMode()),
                    "§7Sortierung: §e" + sortLabel(state.getSortMode()),
                    "",
                    "§7Klick zum Eingeben eines neuen Begriffs",
                    "§7oder §aEnter §7ohne Text für alle Module"
            ));
            go.setItemMeta(gm);
        }
        inv.setItem(22, go);
    }

    private ItemStack matchItem(SearchState.MatchMode mode, SearchState state,
                                Material mat, String name, String desc) {
        boolean active = state.getMatchMode() == mode;
        ItemStack item = new ItemStack(active ? Material.CYAN_STAINED_GLASS_PANE : mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(active ? "§a§l✔ " + name : "§7" + name);
            meta.setLore(Arrays.asList(desc, active ? "§a§oAktiv" : "§8Klick zum Auswählen"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack sortItem(SearchState.SortMode mode, SearchState state,
                               Material mat, String name, String desc) {
        boolean active = state.getSortMode() == mode;
        ItemStack item = new ItemStack(active ? Material.YELLOW_STAINED_GLASS_PANE : mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(active ? "§a§l✔ " + name : "§7" + name);
            meta.setLore(Arrays.asList(desc, active ? "§a§oAktiv" : "§8Klick zum Auswählen"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String matchLabel(SearchState.MatchMode m) {
        return switch (m) {
            case STARTSWITH -> "Startswith";
            case CONTAINS   -> "Contains";
            case ENDSWITH   -> "Endswith";
        };
    }

    private String sortLabel(SearchState.SortMode s) {
        return switch (s) {
            case AZ             -> "A → Z";
            case ZA             -> "Z → A";
            case ENABLED_FIRST  -> "Enabled first";
            case DISABLED_FIRST -> "Disabled first";
        };
    }

    // ─── Chat input flow ──────────────────────────────────────────────────────

    public void enterSearchInput(Player player) {
        searchingPlayers.add(player.getUniqueId());
        player.closeInventory();
        player.sendMessage("§8[§bCoreon§8] §7Suchbegriff eingeben §8(§ccancel§8 §7= Abbrechen, §aEnter §7= alle§8)§7:");
    }

    public boolean isSearching(Player player) {
        return searchingPlayers.contains(player.getUniqueId());
    }

    public void handleSearchInput(Player player, String input) {
        searchingPlayers.remove(player.getUniqueId());
        if (input.equalsIgnoreCase("cancel")) {
            openSearchMenu(player);
            return;
        }
        getSearchState(player).setQuery(input.trim());
        coreonSettings(player);
    }

    // ─── Helper items for main GUI ────────────────────────────────────────────

    private ItemStack createExitItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName("§cExit"); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack createSearchItem(SearchState state) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bSearch");
            meta.setLore(Arrays.asList(
                    "§7Begriff: §b" + (state.getQuery().isEmpty() ? "§o(alle)" : state.getQuery()),
                    "§7Modus: §b"  + matchLabel(state.getMatchMode()),
                    "§7Sort: §e"   + sortLabel(state.getSortMode()),
                    "",
                    "§7Klick zum Öffnen der Sucheinstellungen"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSortIndicator(SearchState state) {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eSortierung: §f" + sortLabel(state.getSortMode()));
            meta.setLore(Collections.singletonList("§7Klick für Sucheinstellungen"));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ─── Module Toggle ────────────────────────────────────────────────────────

    public void partSettings(String moduleName, Player player) {
        ConfigurationSection modules = plugin.getConfig().getConfigurationSection("modules");
        if (modules == null || !modules.contains(moduleName)) return;
        plugin.getConfig().set("modules." + moduleName, !modules.getBoolean(moduleName));
        plugin.saveConfig();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory open = player.getOpenInventory().getTopInventory();
            loadSettings(open, player);
            player.updateInventory();
        });
    }

    public void changeActive(String moduleName, Player player) {
        partSettings(moduleName, player);
    }

    // ─── Confirm/Cancel screen ────────────────────────────────────────────────

    public void deActiveSettings(String moduleName, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8(de-)activation §7" + moduleName);
        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta cm = confirm.getItemMeta();
        if (cm != null) { cm.setDisplayName("§aConfirm"); confirm.setItemMeta(cm); }
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta rm = cancel.getItemMeta();
        if (rm != null) { rm.setDisplayName("§cCancel"); cancel.setItemMeta(rm); }
        inv.setItem(11, confirm);
        inv.setItem(15, cancel);
        player.openInventory(inv);
    }
}
