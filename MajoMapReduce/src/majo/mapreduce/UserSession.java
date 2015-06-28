package majo.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

/**
 * Beschreibt eine Benutzersitzung.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class UserSession implements Writable, WritableComparable<UserSession> {
	
	/** ein Datums-Format */
	private static final SimpleDateFormat DATE_FORMAT = 
			new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	/** Benutzername für Session */
	private String username = "???";
	
	/** Einträge der Sitzung; Sortierung entspricht Zeitschlüssel */
	private final TreeMap<Long, String> menues = new TreeMap<>();

	/**
	 * Parameterloser Konstruktor für {@link Writable}.
	 */
	public UserSession() {
	}
	
	/**
	 * Instanziiert eine Session für den Benutzernamen {@code username} und
	 * legt dessen ersten Eintrag an.
	 * @param username der Benutzer
	 * @param time das Datum
	 * @param menue der erste Menüeintrag
	 */
	public UserSession(
			final String username, final long time, final String menue) {
		this.username = username;
		menues.put(time, menue);
	}

	/**
	 * Liefert den Benutzernamen der Sitzung.
	 * @return eine Zeichenkette, niemals <code>null</code>
	 */
	public String getUser() {
		return username;
	}

	/**
	 * Liefert die gespeicherten Menüeinträge.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Map<Long, String> getMenues() {
		return menues;
	}

	/**
	 * Liefert den ersten Zeitpunkt der Sitzung.
	 * @return eine Zahl
	 */
	public long getFirstTime() {
		return menues.firstKey().longValue();
	}

	/**
	 * Liefert den letzten Zeitpunkt der Sitzung.
	 * @return eine Zahl
	 */
	public long getLastTime() {
		return menues.lastKey().longValue();
	}
	
	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeUTF(username);
		out.writeInt(menues.size());
		for (final Long time: menues.keySet()) {
			out.writeLong(time);
			out.writeUTF(menues.get(time));
		}
	}

	@Override
	public void readFields(final DataInput in) throws IOException {
		username = in.readUTF();
		menues.clear();
		final int size = in.readInt();
		for (int x=0; x<size; x++) {
			final long time = in.readLong();
			final String menue = in.readUTF();
			menues.put(time, menue);
		}
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(DATE_FORMAT.format(new Date(getFirstTime()))).append(" ");
		for (final Entry<Long, String> x: menues.entrySet()) {
			sb.append("[");
			sb.append(x.getKey().longValue());
			sb.append(":");
			sb.append(x.getValue().replaceAll("[\\.:]+", ""));
			sb.append("]");
		}
		return sb.toString();
	}

	@Override
	public int compareTo(final UserSession other) {
		int x = username.compareTo(other.username);
		if (0 == x) {
			x = Long.compare(getFirstTime(), other.getFirstTime());
		}
		return x;
	}

}
