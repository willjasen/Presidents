package PresidentsServer;

import java.io.FileWriter;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * 
 * @author willjasen
 * 
 */
public class ServerLog {

	private String filename;
	private FileWriter toFile;
	private final boolean toLog = false;
	private static final String DATE_FORMAT_NOW = "MM.dd.yyyy hh:mm:ss a";

	public ServerLog() {

	}

	/**
	 * 
	 * @param	filename filename of the log
	 */
	public ServerLog(String file) {
		filename = file;

		try {
			toFile = new FileWriter(filename);
		} catch (Exception e) {
			System.out.println("Cannot create log file.");
		}
	}

	/**
	 * Returns the current time represented by {@link}DATE_FORMAT_NOW
	 * 
	 * @return  the time formatted in a string
	 */
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	/**
	 * 
	 * @param  textToLog  text to write to this log
	 */
	public void write(String textToLog) {

		// if(toLog) {
		// try {
		// toFile.write(textToLog);
		// toFile.flush();
		// }
		// catch(Exception e) {
		// System.out.println("Cannot write to log file.");
		// }
		// }
		// else {
		System.out.println(textToLog);
		// }

	}
}
