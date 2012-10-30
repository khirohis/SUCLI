package net.hogelab.android.SUCLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class SuCommander {
	@SuppressWarnings("unused")
	private static final String TAG = SuCommander.class.getSimpleName();

	protected OutputListener	mOutputListener = null;

	protected Process			mProcess = null;
	protected OutputStream		mStdin = null;
	protected InputStream		mStdout = null;
	protected InputStream		mStderr = null;

	protected Thread			mStdoutReader = null;
	protected Thread			mStderrReader = null;


	public interface OutputListener {
		public void onOutput(String output);
		public void onError(String error);
	}


	public SuCommander() {
	}


	public void setOutputListener(OutputListener listener) {
		synchronized (this) {
			mOutputListener = listener;
		}
	}


	public boolean isSessionOpened() {
		return mProcess != null;
	}

	public boolean openSession() {
		if (isSessionOpened()) {
			closeSession();
		}

		boolean result = false;

		try {
			Runtime runtime = Runtime.getRuntime();
			//mProcess = runtime.exec("sh");
			mProcess = runtime.exec("su");

			mStdin = mProcess.getOutputStream();
			mStdout = mProcess.getInputStream();
			mStderr = mProcess.getErrorStream();

			executeCommand("echo 'su OK'");
			startReaderThread();

			result = true;
		} catch (IOException e) {
		}

		return result;
	}

	public boolean closeSession() {
		if (mProcess != null) {
			executeCommand("exit");
		}

		if (mStdin != null) {
			try {
				mStdin.close();
			} catch (IOException e) {
			}
			mStdin = null;
		}

		if (mStdout != null) {
			try {
				mStdout.close();
			} catch (IOException e) {
			}
			mStdout = null;
		}

		if (mStderr != null) {
			try {
				mStderr.close();
			} catch (IOException e) {
			}
			mStderr = null;
		}

		if (mProcess != null) {
			try {
				mProcess.waitFor();
			} catch (InterruptedException e) {
			}
			mProcess = null;
		}

		return true;
	}


	public boolean executeCommand(String command) {
		if (!isSessionOpened()) {
			return false;
		}

		boolean result = false;

		try {
			mStdin.write(command.getBytes());
			mStdin.write("\n".getBytes());

			result = true;
		} catch (IOException e) {
		}

		return result;
	}


	protected void startReaderThread() {
		ReaderThreadListener listener = new ReaderThreadListener() {
			@Override
			public void onRead(String line) {
				synchronized (SuCommander.this) {
					if (mOutputListener != null) {
						mOutputListener.onOutput(line);
					}
				}
			}
		};
		mStdoutReader = new ReaderThread(listener, mStdout);
		mStdoutReader.start();

		listener = new ReaderThreadListener() {
			@Override
			public void onRead(String line) {
				synchronized (SuCommander.this) {
					if (mOutputListener != null) {
						mOutputListener.onError(line);
					}
				}
			}
		};
		mStderrReader = new ReaderThread(listener, mStderr);
		mStderrReader.start();
	}


	private interface ReaderThreadListener {
		public void onRead(String line);
	}

	private static class ReaderThread extends Thread {
		private ReaderThreadListener mListener;
		private BufferedReader		mReader;

		@SuppressWarnings("unused")
		private ReaderThread() {}

		public ReaderThread(ReaderThreadListener listener, InputStream stream) {
			mListener = listener;
			mReader = new BufferedReader(new InputStreamReader(stream));
		}

		@Override
		public void run() {
			try {
				String line = mReader.readLine();
				while (line != null) {
					if (line.length() > 0) {
						mListener.onRead(line + "\n");
					}

					line = mReader.readLine();
				}
			} catch (IOException e) {
			}

			try {
				mReader.close();
			} catch (IOException e) {
			}
		}
	}
}
