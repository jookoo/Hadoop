package graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class InformationCreator {

	private final Set<SessionLine> sessions = new LinkedHashSet<>();
	
	private final Set<InfoLine> infos = new LinkedHashSet<>();
	
	private final Set<Menu> sizes = new HashSet<>();
	
	private final Set<Edge> weightedEdges = new LinkedHashSet<>();
	
	public InformationCreator() {
	}
	
	public void digest(final String filename) {
		if (null != filename && filename.contains("user_session.txt")) {
			sessions.addAll(createSessionLines(filename));
			calculateEdgeweight();
			calculateSizes();
		} else {
			infos.addAll(createInfoLines(filename));
		}
		
		
	}
	
	private void calculateSizes() {
		double sum = 0D;
		for (final Menu e: getSessionMenues()) {
			sum = sum + e.getWeight();
		}
		if (0D == sum) {
			sum = 1D;
		}
		for (final Menu m: getSessionMenues()) {
			final BigDecimal count = new BigDecimal(m.getWeight());
			final BigDecimal bdsum = new BigDecimal(sum);
			final BigDecimal bd100 = new BigDecimal(100);
			final BigDecimal percentage = 
					count.divide(bdsum.divide(bd100, 5, BigDecimal.ROUND_HALF_UP),  5, BigDecimal.ROUND_HALF_UP);
			m.setSize(percentage);
			sizes.add(m);
		}
	}
	
	private void calculateEdgeweight() {
		double sum = 0D;
		for (final Entry<String, Edge> entry: getSessionEdges().entrySet()) {
			final Edge e = entry.getValue();
			sum = sum + e.getWeight();
		}
		if (0D == sum) {
			sum = 1D;
		}
		for (final Entry<String, Edge> entry: getSessionEdges().entrySet()) {
			final Edge e = entry.getValue();
			final int d = e.getWeight();
			final double percantage = d / (sum / 100);
			e.setThickness((float) percantage);
			weightedEdges.add(e);
		}
	}
	
	public Set<Menu> getSizedMenues() {
		return sizes;
	}
	
	public Set<Edge> getWeightedEdges() {
		return weightedEdges;
	}

	public Set<SessionLine> getSessions() {
		return sessions;
	}
	
	public Set<InfoLine> getInfos() {
		return infos;
	}
	
	
	public Map<String, Edge> getSessionEdges() {
		final Map<String, Edge> x = new HashMap<>();
		for(final SessionLine sl: sessions) {
			final List<Edge> edges = sl.getEdges();
			for (final Edge tmp: edges) {
				final String name = tmp.getName();
				if (!x.containsKey(name)) {
					x.put(name, tmp);
				}
				final Edge e = x.get(name);
				e.addWeight();
			}
		}
		return x;
	}
	
	public Set<Menu> getSessionMenues() {
		final Set<Menu> x = new HashSet<>();
		for(final SessionLine sl: sessions) {
			final Set<Menu> menues = sl.getMenus();
			for (final Menu tmp: menues) {
				if (!x.contains(tmp)) {
					x.add(tmp);
				}
				addweight:for (final Menu m: x) {
					if (m.equals(tmp)) {
						m.addWeight();
						break addweight;
					}
				}
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
            	if(count > 10000) {
            		break einlesen;
            	}
            	count++;
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
	 * Ein Menüpunkt, mit Anzahl an Aufrufen und Ebene 
	 *
	 */
	protected static class Menu {

		private final String name;
		private final String parentNode;
		private BigDecimal size = BigDecimal.ONE;
		private int weight = 1; 
		
		
		public Menu(final String name, final String parentNode) {
			this.name = name;
			this.parentNode = parentNode;
		}
		
		public double getWeight() {
			return weight;
		}

		public void addWeight() {
			weight++;
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
		
		public String getParentNode() {
			return parentNode;
		}
		
		public boolean isStartpage() {
			return null == parentNode;
		}
		
		// Für Ebenen
//		public int getStage() {
//			return
//		}
		
		@Override
		public int hashCode() {
			final int prime = 1013;
			int hashCode = prime * getName().hashCode();
			hashCode = hashCode + (null == getParentNode() ? 1039 :getParentNode().hashCode());
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
	
	/**
	 * Repräsentiert eine Flanke zwischen zwei Menüpunkten.
	 *
	 * Die Dicke entscheidet über die Häufigkeit der Nutzung
	 *
	 */
	protected static class Edge {
		
		private final List<Menu> connectedVertex = new LinkedList<>();
		
		/** Gewicht der Kante, bestimmt durch Häufigkeit der Nutzung */
		private int weight = 1;
		
		/** Prozentuale Größe gegenüber allen bekannten Kanten */
		private float thickness = 0f;

		protected Edge() {
		}
		
		public int getWeight() {
			return weight;
		}

		public void addWeight() {
			weight++;
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

		public final Menu getFrom() {
			return connectedVertex.get(0);
		}
		
		public final Menu getTo() {
			return connectedVertex.get(1);
		}
		
		public String getName() {
			final StringBuffer x = new StringBuffer();
			x.append(getFrom().getName());
			x.append(" --> ");
			x.append(getTo().getName());
			return x.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 1063;
			int hashCode = prime * getName().hashCode();
			hashCode = hashCode + getFrom().hashCode();
			hashCode = hashCode + getTo().hashCode();
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

}
