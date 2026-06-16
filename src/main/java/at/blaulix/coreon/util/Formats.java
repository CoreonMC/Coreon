package at.blaulix.coreon.util;

/**
 * Kleine Helferfunktionen für String-Formatierung.
 */
public class Formats {
    /**
     * Großschreibt das erste Zeichen eines Strings.
     *
     * @param s Eingabestring (muss mindestens 1 Zeichen lang sein)
     * @return String mit großgeschriebenem erstem Zeichen
     */
    public static String capitalizeFirstChar(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
