package graphics;

import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

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
	
	/** Menuaufrufe */
	private final Map<Long,String> map;
	
	
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
	}
	
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
	
	private Map<Long, String> createMap(final String input) {
		final Map<Long, String> map = new LinkedHashMap<>();
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
	
	public Map<Long, String> getMap() {
		return map;
	}

	public String createEdgeName() {
		return null;
	}

	/**
	 * Geht entlang einer Session und baut Flanken zwischen den Menüpunkten auf.
	 * @return
	 */
	public List<Edge> getEdges() {
		final Set<Menu> menus = new LinkedHashSet<>();
		final List<Edge> edges = new LinkedList<>();
		String from = null;
		String to = null;
		for (final Entry<Long, String> entry: map.entrySet()) {
			if (null == from) {
				from = entry.getValue();
			} else {
				to = entry.getValue();
				final Edge edge = new Edge();
				final Menu f = new Menu(from, null);
				final Menu t = new Menu(to, from);
				edge.add(f);
				edge.add(t);
				edges.add(edge);
				from = to;
			}
		}
		return edges;
	}
	
	/**
	 * Geht entlang einer Session und baut die Menüpunkte auf.
	 * @return
	 */
	public Set<Menu> getMenus() {
		final Set<Menu> menus = new LinkedHashSet<>();
		String from = null;
		String to = null;
		for (final Entry<Long, String> entry: map.entrySet()) {
			if (null == from) {
				from = entry.getValue();
				final Menu m = new Menu(from, null);
				menus.add(m);
			} else {
				to = entry.getValue();
				final Menu m = new Menu(to, from);
				if (!menus.contains(m)) {
					menus.add(m);
				}
				from = to;
			}
		}
		return menus;
	}
}
