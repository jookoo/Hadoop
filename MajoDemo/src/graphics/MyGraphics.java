package graphics;

import java.util.HashSet;
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
	
	/** die Startseiten */
	public static final Set<String> STARTPAGES = new HashSet<>();	
	static {
		STARTPAGES.add("1 Aufträge");
		STARTPAGES.add("2 Auskunft");
		STARTPAGES.add("3 Rechnungen");
		STARTPAGES.add("4 Bestellungen");
		STARTPAGES.add("5 Belastungen");
		STARTPAGES.add("6 Gutschriften / Abholscheine");
		STARTPAGES.add("7 Artikel-Verwaltung");
		STARTPAGES.add("8 Textverarbeitung");
		STARTPAGES.add("9 Listen");
		STARTPAGES.add("A Etiketten / Schilder / Belege");
		STARTPAGES.add("D Postrechnungen drucken");
		STARTPAGES.add("E Sonderpreise");
		STARTPAGES.add("F Wareneingang");
		STARTPAGES.add("G Rechnungseingang");
		STARTPAGES.add("H Dienst-Programme");
		STARTPAGES.add("I Gutschrift / Neue Rechnung");
		STARTPAGES.add("K Artikelnummern der Kunden");
		STARTPAGES.add("L Ware abholen (sofort Rechnung)");
		STARTPAGES.add("M Fremdbelege erfassen");
		STARTPAGES.add("N Zusätzliche Pack-Nummern drucken");
		STARTPAGES.add("O Tourenplanung");
		STARTPAGES.add("P Empfangsscheine Scannen");
		STARTPAGES.add("Q Nur für 18");
		STARTPAGES.add("R Belegerfassung");
		STARTPAGES.add("S Anfragen");
		STARTPAGES.add("U Vorgänge");
		STARTPAGES.add("V Fremprogramme");
		STARTPAGES.add("W Nachlieferung");
	}

	private final Graph<Menu, Edge> g = new DirectedSparseMultigraph<Menu, Edge>();

	private final Set<Menu> vertecis;

	private final Set<Edge> set;

	private final DelegateForest<Menu,Edge> mTree;

	public MyGraphics(final Set<Menu> vertecis, final Set<Edge> set) {
		this.vertecis = vertecis;
		this.set = set;
		mTree = new DelegateForest<Menu, Edge>();

		for (final Menu n : vertecis) {
			mTree.addVertex(n);
		}
		for (final Edge e : set) {
			Menu from = null;
			for (final Menu m : vertecis) {
				if (m.getName().equals(e.getFrom().getName())) {
					if (null == m.getParentNode() && null == e.getFrom().getParentNode() 
							 || null != m.getParentNode() && m.getParentNode().equals(e.getFrom().getParentNode())) {
						from = m;
						break;
					}
				}
			}
			Menu to = null;
			for (final Menu m : vertecis) {
				if (m.getName().equals(e.getTo().getName())) {
					if (null != m.getParentNode() && null == e.getTo().getParentNode() 
							|| null != m.getParentNode() && m.getParentNode().equals(e.getTo().getParentNode())) {
						to = m;
						break;
					}
				}
			}
			mTree.addEdge(e, from, to);
		}
	}

	private void createStartpages(final MyOverviewDelegateForest<Menu, Edge> mTree) {
		for (final String s: STARTPAGES) {
			mTree.addVertex(new Menu(s,null));
		}
	}

	public Forest<Menu, Edge> getForest() {
		return mTree;
	}

	/**
	 * Der Delegator ignoriert Loops und Sessions ohne Wurzelknoten.
	 */
	public static class MyOverviewDelegateForest<V,E> extends DelegateForest<V,E> {
		
		@Override
		public boolean addEdge(final E e, final V v1, final V v2) {
			boolean add = true;
			if(delegate.getVertices().contains(v1) == false) {
				add = false;
			}
			if (delegate.getVertices().contains(v2)) {
				add = false;
			}
			if (add) {
				return delegate.addEdge(e, v1, v2);
			} else {
				return add;
			}
		}
		
	}
}
