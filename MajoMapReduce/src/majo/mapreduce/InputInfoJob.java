package majo.mapreduce;

import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import majo.mapreduce.Main.IMenuFilter;
import majo.mapreduce.UserSessionJob.COUNTER;

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
	
	/** Benutzerfilter */
	public static final String FILTER_USER = null;
	
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
			final Configuration conf = context.getConfiguration();
			final String clsString = conf.get(Main.MENULOG_FILTER_MENUE);
			Object clss;
			try {
				clss = Class.forName(clsString).newInstance();
				final IMenuFilter filter = (IMenuFilter) clss;
				final Set<String> filterMenue = filter.getFilterLevel(0);
				final Set<String> filterSecond = filter.getFilterLevel(1);
				final MenulogLine line = new MenulogLine(value.toString());
				final String prg = line.getCleanProgram();
				final String user = line.getCleanUser();
				final String menue = line.getCleanValue();
				if (acceptProgram(prg)) {
//					if (acceptMenu(menue, filterMenue, filterSecond)) {
						// Benutzer
						word.set("USER[" + user + "]");
						context.write(word, one);
						// Auswahl
						word.set("VALUE[" + prg + "~" + menue + "]");
						context.write(word, one);
//					} else {
//						context.getCounter(COUNTER.OTHER_MENUE_LINE).increment(1);					
//					}
				} else {
					context.getCounter(COUNTER.OTHER_PROGRAM_LINE).increment(1);					
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
				ex.printStackTrace();
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
		 * Liefert <code>true</code> wenn {@code menu} einem gesuchten Menüpunkt
		 * entspricht.
		 * @param menu der Benutzer
		 * @param filterUser der gesuchte Benutzer
		 * @return <code>true</code> wenn gesucht
		 */
		public boolean acceptMenu(final String menu, final Set<String> filterMenue, final Set<String> filterSecond) {
			boolean match = false;
			if (null != filterMenue) {
				if (null != menu && null != filterMenue) {
					if (null != filterSecond) {
						match = filterMenue.contains(menu);
					} else {
						match = filterMenue.contains(menu) || filterSecond.contains(menu);
					}
				}
			} else {
				match = true;
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