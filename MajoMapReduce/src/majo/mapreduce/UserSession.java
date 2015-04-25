package majo.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.Writable;

/**
 * Beschreibt eine Benutzersitzung.
 * 
 * @author majo
 *
 */
public class UserSession implements Writable {
	
	/** Benutzername für Session */
	private String username = "???";
	
	/** Einträge der Sitzung; Sortierung entspricht Zeitschlüssel */
	private Map<Long, String> menues = new TreeMap<>();
	
	/**
	 * Instanziiert eine Session für den Benutzernamen {@code username} und
	 * legt dessen ersten Eintrag an.
	 * @param username ein Benutzername
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

	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeUTF(username);
		out.writeInt(menues.size());
		for (Long time: menues.keySet()) {
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
		final StringBuffer sb = new StringBuffer(username).append(" ");
		for (Long x: menues.keySet()) {
			sb.append("[");
			sb.append(x.longValue());
			sb.append(":");
			final String value = menues.get(x);
			sb.append(value.replaceAll("[\\.:]+", ""));
			sb.append("]");
		}
		return sb.toString();
	}

}
