package graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class InformationCreator {

	private static final String FILENAME = "C:\\user_session.txt";
	
	public Set<Edge> doIt() {
		final Set<Edge> set = new LinkedHashSet<>();
		final List<SessionLine> sessions = createEdges();
		for(final SessionLine sl: sessions) {
			final List<Edge> edges = sl.getEdges();
			set.addAll(edges);
		}
		return set;
	}

	private List<SessionLine> createEdges() {
		final List<SessionLine> sessions = new LinkedList<>();
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);

            int count = 0;
            String line = null;
            line = br.readLine();
            einlesen: while (line != null) {
            	System.out.println(line);
            	final SessionLine sl = new SessionLine(line);
            	sessions.add(sl);
            	line = br.readLine();
            	count++;
//            	if(count > 1) {
            		break einlesen;
//            	}
            }
            fr.close();
        } catch (final IOException e){
            System.out.println("Fehler beim Lesen der Datei " + FILENAME);
            System.out.println(e.toString());
        }
        return sessions;
	}
	
	protected static class Edge {
		
		private final List<String> connectedVertex = new LinkedList<>();

		protected Edge() {
		}
		
		public void add(final String s) {
			connectedVertex.add(s);
		}

		public List<String> getConnectedVertecis() {
			return connectedVertex;
		}

		public String getName() {
			final StringBuffer x = new StringBuffer();
			for (final String s :connectedVertex) {
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
}
