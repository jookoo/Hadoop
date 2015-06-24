package graphics;

import java.util.Set;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;

/**
 * Repräsentiert die grafische Darstellung der Ergebnisse.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class Graphics {
	
	private final Graph<Menu, Edge> g = new DirectedOrderedSparseMultigraph<>();
	
	public Graphics(final Set<Menu> vertecis, final Set<Edge> set) {
		for (final Menu m:vertecis) {
			g.addVertex(m);
		}
		for (final Edge e:set) {
			g.addEdge(e, e.getConnectedVertecis());
		}
	}

	public Graph<Menu, Edge> getForest() {
		final TreeBuilder builder = new TreeBuilder(g);
		return g;
	}
	
	public class TreeBuilder {
	    DelegateForest<Menu,Edge> mTree;
	    
	    TreeBuilder(final Graph<Menu,Edge> graph) {
	        mTree = new DelegateForest<>();
//	        for (final String n : graph.getVertices()) {
//        		mTree.addVertex(n);
//	        }
//	        for (final String e : graph.getEdges()) {
//        		mTree.addEdge(e, graph.getSource(e),graph.getDest(e));
//	        }
	    }
	    
	    public DelegateForest<Menu, Edge> getTree()  {
	        return mTree;
	    }
	}
	
}
