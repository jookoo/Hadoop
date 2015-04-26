package majo.mapreduce;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Prüft die Klasse {@link MenulogLineComparator}.
 * 
 * @author majo
 *
 */
public class MenulogLineComparatorTest {

	private static final String[] EXCPECTED = new String[] {
		"erste Auswahl", "zweite Auswahl", "dritte Auswahl"
	};
	
	@Test
	public void testCompareTo() {
		System.out.println(Arrays.asList(TestData.DATA));
		// Sortieren
		final Set<MenulogLine> set = new TreeSet<>(new MenulogLineComparator());
		for (String x: TestData.DATA) {
			final MenulogLine l = new MenulogLine(x);
			set.add(l);
		}
		// Prüfen
		final Iterator<MenulogLine> it = set.iterator();
		int idx = 0;
		while (it.hasNext()) {
			final MenulogLine actual = it.next();
			System.out.println("[" + idx + "] " + actual);
			assertEquals(EXCPECTED[idx], actual.getValue());
			idx++;
		}
	}
	
}
