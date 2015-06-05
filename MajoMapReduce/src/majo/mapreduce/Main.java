package majo.mapreduce;

import java.io.IOException;

import majo.mapreduce.InputInfoJob.IntSumReducer;
import majo.mapreduce.InputInfoJob.TokenizerMapper;
import majo.mapreduce.UserSessionJob.SessionReducer;
import majo.mapreduce.UserSessionJob.UserValueMapper;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Konfiguration und Starter für die gesamte Aufgabenstellung.
 * 
 * @author majo
 *
 */
public class Main {

	/** Konstante für Benutzerfilter-Schlüssel */
	public static final String MENULOG_FILTER_USERNAME = "menulog.filter.username";
	
	/** Konstante für Wartezeit-In-Minuten-Schlüssel */
	public static final String MENULOG_MINUTES_MAX = "menulog.minutes.max";

	public static void main(final String[] args) throws Exception {	
		// Konfiguration
		final Configuration conf = new Configuration();
//		conf.set(MENULOG_FILTER_USERNAME, "22");
//		conf.setInt(MENULOG_MINUTES_MAX, 30);

		int code = 0;
		
		// Job 1 anlegen/ausführen
		final Job inputInfoJob = createInputInfoJob(conf);
		code += (inputInfoJob.waitForCompletion(true) ? 0 : 1);
		
		// Job 2 anlegen/ausführen
		final Job userSessionJob = createUserSessionJob(conf);
		code += (userSessionJob.waitForCompletion(true) ? 0 : 1);
		
		// Ausführung abwarten
		System.exit(code);
	}
	
	/**
	 * Geschützter Konstruktor. Framework startet {@link Main#main(String[])}.
	 */
	private Main() {
	}

	private static Job createInputInfoJob(
			final Configuration conf) throws IOException {
		final Job job = Job.getInstance(conf, InputInfoJob.class.getSimpleName());
		job.setJarByClass(InputInfoJob.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		// Input- und Output-Pfad
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));
		FileOutputFormat.setOutputPath(job, new Path("/input_info"));
		
		return job;
	}
	
	private static Job createUserSessionJob(
			final Configuration conf) throws IOException {
		final Job job = Job.getInstance(conf, 
				UserSessionJob.class.getSimpleName());
		job.setJarByClass(UserSessionJob.class);
		
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
		FileInputFormat.addInputPath(job, new Path("/input/*.CSV"));
		FileOutputFormat.setOutputPath(job, new Path("/user_session"));
		
		return job;
	}
	
}
