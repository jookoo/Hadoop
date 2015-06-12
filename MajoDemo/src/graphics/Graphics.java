package graphics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import graphics.InformationCreator.Edge;

public class Graphics {
	
	private final Graph<String, String> g = new DirectedOrderedSparseMultigraph<String, String>();
	
	public Graphics() {
		 // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        // Add some vertices and edges
		final InformationCreator creator = new InformationCreator();
		
		// der Vertex muss Layer haben...
		// Edge muss Gewichtung haben....
		final Set<Edge> set = creator.doIt();
		final Set<String> vertecis = new HashSet<>();
		for (final Edge e: set) {
			final List<String> verts = e.getConnectedVertecis();
			vertecis.addAll(verts);
			g.addEdge(e.getName(), verts, EdgeType.DIRECTED);
		}
		for (final String i: vertecis) {
			g.addVertex(i);
		}
	}

	public Graph<String, String> getForest() {
		final TreeBuilder builder = new TreeBuilder(g);
		return g;
	}
	
	public class TreeBuilder {
	    DelegateForest<String,String> mTree;
	    
	    TreeBuilder(final Graph<String,String> graph) {
	        mTree = new DelegateForest<>();
	        for (final String n : graph.getVertices()) {
        		mTree.addVertex(n);
	        }
	        for (final String e : graph.getEdges()) {
        		mTree.addEdge(e, graph.getSource(e),graph.getDest(e));
	        }
	    }
	    
	    public DelegateForest<String, String> getTree()  {
	        return mTree;
	    }
	}
	
	
	
//	public static class DelegateForest<V,E> extends GraphDecorator<V,E> implements Forest<V,E> {
//
//		public DelegateForest(final Graph<V, E> delegate) {
//			super(delegate);
//		}
//
//		@Override
//		public Collection<Tree<V, E>> getTrees() {
//			return null;
//		}
//
//		@Override
//		public V getParent(final V vertex) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public E getParentEdge(final V vertex) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Collection<V> getChildren(final V vertex) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Collection<E> getChildEdges(final V vertex) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public int getChildCount(final V vertex) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//		
//	}
}
