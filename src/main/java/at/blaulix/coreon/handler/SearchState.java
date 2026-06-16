package at.blaulix.coreon.handler;

/**
 * Modelliert den Suchzustand eines Spielers im Settings-GUI: Match-Mode, Sort-Mode
 * und der aktuelle Query-String.
 */
public class SearchState {

    public enum MatchMode { STARTSWITH, CONTAINS, ENDSWITH }
    public enum SortMode  { AZ, ZA, ENABLED_FIRST, DISABLED_FIRST }

    private MatchMode matchMode = MatchMode.CONTAINS;
    private SortMode  sortMode  = SortMode.AZ;
    private String    query     = "";

    /**
     * @return aktueller MatchMode
     */
    public MatchMode getMatchMode() { return matchMode; }

    /**
     * @return aktueller SortMode
     */
    public SortMode  getSortMode()  { return sortMode;  }

    /**
     * @return aktueller Query-String (kleingeschrieben)
     */
    public String    getQuery()     { return query;     }

    /**
     * Setzt den Match-Mode.
     */
    public void setMatchMode(MatchMode m) { this.matchMode = m; }

    /**
     * Setzt den Sort-Mode.
     */
    public void setSortMode(SortMode s)   { this.sortMode  = s; }

    /**
     * Setzt den Such-Query; der Wert wird auf Lowercase normalisiert.
     *
     * @param q Suchbegriff
     */
    public void setQuery(String q)        { this.query = q.toLowerCase(java.util.Locale.ROOT); }

    /**
     * Prüft, ob ein Modul-Key zum aktuellen Query gemäß MatchMode passt.
     *
     * @param key Modul-Key
     * @return {@code true}, wenn der Key dem Query entspricht oder der Query leer ist
     */
    public boolean matches(String key) {
        if (query.isEmpty()) return true;
        String k = key.toLowerCase(java.util.Locale.ROOT);
        return switch (matchMode) {
            case STARTSWITH -> k.startsWith(query);
            case CONTAINS   -> k.contains(query);
            case ENDSWITH   -> k.endsWith(query);
        };
    }
}
