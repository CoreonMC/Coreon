package at.blaulix.coreon.handler;

public class SearchState {

    public enum MatchMode { STARTSWITH, CONTAINS, ENDSWITH }
    public enum SortMode  { AZ, ZA, ENABLED_FIRST, DISABLED_FIRST }

    private MatchMode matchMode = MatchMode.CONTAINS;
    private SortMode  sortMode  = SortMode.AZ;
    private String    query     = "";

    public MatchMode getMatchMode() { return matchMode; }
    public SortMode  getSortMode()  { return sortMode;  }
    public String    getQuery()     { return query;     }

    public void setMatchMode(MatchMode m) { this.matchMode = m; }
    public void setSortMode(SortMode s)   { this.sortMode  = s; }
    public void setQuery(String q)        { this.query = q.toLowerCase(java.util.Locale.ROOT); }

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
