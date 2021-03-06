/*
 * Copyright (c) 2011-2013 William Bittle  http://www.praisenter.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of Praisenter nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.praisenter.application.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Dialog used to show progress on a task.
 * <p>
 * The task is assumed to be indeterminate.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class TaskProgressDialog extends JDialog {
	/** The version id */
	private static final long serialVersionUID = 2197924276203282851L;

	/** Progress bar for loading */
	private JProgressBar barProgress;
	
	/** The task */
	private Runnable runnable;
	
	/**
	 * Shows an application modal dialog box that cannot be closed, 
	 * blocks the current application, and pre-loads resources.
	 * @param owner the owner of the dialog
	 * @param taskName the task name
	 * @param runnable the task
	 */
	public static final void show(Window owner, String taskName, Runnable runnable) {
		// creating an application modal dialog will block the
		// application but not the EDT
		new TaskProgressDialog(owner, taskName, runnable);
	}
	
	/**
	 * Minimal constructor.
	 * @param owner the owner of this dialog
	 * @param taskName the task name
	 * @param runnable the task
	 */
	private TaskProgressDialog(Window owner, String taskName, Runnable runnable) {
		super(owner, taskName, ModalityType.APPLICATION_MODAL);
		// make sure closing the modal doesn't work (since we can't remove the close button)
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.runnable = runnable;
		
		// create the progress bar
		this.barProgress = new JProgressBar(0, 100);
		this.barProgress.setMinimumSize(new Dimension(200, 50));
		
		// layout the loading
		Container container = this.getContentPane();
		
		GroupLayout layout = new GroupLayout(container);
		container.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(this.barProgress));
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(this.barProgress));
		
		// size the window
		this.pack();
		
		// start the background thread
		// since this dialog is application modal its imperative
		// that the worker thread be started before the dialog
		// is set to visible
		this.start();
		
		// make sure we are in the center of the parent window
		this.setLocationRelativeTo(owner);
		
		// show the dialog
		this.setVisible(true);
	}
	
	/**
	 * Starts a new thread to perform the task.
	 */
	private void start() {
		// we need to execute the task on another
		// thread so that we don't block the EDT
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// update the progress bar
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							barProgress.setIndeterminate(true);
						}
					});
				// just eat the exceptions
				} catch (Exception e) {}
				
				// begin the tasks
				runnable.run();
				
				// wait a bit to allow the user to see
				// that the task has completed
				try {
					Thread.sleep(500);
					// just eat the exception if we get one
				} catch (Exception e) {}
				
				// once the task is complete then
				// close the modal and resume normal
				// application flow
				close();
			}
		}, "TaskProgressThread");
		// don't block the closing of the app by this thread
		thread.setDaemon(true);
		// start the task thread
		thread.start();
	}
	
	/**
	 * Closes this dialog and disposes any temporary resources.
	 * <p>
	 * This method will execute the close command on the EDT at 
	 * some time in the future.
	 */
	private void close() {
		// close the dialog later
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// close it
				setVisible(false);
				// then dispose of the resources
				dispose();
			}
		});
	}
}
