package graphics;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

public class InformationCreator {

	private final Set<SessionLine> sessions = new LinkedHashSet<>();
	
	private final Set<InfoLine> infos = new LinkedHashSet<>();
	
	private final Set<Menu> sizes = new LinkedHashSet<>();
	
	private final Set<Edge> weights = new LinkedHashSet<>();
	
	public InformationCreator() {
	}
	
	public void digest(final String filename) {
		if (null != filename && filename.contains("user_session.txt")) {
			sessions.addAll(createSessionLines(filename));
			calculateEdgeweight();
		} else {
			infos.addAll(createInfoLines(filename));
			calculateSizes();
		}
		
		
	}
	
	private void calculateSizes() {
		long sum = 0;
		for (final InfoLine il: infos) {
			if (!il.isWorkerLine()) {
				sum = sum + il.getCount();
			}
		}
		if (0 == sum) {
			sum = 1;
		}
		for (final InfoLine il: infos) {
			if (!il.isWorkerLine()) {
				final BigDecimal count = new BigDecimal(il.getCount());
				final BigDecimal bdsum = new BigDecimal(sum);
				final BigDecimal bd100 = new BigDecimal(100);
				final BigDecimal percentage = count.divide(bdsum.divide(bd100, 5, BigDecimal.ROUND_HALF_UP),  5, BigDecimal.ROUND_HALF_UP);
				final Menu m = new Menu(il.getMenu());
				if (1 > percentage.doubleValue()) {
					System.out.println("");
				}
				m.setSize(percentage);
				sizes.add(m);
			}
		}
	}
	
	private void calculateEdgeweight() {
		double sum = 0D;
		for (final Edge e: getSessionEdges()) {
			sum = sum + e.getThickness();
		}
		if (0D == sum) {
			sum = 1D;
		}
		for (final Edge e: getSessionEdges()) {
			final Float d = e.getThickness();
			final double percantage = d / (sum / 100);
			e.setThickness((float) percantage);
			weights.add(e);
		}
	}
	
	public Set<Menu> getSizes() {
		return sizes;
	}
	
	public Set<Edge> getWeightedEdges() {
		return weights;
	}

	public Set<SessionLine> getSessions() {
		return sessions;
	}
	
	public Set<InfoLine> getInfos() {
		return infos;
	}
	
	
	public Set<Edge> getSessionEdges() {
		final Set<Edge> x = new LinkedHashSet<>();
		for(final SessionLine sl: sessions) {
			final List<Edge> edges = sl.getEdges();
			for (final Edge e: edges) {
				final float f = e.getThickness();
				e.setThickness(f + 1);
				x.add(e);
			}
		}
		return x;
	}

	private Set<InfoLine> createInfoLines(final String filename) {
		final Set<InfoLine> x = new LinkedHashSet<>();
		 FileReader fr;
	        BufferedReader br;
	        try {
	            fr = new FileReader(filename);
	            br = new BufferedReader(fr);

	            String line = null;
	            line = br.readLine();
	            einlesen: while (line != null) {
	            	final InfoLine il = new InfoLine(line);
	            	x.add(il);
	            	line = br.readLine();
	            }
	            fr.close();
	        } catch (final IOException e){
	            System.out.println("Fehler beim Lesen der Datei " + filename);
	            System.out.println(e.toString());
	        }
		return x;
	}
	
	private Set<SessionLine> createSessionLines(final String filename) {
		final Set<SessionLine> x = new LinkedHashSet<>();
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);

            int count = 0;
            String line = null;
            line = br.readLine();
            einlesen: while (line != null) {
            	final SessionLine sl = new SessionLine(line);
            	x.add(sl);
            	line = br.readLine();
            	count++;
            	if(count > 1000) {
            		break einlesen;
            	}
            }
            fr.close();
        } catch (final IOException e){
            System.out.println("Fehler beim Lesen der Datei " + filename);
            System.out.println(e.toString());
        }
        return x;
	}
	
	/**
	 * Spiegelt die Ebenen des B+R Systems wieder um die Struktur
	 * lesbarer aufzubauen.
	 */
	public static enum EBENEN {
		EBENE1{
			
		},
		EBENE2{
			
		};
		
	}
	/**
	 * Ein Menüpunkt, mit anzahl an Aufrufen und Ebene 
	 *
	 */
	protected static class Menu {

		/** die Startseiten */
		private static final Set<String> STARTS = new HashSet<>();	
		static {
			STARTS.add("1 Aufträge");
			STARTS.add("2 Auskunft");
			STARTS.add("3 Rechnungen");
			STARTS.add("4 Bestellungen");
			STARTS.add("5 Belastungen");
			STARTS.add("6 Gutschriften / Abholscheine");
			STARTS.add("7 Artikel-Verwaltung");
			STARTS.add("8 Textverarbeitung");
			STARTS.add("9 Listen");
			STARTS.add("A Etiketten / Schilder / Belege");
			STARTS.add("D Postrechnungen drucken");
			STARTS.add("E Sonderpreise");
			STARTS.add("F Wareneingang");
			STARTS.add("G Rechnungseingang");
			STARTS.add("H Dienst-Programme");
			STARTS.add("I Gutschrift / Neue Rechnung");
			STARTS.add("K Artikelnummern der Kunden");
			STARTS.add("L Ware abholen (sofort Rechnung)");
			STARTS.add("M Fremdbelege erfassen");
			STARTS.add("N Zusätzliche Pack-Nummern drucken");
			STARTS.add("O Tourenplanung");
			STARTS.add("P Empfangsscheine Scannen");
			STARTS.add("Q Nur für 18");
			STARTS.add("R Belegerfassung");
			STARTS.add("S Anfragen");
			STARTS.add("U Vorgänge");
			STARTS.add("V Fremprogramme");
			STARTS.add("W Nachlieferung");
		}
		
		
		private final String name;
		private BigDecimal size = BigDecimal.ONE;
		public Menu(final String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setSize(final BigDecimal size) {
			this.size  = size;
		}
		
		@Override
		public String toString() {
			return name;
		}

		public BigDecimal getSize() {
			return size;
		}
		
		public boolean isStartpage() {
			return STARTS.contains(name);
		}
		
		// Für Ebenen
//		public int getStage() {
//			return
//		}
		
	}
	
	protected static class Edge {
		
		private final List<Menu> connectedVertex = new LinkedList<>();
		
		private float thickness = 1;

		protected Edge() {
		}
		
		public void add(final Menu s) {
			connectedVertex.add(s);
		}

		public List<Menu> getConnectedVertecis() {
			return connectedVertex;
		}
		
		public void setThickness(final float thickness) {
			this.thickness  = thickness;
		}
		
		public float getThickness() {
			return thickness;
		}

		public String getName() {
			final StringBuffer x = new StringBuffer();
			for (final Menu s :connectedVertex) {
				if (0 == x.length()) {
					x.append(s);
				} else {
					x.append(" --> ");
					x.append(s);
				}
			}
			return x.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 1009;
			final int hashCode = prime * getName().hashCode();
			return hashCode;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			return this.hashCode() == obj.hashCode();
		}
	}

	protected static class ConnectorInformation {
		
	}

	public Transformer<Menu, Shape> createVertexSizeTransformer() {
		final Transformer<Menu,Shape> vertexSize = new Transformer<Menu, Shape>(){
            @Override
			public Shape transform(final Menu m){
                final Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
                // in this case, the vertex is twice as large
    			final BigDecimal percentage = m.getSize();
    			System.out.println(percentage);
        		return AffineTransform.getScaleInstance(percentage.floatValue(), percentage.floatValue()).createTransformedShape(circle);
            }
        };
        return vertexSize;
	}
}
