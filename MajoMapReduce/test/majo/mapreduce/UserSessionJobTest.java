package majo.mapreduce;

import static org.junit.Assert.*;

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
				{"C:\\AUFTRAG\\AUFTRAG.EXE", Boolean.FALSE},
				{"C:\\AUFTRAG\\auftrag.exe", Boolean.FALSE},
				{"C:\\BUR\\GH\\AUF\\AU_BLI.EXE", Boolean.FALSE},
				{"R:\\XPRG\\VOLLNEU\\AU\\AUWIN952.EXE", Boolean.FALSE},
				{"R:\\XPRG\\VOLLNEU\\AU\\AUWINXYZ.EXE", Boolean.FALSE},
				{"R:\\XPRG\\VOLLNEU\\BUCHPRG\\BUCH.EXE", Boolean.FALSE},
				{"GH", Boolean.TRUE},
				{"KND_BUHA", Boolean.FALSE},
				{"LIEF_BUHA", Boolean.FALSE},
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
