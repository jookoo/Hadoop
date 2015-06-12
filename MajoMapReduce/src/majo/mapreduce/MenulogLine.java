package majo.mapreduce;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Beschreibt eine Zeile der Menulog-Dateien.
 * <pre>
 *20150214;09:02:22;21;C:\PATH\XYZ.EXE;;Eine Auswahl;ABC->DEF</pre>
 * 
 * @author majo
 *
 */
public class MenulogLine {
	
	private static final int INDEX_MAX = 7;
	
	/**
	 * 3.  Bis zum 04.05.2015
	 * 2. Preise von Kunde 16725
	 * 2.   Ab dem 07.04.2015
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
		"(\\d+. E-Mail-Bestellung) \\(([\\w]+[\\.\\-]?)+@([\\w\\-]+\\.)+[a-zA-Z]{2,4}\\)", 
	};

	/** Tag/Uhrzeit der Aktion */
	private final Calendar date;
	
	/** Benutzerkürzel */
	private final String user;
	
	/** Pfad zum Programm */
	private final File program;
	
	/** Auswahl vom benutzer */
	private final String value;
	
	/** bereinigte Auswahl vom benutzer */
	private final String cleanValue;
	
	/** Funktionsstack bei Aktion */
	private final Set<String> stack;
	
	/**
	 * Erstellt ein Objekt mit den Werten von {@code line}.
	 * @param line eine Menulog-Zeile
	 */
	public MenulogLine(final String line) {
		Objects.requireNonNull(line);
		final String[] tokens = line.split(";");
		if (tokens.length != INDEX_MAX) {
			throw new IllegalArgumentException("[tokens.length] != " + INDEX_MAX);
		}
		date = createDate(tokens[0], tokens[1]);
		user = tokens[2];
		program = new File(tokens[3]);
		value = tokens[5];
		cleanValue = cleanup(value);
		stack = new LinkedHashSet<>();
	}

	private String cleanup(final String x) {
		String y = null;
		if (null != x) {
			// eindampfen
			for (final String regex: CLEANUP) {
				final Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				final Matcher m = p.matcher(x);
				if (m.find()) {
					y = m.group(1);
				}
			}
			// Leerzeichen
			if (null != y) {
				y = y.replaceAll("[ ]{2,}", " ");
			}
		}
		return y;
	}
	
	private Calendar createDate(final String yyyymmdd, final String hhMMss) {
		final Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(yyyymmdd.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(yyyymmdd.substring(4, 6)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(yyyymmdd.substring(6, 8)));
		final String x = hhMMss.replaceAll(":", "");
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(x.substring(0, 2)));
		cal.set(Calendar.MINUTE, Integer.parseInt(x.substring(2, 4)));
		cal.set(Calendar.SECOND, Integer.parseInt(x.substring(4, 6)));
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	public Calendar getDateTime() {
		return date;
	}

	public String getUser() {
		return user;
	}

	public File getProgram() {
		return program;
	}
	
	public String getValue() {
		return value;
	}

	public String getCleanValue() {
		return (null == cleanValue ? value : cleanValue);
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(MenulogLine.class.getName()).append(" [");
		final DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		sb.append("date = ").append(df.format(date.getTime())).append(", ");
		sb.append("program = ").append(program.getAbsolutePath()).append(", ");
		sb.append("user = ").append(user).append(", ");
		sb.append("value = ").append(value);
		sb.append("]");
		return sb.toString();
	}

}
