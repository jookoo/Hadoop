package majo.mapreduce;

/**
 * Sammelt Ereignisse und gibt sie lesbar aus (Standard: {@link System#out}).
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class Monitor {

	/** ein Zähler */
	private static int count = 0;
	
	/** <code>true</code> wenn Ausgabe gewünscht */
	private final boolean onoff;
	
	/**
	 * Instanziiert die Ausgabe im ein- oder ausgeschalteten Zustand.
	 * @param onoff <code>true</code> schaltet Ausgabe ein
	 */
	public Monitor(final boolean onoff) {
		this.onoff = onoff;
		count++;
	}
	
	/**
	 * Ausgabe über die Standard-Konsole.
	 * @param value der auszugebende Wert
	 */
	public void println(final String value) {
		if (onoff) {
			System.out.println(String.format("[%d] %s\n", count, value));
		}
	}
	
}
