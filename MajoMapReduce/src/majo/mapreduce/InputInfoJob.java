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

	/** der Programmfilter */
	public static final String FILTER_PRG = 
			"(GH)|(KND_BUHA)|(LIEF_BUHA)";
	
	/**
	 * Interpretiert eine MenulogLine und weist jeweils eine Benutzerzeile
	 * zu einer Eins sowie einer Kombination aus 
	 * Programm und Menüaufruf eine Zeile mit einer 1 zu. (Wordcount)
	 * 
	 * <p>
	 * (Benutzer, 1)
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
			final String prg = line.getCleanProgram();
			if (acceptProgram(prg)) {
				// Benutzer
				word.set("USER[" + line.getCleanUser() + "]");
				context.write(word, one);
				// Auswahl
				word.set("VALUE[" + prg + "~" + line.getCleanValue() + "]");
				context.write(word, one);
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