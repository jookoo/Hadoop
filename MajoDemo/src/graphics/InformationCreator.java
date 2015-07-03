package graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Verarbeitet die Ergebnis-Dateien aus den MapReduce-Jobs die zur 
 * Auswertung der Menülog-Dateien entwickelt wurden
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InformationCreator {

	/** die ermittelten Infos */
	private final Set<InfoLine> infos = new LinkedHashSet<>();

	/** die kumulierten Menüpunkte */
	private final Set<Menu> sizes = new LinkedHashSet<>();

	/** die kumulierten Kanten */
	private final Set<Edge> weightedEdges = new LinkedHashSet<>();

	/**
	 * ein Konstruktor
	 */
	public InformationCreator() {
	}

	/**
	 * Verarbeitet die Ergebnisdateien.
	 * @param filename der Pfad zur Datei
	 */
	public void digest(final String filename) {
		if (null != filename && filename.contains("user_session.txt")) {
			final Set<SessionLine> sessions = createSessionLines(filename);
			calculateEdgeweight(sessions);
			calculateSizes(sessions);
		} else {
			infos.addAll(createInfoLines(filename));
		}
	}

	/**
	 * Kumuliert und berechnet die Menüs.
	 * @param sessions die ermittelten Sessions.
	 */
	private void calculateSizes(final Set<SessionLine> sessions) {
		final Set<Menu> sessionMenu = createSessionMenues(sessions);
		double sum = 0D;
		for (final Menu e: sessionMenu) {
			sum = sum + e.getWeight();
		}
		if (0D == sum) {
			sum = 1D;
		}
		for (final Menu m: sessionMenu) {
			final BigDecimal count = new BigDecimal(m.getWeight());
			final BigDecimal bdsum = new BigDecimal(sum);
			final BigDecimal bd100 = new BigDecimal(100);
			final BigDecimal percentage = 
					count.divide(bdsum.divide(bd100, 5, BigDecimal.ROUND_HALF_UP),  5, BigDecimal.ROUND_HALF_UP);
			m.setSize(percentage.floatValue());
			sizes.add(m);
		}
	}

	/**
	 * Kumuliert und berechnet die Kanten.
	 * @param sessions die ermittelten Sessions
	 */
	private void calculateEdgeweight(final Set<SessionLine> sessions) {
		final Set<Entry<String, Edge>> x = createSessionEdges(sessions).entrySet();
		double sum = 0D;
		for (final Entry<String, Edge> entry: x) {
			final Edge e = entry.getValue();
			sum = sum + e.getWeight();
		}
		if (0D == sum) {
			sum = 1D;
		}
		for (final Entry<String, Edge> entry: x) {
			final Edge e = entry.getValue();
			final int d = e.getWeight();
			final double percantage = d / (sum / 100);
			e.setThickness((float) percantage);
			weightedEdges.add(e);
		}
	}

	/**
	 * Liefert die kumulierten und gewichteten Menüpunkte.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Set<Menu> getSizedMenues() {
		return sizes;
	}

	/**
	 * Liefert die kumulierten und gewichteten Kanten.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Set<Edge> getWeightedEdges() {
		return weightedEdges;
	}

	/**
	 * Liefert die ermittelten {@link InfoLine}
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Set<InfoLine> getInfos() {
		return infos;
	}

	/**
	 * Erzeugt kumulierte Kanten und fügt den Kanten-Instanzen entsprechend
	 * der Häufigkeit mehr Gewicht hinzu.
	 * @param sessions die ermittelten Sessions
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Map<String, Edge> createSessionEdges(final Set<SessionLine> sessions) {
		final Map<String, Edge> x = new LinkedHashMap<>();
		for(final SessionLine sl: sessions) {
			final Set<Edge> edges = sl.getEdge();
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

	/**
	 * Erzeugt kumulierte Menüpunkte und fügt den Menu-Instanzen entsprechend
	 * der Häufigkeit mehr Gewicht hinzu.
	 * @param sessions die ermittelten Sessions
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Set<Menu> createSessionMenues(final Set<SessionLine> sessions) {
		final Set<Menu> x = new LinkedHashSet<>();
		for(final SessionLine sl: sessions) {
			final Set<Menu> menues = sl.getMenu();
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

	/**
	 * Liest die Ergebnisdatei vom Input-Info-Job ein und erzeugt pro Zeile
	 * eine {@link InfoLine}.
	 * @param filename der Dateipfad
	 * @return ein Objekt, niemals <code>null</code>
	 */
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

	/**
	 * Liest die Ergebnisdatei vom User-Session-Job ein und erzeugt pro Zeile
	 * eine {@link SessionLine}. 
	 * @param filename der Dateipfad
	 * @return ein Objekt, niemals <code>null</code>
	 */
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
	 * Ein Menüpunkt, mit Anzahl an Aufrufen und Ebene 
	 */
	public static class Menu implements Comparable<Menu>{

		/** der Name */
		private final String name;
		
		/** der Elternknoten */
		private final String parentNode;
		
		/** die prozentuale Göße */
		private float size = 1;
		
		/** die absolute Größe */
		private int weight = 0; 

		/**
		 * ein geschützter Konstruktor
		 * @param name der Name des Menüpunkts
		 * @param parentNode der Eltern-Knoten
		 */
		public Menu(final String name, final String parentNode) {
			this.name = name;
			this.parentNode = parentNode;
		}

		/**
		 * Liefert die absolute Aufrufhäufigkeit des Knotens
		 * @return die absolute Aufrufhäufigkeit des Knotens
		 */
		public double getWeight() {
			return weight;
		}

		/**
		 * Fügt dem Knoten-Gewichtszähler 1 hinzu.
		 */
		public void addWeight() {
			weight++;
		}

		/**
		 * Liefert den Namen des Knotens
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public String getName() {
			return name;
		}

		/**
		 * Setzt die aktuelle prozentuale Größe.
		 * @param size eine Größe
		 */
		public void setSize(final float size) {
			this.size  = size;
		}

		/**
		 * Liefert die gestufte Größe
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public Weights getStagedSize() {
			final Weights w = Weights.find(size);
			return w;
		}

		/**
		 * Liefert die prozentuale Größe bezogen auf alle vorhandenen Menüpunkte.
		 * @return die prozentuale Größe
		 */
		public float getSize() {
			return size;
		}

		/**
		 * Liefert den Eltern-Knoten
		 * @return ein Objekt oder <code>null</code>
		 */
		public String getParentNode() {
			return parentNode;
		}

		/**
		 * Liefert ob es sich um eine Startpage handelt.
		 * @return <code>true</code> wenn Startpage
		 */
		public boolean isStartpage() {
			return null == parentNode;
		}

		@Override
		public String toString() {
			return name+(null == parentNode ? "":" "+ parentNode);
		}
		
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

		@Override
		public int compareTo(final Menu o) {
			System.out.println(name + " other name "+ o.name);
			int x = name.compareTo(o.name);
			if (x == 0) {
				if (null == parentNode && null != o.parentNode) {
					x = 1;
				} else if (null != parentNode && null == o.parentNode) {
					x = -1;
				} else if (null == parentNode && null == o.parentNode) {
					x = 0;
				} else {
					x = parentNode.compareTo(parentNode);
				}
			}
			return x;
		}
	}

	/**
	 * Repräsentiert eine Kante zwischen zwei Menüpunkten.
	 * <p>
	 * Die Dicke entscheidet über die Häufigkeit der Nutzung
	 *
	 */
	public static class Edge {

		/** die verbundenen Menüpunkte */
		private final List<Menu> connectedVertex = new LinkedList<>();

		/** Gewicht der Kante, bestimmt durch Häufigkeit der Nutzung */
		private int weight = 0;

		/** Prozentuale Größe gegenüber allen bekannten Kanten */
		private float thickness = 0f;

		/**
		 * ein geschützter Konstruktor
		 */
		protected Edge() {
		}

		/**
		 * Liefert die absolute Anzahl von Nutzungen dieser Kante.
		 * @return die absolute Anzahl von Nutzungen dieser Kante
		 */
		public int getWeight() {
			return weight;
		}

		/**
		 * Fügt dem Zähle für das gewicht der Kante +1 hinzu.
		 */
		public void addWeight() {
			weight++;
		}

		/**
		 * Fügt der Kante einen Verbundenen Vertex hinzu
		 * @param s ein Verbindungspunkt
		 */
		public void add(final Menu s) {
			connectedVertex.add(s);
		}

		/**
		 * Liefert die verbundenen Knoten
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public List<Menu> getConnectedVertecis() {
			return connectedVertex;
		}

		/**
		 * Setzt die prozentuale Größe der Kante.
		 * @param thickness die prozentuale Größe
		 */
		public void setThickness(final float thickness) {
			this.thickness  = thickness;
		}

		/** 
		 * Liefert die prozentuale Größe der Kante.
		 * @return die prozentuale Größe der Kante
		 */
		public float getThickness() {
			return thickness;
		}

		/**
		 * Liefert den Ursprung der Kante.
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public final Menu getFrom() {
			return connectedVertex.get(0);
		}

		/**
		 * Liefert das Ziel der Kante.
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public final Menu getTo() {
			return connectedVertex.get(1);
		}

		/**
		 * Liefert den Namen der Kante.
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public String getName() {
			final StringBuffer x = new StringBuffer();
			x.append(getFrom().getName());
			x.append(" --> ");
			x.append(getTo().getName());
			return x.toString();
		}

		@Override
		public String toString() {
			return getName();
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

	/**
	 * Staffelung der Größen für Menüpunkt-Nutzungen.
	 */
	public enum Weights {
		// Kaum genutzte Menüpunkte
		HARDLY_USED {
			@Override
			public float getSize() {
				return 0.3f;
			}
		},
		// Wenig genutzte Menüpunkte
		SMALL {
			@Override
			public float getSize() {
				return 0.8f;
			}
		},
		// Durchschnittlich genutzte Menüpunkte
		MEDIUM {
			@Override
			public float getSize() {
				return 2f;
			}
		},
		// Überdurchschnittlich genutzte Menüpunkte
		BIG {
			@Override
			public float getSize() {
				return 4f;
			}
		};

		/**
		 * Liefert die ermittelte Größenstaffel.
		 * @return die gestaffelte Größe
		 */
		public float getSize() {
			return 0f;
		}

		/**
		 * Ermittelt zur prozentualen Größe des Elements die Staffel-Größe.
		 * @param percentage ein prozentualer Wert
		 * @return ein Objekt, niemals <code>null</code>
		 */
		public static Weights find(final float f) {
			final Weights x;
			if (1 > f) {
				x = HARDLY_USED;
			} else if (2 > f ) {
				x = SMALL;
			} else if (3 > f) {
				x = MEDIUM;
			} else {
				x = BIG;
			}
			return x;
		}
	}
}
