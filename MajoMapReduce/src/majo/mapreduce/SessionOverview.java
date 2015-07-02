package majo.mapreduce;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import majo.mapreduce.UserSessionJob.COUNTER;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class SessionOverview {

	
	/**
	 * Interpretiert eine Menulog-Zeile. Wird der Programmname vom Filter 
	 * {@link UserValueMapper#FILTER_PRG} akzeptiert, wird ein Schlüssel/Wert-
	 * Paar im Kontext abgelegt. Schlüssel ist der Benutzername und ein Wert 
	 * ist ein Objekt {@link UserSession}. Letzteres nimmt den Benutzernamen 
	 * (zusätzlich zum Schlüssel), den Zeitpunkt und den Menüpunkt auf.
	 */
	public static class UserValueMapper extends Mapper<Object, Text, Text, UserSession> {

		/** der Programmfilter */
		public static final String FILTER_PRG = 
				"(GH)|(KND_BUHA)|(LIEF_BUHA)";
		
		@Override
		public void map(
				final Object key, final Text value, final Context context) 
						throws IOException, InterruptedException {
			final Configuration conf = context.getConfiguration();
			final String filterUser = conf.get(Main.MENULOG_FILTER_USERNAME);
			
			/**
			 * Mapper lesen im Standardfall CSV Dateien Zeilenweise ein,
			 * daher entspricht {@code value} einer Zeile im CSV und kann
			 * in das Objekt MenulogLine übersetzt werden.
			 */
			final MenulogLine line = new MenulogLine(value.toString());
			
			final String prg = line.getCleanProgram();
			// Falls bestimmte Programme von der Auswertung ausgeschlossen werden sollen
			if (acceptProgram(prg)) {
				final String usernameTmp = line.getUser();
				final String username = (null == usernameTmp ? null : usernameTmp.toLowerCase(Locale.GERMAN));
				// Falls eine Auswertung zu einem spezifischen Benutzer gemacht werden soll
				if (null == filterUser || filterUser.equals(username)) {
					final Calendar cal = line.getDateTime();
					final long timestamp = cal.getTimeInMillis();
					final String menue = line.getCleanValue();
					final UserSession session = 
							new UserSession(username, timestamp, menue);
					context.write(new Text(username), session);
				} else {
					context.getCounter(COUNTER.OTHER_USERNAME_LINE).increment(1);					
				}
			} else {
				context.getCounter(COUNTER.OTHER_PROGRAM_LINE).increment(1);
			}
		}
		
		/**
		 * Liefert <code>true</code> wenn {@code prg} einem gesuchten 
		 * Programmnamen entspricht.
		 * @param prg ein Programmname
		 * @return <code>true</code> wenn gesucht
		 */
		public boolean acceptProgram(final String prg) {
			boolean match = false;
			if (null != prg) {
				match = prg.toUpperCase().matches(FILTER_PRG);
			}
			return match;
		}
		
	}

	/**
	 * Reduziert die vom {@link UserValueMapper} gesammelten Werte pro Benutzer.
	 */
	public static class SessionReducer extends Reducer<Text, UserSession, Text, UserSession> {

		/** eine Ausgabe zur Fehlersuche */
		private final Monitor monitor = new Monitor(false);
		
		/** die Startseiten */
		public static final Set<String> STARTPAGES = new HashSet<>();	
		static {
			STARTPAGES.add("1. Aufträge");
			STARTPAGES.add("2. Auskunft");
			STARTPAGES.add("3. Rechnungen");
			STARTPAGES.add("4. Bestellungen");
			STARTPAGES.add("5. Belastungen");
			STARTPAGES.add("6. Gutschriften / Abholscheine");
			STARTPAGES.add("7. Artikel-Verwaltung");
			STARTPAGES.add("8. Textverarbeitung");
			STARTPAGES.add("9. Listen");
			STARTPAGES.add("A. Etiketten / Schilder / Belege");
			STARTPAGES.add("D. Postrechnungen drucken");
			STARTPAGES.add("E. Sonderpreise");
			STARTPAGES.add("F. Wareneingang");
			STARTPAGES.add("G. Rechnungseingang");
			STARTPAGES.add("H. Dienst-Programme");
			STARTPAGES.add("I. Gutschrift / Neue Rechnung");
			STARTPAGES.add("K. Artikelnummern der Kunden");
			STARTPAGES.add("L. Ware abholen (sofort Rechnung)");
			STARTPAGES.add("M. Fremdbelege erfassen");
			STARTPAGES.add("N. Zusätzliche Pack-Nummern drucken");
			STARTPAGES.add("O. Tourenplanung");
			STARTPAGES.add("P. Empfangsscheine Scannen");
			STARTPAGES.add("Q. Nur für 18");
			STARTPAGES.add("R. Belegerfassung");
			STARTPAGES.add("S. Anfragen");
			STARTPAGES.add("U. Vorgänge");
			STARTPAGES.add("V. Fremprogramme");
			STARTPAGES.add("W. Nachlieferung");
		}
		
		/** die Abschlussseiten */
		private static final Set<String> ENDPAGES = new HashSet<>();	
		static {
			ENDPAGES.add("Z. Programm beenden");
		}
		
		/** 
		 * maximaler Abstand zwischen zwei Einträgen in der Session:
		 * x Minuten * 60 Sekunden * 1000 Millisekunden
		 * 30 Minuten = 1.800.000
		 */
		private static final long FACTOR = (60 * 1000);
		
		/** 
		 * die Verzögerung, die Zeiten zwischen zwei 
		 * Menüklicks als Teil der Session akzeptiert 
		 */
		private long delay = (30 * FACTOR);
		
		@Override
		public void reduce(
				final Text key, final Iterable<UserSession> values,
				final Context context) 
						throws IOException, InterruptedException {
			// gesammelte Sitzungen
			final TreeSet<UserSession> sessions = new TreeSet<>();
			// TreeMap um nach Zeit zu sortieren
			final TreeMap<Long,String> cache = new TreeMap<>();

			// Konfiguration
			final Configuration conf = context.getConfiguration();
			final int minutes = conf.getInt(Main.MENULOG_MINUTES_MAX, 30);
			delay = (minutes * FACTOR);

			monitor.println("username = " + key.toString() + ", delay = " + delay);
			
			// Sitzungen ermitteln
			int count = 0;
			for (final UserSession x: values) {
				final Map<Long, String> menues = x.getMenues();
				monitor.println("\tmenues: first = " + x.getFirstTime() + ", size = " + menues.size());
				for (final Entry<Long, String> e: menues.entrySet()) {
					// aktuelle Wert: Zeit + Menüpunkt
					final long time = e.getKey().longValue();
					final String menue = e.getValue();
					if (!ENDPAGES.contains(menue)) {
						if (cache.isEmpty()) {
							// neue Sitzung beginnen // geht fälschlicherweise von start mit startpage aus
							cache.put(Long.valueOf(time), menue);
							monitor.println("\t\tinit");
						} else {
							if (!STARTPAGES.contains(menue) && 
									checkMinMaxTime(
									cache.firstKey().longValue(), 
									cache.lastKey().longValue(), 
									time)) {
								// gehört mit zur akt. Sitzung
								cache.put(Long.valueOf(time), menue);
								monitor.println("\t\tcache: " + time + " >> " + cache.size());
							} else {
								// akt. Sitzung übernehmen (akt. Wert gehört nicht dazu)
								final UserSession newSession = new UserSession();
								newSession.getMenues().putAll(cache);
								sessions.add(newSession);
								monitor.println("\t\tcreate: " + newSession.getFirstTime() + ", size = " + newSession.getMenues().size());
								cache.clear();
								// neue Sitzung beginnen (mit Wert, der nicht dazu gehört)
								cache.put(Long.valueOf(time), menue);
							}
						}
					}
				}
				count++;
			}			
			
			// Sitzungen veröffentlichen 
			for (final UserSession x: sessions) {
				context.write(key, x);
				monitor.println("\t\t\tpublish: " + x.getFirstTime() + " --> " + x.getMenues().size());
			}
			
			monitor.println(key.toString() + " (" + count + ") --> " + sessions.size());
			
			sessions.clear();
			
		}
		
		/**
		 * Prüft den Zeitpunkt, ob dieser zur Sitzung passt.
		 * @param x eine Benutzersitzung
		 * @param time ein Zeitpunkt
		 */
		public boolean checkMinMaxTime(
				final long first, final long last, final long time) {
			boolean success = false;
			final long min = first - delay;
			final long max = last + delay;
			if (min <= time && time <= max) {
				success = true;
			}
			monitor.println("\t\tcheck: " + min + " / " + time + " / " + max + " = " + success);
			return success;
		}

	}
}
