package graphics;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;

/**
 * Repräsentiert eine Zeile in der user_session.txt.
 * <p>
 * Diese Datei entsteht durch den MapReduceJob und gibt Zeilenweise
 * Sitzungen sortiert nach Benutzer aus.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class SessionLine {

	/** maximaler Index der Session-Line-Tokens */
	private static final int INDEX_MAX = 2;
	
	/** die Startseiten */
	public static final Set<String> STARTPAGES = new HashSet<>();	
	static {
		STARTPAGES.add("1 Aufträge");
		STARTPAGES.add("2 Auskunft");
		STARTPAGES.add("3 Rechnungen");
		STARTPAGES.add("4 Bestellungen");
		STARTPAGES.add("5 Belastungen");
		STARTPAGES.add("6 Gutschriften / Abholscheine");
		STARTPAGES.add("7 Artikel-Verwaltung");
		STARTPAGES.add("8 Textverarbeitung");
		STARTPAGES.add("9 Listen");
		STARTPAGES.add("A Etiketten / Schilder / Belege");
		STARTPAGES.add("D Postrechnungen drucken");
		STARTPAGES.add("E Sonderpreise");
		STARTPAGES.add("F Wareneingang");
		STARTPAGES.add("G Rechnungseingang");
		STARTPAGES.add("H Dienst-Programme");
		STARTPAGES.add("I Gutschrift / Neue Rechnung");
		STARTPAGES.add("K Artikelnummern der Kunden");
		STARTPAGES.add("L Ware abholen (sofort Rechnung)");
		STARTPAGES.add("M Fremdbelege erfassen");
		STARTPAGES.add("N Zusätzliche Pack-Nummern drucken");
		STARTPAGES.add("O Tourenplanung");
		STARTPAGES.add("P Empfangsscheine Scannen");
		STARTPAGES.add("Q Nur für 18");
		STARTPAGES.add("R Belegerfassung");
		STARTPAGES.add("S Anfragen");
		STARTPAGES.add("U Vorgänge");
		STARTPAGES.add("V Fremprogramme");
		STARTPAGES.add("W Nachlieferung");
	}
	
	/** Benutzerkürzel */
	private final String user;
	
	/** Tag/Uhrzeit der Aktion */
	private final Calendar date;
	
	/** Menuaufrufe der Sessionline sortiert nach Zeitstempel (TreeSet) */
	private final Map<Long,String> map;
	
	/** Die Menüpunkte extrahiert aus der Session */
	private final Set<Menu> menus = new LinkedHashSet<Menu>();
	
	/** Die Kanten extrahiert aus der Session */
	private final Set<Edge> edges = new LinkedHashSet<Edge>();
	
	/**
	 * ein Konstruktor.
	 * @param line die Zeile in der Ergebnis-Datei des User-Session-Jobs
	 */
	public SessionLine(final String line) {
		Objects.requireNonNull(line);
		final int trenner = line.indexOf("[") - 1;
		if (-1 == trenner) {
			throw new IllegalArgumentException("[trenner] not found");
		}
		final String userAndDate = line.substring(0,trenner);
		final String menuHits = line.substring(trenner, line.length());
		final String[] tokens = userAndDate.split("\\t");
		if (tokens.length != INDEX_MAX) {
			throw new IllegalArgumentException("[tokens.length] != " + INDEX_MAX);
		}
		user = tokens[0];
		date = createDate(tokens[1]);
		map = createMap(menuHits);
		analyze();
	}
	
	/**
	 * Erzeugt einen Kalender mit entsprechend voreingestellten Werten.
	 * @param yyyymmddhhMMss das Datum als Zeichenkette
	 * @return ein Objekt, niemals <code>null</code>
	 */
	private Calendar createDate(final String yyyymmddhhMMss) {
		final String[] split = yyyymmddhhMMss.split(" ");
		final String yyyymmdd = split[0];
		final String hhMMss = split[1];
		final Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(yyyymmdd.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(yyyymmdd.substring(5, 7)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(yyyymmdd.substring(8, 9)));
		final String x = hhMMss.replaceAll(":", "");
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(x.substring(0, 2)));
		cal.set(Calendar.MINUTE, Integer.parseInt(x.substring(2, 4)));
		cal.set(Calendar.SECOND, Integer.parseInt(x.substring(4, 6)));
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	/**
	 * Baut eine Map mit den <Zeitpunkt, Menüpunkt> auf.
	 * @param input die bereits unterteile Zeile
	 * @return ein Objekt niemals <code>null</code>
	 */
	private Map<Long, String> createMap(final String input) {
		final Map<Long, String> map = new TreeMap<>();
		if (null != input && 0 < input.length()) {
			final String[] tokens = input.split("\\[");
			for (final String s: tokens) {
				final String[] split = s.split(":");
				if (null != split && 0 < split[0].trim().length()) {
					final Long l = Long.valueOf(split[0]);
					final String menu = split[1].substring(0, split[1].length()-1);
					map.put(l, menu);
				}
			}
		}
		return map;
	}
	
	/**
	 * Liefert die Map mit Menüpunkten.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Map<Long, String> getMap() {
		return map;
	}

	/**
	 * Ermittelt Menüpunkte und Kanten für die gegebenen Daten der einzelnen
	 * Zeile.
	 */
	private void analyze() {
		String from = null;
		String to = null;
		Menu lastmenu = null;
		for (final Entry<Long, String> entry: map.entrySet()) {
			if (null == from) {
				from = entry.getValue();
				lastmenu = new Menu(from, null);
				menus.add(lastmenu);
			} else {
				to = entry.getValue();
				final Edge edge = new Edge();
				final Menu t = new Menu(to, from);
				menus.add(t);
				edge.add(lastmenu);
				edge.add(t);
				edges.add(edge);
				from = to;
				lastmenu = t;
			}
		}
	}

	/**
	 * Liefert den Benutzer der Session
	 * @return
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Liefert die Menüpunkte der Session.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Set<Menu> getMenu() {
		return menus;
	}
	
	/**
	 * Liefert die Kanten der Session.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Set<Edge> getEdge() {
		return edges;
	}
}
