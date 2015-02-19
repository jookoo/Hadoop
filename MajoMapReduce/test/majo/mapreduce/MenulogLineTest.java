package majo.mapreduce;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * Prüft die Klasse {@link MenulogLine}.
 * 
 * @author majo
 *
 */
public class MenulogLineTest {
	
	private static final String LINE = "20150214;09:02:22;21;C:\\PATH\\XYZ.EXE;;Eine Auswahl;ABC->DEF";
	
	private static final Calendar CAL = GregorianCalendar.getInstance();
	
	static {
		CAL.set(Calendar.YEAR, 2015);
		CAL.set(Calendar.MONTH, Calendar.FEBRUARY);
		CAL.set(Calendar.DAY_OF_MONTH, 14);
		CAL.set(Calendar.HOUR_OF_DAY, 9);
		CAL.set(Calendar.MINUTE, 2);
		CAL.set(Calendar.SECOND, 22);
		CAL.set(Calendar.MILLISECOND, 0);
	}
	
	@Test
	public void testMenulogLine() {
		final MenulogLine x = new MenulogLine(LINE);
		System.out.println(x);
		assertEquals(CAL, x.getDateTime());
		assertEquals("21", x.getUser());
	}

}
