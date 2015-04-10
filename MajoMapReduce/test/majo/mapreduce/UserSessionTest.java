package majo.mapreduce;

import org.junit.Test;

/**
 * Prüft die Klasse {@link UserSession}.
 * 
 * @author maik.boettcher@bur-kg.de
 *
 */
public class UserSessionTest {

	@Test
	public void testToString() {
		UserSession session = null;
		for (String x: TestData.DATA) {
			final MenulogLine line = new MenulogLine(x);
			final String user = line.getUser();
			final long time = line.getDateTime().getTimeInMillis();
			final String menue = line.getValue();
			if (null == session) {
				session = new UserSession(user, time, menue);
			} else {
				session.add(time, menue);
			}
		}
		System.out.println(session);
	}
	
}
