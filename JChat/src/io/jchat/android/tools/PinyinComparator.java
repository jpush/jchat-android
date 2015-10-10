package io.jchat.android.tools;

import java.util.Comparator;

import io.jchat.android.entity.TestSortModel;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<TestSortModel> {

	public int compare(TestSortModel o1, TestSortModel o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
