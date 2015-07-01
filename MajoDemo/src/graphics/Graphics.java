package graphics;

import java.util.Set;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
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
	
	private final Graph<Menu, Edge> g = new DirectedSparseMultigraph<Menu, Edge>();
	
	public Graphics(final Set<Menu> vertecis, final Set<Edge> set) {
		for (final Menu m:vertecis) {
			g.addVertex(m);
		}
		for (final Edge e: set) {
			g.addEdge(e, e.getFrom(), e.getTo());
		}
	}

	public Forest<Menu, Edge> getForest() {
		final TreeBuilder builder = new TreeBuilder(g);
		return builder.getTree();
	}
	
	public static class TreeBuilder {
		
	    private final DelegateForest<Menu,Edge> mTree;
	    
	    TreeBuilder(final Graph<Menu,Edge> graph) {
	        mTree = new DelegateForest<>();
	        for (final Menu n : graph.getVertices()) {
					mTree.addVertex(n);
	        }
	        for (final Edge e : graph.getEdges()) {
	        	final Menu src = graph.getSource(e);
	        	final Menu dest = graph.getDest(e);
        		mTree.addEdge(e, src, dest);
	        }
	    }
	    
	    public DelegateForest<Menu, Edge> getTree()  {
	        return mTree;
	    }
	}
	
}
