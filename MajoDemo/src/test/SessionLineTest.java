package test;

import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;
import graphics.SessionLine;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * 
 * Prüft die Klasse {@link SessionLine}.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class SessionLineTest {

	/** Testdaten */
	private final static String[] DATA = new String[]{
	"10	2014-10-09 08:34:33 [1412836473000:2 Auskunft][1412836474000:2 Lieferanten][1412836476000:Zuname I][1412837454000:B+R-Nummer][1412837810000:B+R-Nummer]",
	"10	2014-10-09 08:36:33 [1412836593000:2 Auskunft][1412836595000:3 Artikel][1412836597000:B+R-Nummer]",
	"10	2014-10-09 08:36:33 [1412836593000:2 Auskunft][1412836595000:3 Artikel][1412836597000:B+R-Nummer]"};

	@Test
	public void testAnalyse() {
		final Set<SessionLine> set = createSessionlines();
		for (final SessionLine sl : set) {
			final Set<Menu> menus = sl.getMenu();
			System.out.println("-------Menüs------");
			for (final Menu m: menus) {
				System.out.println(m);
			}
			
			System.out.println("------Kanten------");
			final Set<Edge> edges = sl.getEdge();
			for (final Edge e: edges) {
				System.out.println(e);
			}
		}
	}

	private Set<SessionLine> createSessionlines() {
		final Set<SessionLine> x = new HashSet<SessionLine>();
		for (final String s: DATA) {
			x.add(new SessionLine(s));
		}
		return x;
	}

}
