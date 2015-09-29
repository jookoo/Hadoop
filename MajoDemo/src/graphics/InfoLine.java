package graphics;

import java.util.Map;
import java.util.Objects;

/**
 * Repräsentiert ein Zeile in der input_info.txt
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InfoLine {

	/** eine Konstante */
	private static final String PREFIX_USER = "USER";
	
	/** eine Konstante */
	private static final String PREFIX_PATH = "VALUE";

	/** die gezählte Anzahl der Vorkommnisse */
	private final int count;

	/** liefert ob es sich um eine Zeile mit Benutzerbezug handelt */
	private boolean userline = true;
	
	/** der Benutzer */
	private String user = null;
	
	/** der Programmpfad */
	private String prg = null;
	
	/** das Menü */
	private String menu = null;
	
	private Map<String,String> map;
	
	/**
	 * ein Konstruktor.
	 * <p>
	 * USER[sy]	1
	 * VALUE[GH~1. 11 Falschbestellung des Kunden]	5711
	 * @param line die Zeile
	 */
	public InfoLine(final String line, final Map<String,String> translation) {
		Objects.requireNonNull(line);
		this.map = translation;
		final String[] value = readLine(line);
		if (userline) {
			user = value[0];
			count = Integer.valueOf(value[1]);
		} else {
			prg = value[0];
			menu = value[1];
			count = Integer.valueOf(value[2]);
		}
	}
	
	/**
	 * Einlesen einer Zeile.
	 * @param line die Zeile
	 * @return ein Objekt, niemals <code>null</code>
	 */
	final String[] readLine(final String line) {
		final String[] x;
		if (line.startsWith(PREFIX_USER)) {
			x = new String[2];
			final String cutLine = line.substring(PREFIX_USER.length()+1);
			final String[] tokens = cutLine.split("]");
			x[0] = tokens[0].trim();
			x[1] = tokens[1].trim();
			
		} else {
			x = new String[3];
			final String cutLine = line.substring(PREFIX_PATH.length()+1);
			final String[] tokens = cutLine.split("]");
			final String[] prgLine = tokens[0].split("~");
			x[0] = prgLine[0].trim();
			x[1] = prgLine[1].trim();
			x[2] = tokens[1].trim();
			userline = false;
		}
		return x;
	}

	/**
	 * Liefert den Benutzernamen der Zeile.
	 * @return ein Objekt oder <code>null</code>
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Liefert die gezählte Anzahl der Zeile 
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * Liefert den bereits kumulierten Programmpfad der Zeile.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public String getProgram() {
		return prg;
	}

	/**
	 * Liefert den Menüeintrag der Zeile.
	 * @return ein Objekt oder <code>null</code>
	 */
	public String getMenu() {
		return menu;
	}

	/**
	 * <code>true</code> wenn Mitarbeiter
	 * @return
	 */
	public boolean isWorkerLine() {
		return userline;
	}

	/**
	 * Liefert den übersetzten Namen des Mitarbeiters
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public String translatedName() {
		return map.get(user);
	}
	
}
