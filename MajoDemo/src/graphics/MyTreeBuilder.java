package graphics;

import java.util.Set;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;

/**
 * Repräsentiert die Struktur für die Darstellung der Ergebnisse.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class MyTreeBuilder {

	/** die Grafik */
	private final Graph<Menu, Edge> g = new DirectedSparseMultigraph<Menu, Edge>();

	/** die Baumstruktur */
	private final DelegateForest<Menu,Edge> mTree = new DelegateForest<Menu, Edge>();
	
	/**
	 * ein Konstruktor
	 * @param vertecis die bekannten Menüpunkte
	 * @param set die bekannten Kanten
	 */
	public MyTreeBuilder(final Set<Menu> vertecis, final Set<Edge> set) {
		// Grafik füllen
		for (final Menu m : vertecis) {
			g.addVertex(m);
		}
		for (final Edge e : set) {
			g.addEdge(e, e.getConnectedVertecis());
		}
		// Baumstruktur
		for (final Menu m :g.getVertices()) {
			mTree.addVertex(m);
		}
		for (final Edge e: g.getEdges()) {
			mTree.addEdge(e, g.getSource(e) , g.getDest(e));
		}
	}

	/**
	 * Liefert die Baumstruktur.
	 * @return ein Objekt, niemals <code>null</code>
	 */
	public Forest<Menu, Edge> getForest() {
		return mTree;
	}
}
