package majo.mapreduce;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import majo.mapreduce.Main.IMenuFilter;

/**
 * MapReduce-Job zur Analyse der Benutzersitzungen.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class UserSessionJob {

	/**
	 * Beschreibung f�r verschiedene Z�hler im Job.
	 */
	public static enum COUNTER {
		/** Z�hler f�r herausgefilterte Benutzer (durch {@link Main#FILTER_PRG} */
		OTHER_PROGRAM_LINE,
		/** Z�hler f�r herausgefilterte Benutzer (durch {@link UserSessionJob#MENULOG_FILTER_USERNAME} */
		OTHER_USERNAME_LINE,
		/** Z�hler f�r herausgefilterte Benutzer (durch {@link UserSessionJob#MENULOG_FILTER_MENUE} */
		OTHER_MENUE_LINE,
	}

	/**
	 * Interpretiert eine Menulog-Zeile. Wird der Programmname vom Filter 
	 * {@link UserValueMapper#FILTER_PRG} akzeptiert, wird ein Schl�ssel/Wert-
	 * Paar im Kontext abgelegt. Schl�ssel ist der Benutzername und ein Wert 
	 * ist ein Objekt {@link UserSession}. Letzteres nimmt den Benutzernamen 
	 * (zus�tzlich zum Schl�ssel), den Zeitpunkt und den Men�punkt auf.
	 */
	public static class UserValueMapper extends Mapper<Object, Text, Text, UserSession> {

		//		/** der Programmfilter */
		//		public static final String FILTER_PRG = 
		//				"(GH)|(KND_BUHA)|(LIEF_BUHA)";

		/** Benutzerfilter */
		public static final String FILTER_USER = null;

		/** der Programmfilter */
		public static final String FILTER_PRG = 
				"GH";

		@Override
		public void map(
				final Object key, final Text value, final Context context) 
						throws IOException, InterruptedException {
			/**
			 * Mapper lesen im Standardfall CSV Dateien Zeilenweise ein,
			 * daher entspricht {@code value} einer Zeile im CSV und kann
			 * in das Objekt MenulogLine �bersetzt werden.
			 */
			final MenulogLine line = new MenulogLine(value.toString());

			final String prg = line.getCleanProgram();
			// Falls bestimmte Programme von der Auswertung ausgeschlossen werden sollen
			if (acceptProgram(prg)) {
				final String username = line.getCleanUser();
				final Calendar cal = line.getDateTime();
				final long timestamp = cal.getTimeInMillis();
				final String menue = line.getCleanValue();
				final UserSession session = 
						new UserSession(username, timestamp, menue);
				context.write(new Text(username), session);
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

		/**
		 * Liefert <code>true</code> wenn {@code user} einem gesuchten Benutzer
		 * entspricht.
		 * @param user der Benutzer
		 * @param filterUser der gesuchte Benutzer
		 * @return <code>true</code> wenn gesucht
		 */
		public boolean acceptUser(final String user, final String filterUser) {
			boolean match = false;
			if (null != filterUser) {
				if (null != user && null != filterUser) {
					match = user.toUpperCase().matches(filterUser);
				}
			} else {
				match = true;
			}
			return match;
		}

		/**
		 * Liefert <code>true</code> wenn {@code menu} einem gesuchten Men�punkt
		 * entspricht.
		 * @param menu der Benutzer
		 * @param filterUser der gesuchte Benutzer
		 * @return <code>true</code> wenn gesucht
		 */
		public boolean acceptMenu(final String menu, final String filterMenue) {
			boolean match = false;
			if (null != filterMenue) {
				if (null != menu && null != filterMenue) {
					match = menu.toUpperCase().matches(filterMenue);
				}
			} else {
				match = true;
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
			STARTPAGES.add("1. Auftr.+ge");
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
			STARTPAGES.add("N. Zus.+tzliche Pack-Nummern drucken");
			STARTPAGES.add("O. Tourenplanung");
			STARTPAGES.add("P. Empfangsscheine Scannen");
			STARTPAGES.add("Q\\. Nur f.+r 18");
			STARTPAGES.add("R. Belegerfassung");
			STARTPAGES.add("S. Anfragen");
			STARTPAGES.add("U. Vorg.+nge");
			STARTPAGES.add("V. Fremprogramme");
			STARTPAGES.add("W. Nachlieferung");
		}

		/** die Abschlussseiten */
		private static final Set<String> ENDPAGES = new HashSet<>();	
		static {
			ENDPAGES.add("Z. Programm beenden");
		}

		/** 
		 * maximaler Abstand zwischen zwei Eintr�gen in der Session:
		 * x Minuten * 60 Sekunden * 1000 Millisekunden
		 * 30 Minuten = 1.800.000
		 */
		private static final long FACTOR = (60 * 1000);

		/** 
		 * maximaler Abstand zwischen zwei Eintr�gen in der Session:
		 * x Sekunden * 1000 Millisekunden
		 * 30 Sekunden = 30.000
		 */
		private static final long FACTOR_SECONDS = 1000;

		/** 
		 * die Verz�gerung, die Zeiten zwischen zwei 
		 * Men�klicks als Teil der Session akzeptiert 
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
			final TreeMap<Long, String> cache = new TreeMap<>();
			// Konfiguration
			final Configuration conf = context.getConfiguration();
			final int minutes = conf.getInt(Main.MENULOG_SECONDS_MAX, 30);
			delay = (minutes * FACTOR_SECONDS);

			monitor.println("username = " + key.toString() + ", delay = " + delay);

			final String clsString = conf.get(Main.MENULOG_FILTER_MENUE);
			Object clss = null;
			try {
				clss = Class.forName(clsString).newInstance();
				final IMenuFilter filter = (IMenuFilter) clss;
				// Sitzungen ermitteln
				int count = 0;
				for (final UserSession x: values) {
					final Map<Long, String> menues = x.getMenues();
					monitor.println("\tmenues: first = " + x.getFirstTime() + ", size = " + menues.size());
					for (final Entry<Long, String> e: menues.entrySet()) {
						// aktuelle Wert: Zeit + Men�punkt
						final long time = e.getKey().longValue();
						final String menue = e.getValue();
						if (!ENDPAGES.contains(menue)) {
							if (cache.isEmpty()) {
								// neue Sitzung beginnen
								cache.put(Long.valueOf(time), menue);
								monitor.println("\t\tinit");
							} else {
								boolean smatch = false;
								for (String s: STARTPAGES) {
									smatch = Pattern.matches(s, menue);
									if (smatch) {
										break;
									}
								}
								if (!smatch && 
										checkMinMaxTime(
												cache.firstKey().longValue(), 
												cache.lastKey().longValue(), 
												time)) {
									// geh�rt mit zur akt. Sitzung
									cache.put(Long.valueOf(time), menue);
									monitor.println("\t\tcache: " + time + " >> " + cache.size());
								} else {
									// akt. Sitzung �bernehmen (akt. Wert geh�rt nicht dazu)
									final UserSession newSession = new UserSession();
									newSession.getMenues().putAll(cache);
									sessions.add(newSession);
									monitor.println("\t\tcreate: " + newSession.getFirstTime() + ", size = " + newSession.getMenues().size());
									cache.clear();
									// neue Sitzung beginnen (mit Wert, der nicht dazu geh�rt)
									cache.put(Long.valueOf(time), menue);
								}
							}
						}
					}
					count++;
				}			

				// Filter Einlesen
				Set<String> filterMenue = null;
				Set<String> layerTwo = null;
				boolean regexmode = false;
				if (null != filter) {
					filterMenue = ((IMenuFilter)filter).getFilterLevel(0);
					layerTwo = ((IMenuFilter)filter).getFilterLevel(1);
					regexmode = ((IMenuFilter)filter).isRegexMode();
				}
				// Sitzungen ver�ffentlichen 
				for (final UserSession x: sessions) {
					String firstMenue = null;
					String secondMenue = null;
					int cnt = 1;
//					for (final Entry<Long, String> entry: x.getMenues().entrySet()) {
//						if (1 == cnt) {
//							firstMenue = entry.getValue();
//						} else if (2 == cnt) {
//							secondMenue = entry.getValue();
//						}
//						cnt++;
//					}
//					if (regexmode) {
//						boolean firstlevelmatch = false;
//						for (String s: filterMenue) {
//							firstlevelmatch = Pattern.matches(s, firstMenue);
//							if (firstlevelmatch) {
//								break;
//							}
//						}
//						if (null == filter ||  (firstlevelmatch && layerTwo.contains(secondMenue))) {
//							context.write(key, x);
//							monitor.println("\t\t\tpublish: " + x.getFirstTime() + " --> " + x.getMenues().size());
//						}
//					} else {
//						if (null == filter || (filterMenue.contains(firstMenue) && layerTwo.contains(secondMenue))) {
//							context.write(key, x);
//							monitor.println("\t\t\tpublish: " + x.getFirstTime() + " --> " + x.getMenues().size());
//						} 
//					}
					context.write(key, x);
					monitor.println("\t\t\tpublish: " + x.getFirstTime() + " --> " + x.getMenues().size());
				}

				monitor.println(key.toString() + " (" + count + ") --> " + sessions.size());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}

			sessions.clear();

		}

		/**
		 * Pr�ft den Zeitpunkt, ob dieser zur Sitzung passt.
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