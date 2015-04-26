package majo.mapreduce;

import java.util.Comparator;

/**
 * Vergleicht zwei {@link MenulogLine} und sortiert nach Datum/Zeit der 
 * Eintragung.
 * 
 * @author majo
 *
 */
public class MenulogLineComparator implements Comparator<MenulogLine> {

	@Override
	public int compare(final MenulogLine o1, final MenulogLine o2) {
		final long l1 = (null == o1 ? Long.MIN_VALUE : o1.getDateTime().getTimeInMillis());
		final long l2 = (null == o2 ? Long.MIN_VALUE : o2.getDateTime().getTimeInMillis());
		return Long.compare(l1, l2);
	}

}
