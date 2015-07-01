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

		for (final Menu n : vertecis) {
			if (n.getName().contains("Kunden-Nr")) {
				System.out.println("");
			}
			mTree.addVertex(n);
		}
		for (final Edge e : set) {
			Menu from = null;
			for (final Menu m : vertecis) {
				if (m.getName().equals(e.getFrom().getName())) {
					from = m;
				}
			}
			Menu to = null;
			for (final Menu m : vertecis) {
				if (m.getName().equals(e.getTo().getName())) {
					to = m;
				}
			}
			
			mTree.addEdge(e, from, to);
		}
	}

	public Forest<Menu, Edge> getForest() {
		return mTree;
	}

}
