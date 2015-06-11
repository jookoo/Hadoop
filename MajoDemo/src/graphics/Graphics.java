package graphics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import graphics.InformationCreator.Edge;

public class Graphics {

	private final Graph<String, String> g = new SparseMultigraph<String, String>();
	
	public Graphics() {
		 // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        // Add some vertices and edges
		final InformationCreator creator = new InformationCreator();
		final List<Edge> list = creator.doIt();
		final Set<String> vertecis = new HashSet<>();
		for (final Edge e: list) {
			final List<String> verts = e.getConnectedVertecis();
			vertecis.addAll(verts);
			g.addEdge(e.getName(), verts);
		}
		for (final String i: vertecis) {
			g.addVertex(i);
		}
	}

	public Graph<String, String> getGraph() {
		return g;
	}
}
