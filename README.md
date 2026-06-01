# Pflichtenheft Coreon (Flixlix, Nilsch)

Coreon ist ein modular aufgebautes Spigot/Paper‑Plugin für Minecraft,
das serverweite Utility‑Funktionalität über konfigurierbare Module bereitstellt 
und Commands sowie Listener zur Laufzeit ein- oder ausschaltet.
Architektur und Persistenz (z. B. lokale DB/YAML) sind so organisiert,
dass Zustände, Business‑Logik und Modulverwaltung sauber getrennt und live aktualisierbar sind.

## Muss-Ziele:
- [x] Module über Ingame GUI einstellbar (Flixlix)
- [ ] Invsee - Inventar eines Spielers anschauen und in echtzeit Items rausnehmen können(Nilsch)
- [ ] Homes - Home setzen /sethome <name>, teleportieren zum Home /home <name>, löschen von Home /deletehome <name>(Flixlix)
- [x] Vanish - Unsichtbar für jeden und so als wäre man offline /vanish (Nilsch + Flixlix)
- [x] leicht bedienbar - DAU-Friendly(Flixlix + Nilsch)

## Kann-Ziele:
- [ ] Invsee bei offline-Spielern (Nilsch)
- [ ] ec; ecsee - Enderchest von sich selbst oder eines anderen Spielers anschauen und in Items rausnehmen können (Nilsch)
- [ ] Bei rejoin prüfen ob Spieler vanish perms hat (Flixlix=
- [ ] warps - warp setzen /setwarp <name>, teleportieren zu warp /warp <name>, löschen von warp /deletewarp <name> (Admin-Only) (Flixlix + Nilsch)
- [ ] rtp; tpa - rtp(Random Teleport) in gewissem Radius tpt werden, tpa (Teleport Anfrage) an andere Spieler senden und annehmen/ablehnen können (Flixlix + Nilsch)
- [ ] config über Website - GUI statt nur Ingame auch per Webinterface (Flixlix + Nilsch)