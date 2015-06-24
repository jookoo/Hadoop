package graphics;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
				final double percentage = sum / 100 * il.getCount();
				final Menu m = new Menu(il.getMenu());
				m.setSize((float) percentage);
				sizes.add(m);
			}
		}
	}
	
	private void calculateEdgeweight() {
		double sum = 0D;
		for (final Edge e: getSessionEdges()) {
			if (1 < e.getThickness()) {
				System.out.println("size" + e.getThickness());
			}
			sum = sum + e.getThickness();
		}
		if (0D == sum) {
			sum = 1D;
		}
		for (final Edge e: getSessionEdges()) {
			if (1 < e.getThickness()) {
				System.out.println("");
			}
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
			
		}
	}
	/**
	 * Ein Menüpunkt, mit anzahl an Aufrufen und Ebene 
	 *
	 */
	protected static class Menu {

		private final String name;
		private float size = 1;
		public Menu(final String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setSize(final float size) {
			this.size  = size;
		}
		
		@Override
		public String toString() {
			return name;
		}

		public float getSize() {
			return size;
		}
		
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

	public Transformer<String, Shape> createVertexSizeTransformer() {
		final Transformer<String,Shape> vertexSize = new Transformer<String, Shape>(){
            @Override
			public Shape transform(final String s){
                final Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
                // in this case, the vertex is twice as large
                for (final Menu m: getSizes()) {
                	if (m.getName().equals(s)) {
            			final Double percantage = (double) m.getSize();
	                	if (null == percantage) {
	                		return circle;
	                	} else {
	                		return AffineTransform.getScaleInstance(percantage, percantage).createTransformedShape(circle);
	                	}
                	}
                }
                return circle;
            }
        };
        return vertexSize;
	}
}
