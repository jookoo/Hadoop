package majo.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Liefert grundlegende Informationen zu den Input-Dateien.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InputInfoJob {

	public static class TokenizerMapper 
	extends Mapper<Object, Text, Text, IntWritable> {

		/** der Schlüssel */
		private Text word = new Text();
		
		/** der Wert pro Schlüssel */
		private final static IntWritable one = new IntWritable(1);
		
		@Override
		public void map(
				final Object key, final Text value, final Context context) 
						throws IOException, InterruptedException {
			final MenulogLine line = new MenulogLine(value.toString());
			// Benutzer
			word.set("USER[" + line.getUser() + "]");
			context.write(word, one);
			// Auswahl
			word.set("VALUE[" + line.getCleanProgram() + "~" + line.getCleanValue() + "]");
			context.write(word, one);
		}
		
	}

	public static class IntSumReducer 
	extends Reducer<Text, IntWritable, Text, IntWritable> {
	
		/** die Summe aller Vorkommen */
		private IntWritable result = new IntWritable();

		@Override
		public void reduce(final Text key, final Iterable<IntWritable> values,
				final Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (final IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
		
	}

}