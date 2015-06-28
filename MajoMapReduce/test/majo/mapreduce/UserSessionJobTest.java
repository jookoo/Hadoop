package majo.mapreduce;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Prüft die Klasse {@link UserSessionJob}.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class UserSessionJobTest {

	@Test
	public void testFilterPrg() {
		final Object[][] data = new Object[][] {
				{"C:\\AUFTRAG\\AUFTRAG.EXE", Boolean.TRUE},
				{"C:\\AUFTRAG\\auftrag.exe", Boolean.TRUE},
				{"C:\\BUR\\GH\\AUF\\AU_BLI.EXE", Boolean.TRUE},
				{"R:\\XPRG\\VOLLNEU\\AU\\AUWIN952.EXE", Boolean.TRUE},
				{"R:\\XPRG\\VOLLNEU\\AU\\AUWINXYZ.EXE", Boolean.FALSE},
				{"R:\\XPRG\\VOLLNEU\\BUCHPRG\\BUCH.EXE", Boolean.FALSE},
		};
		final UserSessionJob.UserValueMapper main = 
				new UserSessionJob.UserValueMapper(); 
		for (final Object[] x: data) {
			final String prg = (String) x[0];
			final boolean check = (boolean) x[1];
			final boolean result = main.acceptProgram(prg);
			System.out.println(prg + " --> " + result);
			if (check) {
				assertTrue(result);
			} else {
				assertFalse(result);
			}
		}
	}
	
}
