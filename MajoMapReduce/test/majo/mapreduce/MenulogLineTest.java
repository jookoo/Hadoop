package majo.mapreduce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Pr�ft die Klasse {@link MenulogLine}.
 *
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class MenulogLineTest {

	/** eine Testzeile */
	private static final String LINE1 = "20150214;09:02:22;21;C:\\PATH\\XYZ.EXE;;Eine Auswahl;ABC->DEF";

	/** eine Testzeile */
	private static final String LINE2 = "20150214;09:02:22;21;;;Eine Auswahl;ABC->DEF";

	/** eine Testzeile */
	private static final String LINE3 = "20150214;09:02:22;21;;;3.  Bis zum 05.01.2015;ABC->DEF";

	/** eine Testzeile */
	private static final String LINE4 = "20150214;09:02:22;21;;;2. Preise von Kunde 16725;ABC->DEF";

	/** eine Testzeile */
	private static final String LINE5 = "20150214;09:02:22;21;;;2.   Ab dem 07.04.2015;ABC->DEF";

	/** eine Testzeile */
	private static final String LINE6 = "20150214;09:02:22;21;;;1. Genau am 03.04.2015;ABC->DEF";

	/** eine Testzeile */
	private static final String LINE7 = "20150214;09:02:22;21;;;1. Fax-Bestellung (0921-89721);ABC->DEF";

	/** eine Testzeile */
	private static final String LINE7B = "20150214;09:02:22;21;;;1. Fax-Bestellung (02102-2047-109);ABC->DEF";

	/** eine Testzeile */
	private static final String LINE8 = "20150214;09:02:22;21;;;4. E-Mail-Bestellung (Anja.Luesebrink@ampri.de);ABC->DEF";
	
	/** eine Testzeile Q. Nur f\u00FCr 18 */
	private static final String LINE9 = new String("20150214;09:02:22;21;;;Q. Nur f�r 18;ABC->DEF".getBytes(Charset.forName("UTF-8")),Charset.forName("windows-1252"));

	/** verschiedene Testzeilen mit dem dazugeh�rigen Ergebnis */
	final static String[][] CMD_REP = new String[][] {
			{"20150214;09:02:22;21;C:\\AUFTRAG\\AUFTRAG.EXE;;Eine Auswahl;ABC->DEF", "GH" },
			{"20150214;09:02:22;21;C:\\BUR\\GH\\AUF\\AU_BLI.EXE;;Eine Auswahl;ABC->DEF", "GH" },
			{"20150214;09:02:22;21;R:\\XPRG\\VOLLNEU\\AU\\AUWIN950.EXE;;Eine Auswahl;ABC->DEF", "GH" },
			{"20150214;09:02:22;21;C:\\BUR\\BUCHHALT\\BUCH.EXE;;Eine Auswahl;ABC->DEF", "KND_BUHA" },
			{"20150214;09:02:22;21;R:\\BUCHPRG\\BUCHHALT\\BUCH.EXE;;Eine Auswahl;ABC->DEF", "KND_BUHA" },
			{"20150214;09:02:22;21;C:\\BUR\\LIEFBUCH\\LIEFBUCH.EXE;;Eine Auswahl;ABC->DEF", "LIEF_BUHA" },
			{"20150214;09:02:22;21;R:\\BUCHPRG\\LIEFBUCH\\LIEFBUCH.EXE;;Eine Auswahl;ABC->DEF", "LIEF_BUHA" },
	// {"20150214;09:02:22;21;R:\BUCHPRG\BUCHHALT\BUCHDEMO.EXE;;Eine
	// Auswahl;ABC->DEF", null}
	};

	/** ein Kalender */
	private static final Calendar CAL = GregorianCalendar.getInstance();

	/* statische Konfiguration des Testkalenders */
	static {
		CAL.set(Calendar.YEAR, 2015);
		CAL.set(Calendar.MONTH, Calendar.FEBRUARY);
		CAL.set(Calendar.DAY_OF_MONTH, 14);
		CAL.set(Calendar.HOUR_OF_DAY, 9);
		CAL.set(Calendar.MINUTE, 2);
		CAL.set(Calendar.SECOND, 22);
		CAL.set(Calendar.MILLISECOND, 0);
	}

	@Test(expected = NullPointerException.class)
	public void testNullPointerException() {
		new MenulogLine(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalArgumentException() {
		new MenulogLine(";;;");
	}

	@Test
	public void testMenulogLine1() {
		final MenulogLine x = new MenulogLine(LINE1);
		System.out.println(x);
		assertEquals(CAL, x.getDateTime());
		assertEquals("21", x.getCleanUser());
		assertEquals("Eine Auswahl", x.getValue());
	}

	@Test
	public void testMenulogLine2() {
		final MenulogLine x = new MenulogLine(LINE2);
		System.out.println(x);
		assertEquals(CAL, x.getDateTime());
		assertEquals("21", x.getCleanUser());
		assertEquals("Eine Auswahl", x.getValue());
	}

	@Test
		public void testCleanupValueValueBiszum() {
			final MenulogLine x = new MenulogLine(LINE3);
			System.out.println(x);
			assertEquals("3. Bis zum", x.getCleanValue());
		}

	@Test
		public void testCleanupValueValuePreise() {
			final MenulogLine x = new MenulogLine(LINE4);
			System.out.println(x);
			assertEquals("2. Preise von Kunde", x.getCleanValue());
		}

	@Test
		public void testCleanupValueValueAbdem() {
			final MenulogLine x = new MenulogLine(LINE5);
			System.out.println(x);
			assertEquals("2. Ab dem", x.getCleanValue());
		}

	@Test
		public void testCleanupValueValueGenauam() {
			final MenulogLine x = new MenulogLine(LINE6);
			System.out.println(x);
			assertEquals("1. Genau am", x.getCleanValue());
		}

	@Test
		public void testCleanupValueValueEmail() {
			final MenulogLine x = new MenulogLine(LINE8);
			System.out.println(x);
			assertEquals("4. E-Mail-Bestellung", x.getCleanValue());
		}

	@Test
		public void testCleanupValueValueFax() {
			final MenulogLine x1 = new MenulogLine(LINE7);
			System.out.println(x1);
			assertEquals("1. Fax-Bestellung", x1.getCleanValue());
	
			final MenulogLine x2 = new MenulogLine(LINE7B);
			System.out.println(x2);
			assertEquals("1. Fax-Bestellung", x2.getCleanValue());
		}
	
	@Test
	public void testCleanupValueSonderzeichen() {
		final MenulogLine x1 = new MenulogLine(LINE9);
		System.out.println(x1.getCleanValue());
		assertTrue(Pattern.matches("Q\\. Nur f.+r 18", x1.getCleanValue()));
//		
//		final MenulogLine x2 = new MenulogLine(LINE7B);
//		System.out.println(x2);
//		assertEquals("1. Fax-Bestellung", x2.getCleanValue());
	}

	@Test
	public void testCleanupValueProgramAuftragexe() {
		for (final String[] line : CMD_REP) {
			final String actual = line[0];
			final String expected = line[1];
			final MenulogLine x = new MenulogLine(actual);
			System.out.println(x);
			assertEquals(expected, x.getCleanProgram());
		}
	}

}
