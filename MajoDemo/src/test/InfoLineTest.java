package test;

import org.junit.Test;

import graphics.InfoLine;

/**
 * Prüft die Klasse InfoLine.
 * 
 * @author joshua.kornfeld@bur-kg.de
 *
 */
public class InfoLineTest {

	@Test
	public void testUser() {
		final String line = "USER[sy]	1";
		final InfoLine il = new InfoLine(line, null);
		System.out.println(il.getUser());
		System.out.println(il.getCount());
	}
	
	@Test
	public void testPrg() {
		final String line = "VALUE[GH~1. 11 Falschbestellung des Kunden]	5711";
		final InfoLine il = new InfoLine(line, null);
		System.out.println(il.getProgram());
		System.out.println(il.getMenu());
		System.out.println(il.getCount());
	}
}
