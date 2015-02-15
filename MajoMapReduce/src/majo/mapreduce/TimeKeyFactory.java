package majo.mapreduce;

/**
 * Erstellt Zeitschlüssel in festen Zyklen. 
 * <p>
 * 10.000                15.000
 * |----------|----------|
 * Zyklus.:  5.000
 * Start..: 10.000
 * 
 * 
 * @author majo
 *
 */
public class TimeKeyFactory {
	
	/** Konstante für kleinste Zeiteinheit */
	public static final long MIN = 5000L;

	/**
	 * Startet eine Demo.
	 * @param args wird nicht verwendet
	 * @throws InterruptedException 
	 */
	public static void main(final String[] args) throws InterruptedException {
		for (int idx=0; idx<15; idx++) {
			final TimeKey x = createNew();
			System.out.println(String.format("[%03d] %s", idx, x));
			Thread.sleep(1000);
		}
	}

	public static TimeKey createNew() {
		final TimeKey x = new TimeKey();
		return x;
	}
	
	/**
	 * Geschützter Konstruktor. Statische Methoden verwenden!
	 */
	private TimeKeyFactory() {
	}
	
	/**
	 * Beschreibt einen Schlüssel.
	 */
	public static class TimeKey {
		
		/** Zeitpunkt der Erstellung */
		private final long created;
		
		/** Basiswert vom Erstellungszeitpunkt */
		private final long base;

		/** Abweichung vom Basiswert */
		private final double rest;
		
		/** der Schlüsselwert */
		private final long value;
		
		/**
		 * Geschützter Konstruktor. Statische Methoden von 
		 * {@link TimeKeyFactory} nutzen.
		 */
		public TimeKey() {
			this.created = System.currentTimeMillis();
			// 5000 --> 4
			final double fix = Math.ceil(Math.log10(MIN));
			// 10^4 --> 10.000
			final double pow = Math.pow(10, fix); 
			// 1.423.961.471.118 --> 1.423.961.470.000
			base = (long) (((long) (created/pow)) * pow);
			rest = (created - base) / pow;
			value = (long) (base + (MIN * Math.floor(rest)));
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(TimeKey.class.getName()).append(" [");
			sb.append("created = ").append(created).append(", ");
			sb.append("base = ").append(base).append(", ");
			sb.append("rest = ").append(rest).append(", ");
			sb.append("value = ").append(value);
			sb.append("]");
			return sb.toString();
		}
		
	}

}
