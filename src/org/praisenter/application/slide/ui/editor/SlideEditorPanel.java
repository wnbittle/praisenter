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
package org.praisenter.application.slide.ui.editor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.praisenter.application.icons.Icons;
import org.praisenter.application.preferences.Resolution;
import org.praisenter.application.preferences.Resolutions;
import org.praisenter.application.resources.Messages;
import org.praisenter.application.slide.ui.editor.command.BoundsCommand;
import org.praisenter.application.slide.ui.editor.command.ComponentBoundsCommandBeginArguments;
import org.praisenter.application.slide.ui.editor.command.MoveCommand;
import org.praisenter.application.slide.ui.editor.command.MutexCommandGroup;
import org.praisenter.application.slide.ui.editor.command.ResizeCommandBeginArguments;
import org.praisenter.application.slide.ui.editor.command.ResizeHeightCommand;
import org.praisenter.application.slide.ui.editor.command.ResizeOperation;
import org.praisenter.application.slide.ui.editor.command.ResizeProngLocation;
import org.praisenter.application.slide.ui.editor.command.ResizeWidthAndHeightCommand;
import org.praisenter.application.slide.ui.editor.command.ResizeWidthCommand;
import org.praisenter.application.slide.ui.editor.command.SlideBoundsCommandBeginArguments;
import org.praisenter.application.ui.SelectTextFocusListener;
import org.praisenter.common.utilities.FontManager;
import org.praisenter.common.utilities.LookAndFeelUtilities;
import org.praisenter.common.utilities.WindowUtilities;
import org.praisenter.media.MediaLibrary;
import org.praisenter.media.MediaType;
import org.praisenter.slide.AbstractPositionedSlide;
import org.praisenter.slide.BackgroundComponent;
import org.praisenter.slide.GenericComponent;
import org.praisenter.slide.PositionedComponent;
import org.praisenter.slide.RenderableComponent;
import org.praisenter.slide.Slide;
import org.praisenter.slide.SlideComponent;
import org.praisenter.slide.graphics.LinearGradientFill;
import org.praisenter.slide.media.AudioMediaComponent;
import org.praisenter.slide.media.ImageMediaComponent;
import org.praisenter.slide.media.VideoMediaComponent;
import org.praisenter.slide.text.DateTimeComponent;
import org.praisenter.slide.text.TextComponent;

/**
 * Panel used to edit a slide.
 * @author William Bittle
 * @version 2.0.2
 * @since 2.0.0
 */
public class SlideEditorPanel extends JPanel implements MouseMotionListener, MouseListener, EditorListener, ActionListener, ItemListener, DocumentListener {
	/** The version id */
	private static final long serialVersionUID = -927595042247907332L;

	/** The class level logger */
	private static final Logger LOGGER = Logger.getLogger(SlideEditorPanel.class);
	
	/** The generic editor panel card */
	private static final String GENERIC_CARD = "Generic";
	
	/** The text editor panel card */
	private static final String TEXT_CARD = "Text";
	
	/** The date/time editor panel card */
	private static final String DATE_CARD = "Date";
	
	/** The image editor panel card */
	private static final String IMAGE_CARD = "Image";
	
	/** The video editor panel card */
	private static final String VIDEO_CARD = "Video";
	
	/** The audio editor panel card */
	private static final String AUDIO_CARD = "Audio";
	
	/** A blank card */
	private static final String BLANK_CARD = "Blank";
	
	/** The background editor panel card */
	private static final String BACKGROUND_CARD = "Background";

	// data
	
	/** True if the slide/template has been changed */
	private boolean slideUpdated;
	
	// input
	
	/** The slide being edited */
	private Slide slide;

	// components
	
	/** The slide/template name */
	private JTextField txtSlideName;
	
	/** The list resolution targets */
	private JComboBox<Resolution> cmbResolutionTargets;
	
	/** A button to add resolutions */
	private JButton btnAddResolution;

	/** A button to remove a resolution */
	private JButton btnRemoveResolution;
	
	/** The list of components */
	private JComboBox<SlideComponent> cmbComponents;
	
	/** The panel used to preview the changes */
	private SlideEditorPreviewPanel pnlSlidePreview;
	
	/** Button to move the component back */
	private JButton btnMoveBack;
	
	/** Button to move the component forward */
	private JButton btnMoveForward;

	/** Button to remove the component */
	private JButton btnRemoveComponent;
	
	/** Button to add a new generic component */
	private JButton btnAddGenericComponent;
	
	/** Button to add a new text component */
	private JButton btnAddTextComponent;

	/** Button to add a new date component */
	private JButton btnAddDateComponent;
	
	/** Button to add a new image component */
	private JButton btnAddImageComponent;
	
	/** Button to add a new video component */
	private JButton btnAddVideoComponent;
	
	/** Button to add a new audio component */
	private JButton btnAddAudioComponent;
	
	/** Toggle button to snap components to a grid */
	private JToggleButton tglSnapToGrid;
	
	// editor panels
	
	/** Generic editor panel for {@link GenericComponent}s */
	private PositionedComponentEditor<GenericComponent> pnlGeneric;
	
	/** Text editor panel for {@link TextComponent}s */
	private TextComponentEditorPanel<TextComponent> pnlText;
	
	/** Date/time editor panel for {@link DateTimeComponent}s */
	private DateTimeComponentEditorPanel pnlDate;
	
	/** Image editor panel for {@link ImageMediaComponent}s */
	private ImageMediaComponentEditorPanel pnlImage;
	
	/** Video editor panel for {@link VideoMediaComponent}s */
	private VideoMediaComponentEditorPanel pnlVideo;
	
	/** Audio editor panel for {@link AudioMediaComponent}s */
	private AudioMediaComponentEditorPanel pnlAudio;
	
	/** Background editor panel */
	private BackgroundEditorPanel pnlBackground;
	
	/** Panel for the editor panels */
	private JPanel pnlEditorCards;
	
	/** The layout for the editor panel */
	private CardLayout layEditorCards;
	
	// mouse commands
	
	/** Mouse command for moving components */
	private MoveCommand moveCommand = new MoveCommand();
	
	/** Mouse command for resizing components */
	private ResizeWidthAndHeightCommand resizeCommand = new ResizeWidthAndHeightCommand();
	
	/** Mouse command for resizing the width of components */
	private ResizeWidthCommand resizeWidthCommand = new ResizeWidthCommand();
	
	/** Mouse command for resizing the height of components */
	private ResizeHeightCommand resizeHeightCommand = new ResizeHeightCommand();
	
	/** Mouse command mutually exclusive group */
	private MutexCommandGroup<BoundsCommand<?>> mouseCommandGroup = new MutexCommandGroup<BoundsCommand<?>>(new BoundsCommand[] {
			this.moveCommand,
			this.resizeCommand,
			this.resizeWidthCommand,
			this.resizeHeightCommand
	});
	
	// computed 

	/** The grid spacing */
	protected int gridSpacing = 20;
	
	/**
	 * Minimal constructor.
	 * @param slide the slide to edit
	 */
	public SlideEditorPanel(Slide slide) {
		this.slideUpdated = false;
		
		this.slide = slide;
		
		// get media support
		boolean imageSupport = MediaLibrary.isMediaSupported(MediaType.IMAGE);
		boolean videoSupport = MediaLibrary.isMediaSupported(MediaType.VIDEO);
		boolean audioSupport = MediaLibrary.isMediaSupported(MediaType.AUDIO);
		
		int width = slide.getWidth();
		int height = slide.getHeight();
		if (slide instanceof AbstractPositionedSlide) {
			AbstractPositionedSlide pSlide = (AbstractPositionedSlide)slide;
			width = pSlide.getDeviceWidth();
			height = pSlide.getDeviceHeight();
		}
		
		int dsw = (int)Math.floor(width * 0.45);
		int dsh = (int)Math.floor(height * 0.45);
		
		// compute grid spacing
		this.gridSpacing = width > height ? width / 32 : height / 32;
		
		// setup the preview panel
		Dimension previewSize = new Dimension(dsw, dsh);
		this.pnlSlidePreview = new SlideEditorPreviewPanel();
		this.pnlSlidePreview.setSlide(this.slide);
		this.pnlSlidePreview.setMinimumSize(previewSize);
		this.pnlSlidePreview.setPreferredSize(previewSize);
		this.pnlSlidePreview.addMouseListener(this);
		this.pnlSlidePreview.addMouseMotionListener(this);
		this.pnlSlidePreview.setGridSpacing(this.gridSpacing);
		
		JLabel lblSlideName = new JLabel(Messages.getString("panel.slide.editor.name"));
		this.txtSlideName = new JTextField(slide.getName());
		this.txtSlideName.getDocument().addDocumentListener(this);
		this.txtSlideName.addFocusListener(new SelectTextFocusListener(this.txtSlideName));
		
		// get the resolutions
		List<Resolution> resolutions = Resolutions.getResolutions();
		// add any window sizes to the targets
		GraphicsDevice[] devices = WindowUtilities.getDevices();
		for (int i = 0; i < devices.length; i++) {
			GraphicsDevice device = devices[i];
			DisplayMode mode = device.getDisplayMode();
			Resolution resolution = new Resolution(mode.getWidth(), mode.getHeight());
			// see if it already exists in the list
			boolean found = false;
			for (Resolution r : resolutions) {
				if (r.equals(resolution)) {
					found = true;
					break;
				}
			}
			if (!found) {
				resolutions.add(resolution);
			}
		}
		// make sure they are sorted
		Collections.sort(resolutions);
		
		JLabel lblResolutionTarget = new JLabel(Messages.getString("panel.slide.editor.resolution"));
		// create the targets drop down
		this.cmbResolutionTargets = new JComboBox<Resolution>(resolutions.toArray(new Resolution[0]));
		this.cmbResolutionTargets.setSelectedItem(new Resolution(width, height));
		this.cmbResolutionTargets.setToolTipText(Messages.getString("panel.slide.editor.resolution.tooltip"));
		this.cmbResolutionTargets.addItemListener(this);
		this.cmbResolutionTargets.setRenderer(new ResolutionListCellRenderer());
		
		this.btnAddResolution = new JButton(Messages.getString("panel.slide.editor.resolution.add"));
		this.btnAddResolution.setToolTipText(Messages.getString("panel.slide.editor.resolution.add.tooltip"));
		this.btnAddResolution.addActionListener(this);
		this.btnAddResolution.setActionCommand("add-resolution");
		
		this.btnRemoveResolution = new JButton(Messages.getString("panel.slide.editor.resolution.remove"));
		this.btnRemoveResolution.setToolTipText(Messages.getString("panel.slide.editor.resolution.remove.tooltip"));
		this.btnRemoveResolution.addActionListener(this);
		this.btnRemoveResolution.setActionCommand("remove-resolution");
		
		JPanel pnlTop = new JPanel();
		// attempt to use the border color of the laf
		Color color = Color.GRAY;
		if (LookAndFeelUtilities.isNimbusLookAndFeel()) {
			color = UIManager.getColor("nimbusBorder");
		} else {
			color = UIManager.getColor("Separator.foreground");
		}
		pnlTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, color));
		GroupLayout tLayout = new GroupLayout(pnlTop);
		pnlTop.setLayout(tLayout);
		
		tLayout.setAutoCreateContainerGaps(true);
		tLayout.setAutoCreateGaps(true);
		tLayout.setHorizontalGroup(tLayout.createSequentialGroup()
				.addComponent(lblSlideName)
				.addComponent(this.txtSlideName)
				.addComponent(lblResolutionTarget)
				.addComponent(this.cmbResolutionTargets)
				.addComponent(this.btnRemoveResolution)
				.addComponent(this.btnAddResolution));
		tLayout.setVerticalGroup(tLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lblSlideName)
				.addComponent(this.txtSlideName)
				.addComponent(lblResolutionTarget)
				.addComponent(this.cmbResolutionTargets)
				.addComponent(this.btnRemoveResolution)
				.addComponent(this.btnAddResolution));
		
		// get all the components on the slide/template
		List<SlideComponent> components = slide.getComponents(SlideComponent.class, true);
		this.cmbComponents = new JComboBox<SlideComponent>();
		this.cmbComponents.addItemListener(this);
		DefaultComboBoxModel<SlideComponent> model = new DefaultComboBoxModel<SlideComponent>();
		
		for (SlideComponent component : components) {
			// verify the component types are supported
			if (component instanceof ImageMediaComponent && !imageSupport) {
				LOGGER.warn("Image media support is missing. Cannot modify [" + component.getName() + "] on slide/template [" + slide.getName() + "]");
				continue;
			} else if (component instanceof VideoMediaComponent && !videoSupport) {
				LOGGER.warn("Video media support is missing. Cannot modify [" + component.getName() + "] on slide/template [" + slide.getName() + "]");
				continue;
			} else if (component instanceof AudioMediaComponent && !audioSupport) {
				LOGGER.warn("Audio media support is missing. Cannot modify [" + component.getName() + "] on slide/template [" + slide.getName() + "]");
				continue;
			}
			model.addElement(component);
		}
		this.cmbComponents.setModel(model);
		this.cmbComponents.setRenderer(new ComponentListCellRenderer());
		
		this.btnRemoveComponent = new JButton(Icons.REMOVE);
		this.btnRemoveComponent.setToolTipText(Messages.getString("panel.slide.editor.remove"));
		this.btnRemoveComponent.addActionListener(this);
		this.btnRemoveComponent.setActionCommand("remove");
		this.btnRemoveComponent.setEnabled(false);
		
		this.btnMoveBack = new JButton(Icons.MOVE_BACK);
		this.btnMoveBack.setToolTipText(Messages.getString("panel.slide.editor.moveBack"));
		this.btnMoveBack.addActionListener(this);
		this.btnMoveBack.setActionCommand("moveBack");
		this.btnMoveBack.setEnabled(false);
		
		this.btnMoveForward = new JButton(Icons.MOVE_FORWARD);
		this.btnMoveForward.setToolTipText(Messages.getString("panel.slide.editor.moveForward"));
		this.btnMoveForward.addActionListener(this);
		this.btnMoveForward.setActionCommand("moveForward");
		this.btnMoveForward.setEnabled(false);
		
		this.btnAddGenericComponent = new JButton(Icons.GENERIC_COMPONENT);
		this.btnAddGenericComponent.setToolTipText(Messages.getString("panel.slide.editor.add.generic"));
		this.btnAddGenericComponent.addActionListener(this);
		this.btnAddGenericComponent.setActionCommand("add-generic");
		
		this.btnAddTextComponent = new JButton(Icons.TEXT_COMPONENT);
		this.btnAddTextComponent.setToolTipText(Messages.getString("panel.slide.editor.add.text"));
		this.btnAddTextComponent.addActionListener(this);
		this.btnAddTextComponent.setActionCommand("add-text");

		this.btnAddDateComponent = new JButton(Icons.DATE_COMPONENT);
		this.btnAddDateComponent.setToolTipText(Messages.getString("panel.slide.editor.add.date"));
		this.btnAddDateComponent.addActionListener(this);
		this.btnAddDateComponent.setActionCommand("add-date");
		
		this.btnAddImageComponent = new JButton(Icons.IMAGE_COMPONENT);
		this.btnAddImageComponent.setToolTipText(Messages.getString("panel.slide.editor.add.image"));
		this.btnAddImageComponent.addActionListener(this);
		this.btnAddImageComponent.setActionCommand("add-image");
		if (!imageSupport) {
			this.btnAddImageComponent.setEnabled(false);
		}
		
		this.btnAddVideoComponent = new JButton(Icons.VIDEO_COMPONENT);
		this.btnAddVideoComponent.setToolTipText(Messages.getString("panel.slide.editor.add.video"));
		this.btnAddVideoComponent.addActionListener(this);
		this.btnAddVideoComponent.setActionCommand("add-video");
		if (!videoSupport) {
			this.btnAddVideoComponent.setEnabled(false);
		}
		
		this.btnAddAudioComponent = new JButton(Icons.AUDIO_COMPONENT);
		this.btnAddAudioComponent.setToolTipText(Messages.getString("panel.slide.editor.add.audio"));
		this.btnAddAudioComponent.addActionListener(this);
		this.btnAddAudioComponent.setActionCommand("add-audio");
		if (!audioSupport) {
			this.btnAddAudioComponent.setEnabled(false);
		}
		
		this.tglSnapToGrid = new JToggleButton(Icons.SNAP);
		this.tglSnapToGrid.setToolTipText(Messages.getString("panel.slide.editor.snap"));
		this.tglSnapToGrid.setEnabled(false);
		
		JPanel pnlComponentLists = new JPanel();
		GroupLayout clLayout = new GroupLayout(pnlComponentLists);
		pnlComponentLists.setLayout(clLayout);
		
		clLayout.setAutoCreateGaps(true);
		clLayout.setAutoCreateContainerGaps(true);
		clLayout.setHorizontalGroup(clLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(this.cmbComponents, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(clLayout.createSequentialGroup()
						.addComponent(this.btnMoveBack)
						.addComponent(this.btnMoveForward)
						.addComponent(this.btnRemoveComponent)
						.addComponent(this.tglSnapToGrid))
				.addGroup(clLayout.createSequentialGroup()
						.addComponent(this.btnAddGenericComponent)
						.addComponent(this.btnAddTextComponent)
						.addComponent(this.btnAddDateComponent)
						.addComponent(this.btnAddImageComponent)
						.addComponent(this.btnAddVideoComponent)
						.addComponent(this.btnAddAudioComponent)));
		clLayout.setVerticalGroup(clLayout.createSequentialGroup()
				.addComponent(this.cmbComponents, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(clLayout.createParallelGroup()
						.addComponent(this.btnMoveBack)
						.addComponent(this.btnMoveForward)
						.addComponent(this.btnRemoveComponent)
						.addComponent(this.tglSnapToGrid))
				.addGroup(clLayout.createParallelGroup()
						.addComponent(this.btnAddGenericComponent)
						.addComponent(this.btnAddTextComponent)
						.addComponent(this.btnAddDateComponent)
						.addComponent(this.btnAddImageComponent)
						.addComponent(this.btnAddVideoComponent)
						.addComponent(this.btnAddAudioComponent)));
		
		this.pnlGeneric = new PositionedComponentEditor<>();
		this.pnlGeneric.addEditorListener(this);
		
		this.pnlText = new TextComponentEditorPanel<TextComponent>();
		this.pnlText.addEditorListener(this);
		
		this.pnlDate = new DateTimeComponentEditorPanel();
		this.pnlDate.addEditorListener(this);
		
		this.pnlImage = new ImageMediaComponentEditorPanel();
		this.pnlImage.addEditorListener(this);
		
		this.pnlVideo = new VideoMediaComponentEditorPanel();
		this.pnlVideo.addEditorListener(this);
		
		this.pnlAudio = new AudioMediaComponentEditorPanel();
		this.pnlAudio.addEditorListener(this);
		
		this.pnlBackground = new BackgroundEditorPanel();
		this.pnlBackground.addEditorListener(this);
		
		JPanel pnlBlank = new JPanel();
		pnlBlank.setLayout(new BorderLayout());
		JLabel lblSelect = new JLabel(Messages.getString("panel.slide.editor.selectComponent"));
		lblSelect.setAlignmentX(CENTER_ALIGNMENT);
		lblSelect.setHorizontalAlignment(SwingConstants.CENTER);
		Font font = lblSelect.getFont();
		lblSelect.setFont(font.deriveFont(Font.BOLD, 1.5f * font.getSize()));
		pnlBlank.add(lblSelect);
		
		this.pnlEditorCards = new JPanel();
		this.layEditorCards = new CardLayout();
		this.pnlEditorCards.setLayout(this.layEditorCards);
		
		this.pnlEditorCards.add(pnlBlank, BLANK_CARD);
		this.pnlEditorCards.add(this.pnlGeneric, GENERIC_CARD);
		this.pnlEditorCards.add(this.pnlText, TEXT_CARD);
		this.pnlEditorCards.add(this.pnlDate, DATE_CARD);
		this.pnlEditorCards.add(this.pnlImage, IMAGE_CARD);
		this.pnlEditorCards.add(this.pnlVideo, VIDEO_CARD);
		this.pnlEditorCards.add(this.pnlAudio, AUDIO_CARD);
		this.pnlEditorCards.add(this.pnlBackground, BACKGROUND_CARD);
		
		JSplitPane splEditorControls = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splEditorControls.setTopComponent(pnlComponentLists);
		splEditorControls.setBottomComponent(this.pnlEditorCards);
		splEditorControls.setBorder(null);
		
		JSplitPane splPreviewEditor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splPreviewEditor.setLeftComponent(splEditorControls);
		splPreviewEditor.setRightComponent(this.pnlSlidePreview);
		splPreviewEditor.setBorder(null);
		
		this.setLayout(new BorderLayout());
		this.add(pnlTop, BorderLayout.PAGE_START);
		this.add(splPreviewEditor, BorderLayout.CENTER);
		
		// add a key binding for the delete key to both the preview panel and the jlist
		String deleteActionName = "deleteAction";
		@SuppressWarnings("serial")
		AbstractAction deleteAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SlideEditorPanel.this.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, "remove"));
			}
		};
		this.cmbComponents.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteActionName);
		this.cmbComponents.getActionMap().put(deleteActionName, deleteAction);
		this.pnlSlidePreview.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteActionName);
		this.pnlSlidePreview.getActionMap().put(deleteActionName, deleteAction);
		
		// make sure the first item is selected
		this.itemStateChanged(new ItemEvent(this.cmbComponents, ItemEvent.ITEM_STATE_CHANGED, 0, ItemEvent.SELECTED));
	}
	
	/**
	 * Returns the top level component that is at the given point.
	 * @param point the Slide space point
	 * @return {@link PositionedComponent}
	 */
	protected PositionedComponent getComponentAtPoint(Point point) {
		if (this.slide != null) {
			List<PositionedComponent> components = this.slide.getComponents(PositionedComponent.class);
			// reverse the list so that we find the highest component first
			Collections.reverse(components);
			for (PositionedComponent component : components) {
				if (this.isInside(point, component)) {
					return component;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns true if the given point is inside any component on the slide.
	 * @param point the Slide space point
	 * @return boolean
	 */
	protected boolean isInside(Point point) {
		if (this.slide != null) {
			List<PositionedComponent> components = this.slide.getComponents(PositionedComponent.class);
			for (PositionedComponent component : components) {
				if (this.isInside(point, component)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the given point is inside the given component.
	 * @param point the Slide space point
	 * @param component the component
	 * @return boolean
	 */
	protected boolean isInside(Point point, PositionedComponent component) {
		// get the bounds
		Rectangle bounds = component.getRectangleBounds();
		
		// we need to include the extra space caused by the resize prongs
		int rps = this.pnlSlidePreview.getProngSize();
		int hrps = (rps + 1) / 2;
		bounds.translate(-hrps, -hrps);
		bounds.width += rps;
		bounds.height += rps;
		
		// we also need to translate by the slide position (if applicable)
		if (this.slide instanceof AbstractPositionedSlide) {
			AbstractPositionedSlide slide = (AbstractPositionedSlide)this.slide;
			bounds.x += slide.getX();
			bounds.y += slide.getY();
		}
		
		// see if the bounds contains the point
		return bounds.contains(point);
	}
	
	/**
	 * Returns true if the given point is inside the slide.
	 * @param point the Slide space point
	 * @return boolean
	 */
	protected boolean isInsideSlide(Point point) {
		Rectangle bounds = new Rectangle(0, 0, this.slide.getWidth(), this.slide.getHeight());
		
		// we need to include the extra space caused by the resize prongs
		int rps = this.pnlSlidePreview.getProngSize();
		int hrps = (rps + 1) / 2;
		bounds.translate(-hrps, -hrps);
		bounds.width += rps;
		bounds.height += rps;
		
		// we also need to translate by the slide position (if applicable)
		if (this.slide instanceof AbstractPositionedSlide) {
			AbstractPositionedSlide slide = (AbstractPositionedSlide)this.slide;
			bounds.x += slide.getX();
			bounds.y += slide.getY();
		}
		
		// see if the bounds contains the point
		return bounds.contains(point);
	}
	
	/**
	 * Returns a new rectangle for the given resize prong location for the given component.
	 * <p>
	 * Returns null if the given location is null.
	 * @param location the prong location
	 * @param bounds the rectangular bounds
	 * @return Rectangle
	 */
	protected Rectangle getResizeProng(ResizeProngLocation location, Rectangle bounds) {
		int s = this.pnlSlidePreview.getProngSize();
		int hs = (s + 1) / 2;
		int x = 0;
		int y = 0;
		
		if (location == ResizeProngLocation.BOTTOM) {
			x = bounds.x + bounds.width / 2 - hs;
			y = bounds.y + bounds.height - hs;
		} else if (location == ResizeProngLocation.BOTTOM_LEFT) {
			x = bounds.x - hs;
			y = bounds.y + bounds.height - hs;
		} else if (location == ResizeProngLocation.BOTTOM_RIGHT) {
			x = bounds.x + bounds.width - hs;
			y = bounds.y + bounds.height - hs;
		} else if (location == ResizeProngLocation.LEFT) {
			x = bounds.x - hs;
			y = bounds.y + bounds.height / 2 - hs;
		} else if (location == ResizeProngLocation.RIGHT) {
			x = bounds.x + bounds.width - hs;
			y = bounds.y + bounds.height / 2 - hs;
		} else if (location == ResizeProngLocation.TOP) {
			x = bounds.x + bounds.width / 2 - hs;
			y = bounds.y - hs;
		} else if (location == ResizeProngLocation.TOP_LEFT) {
			x = bounds.x - hs;
			y = bounds.y - hs;
		} else if (location == ResizeProngLocation.TOP_RIGHT) {
			x = bounds.x + bounds.width - hs;
			y = bounds.y - hs;
		} else {
			return null;
		}
		
		return new Rectangle(x, y, s, s);
	}
	
	/**
	 * Returns the {@link ResizeProngLocation} for the given point on the given component.
	 * <p>
	 * Returns null if the point is not over a resize prong.
	 * @param point the slide space point
	 * @param component the component
	 * @return {@link ResizeProngLocation}
	 */
	protected ResizeProngLocation getResizeProngLocation(Point point, PositionedComponent component) {
		Rectangle prong = null;
		for (ResizeProngLocation location : ResizeProngLocation.values()) {
			prong = this.getResizeProng(location, component.getRectangleBounds());
			// include the slide position
			if (this.slide instanceof AbstractPositionedSlide) {
				AbstractPositionedSlide slide = (AbstractPositionedSlide)this.slide;
				prong.x += slide.getX();
				prong.y += slide.getY();
			}
			if (prong.contains(point)) {
				return location;
			}
		}
		return null;
	}
	
	/**
	 * Returns the {@link ResizeProngLocation} for the slide.
	 * <p>
	 * Returns null if the point is not over a resize prong.
	 * @param point the slide space point
	 * @return {@link ResizeProngLocation}
	 */
	protected ResizeProngLocation getResizeProngLocation(Point point) {
		int x = 0;
		int y = 0;
		if (this.slide instanceof AbstractPositionedSlide) {
			AbstractPositionedSlide slide = (AbstractPositionedSlide)this.slide;
			x = slide.getX();
			y = slide.getY();
		}
		
		Rectangle prong = null;
		Rectangle bounds = new Rectangle(x, y, this.slide.getWidth(), this.slide.getHeight());
		for (ResizeProngLocation location : ResizeProngLocation.values()) {
			prong = this.getResizeProng(location, bounds);
			if (prong.contains(point)) {
				return location;
			}
		}
		return null;
	}

	/**
	 * Returns a rounded value.
	 * @param value the value to normalize
	 * @param factor the normalization factor
	 * @return int
	 */
	private static final int round(int value, int factor) {
		return (value / factor) * factor;
	}

	/**
	 * Returns a rounded value.
	 * <p>
	 * If the modulus of the value and factor is greater than half of factor
	 * the value is rounded up, otherwise its rounded down.
	 * @param value the value to normalize
	 * @param factor the normalization factor
	 * @return int
	 */
	private static final int roundAtHalf(int value, int factor) {
		int mod = value % factor;
		if (mod == 0) return value;
		if (mod < (factor / 2)) {
			return (value / factor) * factor;
		} else {
			return ((value / factor) + 1) * factor;
		}
	}
	
	/**
	 * Returns a point on the grid closest to the given point.
	 * @param point the point
	 * @return Point
	 */
	protected Point getSnapToGridPoint(Point point) {
		int gs = this.gridSpacing;
		int x = point.x;
		int y = point.y;
		
		x = roundAtHalf(x, gs);
		y = roundAtHalf(y, gs);
		
		return new Point(x, y);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		if (!this.mouseCommandGroup.isCommandActive()) {
			this.setCursor(Cursor.getDefaultCursor());
			this.pnlSlidePreview.setMouseOverComponent(null);
			this.pnlSlidePreview.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// check for mouse button 1
		if (e.getButton() == MouseEvent.BUTTON1) {
			// make sure no other command is active
			if (!this.mouseCommandGroup.isCommandActive()) {
				// convert the point to display space
				Point point = this.pnlSlidePreview.getSlideSpacePoint(e.getPoint());
				PositionedComponent cMouseOver = this.pnlSlidePreview.getMouseOverComponent();
				PositionedComponent cSelected = this.pnlSlidePreview.getSelectedComponent();
				PositionedComponent component = null;
				// see if we are still over the same component
				if (cSelected != null && this.isInside(point, cSelected)) {
					// then set the component as the one we are still over
					component = cSelected;
				} else if (cMouseOver != null && this.isInside(point, cMouseOver)) {
					component = cMouseOver;
				} else {
					// otherwise see if we are over a different one
					component = this.getComponentAtPoint(point);
				}
				// make sure we found one
				if (this.slide != null && component != null) {
					// see what command we need to begin
					ResizeProngLocation location = this.getResizeProngLocation(point, component);
					if (this.tglSnapToGrid.isSelected()) {
						point = this.getSnapToGridPoint(point);
						// normalize the component coordinates and height/width
						component.setX(round(component.getX(), this.gridSpacing));
						component.setY(round(component.getY(), this.gridSpacing));
						component.setWidth(round(component.getWidth(), this.gridSpacing));
						component.setHeight(round(component.getHeight(), this.gridSpacing));
					}
					ComponentBoundsCommandBeginArguments bArgs = new ComponentBoundsCommandBeginArguments(point, component);
					if (location != null) {
						ResizeCommandBeginArguments rArgs = new ResizeCommandBeginArguments(bArgs, location);
						if (location.getResizeOperation() == ResizeOperation.BOTH) {
							this.resizeCommand.begin(rArgs);
						} else if (location.getResizeOperation() == ResizeOperation.WIDTH) {
							this.resizeWidthCommand.begin(rArgs);
						} else if (location.getResizeOperation() == ResizeOperation.HEIGHT) {
							this.resizeHeightCommand.begin(rArgs);
						}
					} else {
						this.moveCommand.begin(bArgs);
					}
					
					this.pnlSlidePreview.setSelectedComponent(component);
					this.cmbComponents.setSelectedItem(component);
					this.pnlSlidePreview.repaint();
				// now check if they are wanting the background component
				} else if (this.slide != null && this.isInsideSlide(point)) {
					// assume they want to select the background component
					this.pnlSlidePreview.setSelectedBackgroundComponent(this.slide.getBackground());
					this.cmbComponents.setSelectedItem(this.slide.getBackground());
					this.pnlSlidePreview.repaint();
					// now if its a positioned slide, then we need to show prongs
					if (this.slide instanceof AbstractPositionedSlide) {
						AbstractPositionedSlide aps = (AbstractPositionedSlide)this.slide;
						// see what command we need to begin
						ResizeProngLocation location = this.getResizeProngLocation(point);
						if (this.tglSnapToGrid.isSelected()) {
							point = this.getSnapToGridPoint(point);
							// normalize the component coordinates and height/width
							aps.setX(round(aps.getX(), this.gridSpacing));
							aps.setY(round(aps.getY(), this.gridSpacing));
							aps.setWidth(round(aps.getWidth(), this.gridSpacing));
							aps.setHeight(round(aps.getHeight(), this.gridSpacing));
						}
						SlideBoundsCommandBeginArguments bArgs = new SlideBoundsCommandBeginArguments(point, aps);
						if (location != null) {
							ResizeCommandBeginArguments rArgs = new ResizeCommandBeginArguments(bArgs, location);
							if (location.getResizeOperation() == ResizeOperation.BOTH) {
								this.resizeCommand.begin(rArgs);
							} else if (location.getResizeOperation() == ResizeOperation.WIDTH) {
								this.resizeWidthCommand.begin(rArgs);
							} else if (location.getResizeOperation() == ResizeOperation.HEIGHT) {
								this.resizeHeightCommand.begin(rArgs);
							}
						} else {
							this.moveCommand.begin(bArgs);
						}
					}
				} else {
					this.pnlSlidePreview.setSelectedComponent(null);
					this.pnlSlidePreview.setSelectedBackgroundComponent(null);
					this.cmbComponents.setSelectedItem(null);
					this.layEditorCards.show(this.pnlEditorCards, BLANK_CARD);
					this.pnlSlidePreview.repaint();
				}
				// request focus for the panel so that the key bindings work
				this.pnlSlidePreview.requestFocus();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// check for mouse button 1
		if (e.getButton() == MouseEvent.BUTTON1) {
			// end all the mouse button 1 commands
			this.moveCommand.end();
			this.resizeCommand.end();
			this.resizeWidthCommand.end();
			this.resizeHeightCommand.end();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// see if any mouse command is active
		if (this.mouseCommandGroup.isCommandActive()) {
			// convert the point to display space
			Point end = this.pnlSlidePreview.getSlideSpacePoint(e.getPoint());
			if (this.tglSnapToGrid.isSelected()) {
				end = this.getSnapToGridPoint(end);
			}
			// check if we are still over a component
			if (this.moveCommand.isActive()) {
				this.moveCommand.update(end);
				this.slideUpdated = true;
			} else if (this.resizeCommand.isActive()) {
				this.resizeCommand.update(end);
				this.slideUpdated = true;
			} else if (this.resizeWidthCommand.isActive()) {
				this.resizeWidthCommand.update(end);
				this.slideUpdated = true;
			} else if (this.resizeHeightCommand.isActive()) {
				this.resizeHeightCommand.update(end);
				this.slideUpdated = true;
			}
			// if we modify a component then we need to redraw it
			this.pnlSlidePreview.repaint();
			// request focus for the panel so that the key bindings work
			this.pnlSlidePreview.requestFocus();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// make sure no other command is active in this group
		if (!this.mouseCommandGroup.isCommandActive()) {
			// convert the point to display space
			Point point = this.pnlSlidePreview.getSlideSpacePoint(e.getPoint());
			PositionedComponent cMouseOver = this.pnlSlidePreview.getMouseOverComponent();
			PositionedComponent cSelected = this.pnlSlidePreview.getSelectedComponent();
			PositionedComponent component = null;
			// see if we are still over the same component
			if (cSelected != null && this.isInside(point, cSelected)) {
				// then set the component as the one we are still over
				component = cSelected;
			} else {
				// otherwise see if we are over a different one
				component = this.getComponentAtPoint(point);
			}
			// make sure we found one
			if (this.slide != null && component != null) {
				// set the hover component
				if (component != cMouseOver) {
					this.pnlSlidePreview.setMouseOverComponent(component);
					this.pnlSlidePreview.repaint();
				}
				// see if we need to change the cursor
				if (component == cSelected) {
					ResizeProngLocation location = this.getResizeProngLocation(point, component);
					this.setCursorByResizeProngLocation(location);
				} else {
					this.setCursorByResizeProngLocation(null);
				}
			} else {
				this.pnlSlidePreview.setMouseOverComponent(null);
				this.pnlSlidePreview.repaint();
				
				// check the background
				if (this.slide != null && this.slide instanceof AbstractPositionedSlide) {
					// see if we are inside the slide
					if (this.isInsideSlide(point)) {
						// see if we need to change the cursor
						ResizeProngLocation location = this.getResizeProngLocation(point);
						this.setCursorByResizeProngLocation(location);
					} else {
						this.setCursor(Cursor.getDefaultCursor());
					}
				} else {
					this.setCursor(Cursor.getDefaultCursor());
				}
			}
		}
	}
	
	/**
	 * Sets the mouse cursor to a resize or move cursor depending on the given resize prong location.
	 * @param location the prong location
	 */
	private void setCursorByResizeProngLocation(ResizeProngLocation location) {
		if (location == ResizeProngLocation.BOTTOM) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.BOTTOM_LEFT) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.BOTTOM_RIGHT) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.LEFT) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.RIGHT) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.TOP) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.TOP_LEFT) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
		} else if (location == ResizeProngLocation.TOP_RIGHT) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
		} else {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("remove".equals(command)) {
			SlideComponent component = (SlideComponent)this.cmbComponents.getSelectedItem();
			// make sure a component is selected
			if (component != null && this.slide != null) {
				// make sure its not a static component
				if (!this.slide.isStaticComponent(component)) {
					// make sure the user really wants to
					int choice = JOptionPane.showConfirmDialog(
									WindowUtilities.getParentWindow(this), 
									Messages.getString("panel.slide.editor.remove.component.text"), 
									Messages.getString("panel.slide.editor.remove.component.title"), 
									JOptionPane.YES_NO_CANCEL_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						// remove the component from the slide and the list
						DefaultComboBoxModel<SlideComponent> model = (DefaultComboBoxModel<SlideComponent>)this.cmbComponents.getModel();
						this.pnlSlidePreview.setSelectedComponent(null);
						model.removeElement(component);
						this.slide.removeComponent(component);
						this.pnlSlidePreview.repaint();
						this.slideUpdated = true;
					}
				}
			}
		} else if ("add-generic".equals(command)) {
			if (this.slide != null) {
				GenericComponent component = new GenericComponent(
						Messages.getString("panel.slide.editor.new.name"), 
						50, 
						50, 
						this.slide.getWidth() / 2, 
						this.slide.getHeight() / 2);
				component.setBackgroundFill(new LinearGradientFill());
				component.setBackgroundVisible(true);
				this.onAddComponent(component);
			}
		} else if ("add-text".equals(command)) {
			if (this.slide != null) {
				TextComponent component = new TextComponent(
						Messages.getString("panel.slide.editor.new.name"), 
						50, 
						50, 
						this.slide.getWidth() / 2, 
						this.slide.getHeight() / 2,
						Messages.getString("panel.slide.editor.new.text"));
				component.setTextFont(FontManager.getDefaultFont().deriveFont(30.0f));
				this.onAddComponent(component);
			}
		} else if ("add-date".equals(command)) {
			if (this.slide != null) {
				DateTimeComponent component = new DateTimeComponent(
						Messages.getString("panel.slide.editor.new.name"), 
						50, 
						50, 
						this.slide.getWidth() / 2, 
						100);
				component.setTextFont(FontManager.getDefaultFont().deriveFont(50.0f));
				this.onAddComponent(component);
			}
		} else if ("add-image".equals(command)) {
			if (this.slide != null) {
				ImageMediaComponent component = new ImageMediaComponent(
						Messages.getString("panel.slide.editor.new.name"),
						null,
						50, 
						50, 
						this.slide.getWidth() / 2, 
						this.slide.getHeight() / 2);
				this.onAddComponent(component);
			}
		} else if ("add-video".equals(command)) {
			if (this.slide != null) {
				VideoMediaComponent component = new VideoMediaComponent(
						Messages.getString("panel.slide.editor.new.name"),
						null,
						50, 
						50, 
						this.slide.getWidth() / 2, 
						this.slide.getHeight() / 2);
				this.onAddComponent(component);
			}
		} else if ("add-audio".equals(command)) {
			if (this.slide != null) {
				AudioMediaComponent component = new AudioMediaComponent(
						Messages.getString("panel.slide.editor.new.name"),
						null);
				this.onAddComponent(component);
			}
		} else if ("moveBack".equals(command)) {
			SlideComponent component = (SlideComponent)this.cmbComponents.getSelectedItem();
			if (component != this.slide.getBackground() && component instanceof RenderableComponent) {
				RenderableComponent rc = (RenderableComponent)component;
				this.slide.moveComponentDown(rc);
				this.pnlSlidePreview.repaint();
				this.slideUpdated = true;
			}
		} else if ("moveForward".equals(command)) {
			SlideComponent component = (SlideComponent)this.cmbComponents.getSelectedItem();
			if (component != this.slide.getBackground() && component instanceof RenderableComponent) {
				RenderableComponent rc = (RenderableComponent)component;
				this.slide.moveComponentUp(rc);
				this.pnlSlidePreview.repaint();
				this.slideUpdated = true;
			}
		} else if ("add-resolution".equals(command)) {
			// show the custom resolution dialog
			final Resolution resolution = AddResolutionDialog.show(WindowUtilities.getParentWindow(this));
			if (resolution != null) {
				// see where it needs to go in the list of resolutions
				int n = this.cmbResolutionTargets.getItemCount();
				int p = -1;
				for (int i = 0; i < n; i++) {
					Resolution r = this.cmbResolutionTargets.getItemAt(i);
					if (r.equals(resolution)) {
						// it already exists
						return;
					}
					if (resolution.compareTo(r) < 0) {
						p = i;
						break;
					}
				}
				if (p < 0) {
					this.cmbResolutionTargets.addItem(resolution);
				} else {
					this.cmbResolutionTargets.insertItemAt(resolution, p);
				}
				
				if (this.cmbResolutionTargets.getItemCount() > 1) {
					this.btnRemoveResolution.setEnabled(true);
				}
				
				// add it to the config in another thread
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Resolutions.addResolution(resolution);
							// just log it if it fails
						} catch (JAXBException ex) {
							LOGGER.error("Failed to save resolutions (JAXB) after adding a new resolution: " + resolution, ex);
						} catch (IOException ex) {
							LOGGER.error("Failed to save resolutions (IO) after adding a new resolution: " + resolution, ex);
						}
					}
				});
				t.setDaemon(true);
				t.start();
			}
		} else if ("remove-resolution".equals(command)) {
			// remove the currently selected resolution
			final Resolution resolution = (Resolution)this.cmbResolutionTargets.getSelectedItem();
			this.cmbResolutionTargets.removeItem(resolution);
			
			if (this.cmbResolutionTargets.getItemCount() <= 1) {
				this.btnRemoveResolution.setEnabled(false);
			}
			
			// remove it from the config in another thread
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Resolutions.removeResolution(resolution);
						// just log it if it fails
					} catch (JAXBException ex) {
						LOGGER.error("Failed to save resolutions (JAXB) after removing the resolution: " + resolution, ex);
					} catch (IOException ex) {
						LOGGER.error("Failed to save resolutions (IO) after removing the resolution: " + resolution, ex);
					}
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}
	
	/**
	 * Adds the given component to the slide and components listing and make it the selected component.
	 * @param component the new component
	 */
	private void onAddComponent(SlideComponent component) {
		// add to the slide
		this.slide.addComponent(component);
		this.slideUpdated = true;
		
		// add to the jlist
		DefaultComboBoxModel<SlideComponent> model = (DefaultComboBoxModel<SlideComponent>)this.cmbComponents.getModel();
		// we need to remove all and add all components since the ordering can change if they add an audio component
		if (component instanceof AudioMediaComponent) {
			model.removeAllElements();
			// get all the components on the slide/template
			List<SlideComponent> components = this.slide.getComponents(SlideComponent.class);
			// add in the background component
			components.add(0, this.slide.getBackground());
			for (SlideComponent cmp : components) {
				model.addElement(cmp);
			}
		} else {
			// otherwise we can just add it at the end of the list
			model.addElement(component);
		}
		
		// set the new component as the selected component
		this.cmbComponents.setSelectedItem(component);
		
		// repaint the preview panel
		this.pnlSlidePreview.repaint();
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.ui.editor.EditorListener#editPerformed(org.praisenter.slide.ui.editor.EditEvent)
	 */
	@Override
	public void editPerformed(EditEvent event) {
		if (event.getSource() == this.pnlBackground) {
			// check if the background has been changed
			BackgroundComponent oldbg = this.slide.getBackground();
			BackgroundComponent newbg = this.pnlBackground.getSlideComponent();
			if (newbg != oldbg) {
				// if it has then we need to set it and update the component drop down
				this.slide.setBackground(newbg);
				DefaultComboBoxModel<SlideComponent> model = (DefaultComboBoxModel<SlideComponent>)this.cmbComponents.getModel();
				// replace the item
				int index = model.getIndexOf(oldbg);
				model.insertElementAt(newbg, index);
				model.removeElementAt(index + 1);
			}
		}
		this.pnlSlidePreview.repaint();
		this.cmbComponents.repaint();
		this.slideUpdated = true;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object source = e.getSource();
			if (source == this.cmbResolutionTargets) {
				Resolution resolution = (Resolution)e.getItem();
				if (this.slide != null) {
					// adjust the current slide
					this.slide.adjustSize(resolution.getWidth(), resolution.getHeight());
					this.pnlSlidePreview.repaint();
					this.slideUpdated = true;
				}
			} else if (source == this.cmbComponents) {
				if (this.slide == null) return;
				SlideComponent component = (SlideComponent)this.cmbComponents.getSelectedItem();
				if (component != null) {
					boolean isBackground = component instanceof BackgroundComponent && this.slide.getBackground() == component;
					boolean isStatic = this.slide.isStaticComponent(component);
					
					if (isBackground) {
						this.pnlBackground.setSlideComponent(this.slide.getBackground(), isStatic);
						this.layEditorCards.show(this.pnlEditorCards, BACKGROUND_CARD);
					} else if (component instanceof DateTimeComponent) {
						this.pnlDate.setSlideComponent((DateTimeComponent)component, isStatic);
						this.layEditorCards.show(this.pnlEditorCards, DATE_CARD);
					} else if (component instanceof TextComponent) {
						this.pnlText.setSlideComponent((TextComponent)component, isStatic);
						this.layEditorCards.show(this.pnlEditorCards, TEXT_CARD);
					} else if (component instanceof ImageMediaComponent) {
						this.pnlImage.setSlideComponent((ImageMediaComponent)component, isStatic);
						this.layEditorCards.show(this.pnlEditorCards, IMAGE_CARD);
					} else if (component instanceof VideoMediaComponent) {
						this.pnlVideo.setSlideComponent((VideoMediaComponent)component, isStatic);
						this.layEditorCards.show(this.pnlEditorCards, VIDEO_CARD);
					} else if (component instanceof AudioMediaComponent) {
						this.pnlAudio.setSlideComponent((AudioMediaComponent)component, isStatic);
						this.layEditorCards.show(this.pnlEditorCards, AUDIO_CARD);
					} else if (component instanceof GenericComponent) {
						this.pnlGeneric.setSlideComponent((GenericComponent)component, isStatic);
						this.layEditorCards.show(this.pnlEditorCards, GENERIC_CARD);
					} else {
						// show the blank card with a "select a control label"
						this.layEditorCards.show(this.pnlEditorCards, BLANK_CARD);
					}
					
					if (!isBackground) {
						if (component instanceof PositionedComponent) {
							this.pnlSlidePreview.setSelectedComponent((PositionedComponent)component);
						} else {
							this.pnlSlidePreview.setSelectedComponent(null);
						}
						this.pnlSlidePreview.setSelectedBackgroundComponent(null);
					} else {
						this.pnlSlidePreview.setSelectedBackgroundComponent((BackgroundComponent)component);
						this.pnlSlidePreview.setSelectedComponent(null);
					}
					
					if (component instanceof RenderableComponent && !isBackground) {
						this.btnMoveBack.setEnabled(true);
						this.btnMoveForward.setEnabled(true);
					} else {
						this.btnMoveBack.setEnabled(false);
						this.btnMoveForward.setEnabled(false);
					}
					
					if (component instanceof RenderableComponent) {
						if (isBackground) {
							if (this.slide instanceof AbstractPositionedSlide) {
								this.tglSnapToGrid.setEnabled(true);
							} else {
								this.tglSnapToGrid.setEnabled(false);
							}
						} else {
							this.tglSnapToGrid.setEnabled(true);
						}
					} else {
						this.tglSnapToGrid.setEnabled(false);
					}
					
					this.btnRemoveComponent.setEnabled(!isStatic);
				}
				this.pnlSlidePreview.repaint();
			}
		// handle setting the selected item to null
		} else if (e.getStateChange() == ItemEvent.DESELECTED && e.getSource() == this.cmbComponents) {
			this.pnlSlidePreview.setSelectedComponent(null);
			this.pnlSlidePreview.setSelectedBackgroundComponent(null);
			this.layEditorCards.show(this.pnlEditorCards, BLANK_CARD);
			
			this.btnMoveBack.setEnabled(false);
			this.btnMoveForward.setEnabled(false);
			this.btnRemoveComponent.setEnabled(false);
			this.tglSnapToGrid.setEnabled(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		if (this.txtSlideName.getDocument() == e.getDocument()) {
			if (this.slide != null) {
				this.slide.setName(this.txtSlideName.getText());
				this.slideUpdated = true;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		if (this.txtSlideName.getDocument() == e.getDocument()) {
			if (this.slide != null) {
				this.slide.setName(this.txtSlideName.getText());
				this.slideUpdated = true;
			}
		}
	}
	
	/**
	 * Returns true if the slide was changed.
	 * @return boolean
	 */
	public boolean isSlideUpdated() {
		return this.slideUpdated;
	}
	
	/**
	 * Custom list cell renderer for {@link SlideComponent}s.
	 * @author William Bittle
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public class ComponentListCellRenderer extends DefaultListCellRenderer {	
		/** The version id */
		private static final long serialVersionUID = 3300876219352845322L;
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof SlideComponent) {
				SlideComponent sc = (SlideComponent)value;
				if (slide != null && slide.isStaticComponent(sc)) {
					this.setText(MessageFormat.format(Messages.getString("panel.slide.editor.staticComponent"), sc.getName()));
					this.setToolTipText(Messages.getString("panel.slide.editor.staticComponent.tooltip"));
				} else {
					this.setText(sc.getName());
					this.setToolTipText(null);
				}
			}
			return this;
		}
	}
}
