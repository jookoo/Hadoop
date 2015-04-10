package majo.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.io.Writable;

/**
 * Beschreibt eine Benutzersitzung.
 * 
 * @author majo
 *
 */
public class UserSession implements Writable {
	
	/** 
	 * maximaler Abstand zwischen zwei Einträgen in der Session:
	 * 10 Minuten * 60 Sekunden * 1000 Millisekunden 
	 */
	public static final long MAX = (10 * 60 * 1000);
	
	private static final Set<String> STARTPAGES = new HashSet<>();
	
	static {
		STARTPAGES.add("2. Auskunft");
	}
	
	/** Benutzername für Session */
	private String username = "<unknown>";
	
	/** 
	 * Startzeitpunkt der Session; wird im Konstruktor UND beim 
	 * {@link #readFields(DataInput)} besetzt 
	 */
	private long start = 0L;
	
	/** Einträge der Sitzung; Sortierung entspricht Zeitschlüssel */
	private Map<Long, String> menues = new TreeMap<>();
	
	/**
	 * Instanziiert eine Session für den Benutzernamen {@code username} und
	 * legt dessen ersten Eintrag an.
	 * @param username ein Benutzername
	 */
	public UserSession(final String username, final long time, final String menue) {
		this.username = username;
		this.start = time;
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
	 * Liefert den Startzeitpunkt der Session.
	 * @return eine Zahl >= 0
	 */
	public long getStartTime() {
		return start;
	}

	/**
	 * Fügt der Sitzung einen Eintrag hinzu.
	 * @param time der Zeitpunkt
	 * @param menue der Menüeintrag
	 * @throws NullPointerException wenn {@code menue} gleich <code>null</code>
	 */
	public boolean add(final long time, final String menue) {
		Objects.requireNonNull(menue);
		boolean success = false;
		final SortedSet<Long> keys = new TreeSet<>(menues.keySet());
		final long first = keys.first() - MAX;
		final long last = keys.last() + MAX;
		if (first <= time && time <= last) {
			menues.put(Long.valueOf(time), menue);
			success = true;
		}
		return success;
	}

	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeUTF(username);
		out.writeLong(start);
		out.writeInt(menues.size());
		for (Long time: menues.keySet()) {
			out.writeLong(time);
			out.writeUTF(menues.get(time));
		}
	}

	@Override
	public void readFields(final DataInput in) throws IOException {
		username = in.readUTF();
		start = in.readLong();
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
		final StringBuffer sb = new StringBuffer(username);
		sb.append("[").append(start).append("]");
		for (Long x: menues.keySet()) {
			if (0 < sb.length()) {
				sb.append(";");
			}
			sb.append(x.longValue());
			sb.append(":");
			sb.append(menues.get(x));
		}
		return sb.toString();
	}

}
