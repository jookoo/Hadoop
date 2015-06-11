package graphics;

import java.util.LinkedList;
import java.util.List;

public class InformationCreator {

	public List<Edge> doIt() {
		final List<Edge> list = new LinkedList<>();
		final Edge a = new Edge("Erste Kante");
		a.add("Auskunft");
		a.add("B+R Nummer");
		list.add(a);
		final Edge b = new Edge("Zweite Kante");
		b.add("B+R Nummer");
		b.add("Ausdruck");
		list.add(b);
		
		final Edge c = new Edge("Zweite Kante 2");
		c.add("B+R Nummer");
		c.add("Ausdruck");
		list.add(c);
//		 g.addVertex(1);
//	        g.addVertex(2);
//	        g.addVertex(3); 
//	        g.addEdge("Edge-A", 1, 2); 
//	        g.addEdge("Edge-B", 2, 3);  
		return list;
	}

	public void createEdges() {
		
	}
	
	protected static class Edge {
		
		private final String name;
		
		private final List<String> connectedVertex = new LinkedList<>();

		private Edge(final String name) {
			this.name = name;
		}
		
		public void add(final String s) {
			connectedVertex.add(s);
		}

		public List<String> getConnectedVertecis() {
			return connectedVertex;
		}

		public String getName() {
			return name;
		}
	}
	
	
	protected static class ConnectorInformation {
		
	}
}
