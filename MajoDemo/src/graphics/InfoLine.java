package graphics;

import java.util.Objects;

/**
 * Repräsentiert ein Zeile in der input_info.txt
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InfoLine {

	private static final int INDEX_MAX = 1;
	
	private static final String PREFIX_USER = "USER";
	
	private static final String PREFIX_PATH = "VALUE";

	private final int count;

	private boolean userline = true;
	
	private String user = null;
	
	private String prg = null;
	
	private String menu = null;
	
	
	/**
	 * USER[sy]	1
	 * VALUE[GH~1. 11 Falschbestellung des Kunden]	5711
	 * @param line
	 */
	public InfoLine(final String line) {
		Objects.requireNonNull(line);
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

	public String getUser() {
		return user;
	}

	public Integer getCount() {
		return count;
	}

	public String getProgram() {
		return prg;
	}

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
	
}
