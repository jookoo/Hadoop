package test;

import static org.junit.Assert.*;
import graphics.InformationCreator;
import graphics.InformationCreator.Edge;
import graphics.InformationCreator.Menu;
import graphics.SessionLine;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Prüft die Klasse {@link InformationCreator}
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InformationCreatorTest {

	/** Testdaten */
	private final static String[] DATA = new String[]{
	"10	2014-10-09 08:34:33 [1412836473000:2 Auskunft][1412836474000:2 Lieferanten][1412836476000:Zuname I][1412837454000:B+R-Nummer][1412837810000:B+R-Nummer]",
	"10	2014-10-09 08:36:33 [1412836593000:2 Auskunft][1412836595000:3 Artikel][1412836597000:B+R-Nummer]",
	"10	2014-10-09 08:36:33 [1412836593000:2 Auskunft][1412836595000:3 Artikel][1412836597000:B+R-Nummer]"};
	
	@Test
	public void testCreateSessionEdges() {
		final Set<SessionLine> set = createSessionlines();
		final InformationCreator creator = new InformationCreator();
		final Map<String, Edge> map = creator.createSessionEdges(set);
		for (final Entry<String, Edge> entry: map.entrySet()) {
			final Edge edge = entry.getValue();
			System.out.println(edge + " [weight=" + edge.getWeight() + "]");
		}
	}
	
	@Test
	public void testCreateSessionMenus() {
		final Set<SessionLine> set = createSessionlines();
		final InformationCreator creator = new InformationCreator();
		final Set<Menu> menus= creator.createSessionMenues(set);
		for (final Menu m: menus) {
			System.out.println(m + " [parent="+m.getParentNode()+"]" + " [weight=" + m.getWeight() + "]");
		}
	}

	@Test
	public void testMenuEquals() {
		final Menu m1 = new Menu("B", null);
		final Menu m2 = new Menu("B", "A");
		assertFalse(m1.equals(m2));
		final Set<Menu> treeset = new TreeSet<>();
		treeset.add(m1);
		treeset.add(m2);
		assertTrue(2 == treeset.size());
	}
	
	private Set<SessionLine> createSessionlines() {
		final Set<SessionLine> x = new LinkedHashSet<SessionLine>();
		for (final String s: DATA) {
			x.add(new SessionLine(s));
		}
		return x;
	}
	
}
