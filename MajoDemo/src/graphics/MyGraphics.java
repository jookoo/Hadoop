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
public class MyGraphics {

	private final Graph<Menu, Edge> g = new DirectedSparseMultigraph<Menu, Edge>();

	private final Set<Menu> vertecis;

	private final Set<Edge> set;

	private final DelegateForest<Menu,Edge> mTree;

	public MyGraphics(final Set<Menu> vertecis, final Set<Edge> set) {
		this.vertecis = vertecis;
		this.set = set;
		mTree = new DelegateForest<Menu, Edge>();

		for (final Menu m:vertecis) {
			g.addVertex(m);
		}
		for (final Edge e: set) {
			g.addEdge(e, e.getFrom(), e.getTo());
		}

		for (final Menu n : vertecis) {
			mTree.addVertex(n);
		}
		for (final Edge e : set) {
			mTree.addEdge(e, e.getFrom(), e.getTo());
		}
	}

	public Forest<Menu, Edge> getForest() {
		return mTree;
	}

}
