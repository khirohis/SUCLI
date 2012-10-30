package net.hogelab.android.SUCLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import net.hogelab.android.SUCLI.SuCommander.OutputListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity implements OutputListener {
	@SuppressWarnings("unused")
	private static final String TAG = MainActivity.class.getSimpleName();

	private Spinner				mSpinnerRecent = null;
	private EditText			mEditCommand = null;
	private CheckBox			mCheckSudo = null;
	private TextView			mTextOutput = null;

	private RecentCommandManager mRecentCommandManager = null;
	private SuCommander			mSuCommander = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mSpinnerRecent = (Spinner)findViewById(R.id.spinner_recent);
		mSpinnerRecent.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				onRecentSelected(parent, view, position, id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		mEditCommand = (EditText)findViewById(R.id.edit_command);
		mCheckSudo = (CheckBox)findViewById(R.id.check_sudo);
		mTextOutput = (TextView)findViewById(R.id.text_output);

		mRecentCommandManager = new RecentCommandManager(this);
		resetRecent();
	}


	@Override
	public void onStart() {
		super.onStart();

		mSuCommander = new SuCommander();
		mSuCommander.setOutputListener(this);
		mSuCommander.openSession();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (mSuCommander != null) {
			mSuCommander.setOutputListener(null);
			mSuCommander.closeSession();
			mSuCommander = null;
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	public void onRecentSelected(AdapterView<?> parent, View view, int position, long id) {
		String command = (String)mSpinnerRecent.getItemAtPosition(position);
		mEditCommand.setText(command);
	}

	public void onExecuteClick(View v) {
		String command = mEditCommand.getText().toString();
		if (command == null || command.length() <= 0) {
			return;
		}

		mTextOutput.setText(null);

		if (mCheckSudo.isChecked()) {
			mSuCommander.executeCommand(command);
		} else {
			executeCommand(command);
		}

		mRecentCommandManager.addCommand(command);
		resetRecent();
	}


	@Override
	public void onOutput(String output) {
		final String foutput = output;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextOutput.append(foutput);
			}
		});
	}

	@Override
	public void onError(String error) {
		final String ferror = error;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextOutput.append(ferror);
			}
		});
	}


	private void executeCommand(String command) {
		StringBuffer output = new StringBuffer();
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				output.append(line);
				output.append("\n");

				line = reader.readLine();
			}

			reader.close();
			process.waitFor();

			mTextOutput.setText(output);
		} catch (IOException e) {
        } catch (InterruptedException e) {
        }
	}


	private void resetRecent() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		List<String> recentList = mRecentCommandManager.getRecentList();
		for (String command: recentList) {
			adapter.add(command);
		}

		mSpinnerRecent.setAdapter(adapter);
	}
}
