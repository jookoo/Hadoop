package majo.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Liefert grundlegende Informationen zu den Input-Dateien.
 * 
 * <p>
 * Der Job diente in erster Linie für einen ersten Überblick auf die 
 * Daten.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InputInfoJob {

	/**
	 * Interpretiert eine MenulogLine und weist jeweils eine Benutzerzeile
	 * zu einer Eins sowie einer Kombination aus 
	 * Programm und Menüaufruf eine Zeile mit einer 1 zu. (Wordcount)
	 * 
	 * <p>
	 * (Benuter, 1)
	 * (Programm~Menü, 1)
	 * 
	 */
	public static class TokenizerMapper 
	extends Mapper<Object, Text, Text, IntWritable> {

		/** der Schlüssel */
		private final Text word = new Text();

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

	/**
	 *  Fasst die Vorkommnisse der einzelnen Schlüssel zusammen.
	 *  
	 *  <p>
	 *  (Ben1, 1);(Ben1, 1);(Ben1, 1) ==> (Ben1, 3)
	 */
	public static class IntSumReducer 
	extends Reducer<Text, IntWritable, Text, IntWritable> {

		/** die Summe aller Vorkommen */
		private final IntWritable result = new IntWritable();

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