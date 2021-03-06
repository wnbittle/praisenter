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
package org.praisenter.application.preferences.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.praisenter.animation.easings.Easing;
import org.praisenter.animation.easings.Easings;
import org.praisenter.animation.transitions.Transition;
import org.praisenter.animation.transitions.TransitionType;
import org.praisenter.animation.transitions.Transitions;
import org.praisenter.application.preferences.NotificationPreferences;
import org.praisenter.application.preferences.Preferences;
import org.praisenter.application.resources.Messages;
import org.praisenter.application.slide.ui.EasingListCellRenderer;
import org.praisenter.application.slide.ui.SlideLibraryDialog;
import org.praisenter.application.slide.ui.SlideLibraryListener;
import org.praisenter.application.slide.ui.SlideThumbnailComboBoxRenderer;
import org.praisenter.application.slide.ui.TransitionListCellRenderer;
import org.praisenter.application.ui.OpaquePanel;
import org.praisenter.application.ui.SelectTextFocusListener;
import org.praisenter.common.NotInitializedException;
import org.praisenter.common.utilities.ComponentUtilities;
import org.praisenter.common.utilities.WindowUtilities;
import org.praisenter.slide.NotificationSlideTemplate;
import org.praisenter.slide.SlideFile;
import org.praisenter.slide.SlideLibrary;
import org.praisenter.slide.SlideThumbnail;

/**
 * Panel used to set the {@link NotificationPreferences}.
 * @author William Bittle
 * @version 2.0.1
 * @since 2.0.0
 */
public class NotificationPreferencesPanel extends OpaquePanel implements PreferencesEditor, ActionListener, SlideLibraryListener {
	/** The verison id */
	private static final long serialVersionUID = -2274087793090854194L;

	/** The default wait period text box */
	private JFormattedTextField txtDefaultWaitPeriod;

	// template
	
	/** The template combo box */
	private JComboBox<SlideThumbnail> cmbTemplates;
	
	// transitions
	
	/** The combo box of send transitions */
	private JComboBox<Transition> cmbSendTransitions;
	
	/** The text box for the send transition duration */
	private JFormattedTextField txtSendTransitions;
	
	/** The combo box of send easings */
	private JComboBox<Easing> cmbSendEasings;
	
	/** The combo box of clear transitions */
	private JComboBox<Transition> cmbClearTransitions;
	
	/** The text box for the clear transition duration */
	private JFormattedTextField txtClearTransitions;
	
	/** The combo box of clear easings */
	private JComboBox<Easing> cmbClearEasings;
	
	/**
	 * Default constructor.
	 */
	public NotificationPreferencesPanel() {
		Preferences preferences = Preferences.getInstance();
		NotificationPreferences nPreferences = preferences.getNotificationPreferences();
		
		// general notification settings
		JLabel lblDefaultWaitPeriod = new JLabel(Messages.getString("panel.notification.preferences.defaultWaitPeriod"));
		this.txtDefaultWaitPeriod = new JFormattedTextField(new DecimalFormat("0"));
		this.txtDefaultWaitPeriod.setToolTipText(Messages.getString("panel.notification.preferences.defaultWaitPeriod.tooltip"));
		this.txtDefaultWaitPeriod.setValue(nPreferences.getWaitPeriod());
		this.txtDefaultWaitPeriod.setColumns(6);
		this.txtDefaultWaitPeriod.addFocusListener(new SelectTextFocusListener(this.txtDefaultWaitPeriod));

		// template
		
		JLabel lblTemplate = new JLabel(Messages.getString("panel.preferences.template"));
		SlideThumbnail[] thumbs = this.getSlideThumnails();
		// find the selected template
		SlideThumbnail selected = null;
		for (SlideThumbnail thumb : thumbs) {
			if (thumb.getFile() == SlideFile.NOT_STORED) {
				if (nPreferences.getTemplate() == null) {
					selected = thumb;
					break;
				}
			} else if (thumb.getFile().getRelativePath().equals(nPreferences.getTemplate())) {
				selected = thumb;
				break;
			}
		}
		Arrays.sort(thumbs);
		this.cmbTemplates = new JComboBox<SlideThumbnail>(thumbs);
		if (selected != null) {
			this.cmbTemplates.setSelectedItem(selected);
		}
		this.cmbTemplates.setToolTipText(Messages.getString("panel.preferences.template.tooltip"));
		this.cmbTemplates.setRenderer(new SlideThumbnailComboBoxRenderer());
		JButton btnAddTemplate = new JButton(Messages.getString("template.manage"));
		btnAddTemplate.setActionCommand("addTemplate");
		btnAddTemplate.setToolTipText(Messages.getString("template.manage.tooltip"));
		btnAddTemplate.addActionListener(this);
		
		// transitions
		
		JLabel lblSendTransition = new JLabel(Messages.getString("panel.preferences.transition.defaultSend"));
		this.cmbSendTransitions = new JComboBox<Transition>(Transitions.IN);
		this.cmbSendTransitions.setRenderer(new TransitionListCellRenderer(this.cmbSendTransitions));
		this.cmbSendTransitions.setSelectedItem(Transitions.getTransitionForId(nPreferences.getSendTransitionId(), TransitionType.IN));
		this.txtSendTransitions = new JFormattedTextField(new DecimalFormat("0"));
		this.txtSendTransitions.addFocusListener(new SelectTextFocusListener(this.txtSendTransitions));
		this.txtSendTransitions.setToolTipText(Messages.getString("transition.duration.tooltip"));
		this.txtSendTransitions.setValue(nPreferences.getSendTransitionDuration());
		this.txtSendTransitions.setColumns(3);
		this.cmbSendEasings = new JComboBox<Easing>(Easings.EASINGS);
		this.cmbSendEasings.setRenderer(new EasingListCellRenderer());
		this.cmbSendEasings.setSelectedItem(Easings.getEasingForId(nPreferences.getSendTransitionEasingId()));
		this.cmbSendEasings.setToolTipText(Messages.getString("easing.tooltip"));
		
		JLabel lblClearTransition = new JLabel(Messages.getString("panel.preferences.transition.defaultClear"));
		this.cmbClearTransitions = new JComboBox<Transition>(Transitions.OUT);
		this.cmbClearTransitions.setRenderer(new TransitionListCellRenderer(this.cmbClearTransitions));
		this.cmbClearTransitions.setSelectedItem(Transitions.getTransitionForId(nPreferences.getClearTransitionId(), TransitionType.OUT));
		this.txtClearTransitions = new JFormattedTextField(new DecimalFormat("0"));
		this.txtClearTransitions.addFocusListener(new SelectTextFocusListener(this.txtClearTransitions));
		this.txtClearTransitions.setToolTipText(Messages.getString("transition.duration.tooltip"));
		this.txtClearTransitions.setValue(nPreferences.getClearTransitionDuration());
		this.txtClearTransitions.setColumns(3);
		this.cmbClearEasings = new JComboBox<Easing>(Easings.EASINGS);
		this.cmbClearEasings.setRenderer(new EasingListCellRenderer());
		this.cmbClearEasings.setSelectedItem(Easings.getEasingForId(nPreferences.getClearTransitionEasingId()));
		this.cmbClearEasings.setToolTipText(Messages.getString("easing.tooltip"));
		
		JPanel pnlTransitions = new OpaquePanel();
		GroupLayout layout = new GroupLayout(pnlTransitions);
		pnlTransitions.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblSendTransition)
						.addComponent(lblClearTransition))
				.addGroup(layout.createParallelGroup()
						.addComponent(this.cmbSendTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.cmbClearTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(this.txtSendTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.txtClearTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup()
						.addComponent(this.cmbSendEasings, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.cmbClearEasings, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblSendTransition)
						.addComponent(this.cmbSendTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.txtSendTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.cmbSendEasings, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lblClearTransition)
						.addComponent(this.cmbClearTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.txtClearTransitions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.cmbClearEasings, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		
		// setup the template panel
		JPanel pnlTemplate = new OpaquePanel();
		layout = new GroupLayout(pnlTemplate);
		pnlTemplate.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(lblTemplate)
				.addComponent(this.cmbTemplates, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(btnAddTemplate));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lblTemplate)
				.addComponent(this.cmbTemplates, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(btnAddTemplate));
		
		// setup the layout
		JPanel pnlGeneral = new OpaquePanel();
		layout = new GroupLayout(pnlGeneral);
		pnlGeneral.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(lblDefaultWaitPeriod)
				.addComponent(this.txtDefaultWaitPeriod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lblDefaultWaitPeriod)
				.addComponent(this.txtDefaultWaitPeriod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
		
		ComponentUtilities.setMinimumSize(lblDefaultWaitPeriod, lblTemplate, lblClearTransition, lblSendTransition);
		
		JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
		
		layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(pnlGeneral)
				.addComponent(sep1)
				.addComponent(pnlTemplate)
				.addComponent(sep2)
				.addComponent(pnlTransitions));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(pnlGeneral)
				.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(pnlTemplate)
				.addComponent(sep2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(pnlTransitions));
	}

	/**
	 * Returns the slide thumbnail list for all the templates.
	 * @return {@link SlideThumbnail}[]
	 */
	private SlideThumbnail[] getSlideThumnails() {
		// we need to refresh the templates listing
		List<SlideThumbnail> thumbs = null;
		try {
			thumbs = SlideLibrary.getInstance().getThumbnails(NotificationSlideTemplate.class);
		} catch (NotInitializedException ex) {
			thumbs = new ArrayList<SlideThumbnail>();
		}
		
		// add in the default template
		Preferences preferences = Preferences.getInstance();
		Dimension displaySize = preferences.getPrimaryOrDefaultDeviceResolution();
		NotificationSlideTemplate template = NotificationSlideTemplate.getDefaultTemplate(displaySize.width, displaySize.height);
		BufferedImage image = template.getThumbnail(SlideLibrary.THUMBNAIL_SIZE);
		SlideThumbnail temp = new SlideThumbnail(SlideFile.NOT_STORED, template.getName(), image);
		thumbs.add(temp);
		
		Collections.sort(thumbs);
		
		return thumbs.toArray(new SlideThumbnail[0]);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if ("addTemplate".equals(command)) {
			boolean libraryUpdated = SlideLibraryDialog.show(WindowUtilities.getParentWindow(this), NotificationSlideTemplate.class);
			if (libraryUpdated) {
				firePropertyChange(PreferencesDialog.PROPERTY_SLIDE_TEMPLATE_LIBRARY_CHANGED, null, null);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.application.slide.ui.SlideLibraryListener#slideLibraryChanged()
	 */
	@Override
	public void slideLibraryChanged() {
		SlideThumbnail[] thumbs = this.getSlideThumnails();
		// save the currently selected template
		SlideThumbnail selected = (SlideThumbnail)this.cmbTemplates.getSelectedItem();
		this.cmbTemplates.removeAllItems();
		for (SlideThumbnail thumb : thumbs) {
			this.cmbTemplates.addItem(thumb);
		}
		this.cmbTemplates.setSelectedItem(selected);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.preferences.ui.PreferencesEditor#applyPreferences()
	 */
	@Override
	public void applyPreferences() {
		Preferences preferences = Preferences.getInstance();
		NotificationPreferences nPreferences = preferences.getNotificationPreferences();
		
		// save this panel's settings
		nPreferences.setWaitPeriod(((Number)this.txtDefaultWaitPeriod.getValue()).intValue());
		
		// template
		SlideThumbnail thumbnail = ((SlideThumbnail)this.cmbTemplates.getSelectedItem());
		// check for the default template
		if (thumbnail.getFile() != SlideFile.NOT_STORED) {
			nPreferences.setTemplate(thumbnail.getFile().getRelativePath());
		} else {
			nPreferences.setTemplate(null);
		}
		
		// transitions
		nPreferences.setSendTransitionId(((Transition)this.cmbSendTransitions.getSelectedItem()).getId());
		nPreferences.setSendTransitionDuration(((Number)this.txtSendTransitions.getValue()).intValue());
		nPreferences.setSendTransitionEasingId(((Easing)this.cmbSendEasings.getSelectedItem()).getId());
		nPreferences.setClearTransitionId(((Transition)this.cmbClearTransitions.getSelectedItem()).getId());
		nPreferences.setClearTransitionDuration(((Number)this.txtClearTransitions.getValue()).intValue());
		nPreferences.setClearTransitionEasingId(((Easing)this.cmbClearEasings.getSelectedItem()).getId());
	}
}
