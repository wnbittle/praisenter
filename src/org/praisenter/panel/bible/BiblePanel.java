package org.praisenter.panel.bible;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.praisenter.DisplayWindow;
import org.praisenter.DisplayWindow.ShowResult;
import org.praisenter.control.AutoCompleteComboBoxEditor;
import org.praisenter.control.EmptyNumberFormatter;
import org.praisenter.control.SelectTextFocusListener;
import org.praisenter.control.WaterMark;
import org.praisenter.data.DataException;
import org.praisenter.data.bible.Bible;
import org.praisenter.data.bible.BibleSearchType;
import org.praisenter.data.bible.Bibles;
import org.praisenter.data.bible.Book;
import org.praisenter.data.bible.Verse;
import org.praisenter.dialog.ExceptionDialog;
import org.praisenter.display.BibleDisplay;
import org.praisenter.display.TextComponent;
import org.praisenter.icons.Icons;
import org.praisenter.panel.display.MultipleDisplayPreviewPanel;
import org.praisenter.resources.Messages;
import org.praisenter.settings.BibleDisplaySettings;
import org.praisenter.settings.GeneralSettings;
import org.praisenter.settings.SettingsListener;
import org.praisenter.utilities.StringUtilities;
import org.praisenter.utilities.WindowUtilities;

/**
 * Panel for bible lookup and searching.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class BiblePanel extends JPanel implements ActionListener, SettingsListener {
	/** The version id */
	private static final long serialVersionUID = 5706187704789309806L;

	/** Static logger */
	private static final Logger LOGGER = Logger.getLogger(BiblePanel.class);
	
	// normal bible lookup
	
	/** The combo box of bibles */
	private JComboBox<Bible> cmbBibles;
	
	/** The combo box of books (for the selected bible) */
	private JComboBox<Book> cmbBooks;
	
	/** The chapter count label */
	private JLabel lblChapterCount;
	
	/** The text box for the chapter number */
	private JFormattedTextField txtChapter;
	
	/** The verse count label */
	private JLabel lblVerseCount;
	
	/** The text box for the verse number */
	private JFormattedTextField txtVerse;
	
	/** The found/not-found verse label */
	private JLabel lblFound;
	
	/** The table of saved verses */
	private JTable tblSavedVerses;
	
	// bible searching
	
	/** The bible searching thread */
	private BibleSearchThread bibleSearchThread;
	
	/** The bible searching text box */
	private JTextField txtBibleSearch;
	
	/** The bible searching type combo box */
	private JComboBox<BibleSearchType> cmbBibleSearchType;
	
	/** The bible search results label */
	private JLabel lblBibleSearchResults;
	
	/** The bible search results table */
	private JTable tblBibleSearchResults;
	
	// preview
	
	/** The preview panel */
	private MultipleDisplayPreviewPanel pnlPreview;
	
	/** The previous verse display */
	private BibleDisplay prevVerseDisplay;
	
	/** The current verse display */
	private BibleDisplay currVerseDisplay;
	
	/** The next verse display */
	private BibleDisplay nextVerseDisplay;
	
	/**
	 * Default constructor.
	 */
	@SuppressWarnings("serial")
	public BiblePanel() {
		// get the settings
		GeneralSettings gSettings = GeneralSettings.getInstance();
		BibleDisplaySettings bSettings = BibleDisplaySettings.getInstance();
		
		// get the display size
		Dimension displaySize = gSettings.getPrimaryDisplaySize();
		
		// create the displays
		this.prevVerseDisplay = bSettings.getDisplay(displaySize);
		this.currVerseDisplay = bSettings.getDisplay(displaySize);
		this.nextVerseDisplay = bSettings.getDisplay(displaySize);
		
		// create the preview panel
		this.pnlPreview = new MultipleDisplayPreviewPanel();
		this.pnlPreview.addDisplay(this.prevVerseDisplay);
		this.pnlPreview.addDisplay(this.currVerseDisplay);
		this.pnlPreview.addDisplay(this.nextVerseDisplay);
		
		this.pnlPreview.setMinimumSize(300);
		
		// normal bible lookup
		
		// get the bibles
		Bible[] bibles = null;
		try {
			bibles = Bibles.getBibles().toArray(new Bible[0]);
		} catch (DataException ex) {
			LOGGER.error("Bibles could not be retrieved:", ex);
		}
		
		// the bible combobox
		if (bibles == null) {
			this.cmbBibles = new JComboBox<Bible>();
		} else {
			this.cmbBibles = new JComboBox<Bible>(bibles);
		}
		this.cmbBibles.setRenderer(new BibleListCellRenderer());
		this.cmbBibles.addItemListener(new ItemListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				// only perform the following when the event is a selected event
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// look up the number of chapters in the book
					Bible bible = (Bible)cmbBibles.getSelectedItem();
					if (bible != null) {
						// load up all the books
						try {
							boolean ia = GeneralSettings.getInstance().isApocryphaIncluded();
							List<Book> books = Bibles.getBooks(bible, ia);
							cmbBooks.removeAllItems();
							for (Book book : books) {
								cmbBooks.addItem(book);
							}
							// select the first book
							if (books.size() > 0) {
								cmbBooks.setSelectedIndex(0);
							} else {
								cmbBooks.setSelectedItem(null);
							}
							updateLabels();
						} catch (DataException ex) {
							ExceptionDialog.show(
									BiblePanel.this, 
									Messages.getString("panel.bible.data.bookListing.exception.title"), 
									MessageFormat.format(Messages.getString("panel.bible.data.bookListing.exception.text"), bible.getName()), 
									ex);
						}
					}
				}
			}
		});
		
		// get the books
		List<Book> books = new ArrayList<Book>();
		// get the default bible
		Bible bible = null;
		try {
			int id = gSettings.getDefaultBibleId();
			if (id > 0) {
				bible = Bibles.getBible(id);
			} else if (bibles != null && bibles.length > 0) {
				bible = bibles[0];
			}
			
		} catch (DataException ex) {
			LOGGER.error("Default bible could not be retrieved:", ex);
		}
		
		if (bible != null) {
			try {
				boolean ia = gSettings.isApocryphaIncluded();
				books = Bibles.getBooks(bible, ia);
			} catch (DataException ex) {
				LOGGER.error("An error occurred when trying to get the listing of books for the bible: " + bible.getName(), ex);
			}
		} else {
			LOGGER.error("The selected bible is null; index = 0");
		}
		
		// book combo box
		this.cmbBooks = new JComboBox<Book>(books.toArray(new Book[0])) {
			/* (non-Javadoc)
			 * @see javax.swing.JComboBox#getSelectedItem()
			 */
			@Override
			public Object getSelectedItem() {
				Object value = super.getSelectedItem();
				if (value != null && value instanceof Book) {
					Book book = (Book)value;
					JTextComponent field = (JTextComponent)this.editor.getEditorComponent();
					if (book.getName().equals(field.getText())) {
						return book;
					}
				}
				return null;
			}
			
			/* (non-Javadoc)
			 * @see javax.swing.JComboBox#setSelectedItem(java.lang.Object)
			 */
			@Override
			public void setSelectedItem(Object anObject) {
				super.setSelectedItem(anObject);
				if (anObject != null) {
					JTextComponent field = (JTextComponent)this.editor.getEditorComponent();
					field.setText(((Book)anObject).getName());
				}
				// if the value changes update the labels
				updateLabels();
			}
			
			/* (non-Javadoc)
			 * @see javax.swing.JComboBox#setSelectedIndex(int)
			 */
			@Override
			public void setSelectedIndex(int anIndex) {
				super.setSelectedIndex(anIndex);
				JTextComponent field = (JTextComponent)this.editor.getEditorComponent();
				Object item = this.getSelectedItem();
				if (item != null) {
					field.setText(((Book)item).getName());
				}
				// if the value changes update the labels
				updateLabels();
			}
		};
		this.cmbBooks.setEditable(true);
		this.cmbBooks.setRenderer(new BookListCellRenderer());
		this.cmbBooks.setEditor(new BookComboBoxEditor(cmbBooks.getEditor()));
		this.cmbBooks.addItemListener(new ItemListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				// if the value changes update the labels
				updateLabels();
			}
		});
		
		// chapter text box
		this.txtChapter = new JFormattedTextField(new EmptyNumberFormatter(NumberFormat.getIntegerInstance())) {
			/* (non-Javadoc)
			 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// paint a watermark over the text box
				WaterMark.paintTextWaterMark(g, this, Messages.getString("panel.bible.chapter.watermark"));
			}
		};
		this.txtChapter.setColumns(3);
		this.txtChapter.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// update the labels when the value changes
				updateLabels();
			}
		});
		this.txtChapter.addFocusListener(new SelectTextFocusListener(this.txtChapter));
		
		// verse text box
		this.txtVerse = new JFormattedTextField(new EmptyNumberFormatter(NumberFormat.getIntegerInstance())) {
			/* (non-Javadoc)
			 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// paint a watermark over the text box
				WaterMark.paintTextWaterMark(g, this, Messages.getString("panel.bible.verse.watermark"));
			}
		};
		this.txtVerse.setColumns(3);
		this.txtVerse.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// update the labels when the value changes
				updateLabels();
			}
		});
		this.txtVerse.addFocusListener(new SelectTextFocusListener(this.txtVerse));
		
		// setup the labels
		this.lblChapterCount = new JLabel(MessageFormat.format(Messages.getString("panel.bible.chapterCount"), ""));
		this.lblChapterCount.setToolTipText(Messages.getString("panel.bible.chapterCount.tooltip"));
		this.lblVerseCount = new JLabel("");
		this.lblVerseCount.setToolTipText(Messages.getString("panel.bible.verseCount.tooltip"));
		this.lblFound = new JLabel("");
		
		// setup the buttons
		JButton btnFind = new JButton(Messages.getString("panel.bible.preview"));
		btnFind.setToolTipText(Messages.getString("panel.bible.preview.tooltip"));
		btnFind.addActionListener(this);
		btnFind.setActionCommand("find");
		
		JButton btnAdd = new JButton(Messages.getString("panel.bible.add"));
		btnAdd.setToolTipText(Messages.getString("panel.bible.add.tooltip"));
		btnAdd.addActionListener(this);
		btnAdd.setActionCommand("add");

		JButton btnNext = new JButton(Messages.getString("panel.bible.next"));
		btnNext.setToolTipText(Messages.getString("panel.bible.next.tooltip"));
		btnNext.addActionListener(this);
		btnNext.setActionCommand("next");
		
		JButton btnPrev = new JButton(Messages.getString("panel.bible.previous"));
		btnPrev.setToolTipText(Messages.getString("panel.bible.previous.tooltip"));
		btnPrev.addActionListener(this);
		btnPrev.setActionCommand("prev");
		
		JPanel pnlLookupButtons = new JPanel();
		pnlLookupButtons.setLayout(new GridLayout(2, 2, 5, 5));
		pnlLookupButtons.add(btnFind);
		pnlLookupButtons.add(btnAdd);
		pnlLookupButtons.add(btnPrev);
		pnlLookupButtons.add(btnNext);
		
		// create the send/clear buttons
		
		JButton btnSend = new JButton(Messages.getString("panel.bible.send"));
		btnSend.setToolTipText(Messages.getString("panel.bible.send.tooltip"));
		btnSend.setMinimumSize(new Dimension(200, 0));
		btnSend.setPreferredSize(new Dimension(200, 0));
		btnSend.addActionListener(this);
		btnSend.setActionCommand("send");
		
		JButton btnClear = new JButton(Messages.getString("panel.bible.clear"));
		btnClear.setToolTipText(Messages.getString("panel.bible.clear.tooltip"));
		btnClear.addActionListener(this);
		btnClear.setActionCommand("clear");
		
		JPanel pnlButtons = new JPanel();
		GroupLayout subLayout = new GroupLayout(pnlButtons);
		pnlButtons.setLayout(subLayout);
		
		subLayout.setAutoCreateGaps(true);
		subLayout.setHorizontalGroup(subLayout.createSequentialGroup()
				.addComponent(pnlLookupButtons)
				.addComponent(btnSend)
				.addComponent(btnClear));
		subLayout.setVerticalGroup(subLayout.createParallelGroup()
				.addComponent(pnlLookupButtons)
				.addComponent(btnSend, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(btnClear, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		
		// create the queue table
		this.tblSavedVerses = new JTable(new MutableBibleTableModel()) {
			/* (non-Javadoc)
			 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
			 */
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int row = this.rowAtPoint(p);
				
				// get the text column value
				TableModel model = this.getModel();
				Object object = model.getValueAt(row, 4);
				if (object != null) {
					// get the verse text
					String text = object.toString();
					// split the lines by 50 characters
					return StringUtilities.addLineBreaksAtInterval(text, 50);
				}
				
				return super.getToolTipText(event);
			}
		};
		this.tblSavedVerses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tblSavedVerses.setColumnSelectionAllowed(false);
		this.tblSavedVerses.setCellSelectionEnabled(false);
		this.tblSavedVerses.setRowSelectionAllowed(true);
		this.tblSavedVerses.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				// make sure its a double click
				if (e.getClickCount() == 2) {
					// get the selected row
					int row = tblSavedVerses.rowAtPoint(e.getPoint());
					// get the data
					BibleTableModel model = (BibleTableModel)tblSavedVerses.getModel();
					Verse verse = model.getRow(row);
					// set the selection
					cmbBooks.setSelectedItem(verse.getBook());
					// set the numbers
					txtChapter.setValue(verse.getChapter());
					txtVerse.setValue(verse.getVerse());
					// update the labels
					updateLabels();
					// update the displays
					try {
						updateVerseDisplays(verse);
					} catch (DataException ex) {
						// just log this exception because the user
						// should still be able to click the preview button
						LOGGER.error("An error occurred while updating the verse displays from a saved verse: ", ex);
					}
				}
			}
		});
		this.tblSavedVerses.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		this.setSavedVersesTableWidths();
		
		// wrap the saved verses table in a scroll pane
		JScrollPane scrSavedVerses = new JScrollPane(this.tblSavedVerses);
		scrSavedVerses.setPreferredSize(new Dimension(0, 200));
		
		// need two buttons for the saved verses
		JButton btnRemoveSelected = new JButton(Messages.getString("panel.bible.removeSelected"));
		btnRemoveSelected.setToolTipText(Messages.getString("panel.bible.removeSelected.tooltip"));
		btnRemoveSelected.addActionListener(this);
		btnRemoveSelected.setActionCommand("remove-selected");
		
		JButton btnRemoveAll = new JButton(Messages.getString("panel.bible.removeAll"));
		btnRemoveAll.setToolTipText(Messages.getString("panel.bible.removeAll.tooltip"));
		btnRemoveAll.addActionListener(this);
		btnRemoveAll.setActionCommand("remove-all");
		
		// bible searching
		
		// create and start the bible search thread
		this.bibleSearchThread = new BibleSearchThread();
		this.bibleSearchThread.start();
		
		// the search text field
		this.txtBibleSearch = new JTextField() {
			/* (non-Javadoc)
			 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// paint a watermark on the text box
				WaterMark.paintTextWaterMark(g, this, Messages.getString("panel.bible.search.watermark"));
			}
		};
		this.txtBibleSearch.setActionCommand("search");
		this.txtBibleSearch.addActionListener(this);
		
		this.cmbBibleSearchType = new JComboBox<BibleSearchType>(BibleSearchType.values());
		this.cmbBibleSearchType.setRenderer(new BibleSearchTypeRenderer());
		this.cmbBibleSearchType.setSelectedItem(BibleSearchType.PHRASE);
		this.cmbBibleSearchType.setToolTipText(Messages.getString("panel.bible.search.type"));
		
		// create the search button
		JButton btnSearch = new JButton(Messages.getString("panel.bible.search"));
		btnSearch.setToolTipText(Messages.getString("panel.bible.search.tooltip"));
		btnSearch.addActionListener(this);
		btnSearch.setActionCommand("search");
		
		// create the search results label
		this.lblBibleSearchResults = new JLabel();
		this.lblBibleSearchResults.setVerticalAlignment(SwingConstants.TOP);
		this.lblBibleSearchResults.setMinimumSize(new Dimension(0, 30));
		this.lblBibleSearchResults.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 5));
		
		// create the search results table
		this.tblBibleSearchResults = new JTable(new BibleTableModel()) {
			/* (non-Javadoc)
			 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
			 */
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int row = this.rowAtPoint(p);
				
				// get the text column value
				TableModel model = this.getModel();
				Object object = model.getValueAt(row, 3);
				if (object != null) {
					// get the verse text
					String text = object.toString();
					// split the lines by 50 characters
					return StringUtilities.addLineBreaksAtInterval(text, 50);
				}
				
				return super.getToolTipText(event);
			}
		};
		this.tblBibleSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tblBibleSearchResults.setColumnSelectionAllowed(false);
		this.tblBibleSearchResults.setCellSelectionEnabled(false);
		this.tblBibleSearchResults.setRowSelectionAllowed(true);
		this.tblBibleSearchResults.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				// make sure its a double click
				if (e.getClickCount() == 2) {
					// get the selected row
					int row = tblBibleSearchResults.rowAtPoint(e.getPoint());
					// get the data
					BibleTableModel model = (BibleTableModel)tblBibleSearchResults.getModel();
					Verse verse = model.getRow(row);
					// set the selection
					cmbBooks.setSelectedItem(verse.getBook());
					// set the numbers
					txtChapter.setValue(verse.getChapter());
					txtVerse.setValue(verse.getVerse());
					// update the labels
					updateLabels();
					// update the displays
					try {
						updateVerseDisplays(verse);
					} catch (DataException ex) {
						// just log this exception because the user
						// should still be able to click the preview button
						LOGGER.error("An error occurred while updating the verse displays from a search result: ", ex);
					}
				}
			}
		});
		this.tblBibleSearchResults.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		this.setBibleSearchTableWidths();
		
		// wrap the search table in a scroll pane
		JScrollPane scrBibleSearchResults = new JScrollPane(this.tblBibleSearchResults);
		scrBibleSearchResults.setPreferredSize(new Dimension(0, 150));
		
		// default any fields
		if (bible != null) {
			this.cmbBibles.setSelectedItem(bible);
		}
		if (books != null && books.size() > 0) {
			this.cmbBooks.setSelectedIndex(0);
		}
		
		// create the layout
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		// setup the horizontal layout
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(this.pnlPreview)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(this.cmbBibles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(this.cmbBooks, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(this.lblChapterCount))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(this.txtChapter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(this.lblVerseCount))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(this.txtVerse, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(this.lblFound))
						.addComponent(pnlButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(scrSavedVerses)
				.addGroup(layout.createSequentialGroup()
						.addComponent(btnRemoveSelected)
						.addComponent(btnRemoveAll))
				.addGroup(layout.createSequentialGroup()
						.addComponent(this.txtBibleSearch)
						.addComponent(this.cmbBibleSearchType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(scrBibleSearchResults))
				.addGroup(layout.createSequentialGroup()
						.addComponent(this.lblBibleSearchResults)));
		
		// setup the vertical layout
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(this.pnlPreview)
				.addGroup(layout.createParallelGroup()
						.addComponent(this.cmbBibles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(this.cmbBooks, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(this.txtChapter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(this.txtVerse, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(layout.createParallelGroup()
									.addComponent(this.lblChapterCount)
									.addComponent(this.lblVerseCount)
									.addComponent(this.lblFound)))
						.addComponent(pnlButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(scrSavedVerses, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup()
						.addComponent(btnRemoveSelected, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRemoveAll, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(this.txtBibleSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.cmbBibleSearchType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(scrBibleSearchResults, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.lblBibleSearchResults, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.settings.SettingsListener#settingsSaved()
	 */
	@Override
	public void settingsSaved() {
		// when the settings change, either the general or the bible settings
		// we need to update all the components and redraw them
		
		GeneralSettings gSettings = GeneralSettings.getInstance();
		// in the case of general settings we need to get the assigned device
		// and see if we need to update the display size
		
		// get the display size
		Dimension displaySize = gSettings.getPrimaryDisplaySize();
		
		BibleDisplaySettings bSettings = BibleDisplaySettings.getInstance();
		
		// create new displays using the new settings
		BibleDisplay pDisplay = bSettings.getDisplay(displaySize);
		BibleDisplay cDisplay = bSettings.getDisplay(displaySize);
		BibleDisplay nDisplay = bSettings.getDisplay(displaySize);
		
		// copy over the text values
		this.copyTextValues(this.prevVerseDisplay, pDisplay);
		this.copyTextValues(this.currVerseDisplay, cDisplay);
		this.copyTextValues(this.nextVerseDisplay, nDisplay);
		
		// remove the old displays from the preview panel
		this.pnlPreview.removeDisplay(this.prevVerseDisplay);
		this.pnlPreview.removeDisplay(this.currVerseDisplay);
		this.pnlPreview.removeDisplay(this.nextVerseDisplay);
		
		// re-assign the preview displays
		this.prevVerseDisplay = pDisplay;
		this.currVerseDisplay = cDisplay;
		this.nextVerseDisplay = nDisplay;
		
		// add the new displays to the preview panel
		this.pnlPreview.addDisplay(this.prevVerseDisplay);
		this.pnlPreview.addDisplay(this.currVerseDisplay);
		this.pnlPreview.addDisplay(this.nextVerseDisplay);
		
		this.pnlPreview.setMinimumSize(300);
		
		// redraw the preview panel
		this.pnlPreview.repaint();
	}
	
	/**
	 * Copies the text values from the source display to the destination display.
	 * @param source the source display
	 * @param destination the destination display
	 */
	private void copyTextValues(BibleDisplay source, BibleDisplay destination) {
		TextComponent sTitle = source.getScriptureTitleComponent();
		TextComponent sText = source.getScriptureTextComponent();
		
		TextComponent dTitle = destination.getScriptureTitleComponent();
		TextComponent dText = destination.getScriptureTextComponent();
		
		dTitle.setText(sTitle.getText());
		dText.setText(sText.getText());
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Bible bible = (Bible)cmbBibles.getSelectedItem();
		Object b = this.cmbBooks.getSelectedItem();
		Object c = this.txtChapter.getValue();
		Object v = this.txtVerse.getValue();
		
		boolean ia = GeneralSettings.getInstance().isApocryphaIncluded();
		
		if (b != null && b instanceof Book &&
			c != null && c instanceof Number &&
			v != null && v instanceof Number) {
			
			// get the book, chapter, and verse
			Book book = (Book)b;
			int chapter = ((Number)c).intValue();
			int verse = ((Number)v).intValue();
			
			if ("find".equals(e.getActionCommand())) {
				try {
					// get the verse
					Verse text = Bibles.getVerse(bible, book.getCode(), chapter, verse);
					if (text != null) {
						// update the displays
						this.updateVerseDisplays(text);
					} else {
						LOGGER.info("No verse found for: " + bible.getName() + " " + book.getName() + " " + chapter + ":" + verse);
						this.currVerseDisplay.clearVerse();
					}
				} catch (DataException ex) {
					String message = MessageFormat.format(Messages.getString("panel.bible.data.find.exception.text"), bible.getName(), book.getName(), chapter, verse);
					ExceptionDialog.show(
							BiblePanel.this, 
							Messages.getString("panel.bible.data.find.exception.title"), 
							message, 
							ex);
					LOGGER.error(message, ex);
				}
			} else if ("send".equals(e.getActionCommand())) {
				DisplayWindow primary = DisplayWindow.getPrimaryDisplay();
				ShowResult result = DisplayWindow.show(primary, this.currVerseDisplay);
				if (result == ShowResult.DEVICE_NOT_VALID) {
					// the device is no longer available
					LOGGER.warn("The primary display doesn't exist.");
					JOptionPane.showMessageDialog(
							this, 
							Messages.getString("dialog.device.primary.missing.text"), 
							Messages.getString("dialog.device.primary.missing.title"), 
							JOptionPane.WARNING_MESSAGE);
				}
			} else if ("clear".equals(e.getActionCommand())) {
				DisplayWindow primary = DisplayWindow.getPrimaryDisplay();
				DisplayWindow.hide(primary);
			} else if ("prev".equals(e.getActionCommand())) {
				try {
					Verse text = Bibles.getPreviousVerse(bible, book.getCode(), chapter, verse, ia);
					if (text != null) {
						// change fields to new verse data
						this.cmbBooks.setSelectedItem(text.getBook());
						this.txtChapter.setValue(text.getChapter());
						this.txtVerse.setValue(text.getVerse());
						// update the displays
						this.updateVerseDisplays(text);
					} else {
						LOGGER.info("No previous verse exists for: " + bible.getName() + " " + book.getName() + " " + chapter + ":" + verse);
					}
				} catch (DataException ex) {
					String message = MessageFormat.format(Messages.getString("panel.bible.data.previous.exception.text"), bible.getName(), book.getName(), chapter, verse);
					ExceptionDialog.show(
							WindowUtilities.getParentWindow(BiblePanel.this), 
							Messages.getString("panel.bible.data.previous.exception.title"), 
							message, 
							ex);
					LOGGER.error(message, ex);
				}
			} else if ("next".equals(e.getActionCommand())) {
				try {
					Verse text = Bibles.getNextVerse(bible, book.getCode(), chapter, verse, ia);
					if (text != null) {
						// change fields to new verse data
						this.cmbBooks.setSelectedItem(text.getBook());
						this.txtChapter.setValue(text.getChapter());
						this.txtVerse.setValue(text.getVerse());
						// update the displays
						this.updateVerseDisplays(text);
					} else {
						LOGGER.info("No next verse exists for: " + bible.getName() + " " + book.getName() + " " + chapter + ":" + verse);
					}
				} catch (DataException ex) {
					String message = MessageFormat.format(Messages.getString("panel.bible.data.next.exception.text"), bible.getName(), book.getName(), chapter, verse);
					ExceptionDialog.show(
							WindowUtilities.getParentWindow(BiblePanel.this), 
							Messages.getString("panel.bible.data.next.exception.title"), 
							message, 
							ex);
					LOGGER.error(message, ex);
				}
			} else if ("add".equals(e.getActionCommand())) {
				try {
					Verse text = Bibles.getVerse(bible, book.getCode(), chapter, verse);
					if (text != null) {
						// change fields to new verse data
						this.cmbBooks.setSelectedItem(text.getBook());
						this.txtChapter.setValue(text.getChapter());
						this.txtVerse.setValue(text.getVerse());
						// update the displays
						this.updateVerseDisplays(text);
						// add the verse to the queue
						MutableBibleTableModel model = (MutableBibleTableModel)this.tblSavedVerses.getModel();
						model.addRow(text);
					} else {
						LOGGER.info("No next verse exists for: " + bible.getName() + " " + book.getName() + " " + chapter + ":" + verse);
					}
				} catch (DataException ex) {
					String message = MessageFormat.format(Messages.getString("panel.bible.data.next.exception.text"), bible.getName(), book.getName(), chapter, verse);
					ExceptionDialog.show(
							WindowUtilities.getParentWindow(BiblePanel.this), 
							Messages.getString("panel.bible.data.next.exception.title"), 
							message, 
							ex);
					LOGGER.error(message, ex);
				}
			}
		}
		
		// check for search
		if ("search".equals(e.getActionCommand())) {
			// grab the text from the text box
			String text = this.txtBibleSearch.getText();
			// get the bible search type
			BibleSearchType type = (BibleSearchType)this.cmbBibleSearchType.getSelectedItem();
			if (text != null && text.length() > 0) {
				// execute the search in another thread
				// its possible that the search thread was interrupted or stopped
				// so make sure its still running
				if (!this.bibleSearchThread.isAlive()) {
					// if the current thread is no longer alive (running) then
					// create another and start it
					this.bibleSearchThread = new BibleSearchThread();
					this.bibleSearchThread.start();
				}
				
				// execute the search
				BibleSearch search = new BibleSearch(bible, text, type, new BibleSearchCallback());
				this.bibleSearchThread.queueSearch(search);
			}
		}
		// check for remove selected
		else if ("remove-selected".equals(e.getActionCommand())) {
			MutableBibleTableModel model = (MutableBibleTableModel)this.tblSavedVerses.getModel();
			model.removeSelectedRows();
		}
		// check for remove all
		else if ("remove-all".equals(e.getActionCommand())) {
			MutableBibleTableModel model = (MutableBibleTableModel)this.tblSavedVerses.getModel();
			model.removeAllRows();
		}
	}
	
	/**
	 * Updates the bible displays for the new current verse.
	 * @param verse the new current verse
	 * @throws DataException if an exception occurs while loading the next and previous verses
	 */
	private void updateVerseDisplays(Verse verse) throws DataException {
		boolean ia = GeneralSettings.getInstance().isApocryphaIncluded();
		// set the current verse text
		this.currVerseDisplay.setVerse(verse);
		// then get the previous and next verses as well
		Verse prev = Bibles.getPreviousVerse(verse, ia);
		Verse next = Bibles.getNextVerse(verse, ia);
		// set the previous verse
		if (prev != null) {
			this.prevVerseDisplay.setVerse(prev);
		} else {
			this.prevVerseDisplay.clearVerse();
		}
		// set the next verse
		if (next != null) {
			this.nextVerseDisplay.setVerse(next);
		} else {
			this.nextVerseDisplay.clearVerse();
		}
		// repaint the preview
		this.pnlPreview.repaint();
	}
	
	/**
	 * Updates the labels to show the current information.
	 */
	private void updateLabels() {
		// look up the number of verses in the chapter
		Bible bible = (Bible)cmbBibles.getSelectedItem();
		Book book = (Book)cmbBooks.getSelectedItem();
		Object chap = txtChapter.getValue();
		Object vers = txtVerse.getValue();
		
		if (bible != null && book != null) {
			// show the number of chapters
			try {
				int count = Bibles.getChapterCount(bible, book.getCode());
				lblChapterCount.setText(MessageFormat.format(Messages.getString("panel.bible.chapterCount"), count));
			} catch (DataException ex) {
				LOGGER.error(MessageFormat.format(Messages.getString("panel.bible.data.chapterCount.exception.text"), bible.getName(), book.getName()), ex);
				lblChapterCount.setText("");
			}
			
			if (chap != null && chap instanceof Number) {
				// show the number of verses
				int chapter = ((Number) chap).intValue();
				try {
					int count = Bibles.getVerseCount(bible, book.getCode(), chapter);
					lblVerseCount.setText(MessageFormat.format(Messages.getString("panel.bible.verseCount"), count));
				} catch (DataException ex) {
					LOGGER.error(MessageFormat.format(Messages.getString("panel.bible.data.verseCount.exception.text"), bible.getName(), book.getName(), chapter), ex);
					lblVerseCount.setText("");
				}
				
				if (vers != null && vers instanceof Number) {
					// show the found/not found icon
					int verse = ((Number)vers).intValue();
					try {
						Verse v = Bibles.getVerse(bible, book.getCode(), chapter, verse);
						if (v != null) {
							lblFound.setIcon(Icons.FOUND);
							lblFound.setText(Messages.getString("panel.bible.valid"));
						} else {
							lblFound.setIcon(Icons.NOT_FOUND);
							lblFound.setText(Messages.getString("panel.bible.invalid"));
							lblFound.setToolTipText(MessageFormat.format(Messages.getString("panel.bible.data.verseNotFound"), bible.getName(), book.getName(), chapter, verse));
						}
					} catch (DataException ex) {
						LOGGER.error(MessageFormat.format(Messages.getString("panel.bible.data.validate.exception.text"), bible.getName(), book.getName(), chapter, verse), ex);
						lblFound.setIcon(null);
						lblFound.setText("");
					}
				} else {
					// clear labels if not enough info is given
					lblFound.setIcon(null);
					lblFound.setText("");
				}
			} else {
				// clear labels if not enough info is given
				lblVerseCount.setText("");
				lblFound.setIcon(null);
				lblFound.setText("");
			}
		} else {
			// clear labels if not enough info is given
			lblChapterCount.setText(MessageFormat.format(Messages.getString("panel.bible.chapterCount"), ""));
			lblVerseCount.setText("");
			lblFound.setIcon(null);
			lblFound.setText("");
		}
	}
	
	/**
	 * Sets the table column widths for the bible search results table.
	 */
	private void setBibleSearchTableWidths() {
		this.tblBibleSearchResults.getColumnModel().getColumn(0).setMaxWidth(150);
		this.tblBibleSearchResults.getColumnModel().getColumn(0).setPreferredWidth(110);
		this.tblBibleSearchResults.getColumnModel().getColumn(1).setMaxWidth(35);
		this.tblBibleSearchResults.getColumnModel().getColumn(1).setPreferredWidth(35);
		this.tblBibleSearchResults.getColumnModel().getColumn(2).setMaxWidth(35);
		this.tblBibleSearchResults.getColumnModel().getColumn(2).setPreferredWidth(35);
	}
	
	/**
	 * Sets the table column widths for the saved verses table.
	 */
	private void setSavedVersesTableWidths() {
		this.tblSavedVerses.getColumnModel().getColumn(0).setMaxWidth(35);
		this.tblSavedVerses.getColumnModel().getColumn(0).setPreferredWidth(35);
		this.tblSavedVerses.getColumnModel().getColumn(1).setMaxWidth(150);
		this.tblSavedVerses.getColumnModel().getColumn(1).setPreferredWidth(110);
		this.tblSavedVerses.getColumnModel().getColumn(2).setMaxWidth(35);
		this.tblSavedVerses.getColumnModel().getColumn(2).setPreferredWidth(35);
		this.tblSavedVerses.getColumnModel().getColumn(3).setMaxWidth(35);
		this.tblSavedVerses.getColumnModel().getColumn(3).setPreferredWidth(35);
	}
	
	/**
	 * Custom editor decorator for the book combo box.
	 * @author William Bittle
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	private class BookComboBoxEditor extends AutoCompleteComboBoxEditor {
		/**
		 * Full constructor.
		 * @param comboBoxEditor the editor to decorate
		 */
		public BookComboBoxEditor(ComboBoxEditor comboBoxEditor) {
			super(comboBoxEditor);
		}
		
		/* (non-Javadoc)
		 * @see org.praisenter.control.AutoCompleteComboBoxEditor#match(java.lang.String)
		 */
		@Override
		public String match(String text) {
			if (text == null || text.length() == 0) {
				return null;
			}
			int n = cmbBooks.getItemCount();
			for (int i = 0; i < n; i++) {
				Book book = cmbBooks.getItemAt(i);
				String name = book.getName();
				if (!book.equals(text)) {
					if (name.toUpperCase().startsWith(text.toUpperCase())) {
						// then set the text and select the remaining text
						return name;
					}
				} else {
					return name;
				}
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.praisenter.control.AutoCompleteComboBoxEditor#getValue(java.lang.Object)
		 */
		@Override
		public String getValue(Object o) {
			if (o == null) return null;
			if (o instanceof Book) {
				Book book = (Book)o;
				return book.getName();
			} else {
				return o.toString();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.praisenter.control.AutoCompleteComboBoxEditor#getItem(java.lang.String)
		 */
		@Override
		public Object getItem(String text) {
			if (text == null || text.length() == 0) {
				return null;
			}
			int n = cmbBooks.getItemCount();
			for (int i = 0; i < n; i++) {
				Book book = cmbBooks.getItemAt(i);
				String name = book.getName();
				if (!book.equals(text)) {
					if (name.toUpperCase().startsWith(text.toUpperCase())) {
						// then set the text and select the remaining text
						return book;
					}
				} else {
					return book;
				}
			}
			return null;
		}
	}

	/**
	 * Callback for bible searching to update the table and results text.
	 * @author William Bittle
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	private class BibleSearchCallback extends BibleSearchThread.Callback {
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			List<Verse> verses = this.getResult();
			Exception ex = this.getException();
			BibleSearch search = this.getSearch();
			if (ex == null) {
				String message = Messages.getString("panel.bible.search.results.pattern");
				if (verses != null && verses.size() > 0) {
					tblBibleSearchResults.setModel(new BibleTableModel(verses));
					lblBibleSearchResults.setText(MessageFormat.format(message, verses.size()));
				} else {
					tblBibleSearchResults.setModel(new BibleTableModel());
					lblBibleSearchResults.setText(MessageFormat.format(message, 0));
				}
				setBibleSearchTableWidths();
			} else {
				String message = MessageFormat.format(Messages.getString("panel.bible.data.search.exception.text"), search.getText(), search.getBible().getName());
				ExceptionDialog.show(
						BiblePanel.this, 
						Messages.getString("panel.bible.data.search.exception.title"), 
						message, 
						ex);
				LOGGER.error(message, ex);
			}
		}
	}
	
	/**
	 * ListCellRenderer for the {@link BibleSearchType}.
	 * @author William Bittle
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	private class BibleSearchTypeRenderer extends DefaultListCellRenderer {
		/** The version id */
		private static final long serialVersionUID = 4263685716936842797L;
		
		/* (non-Javadoc)
		 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (value instanceof BibleSearchType) {
				BibleSearchType type = (BibleSearchType)value;
				if (type == BibleSearchType.ALL_WORDS) {
					this.setText(Messages.getString("panel.bible.search.type.allWords"));
					this.setToolTipText(Messages.getString("panel.bible.search.type.allWords.tooltip"));
				} else if (type == BibleSearchType.ANY_WORD) {
					this.setText(Messages.getString("panel.bible.search.type.anyWord"));
					this.setToolTipText(Messages.getString("panel.bible.search.type.anyWord.tooltip"));
				} else if (type == BibleSearchType.PHRASE) {
					this.setText(Messages.getString("panel.bible.search.type.phrase"));
					this.setToolTipText(Messages.getString("panel.bible.search.type.phrase.tooltip"));
				} else if (type == BibleSearchType.LOCATION) {
					this.setText(Messages.getString("panel.bible.search.type.location"));
					this.setToolTipText(Messages.getString("panel.bible.search.type.location.tooltip"));
				}
			}
			
			return this;
		}
	}
}
