package net.hogelab.android.SUCLI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


public class RecentCommandManager {
	@SuppressWarnings("unused")
	private static final String TAG = RecentCommandManager.class.getSimpleName();

	private static final int	RECENT_LIST_MAX = 10;
	private static final String	RECENT_KEY_FORMAT = "command%02d";

	private Context				mContext;
	private List<String>		mRecentList;


	public RecentCommandManager(Context context) {
		mContext = context;

		loadPreferences();
	}


	public List<String> getRecentList() {
		return mRecentList;
	}

	public void addCommand(String newCommand) {
		List<String> newList = new ArrayList<String>(RECENT_LIST_MAX);
		newList.add(newCommand);

		for (String command: mRecentList) {
			if (!command.equals(newCommand)) {
				newList.add(command);
			}

			if (newList.size() >= RECENT_LIST_MAX) {
				break;
			}
		}

		mRecentList = newList;
		savePreferences();
	}


	private void savePreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = prefs.edit();

		Iterator<String> iterator = mRecentList.iterator();
		int listSize = mRecentList.size();
		for (int i = 0; i < RECENT_LIST_MAX; i++) {
			String key = String.format(RECENT_KEY_FORMAT, i);

			if (i < listSize) {
				String command = iterator.next();
				editor.putString(key, command);
			} else {
				editor.remove(key);
			}
		}

		editor.commit();
	}

	private void loadPreferences() {
		mRecentList = new ArrayList<String>(RECENT_LIST_MAX);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		for (int i = 0; i < RECENT_LIST_MAX; i++) {
			String key = String.format(RECENT_KEY_FORMAT, i);
			String command = prefs.getString(key, null);
			if (command == null) {
				break;
			}

			mRecentList.add(command);
		}
	}
}
