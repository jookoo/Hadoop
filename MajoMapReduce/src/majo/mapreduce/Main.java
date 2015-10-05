package majo.mapreduce;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import majo.mapreduce.InputInfoJob.IntSumReducer;
import majo.mapreduce.InputInfoJob.TokenizerMapper;
import majo.mapreduce.UserSessionJob.SessionReducer;
import majo.mapreduce.UserSessionJob.UserValueMapper;
import majo.mapreduce.UserSessionJobKumuliert.SessionKumuliertReducer;
import majo.mapreduce.UserSessionJobKumuliert.UserValueKumuliertMapper;

/**
 * Konfiguration und Starter für die gesamte Datenvorbereitung durch MapReduce.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class Main {

	/** Konstante für Benutzerfilter-Schlüssel */
	public static final String MENULOG_FILTER_USERNAME = "menulog.filter.username";

	/** Konstante für Wartezeit-In-Minuten-Schlüssel */
	public static final String MENULOG_MINUTES_MAX = "menulog.minutes.max";
	
	/** Konstante für Wartezeit-In-Minuten-Schlüssel */
	public static final String MENULOG_SECONDS_MAX = "menulog.seconds.max";

	/** Konstante für Menuefilter-Schlüssel */
	public static final String MENULOG_FILTER_MENUE = "menulog.filter.menu";
	
	public static void main(final String[] args) throws Exception {	
		// Konfiguration
		final JobConf conf = new JobConf();
//		conf.set(MENULOG_FILTER_USERNAME, "10");
		conf.set(MENULOG_FILTER_MENUE,"majo.mapreduce.Main$FilterKuhDreiArena");
		conf.setInt(MENULOG_SECONDS_MAX, 60);
		int code = 0;

		// Aktiviert die Komprimierung der Ergebnis-Datei
		conf.setBoolean("mapreduce.output.fileoutputformat.compress", true);
		conf.setClass("mapreduce.output.fileoutputformat.compress.codec", BZip2Codec.class, CompressionCodec.class);
		
		
		// Job 1 anlegen/ausführen
//		final Job sequenceJob = createFileSequencerJob(conf);
//		code += (sequenceJob.waitForCompletion(true) ? 0 : 1);

		// Job 2 anlegen/ausführen
//		final Job inputInfoJob = createInputInfoJob(conf);
//		code += (inputInfoJob.waitForCompletion(true) ? 0 : 1);

		// Job 3 anlegen/ausführen
//		final Job userSessionJob = createUserSessionJob(conf);
//		code += (userSessionJob.waitForCompletion(true) ? 0 : 1);
		
		// kumilierte Sessions
		final Job userSessionJobKomuliert = createUserSessionJobKumuliert(conf);
		code += (userSessionJobKomuliert.waitForCompletion(true) ? 0 : 1);
		
		// Job 3 anlegen/ausführen
//		final Job sessionOverviewJob = createSessionOverviewJob(conf);
//		code += (userSessionJob.waitForCompletion(true) ? 0 : 1);

		// Ausführung abwarten
		System.exit(code);
	}

	/**
	 * Geschützter Konstruktor. Framework startet {@link Main#main(String[])}.
	 */
	private Main() {
	}

	/**
	 * Erzeugt und konfiguriert den Job um mit Mapper die 
	 * Daten zu verarbeiten.
	 * @param conf eine Konfiguration
	 * @return ein Objekt niemals <code>null</code>
	 * @throws IOException bei Problemen
	 */
	private static Job createFileSequencerJob(
			final Configuration conf) throws IOException {
		final Job job = Job.getInstance(conf, Mapper.class.getSimpleName());
		job.setJarByClass(Mapper.class);

		// Input-Pfad
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));
		job.setInputFormatClass(TextInputFormat.class);

		// Mapper
		job.setMapperClass(Mapper.class);
		
		// Reducer
		job.setNumReduceTasks(10);
		
		// Output
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		
		// Output-Pfad
		SequenceFileOutputFormat.setOutputPath(job, new Path("/input_sequence"));
		return job;
	}

	/**
	 * Erzeugt und konfiguriert den Job um mit InputInfoJob die 
	 * Daten zu verarbeiten
	 * @param conf eine Konfiguration
	 * @return ein Objekt niemals <code>null</code>
	 * @throws IOException bei Problemen
	 */
	private static Job createInputInfoJob(
			final Configuration conf) throws IOException {
		final Job job = Job.getInstance(conf, InputInfoJob.class.getSimpleName());
		// Der Job
		job.setJarByClass(InputInfoJob.class);

		// Input-Pfad
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));

		// Mappper + Combiner
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);

		// Reducer
		job.setReducerClass(IntSumReducer.class);

		// Output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		// Output-Pfad
		FileOutputFormat.setOutputPath(job, new Path("/input_info"));

		return job;
	}

	/**
	 * Erzeugt und konfiguriert den Job um mit UserSessionJob die 
	 * Daten zu verarbeiten
	 * @param conf eine Konfiguration
	 * @return ein Objekt niemals <code>null</code>
	 * @throws IOException bei Problemen
	 */
	private static Job createUserSessionJobKumuliert(
			final Configuration conf) throws IOException {
		final Job job = Job.getInstance(conf, 
				UserSessionJob.class.getSimpleName());
		job.setJarByClass(UserSessionJob.class);

		//Input-Pfad
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));

		// Mapper-Output
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(UserSession.class);

		// Mappper + Combiner
		job.setMapperClass(UserValueKumuliertMapper.class);
		job.setCombinerClass(SessionKumuliertReducer.class);

		// Reducer
		job.setReducerClass(SessionKumuliertReducer.class);

		// Output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(UserSession.class);

		// Output-Pfad
		FileOutputFormat.setOutputPath(job, new Path("/user_session"));

		return job;
	}
	
	
	/**
	 * Erzeugt und konfiguriert den Job um mit UserSessionJob die 
	 * Daten zu verarbeiten
	 * @param conf eine Konfiguration
	 * @return ein Objekt niemals <code>null</code>
	 * @throws IOException bei Problemen
	 */
	private static Job createUserSessionJob(
			final Configuration conf) throws IOException {
		final Job job = Job.getInstance(conf, 
				UserSessionJob.class.getSimpleName());
		job.setJarByClass(UserSessionJob.class);
		
		//Input-Pfad
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));
		
		// Mapper-Output
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(UserSession.class);
		
		// Mappper + Combiner
		job.setMapperClass(UserValueMapper.class);
		job.setCombinerClass(SessionReducer.class);
		
		// Reducer
		job.setReducerClass(SessionReducer.class);
		
		// Output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(UserSession.class);
		
		// Output-Pfad
		FileOutputFormat.setOutputPath(job, new Path("/user_session"));
		
		return job;
	}
	
	private static Job createSessionOverviewJob(final JobConf conf) throws IOException {
		final Job job = Job.getInstance(conf, 
				UserSessionJob.class.getSimpleName());
		job.setJarByClass(UserSessionJob.class);

		//Input-Pfad
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));

		// Mapper-Output
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(UserSession.class);

		// Mappper + Combiner
		job.setMapperClass(UserValueMapper.class);
		job.setCombinerClass(SessionReducer.class);

		// Reducer
		job.setReducerClass(SessionReducer.class);

		// Output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(UserSession.class);

		// Output-Pfad
		FileOutputFormat.setOutputPath(job, new Path("/user_session"));
		
		return job;
	}

	
	public static class FilterMenuNeunListen implements IMenuFilter {
		
		public static final Set<String> OWN_STARTPAGES = new HashSet<>();	
		static {
			OWN_STARTPAGES.add("9. Listen");
		}
		
		/** die Abschlussseiten */
		private static final Set<String> LAYERTWO = new HashSet<>();	
		static {
			LAYERTWO.add("1. Lagerplatz- , Preis-Etiketten");
			LAYERTWO.add("2. Reserveplatz-Etiketten (Herma4609)");
			LAYERTWO.add("3. Reserveplatz-Schilder DIN A4");
			LAYERTWO.add("4. Pendelhefter Etiketten");
			LAYERTWO.add("5. Beschriftungsschilder Vertic");
			LAYERTWO.add("6. Produktionsauftrags-Etiketten");
			LAYERTWO.add("7. Etiketten für Stempelaufträge");
			LAYERTWO.add("8. Etiketten für Kontonummernkarten");
			LAYERTWO.add("9. Adress-Etiketten mit Kostenstelle");
			LAYERTWO.add("A. Adress-Etiketten");
			LAYERTWO.add("B. Sonstige");
			LAYERTWO.add("C. Ablage Lieferanten");
			LAYERTWO.add("D. Kommissionieretiketten");
			LAYERTWO.add("E. Schmidtbank-Formulare");
		}
		
		/** die Abschlussseiten */
		private static final Set<String> ENDPAGES = new HashSet<>();	
		static {
			ENDPAGES.add("Z. Programm beenden");
		}
		
		/**
		 * Liste mit erwarteten Menüpunkten,
		 */
		public static final List<Set<String>> LIST = new LinkedList<>();
		
		static {
			LIST.add(OWN_STARTPAGES);
			LIST.add(LAYERTWO);
		}
		
		public FilterMenuNeunListen() {
		}

		@Override
		public Set<String> getFilterLevel(int level) {
			return LIST.get(level);
		}

		@Override
		public Set<String> getEndpages() {
			return ENDPAGES;
		}

		@Override
		public boolean isRegexMode() {
			return false;
		}
	}
	
	public static class FilterKuhDreiArena implements IMenuFilter {
		
		public static final Set<String> OWN_STARTPAGES = new HashSet<>();	
		static {
			OWN_STARTPAGES.add("Q\\. Nur f.+r 18");
		}
		
		/** die Abschlussseiten */
		private static final Set<String> LAYERTWO = new HashSet<>();	
		static {
			LAYERTWO.add("3. Listen");
		}
		
		/** die Abschlussseiten */
		private static final Set<String> ENDPAGES = new HashSet<>();	
		static {
			ENDPAGES.add("Z. Programm beenden");
		}
		
		/**
		 * Liste mit erwarteten Menüpunkten,
		 */
		public static final List<Set<String>> LIST = new LinkedList<>();
		
		static {
			LIST.add(OWN_STARTPAGES);
			LIST.add(LAYERTWO);
		}
		
		public FilterKuhDreiArena() {
		}
		
		@Override
		public Set<String> getFilterLevel(int level) {
			return LIST.get(level);
		}
		
		@Override
		public Set<String> getEndpages() {
			return ENDPAGES;
		}

		@Override
		public boolean isRegexMode() {
			return true;
		}
	}
	
	interface IMenuFilter {
		
		/**
		 * Liefert die erwarteten Menüpunkte in der übergebenden Tiefe-1.
		 * @param level die gesuchte Tiefe
		 * @return
		 */
		public abstract Set<String> getFilterLevel(int level);
		
		public abstract Set<String> getEndpages();
		
		public abstract boolean isRegexMode();
	}
}
