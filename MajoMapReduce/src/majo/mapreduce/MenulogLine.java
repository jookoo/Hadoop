package majo.mapreduce;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Beschreibt eine Zeile der Menulog-Dateien.
 * 
 * <pre>
 * 20150214;09:02:22;21;C:\PATH\XYZ.EXE;;Eine Auswahl;ABC->DEF
 * 
 * Eine Zeile in einer Menulog-Datei beinhaltet 
 * 1. das Datum des Log-Eintrags
 * 2. die Uhrzeit des Eintrags
 * 3. den Benutzer der den Eintrag zu verantworten hat, referenziert über einen zweistelligen Code
 * 4. die Ausführbare Datei mit der der Eintrag erzeugt wurde
 * 5.
 * 6. der Name des gewählten Menüpunkts
 * 7. der aktuelle Funktionsstack im alten ERP-Programm
 * </pre>
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class MenulogLine {

	/* maximaler Index in einer Menulogline */
	private static final int INDEX_MAX = 7;

	/**
	 * Datensatz bestehend aus REGEX-Pattern zur Bereinigung der Daten.
	 * 
	 *  <p>
	 *  Die Bereinigung der Namen die als Schlüssel fungieren für folgende Fehler:
	 * 3. Bis zum 04.05.2015 
	 * 2. Preise von Kunde 16725 
	 * 2. Ab dem 07.04.2015 
	 * 1. Genau am 03.04.2015 
	 * 1. Fax-Bestellung (0921-89721) 
	 * 1. Fax-Bestellung (02102-2047-109) 
	 * 4. E-Mail-Bestellung (Anja.Luesebrink@ampri.de)
	 * 
	 */
	private static final String[] CLEANUP = new String[] {
			"(3.  Bis zum) \\d\\d\\.\\d\\d\\.\\d\\d\\d\\d",
			"(2. Preise von Kunde) \\d{2,}",
			"(2.   Ab dem) \\d\\d\\.\\d\\d\\.\\d\\d\\d\\d",
			"(1. Genau am) \\d\\d\\.\\d\\d\\.\\d\\d\\d\\d",
			"(1. Fax-Bestellung) \\(.*\\)",
			"(\\d+. E-Mail-Bestellung) \\(([\\w]+[\\.\\-]?)+@([\\w\\-]+\\.)+[a-zA-Z]{2,4}\\)"
		};

	/**
	 * Datensatz bestehen aus REGEX-Pattern um Namen von ausführbaren Dateien
	 * dem jeweiligen Programm zuzuordnen.
	 * 
	 * C:\AUFTRAG\AUFTRAG.EXE -> GH C:\BUR\GH\AUF\AU_BLI.EXE -> GH
	 * R:\XPRG\VOLLNEU\AU\AUWIN950.EXE -> GH C:\BUR\BUCHHALT\BUCH.EXE ||
	 * R:\BUCHPRG\BUCHHALT\BUCH.EXE -> KND_BUHA C:\BUR\LIEFBUCH\LIEFBUCH.EXE ||
	 * R:\BUCHPRG\LIEFBUCH\LIEFBUCH -> LIEF_BUHA TODO
	 * R:\BUCHPRG\BUCHHALT\BUCHDEMO.EXE -> null
	 */
	private static final String[][] REPLACE_PRG = new String[][] {
			{ ".*AUFTRAG\\.EXE$", "GH" }, 
			{ ".*AU_BLI\\.EXE$", "GH" },
			{ ".*AUWIN[\\d]+\\.EXE$", "GH" }, 
			{ ".*BUCH\\.EXE$", "KND_BUHA" },
			{ ".*LIEFBUCH\\.EXE$", "LIEF_BUHA" }
		};

	/** Tag/Uhrzeit der Aktion */
	private final Calendar date;

	/** Benutzerkürzel */
	private final String user;

	/** Pfad zum Programm */
	private final String program;

	/** normalisiertes Programm */
	private final String cleanProgram;

	/** Auswahl vom benutzer */
	private final String value;

	/** bereinigte Auswahl vom benutzer */
	private final String cleanValue;

	/**
	 * Erstellt ein Objekt mit den Werten von {@code line}.
	 * 
	 * @param line eine Menulog-Zeile
	 */
	public MenulogLine(final String line) {
		Objects.requireNonNull(line);
		final String[] tokens = line.split(";");
		if (tokens.length != INDEX_MAX) {
			throw new IllegalArgumentException("[tokens.length] != "
					+ INDEX_MAX);
		}
		date = createDate(tokens[0], tokens[1]);
		user = tokens[2];
		program = tokens[3];
		cleanProgram = cleanupProgram(program);
		value = tokens[5];
		cleanValue = cleanupValue(value);
	}

	/**
	 * Ersetzt Zeichenketten aus {@link MenulogLine#REPLACE_PRG}.
	 * @param x eine Programmzeile
	 * @return ein Objekt oder <code>null</code>
	 */
	private String cleanupProgram(final String x) {
		String y = null;
		if (null != x) {
			// Programmnamen ersetzen
			ersetzung: for (final String[] regex : REPLACE_PRG) {
				if (x.matches(regex[0])) {
					y = regex[1];
					break ersetzung;
				}
			}
		}
		return y;
	}

	/**
	 * Bereinigt Zeilen in denen eine Zeichenkette aus {@value MenulogLine#CLEANUP}
	 * gefunden wird.
	 * @param x eine Zeichenkette
	 * @return ein Objekt oder <code>null</code>
	 */
	private String cleanupValue(final String x) {
		String y = null;
		if (null != x) {
			// eindampfen
			ersetzung: for (final String regex : CLEANUP) {
				final Pattern p = Pattern.compile(regex,
						Pattern.CASE_INSENSITIVE);
				final Matcher m = p.matcher(x);
				if (m.find()) {
					y = m.group(1);
					break ersetzung;
				}
			}
			// Leerzeichen
			if (null != y) {
				y = y.replaceAll("[ ]{2,}", " ");
			}
		}
		return y;
	}

	/**
	 * Erzeugt ein <code>Calendar</code> Objekt aus übergebenen Zeichenketten.
	 * @param yyyymmdd eine Zeichenkette für Jahre, Monat, Tag
	 * @param hhMMss eine Zeichenkette für Stunde, Minute, Sekunde
	 * @return ein Objekt, niemals <code>null</code>
	 */
	private Calendar createDate(final String yyyymmdd, final String hhMMss) {
		final Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(yyyymmdd.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(yyyymmdd.substring(4, 6)) - 1);
		cal.set(Calendar.DAY_OF_MONTH,
				Integer.parseInt(yyyymmdd.substring(6, 8)));
		final String x = hhMMss.replaceAll(":", "");
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(x.substring(0, 2)));
		cal.set(Calendar.MINUTE, Integer.parseInt(x.substring(2, 4)));
		cal.set(Calendar.SECOND, Integer.parseInt(x.substring(4, 6)));
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * Liefert das Calendarobjekt mit dem Datum der Zeile
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Calendar getDateTime() {
		return date;
	}

	/**
	 * Liefert den Benutzer, der die Zeile zu verantworten hat.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Liefert den Programmpfad, mit dem die Zeile erstellt wurde.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public String getProgram() {
		return program;
	}

	/**
	 * Liefert den gewählten Menüpunkt der Zeile.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Liefert den den bereinigten Menüpunkt.
	 * <p>
	 * siehe {@link MenulogLine#CLEANUP} und 
	 * {@link MenulogLine#cleanupValue(String)} für Informationen
	 * zur Bereinigung
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public String getCleanValue() {
		return (null == cleanValue ? value : cleanValue);
	}

	/**
	 * Liefert das Ergebnis des Mappings verschiedener EXE Dateien auf einen
	 * Programmnamen. Wird null geliefert wurde das Programm beabsichtigt
	 * ignoriert.
	 * <p>
	 * siehe {@link MenulogLine#REPLACE_PRG} und 
	 * {@link MenulogLine#cleanupProgram(String)} für Informationen
	 * zur Ersetzung
	 * @return ein Objekt oder <code>null</code>
	 */
	public String getCleanProgram() {
		return cleanProgram;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(MenulogLine.class.getName()).append(" [");
		final DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		sb.append("date = ").append(df.format(date.getTime())).append(", ");
		sb.append("program = ").append(program).append(", ");
		sb.append("user = ").append(user).append(", ");
		sb.append("value = ").append(value);
		sb.append("]");
		return sb.toString();
	}

}
