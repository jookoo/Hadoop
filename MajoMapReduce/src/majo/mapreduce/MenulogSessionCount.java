package majo.mapreduce;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Ananlyse der Benutzersitzungen.
 * 
 * @author majo
 *
 */
public class MenulogSessionCount {
	
	/**
	 * Beschreibung für verschiedene Zähler im Job.
	 */
	public static enum COUNTER {
		  OTHER_PROGRAM_LINE,
		  OTHER_USERNAME_LINE,
		  OTHER_DATETIME_LINE,
		  MATCH_LINE
	}

	private static final String MENULOG_FILTER_USERNAME = "menulog.filter.username";
	
	private static final String MENULOG_MINUTES_MAX = "menulog.minutes.max";

	/**
	 * Konfiguration und Starter für den Job.
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		// Konfiguration
		Configuration conf = new Configuration();
		conf.set(MENULOG_FILTER_USERNAME, "22");
		conf.setInt(MENULOG_MINUTES_MAX, 30);

		// Job anlegen
		final Job job = Job.getInstance(conf, 
				MenulogSessionCount.class.getSimpleName());
		job.setJarByClass(MenulogSessionCount.class);
		
		// Mappper + Combiner + Reducer
		job.setMapperClass(UserValueMapper.class);
		job.setCombinerClass(SessionReducer.class);
		job.setReducerClass(SessionReducer.class);
		
		// Output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(UserSession.class);
		
		// Mapper-Output
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(UserSession.class);
		
		// Input- und Output-Pfad
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		// Ausführung abwarten
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	/**
	 * Interpretiert eine Menulog-Zeile. Wird der Programmname vom Filter 
	 * {@link UserValueMapper#FILTER_PRG} akzeptiert, wird ein Schlüssel/Wert-
	 * Paar im Kontext abgelegt. Schlüssel ist der Benutzername und ein Wert 
	 * ist ein Objekt {@link UserSession}. Letzteres nimmt den Benutzernamen 
	 * (zusätzlich zum Schlüssel), den Zeitpunkt und den Menüpunkt auf.
	 */
	public static class UserValueMapper 
	extends Mapper<Object, Text, Text, UserSession> {

		/** der Programmfilter */
		public static final String FILTER_PRG = 
				".*\\\\AU(FTRAG)?(_BLI)?(WIN[\\d]+)?\\.EXE$";
		
		@Override
		public void map(
				final Object key, final Text value, final Context context) 
						throws IOException, InterruptedException {
			final Configuration conf = context.getConfiguration();
			final String filterUser = conf.get(MENULOG_FILTER_USERNAME);
			
			final MenulogLine line = new MenulogLine(value.toString());
			final String prg = line.getProgram().getName();
			if (acceptProgram(prg)) {
				final String username = line.getUser();
				if (null == filterUser || filterUser.equals(username)) {
					// Filter: /input/150203.CSV
					final Calendar cal = line.getDateTime();
//					if (2015 == cal.get(Calendar.YEAR) 
//							&& Calendar.FEBRUARY == cal.get(Calendar.MONTH)
//							&& 3 == cal.get(Calendar.DAY_OF_MONTH)) {
						final long timestamp = cal.getTimeInMillis();
						final String menue = line.getValue();
						final UserSession session = 
								new UserSession(username, timestamp, menue);
						context.write(new Text(username), session);
//					} else {
//						context.getCounter(COUNTER.OTHER_DATETIME_LINE).increment(1);					
//					}
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
	public static class SessionReducer 
	extends Reducer<Text, UserSession, Text, UserSession> {

		/** eine Ausgabe zur Fehlersuche */
		private final Monitor monitor = new Monitor(false);
		
		/** die Startseiten */
		private static final Set<String> STARTPAGES = new HashSet<>();	
		static {
			STARTPAGES.add("2. Auskunft");
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
		
		private long delay = (30 * FACTOR);
		
		@Override
		public void reduce(
				final Text key, final Iterable<UserSession> values,
				final Context context) 
						throws IOException, InterruptedException {
			// gesammelte Sitzungen
			final TreeSet<UserSession> sessions = new TreeSet<>();
			final TreeMap<Long,String> cache = new TreeMap<>();

			// Konfiguration
			final Configuration conf = context.getConfiguration();
			final int minutes = conf.getInt(MENULOG_MINUTES_MAX, 30);
			delay = (minutes * FACTOR);

			monitor.println("username = " + key.toString() + ", delay = " + delay);
			
			// Sitzungen ermitteln
			int count = 0;
			for (UserSession x: values) {
				final Map<Long, String> menues = x.getMenues();
				monitor.println("\tmenues: first = " + x.getFirstTime() + ", size = " + menues.size());
				for (Entry<Long, String> e: menues.entrySet()) {
					// aktuelle Wert: Zeit + Menüpunkt
					final long time = e.getKey().longValue();
					final String menue = e.getValue();
					if (!ENDPAGES.contains(menue)) {
						if (cache.isEmpty()) {
							// neue Sitzung beginnen
							cache.put(Long.valueOf(time), menue);
							monitor.println("\t\tinit");
						} else {
							if ((!STARTPAGES.contains(menue)) && checkMinMaxTime(
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
			for (UserSession x: sessions) {
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