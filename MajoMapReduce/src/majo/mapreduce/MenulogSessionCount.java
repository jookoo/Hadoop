package majo.mapreduce;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MenulogSessionCount {
	
	public static enum COUNTER {
		  OTHER_PROGRAM_LINE
	};

	public static class UserValueMapper 
	extends Mapper<Object, Text, Text, SessionValues> {

		public static final String FILTER_PRG = ".*\\\\AU(FTRAG)?(_BLI)?(WIN[\\d]+)?\\.EXE$";
		
		@Override
		public void map(
				final Object key, final Text value, final Context context) 
						throws IOException, InterruptedException {
			final MenulogLine line = new MenulogLine(value.toString());
			// Benutzer
			final String prg = line.getProgram().getName();
			if (acceptProgram(prg)) {
				final String username = line.getUser();
				final SessionValues sessionValues = new SessionValues();
				sessionValues.put(
						new LongWritable(line.getDateTime().getTimeInMillis()),
						new Text(line.getValue()));
				context.write(new Text(username), sessionValues);
			} else {
				context.getCounter(COUNTER.OTHER_PROGRAM_LINE).increment(1);
			}
		}
		
		public boolean acceptProgram(final String prg) {
			boolean match = false;
			if (null != prg) {
				match = prg.toUpperCase().matches(FILTER_PRG);
			}
			return match;
		}
		
	}

	public static class SessionReducer 
	extends Reducer<Text, SessionValues, Text, UserSession> {
	
		@Override
		public void reduce(final Text key, final Iterable<SessionValues> values,
				final Context context) throws IOException, InterruptedException {
			final Set<UserSession> sessions = new LinkedHashSet<>();
			UserSession lastUserSession = null;
			// alle Menüeinträge von Benutzer XYZ
			for (SessionValues x: values) {
				for (Writable time: x.keySet()) {
					final long timeLong = ((LongWritable)time).get();
					final String menue = x.get(time).toString();
					if (null == lastUserSession || (!lastUserSession.add(timeLong, menue))) {
						lastUserSession = new UserSession(key.toString(), timeLong, menue);		
						sessions.add(lastUserSession);
					} else {
						lastUserSession.add(timeLong, menue);
					}
				}
			}			
			for (UserSession x: sessions) {
				final StringBuffer sb = new StringBuffer(x.getUser());
				sb.append("[").append(x.getStartTime()).append("]");
				context.write(new Text(sb.toString()), x);
			}
		}
		
	}
	
	public static void main(final String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "session count");
		job.setJarByClass(MenulogSessionCount.class);
		job.setMapperClass(UserValueMapper.class);
//		job.setCombinerClass(SessionReducer.class);
		job.setReducerClass(SessionReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(UserSession.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(SessionValues.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	public static class SessionValues extends MapWritable {
		
		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer();
			Set<Writable> keys = this.keySet();
			for (Writable i: keys) {
				sb.append("{");
				sb.append(i.toString());
				sb.append(":");
				sb.append(this.get(i).toString());
				sb.append("}");
			}
			return sb.toString();
		}
		
	}

}