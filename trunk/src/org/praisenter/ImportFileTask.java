package org.praisenter;

import java.io.File;

/**
 * Represents a file import task.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ImportFileTask implements Runnable {
	/** The file to import */
	private File file;
	
	/** True if the task was successful */
	private boolean successful;

	/** The exception generated in the {@link #run()} method, if any */
	private Exception exception;
	
	/**
	 * Minimal constructor.
	 * @param file the file
	 */
	public ImportFileTask(File file) {
		this.file = file;
		this.successful = false;
		this.exception = null;
	}

	/**
	 * Method that should be called by the implementing class
	 * to handle any exceptions generated in the {@link #run()} method.
	 * @param exception the exception
	 */
	protected void handleException(Exception exception) {
		this.exception = exception;
	}
	
	/**
	 * Sets the successful flag.
	 * @param successful true if the task was successful
	 */
	protected void setSuccessful(boolean successful) {
		this.successful = successful;
	}
	
	/**
	 * Returns the file to import.
	 * @return File
	 */
	public File getFile() {
		return this.file;
	}
	
	/**
	 * Returns true if the task was successful.
	 * @return boolean
	 */
	public boolean isSuccessful() {
		return this.successful;
	}
	
	/**
	 * Returns the exception generated by the task, if any.
	 * @return Exception
	 */
	public Exception getException() {
		return this.exception;
	}
}