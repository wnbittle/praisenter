package org.praisenter.ui.bible;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.praisenter.Constants;
import org.praisenter.async.AsyncHelper;
import org.praisenter.data.bible.Bible;
import org.praisenter.data.bible.Book;
import org.praisenter.data.bible.Chapter;
import org.praisenter.data.bible.Verse;
import org.praisenter.data.json.JsonIO;
import org.praisenter.ui.Action;
import org.praisenter.ui.GlobalContext;
import org.praisenter.ui.controls.Alerts;
import org.praisenter.ui.document.DocumentContext;
import org.praisenter.ui.document.DocumentEditor;
import org.praisenter.ui.events.ActionStateChangedEvent;
import org.praisenter.ui.translations.Translations;
import org.praisenter.ui.undo.UndoManager;

import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

//JAVABUG (L) 11/03/16 Dragging to the edge of a scrollable window doesn't scroll it and there's no good way to scroll it manually

public final class BibleEditor extends BorderPane implements DocumentEditor<Bible> {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final DataFormat BOOK_CLIPBOARD_DATA = new DataFormat("application/x-praisenter-json-list;class=" + Book.class.getName());
	private static final DataFormat CHAPTER_CLIPBOARD_DATA = new DataFormat("application/x-praisenter-json-list;class=" + Chapter.class.getName());
	private static final DataFormat VERSE_CLIPBOARD_DATA = new DataFormat("application/x-praisenter-json-list;class=" + Verse.class.getName());
	
	private static final PseudoClass DRAG_OVER_PARENT = PseudoClass.getPseudoClass("drag-over-parent");
	private static final PseudoClass DRAG_OVER_SIBLING_TOP = PseudoClass.getPseudoClass("drag-over-sibling-top");
	private static final PseudoClass DRAG_OVER_SIBLING_BOTTOM = PseudoClass.getPseudoClass("drag-over-sibling-bottom");
	
	// data
	
	private final GlobalContext context;
	private final DocumentContext<Bible> document;

	// helpers
	
	private final Bible bible;
	private final UndoManager undoManager;
	
	// nodes
	
	private final TreeView<Object> treeView;
	
	public BibleEditor(
			GlobalContext context, 
			DocumentContext<Bible> document) {
		this.getStyleClass().add("p-bible-editor");
		
		this.context = context;
		this.document = document;
		
		// set the helpers
		
		this.bible = document.getDocument();
		this.undoManager = document.getUndoManager();
		
		// the tree
		
		BibleTreeItem root = new BibleTreeItem();
		root.setValue(this.bible);
		
		this.treeView = new TreeView<Object>(root);
		this.treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.treeView.setCellFactory((view) -> {
			BibleTreeCell cell = new BibleTreeCell();
			cell.setOnDragDetected(this::dragDetected);
			cell.setOnDragExited(this::dragExited);
			cell.setOnDragEntered(this::dragEntered);
			cell.setOnDragOver(this::dragOver);
			cell.setOnDragDropped(this::dragDropped);
			cell.setOnDragDone(this::dragDone);
        	return cell;
		});
		
		this.treeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends TreeItem<Object>> change) -> {
			// set the selected items
			document.getSelectedItems().setAll(this.treeView
					.getSelectionModel()
					.getSelectedItems()
					.stream().filter(i -> i != null && i.getValue() != null)
					.map(i -> i.getValue())
					.collect(Collectors.toList()));
		});

		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(
				this.createMenuItem(Action.NEW_BOOK),
				this.createMenuItem(Action.NEW_CHAPTER),
				this.createMenuItem(Action.NEW_VERSE),
				new SeparatorMenuItem(),
				this.createMenuItem(Action.COPY),
				this.createMenuItem(Action.CUT),
				this.createMenuItem(Action.PASTE),
				new SeparatorMenuItem(),
				this.createMenuItem(Action.REORDER),
				this.createMenuItem(Action.RENUMBER),
				new SeparatorMenuItem(),
				this.createMenuItem(Action.DELETE)
			);
		this.treeView.setContextMenu(menu);
		
		// when the menu is shown, update the enabled/disable state
		menu.showingProperty().addListener((obs, ov, nv) -> {
			if (nv) {
				// update the enable state
				for (MenuItem mnu : menu.getItems()) {
					Action action = (Action)mnu.getUserData();
					if (action != null) {
						boolean isEnabled = this.isActionEnabled(action);
						mnu.setDisable(!isEnabled);
					}
				}
			}
		});
		
		this.setCenter(this.treeView);
	}
	
	private MenuItem createMenuItem(Action action) {
		MenuItem mnu = new MenuItem(Translations.get(action.getMessageKey()));
		if (action.getGraphicSupplier() != null) {
			mnu.setGraphic(action.getGraphicSupplier().get());
		}
		// NOTE: due to bug in JavaFX, we don't apply the accelerator here
		//mnu.setAccelerator(value);
		mnu.setOnAction(e -> this.executeAction(action));
		mnu.setUserData(action);
		return mnu;
	}
	
	@Override
	public DocumentContext<Bible> getDocumentContext() {
		return this.document;
	}
	
	@Override
	public void setDefaultFocus() {
		this.treeView.requestFocus();
	}
	
	@Override
	public CompletableFuture<Void> executeAction(Action action) {
		switch (action) {
			case COPY:
				return this.copy(false);
			case PASTE:
				return this.paste();
			case CUT:
				return this.copy(true);
			case DELETE:
				return this.delete();
			case NEW_BOOK:
			case NEW_CHAPTER:
			case NEW_VERSE:
				return this.create(action);
			case RENUMBER:
				return this.renumber();
			case REORDER:
				return this.reorder();
			default:
				return CompletableFuture.completedFuture(null);
		}
	}
	
	@Override
	public boolean isActionEnabled(Action action) {
		DocumentContext<Bible> ctx = this.document;
		switch (action) {
			case COPY:
				return ctx.isSingleTypeSelected() && ctx.getSelectedType() != Bible.class;
			case CUT:
				return ctx.isSingleTypeSelected() && ctx.getSelectedType() != Bible.class;
			case PASTE:
				return (ctx.getSelectedType() == Bible.class && Clipboard.getSystemClipboard().hasContent(BOOK_CLIPBOARD_DATA)) ||
					   (ctx.getSelectedType() == Book.class && Clipboard.getSystemClipboard().hasContent(CHAPTER_CLIPBOARD_DATA)) ||
					   (ctx.getSelectedType() == Chapter.class && Clipboard.getSystemClipboard().hasContent(VERSE_CLIPBOARD_DATA));
			case DELETE:
				return ctx.getSelectedCount() > 0 && ctx.getSelectedType() != Bible.class;
			case NEW_BOOK:
				return ctx.getSelectedCount() == 1 && ctx.getSelectedType() == Bible.class;
			case NEW_CHAPTER:
				return ctx.getSelectedCount() == 1 && ctx.getSelectedType() == Book.class;
			case NEW_VERSE:
				return ctx.getSelectedCount() == 1 && ctx.getSelectedType() == Chapter.class;
			case REDO:
				return ctx.getUndoManager().isRedoAvailable();
			case UNDO:
				return ctx.getUndoManager().isUndoAvailable();
			case RENUMBER:
				return ctx.getSelectedCount() == 1 && ctx.getSelectedType() != Verse.class;
			case REORDER:
				return ctx.getSelectedCount() == 1 && ctx.getSelectedType() != Verse.class;
			default:
				return false;
		}
	}
	
	@Override
	public boolean isActionVisible(Action action) {
		// specifically show these actions
		switch (action) {
			case NEW_BOOK:
			case NEW_CHAPTER:
			case NEW_VERSE:
			case RENUMBER:
			case REORDER:
				return true;
			default:
				return false;
		}
	}
	
	// internal methods

	private CompletableFuture<Void> delete() {
		List<TreeItem<Object>> selected = new ArrayList<>(this.treeView.getSelectionModel().getSelectedItems());
		this.treeView.getSelectionModel().clearSelection();
		this.undoManager.beginBatch("Delete");
		try {
			for (TreeItem<Object> item : selected) {
				Object value = item.getValue();
				TreeItem<Object> parentItem = item.getParent();
				if (parentItem != null) {
					Object parent = parentItem.getValue();
					if (parent != null) {
						if (parent instanceof Bible) {
							((Bible)parent).getBooks().remove(value);
						} else if (parent instanceof Book) {
							((Book)parent).getChapters().remove(value);
						} else if (parent instanceof Chapter) {
							((Chapter)parent).getVerses().remove(value);
						}
					}
				}
			}
			this.undoManager.completeBatch();
		} catch (Exception ex) {
			LOGGER.error("Failed to delete the selected items", ex);
			this.undoManager.discardBatch();
		}
		return AsyncHelper.nil();
	}
	
	private CompletableFuture<Void> create(Action action) {
		switch (action) {
			case NEW_BOOK:
				int number = this.bible.getMaxBookNumber() + 1;
				this.bible.getBooks().add(new Book(number, Translations.get("action.new.bible.book")));
				break;
			case NEW_CHAPTER:
				if (this.document.getSelectedCount() == 1 && this.document.getSelectedType() == Book.class) {
					final Object selected = this.document.getSelectedItem();
					if (selected != null && selected instanceof Book) {
						Book book = (Book)selected;
						int n = book.getMaxChapterNumber();
						book.getChapters().add(new Chapter(n));
					}
				}
				break;
			case NEW_VERSE:
				if (this.document.getSelectedCount() == 1 && this.document.getSelectedType() == Chapter.class) {
					final Object selected = this.document.getSelectedItem();
					if (selected != null && selected instanceof Chapter) {
						Chapter chapter = (Chapter)selected;
						int n = chapter.getMaxVerseNumber();
						chapter.getVerses().add(new Verse(n, Translations.get("action.new.bible.verse")));
					}
				}
				break;
			default:
				break;
		}
		return AsyncHelper.nil();
	}
	
	private CompletableFuture<Void> renumber() {
		if (this.document.getSelectedCount() == 1) {
			// capture the item to be renumbered
			final TreeItem<Object> selected = this.treeView.getSelectionModel().getSelectedItem();
			if (selected != null) {
				final Object value = selected.getValue();
				if (this.context.getConfiguration().isRenumberBibleWarningEnabled()) {
					Alert alert = Alerts.confirmWithOptOut(
							this.context.getStage(), 
							Modality.WINDOW_MODAL, 
							AlertType.CONFIRMATION, 
							Translations.get("action.renumber"), 
							Translations.get("action.confirm"), 
							Translations.get("bible.editor.renumber.description"), 
							Translations.get("action.confirm.optout"), 
							(optOut) -> {
								if (optOut) {
									this.context.getConfiguration().setRenumberBibleWarningEnabled(false);
									this.context.saveConfiguration();
								}
							});
					
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						this.renumber(true, value);
					}
					
					return CompletableFuture.completedFuture(null);
				} else {
					// just do it
					this.renumber(true, value);
				}
			}
		}
		return AsyncHelper.nil();
	}
	
	private void renumber(boolean accepted, Object selected) {		
		this.treeView.requestFocus();
		if (accepted && selected != null) {
			this.undoManager.beginBatch("Renumber");
			try {
				if (selected instanceof Bible) {
					((Bible)selected).renumber();
				} else if (selected instanceof Book) {
					((Book)selected).renumber();
				} else if (selected instanceof Chapter) {
					((Chapter)selected).renumber();
				}
				this.undoManager.completeBatch();
			} catch (Exception ex) {
				LOGGER.error("Failed to renumber", ex);
				this.undoManager.discardBatch();
			}
		}
	}
	
	private CompletableFuture<Void> reorder() {
		if (this.document.getSelectedCount() == 1) {
			// capture the item to be renumbered
			final TreeItem<Object> selected = this.treeView.getSelectionModel().getSelectedItem();
			if (selected != null) {
				final Object value = selected.getValue();
				if (this.context.getConfiguration().isReorderBibleWarningEnabled()) {
					Alert alert = Alerts.confirmWithOptOut(
							this.context.getStage(), 
							Modality.WINDOW_MODAL, 
							AlertType.CONFIRMATION, 
							Translations.get("action.reorder"), 
							Translations.get("action.confirm"), 
							Translations.get("bible.editor.reorder.description"), 
							Translations.get("action.confirm.optout"),
							(optOut) -> {
								if (optOut) {
									this.context.getConfiguration().setReorderBibleWarningEnabled(false);
									this.context.saveConfiguration();
								}
							});
					
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						this.reorder(true, value);
					}
					
					return CompletableFuture.completedFuture(null);
				} else {
					// just do it
					this.reorder(true, value);
				}
			}
		}
		return AsyncHelper.nil();
	}
	
	private void reorder(boolean accepted, Object selected) {		
		this.treeView.requestFocus();
		if (accepted && selected != null) {
			this.undoManager.beginBatch("Renumber");
			try {
				if (selected instanceof Bible) {
					((Bible)selected).reorder();
				} else if (selected instanceof Book) {
					((Book)selected).reorder();
				} else if (selected instanceof Chapter) {
					((Chapter)selected).reorder();
				}
				this.undoManager.completeBatch();
			} catch (Exception ex) {
				LOGGER.error("Failed to reorder", ex);
				this.undoManager.discardBatch();
			}
		}
	}
	
	private ClipboardContent getClipboardContentForSelection(boolean serializeData) throws Exception {
		List<TreeItem<Object>> items = this.treeView.getSelectionModel().getSelectedItems();
		List<Object> objectData = items.stream().map(i -> i.getValue()).collect(Collectors.toList());
		
		// in the case of Drag n' Drop, we don't need to serialize it
		String data = serializeData ? JsonIO.write(objectData) : "NA";
		List<String> textData = new ArrayList<>();
		DataFormat format = null;
		
		Class<?> clazz = this.document.getSelectedType();
		if (clazz == Book.class) {
			format = BOOK_CLIPBOARD_DATA;
			textData = items.stream().map(b -> ((Book)b.getValue()).getName()).collect(Collectors.toList());
		} else if (clazz == Chapter.class) {
			format = CHAPTER_CLIPBOARD_DATA;
			textData = items.stream().map(c -> ((Chapter)c.getValue()).toString()).collect(Collectors.toList());
		} else if (clazz == Verse.class) {
			format = VERSE_CLIPBOARD_DATA;
			textData = items.stream().map(v -> ((Verse)v.getValue()).getText()).collect(Collectors.toList());
		}
		
		ClipboardContent content = new ClipboardContent();
		content.putString(String.join(Constants.NEW_LINE, textData));
		content.put(format, data);
		
		return content;
	}
	
	private CompletableFuture<Void> copy(boolean isCut) {
		Class<?> clazz = this.document.getSelectedType();
		if (clazz != null && clazz != Bible.class) {
			List<TreeItem<Object>> items = this.treeView.getSelectionModel().getSelectedItems();
			List<Object> objectData = items.stream().map(i -> i.getValue()).collect(Collectors.toList());
			try {
				ClipboardContent content = this.getClipboardContentForSelection(true);
				Clipboard clipboard = Clipboard.getSystemClipboard();
				clipboard.setContent(content);
				
				if (isCut) {
					Object parent = items.get(0).getParent().getValue();
					if (clazz == Book.class) {
						((Bible)parent).getBooks().removeAll(objectData);
					} else if (clazz == Chapter.class) {
						((Book)parent).getChapters().removeAll(objectData);
					} else if (clazz == Verse.class) {
						((Chapter)parent).getVerses().removeAll(objectData);
					}
				}
				
				// handle the selection state changing
				this.fireEvent(new ActionStateChangedEvent(this, this.treeView, ActionStateChangedEvent.CLIPBOARD));
			} catch (Exception ex) {
				LOGGER.warn("Failed to create ClipboardContent for current selection (copy/cut)", ex);
			}
		}
		
		return AsyncHelper.nil();
	}
	
	private CompletableFuture<Void> paste() {
		if (this.document.getSelectedCount() == 1) {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			TreeItem<Object> selected = this.treeView.getSelectionModel().getSelectedItem();
			try {
				if (selected.getValue() instanceof Bible && clipboard.hasContent(BOOK_CLIPBOARD_DATA)) {
					Book[] books = JsonIO.read((String)clipboard.getContent(BOOK_CLIPBOARD_DATA), Book[].class);
					this.bible.getBooks().addAll(books);
				} else if (selected.getValue() instanceof Book && clipboard.hasContent(CHAPTER_CLIPBOARD_DATA)) {
					Chapter[] chapters = JsonIO.read((String)clipboard.getContent(CHAPTER_CLIPBOARD_DATA), Chapter[].class);
					((Book)selected.getValue()).getChapters().addAll(chapters);
				} else if (selected.getValue() instanceof Chapter && clipboard.hasContent(VERSE_CLIPBOARD_DATA)) {
					Verse[] verses = JsonIO.read((String)clipboard.getContent(VERSE_CLIPBOARD_DATA), Verse[].class);
					((Chapter)selected.getValue()).getVerses().addAll(verses);
				}
				// TODO select the pasted elements
			} catch (Exception ex) {
				LOGGER.warn("Failed to paste clipboard content (likely due to a JSON deserialization error", ex);
			}
		}
		
		return AsyncHelper.nil();
	}
	
	private void dragDetected(MouseEvent e) {
		if (this.document.isSingleTypeSelected()) {
			try {
				Dragboard db = ((Node)e.getSource()).startDragAndDrop(TransferMode.COPY_OR_MOVE);
				ClipboardContent content = this.getClipboardContentForSelection(false);
				db.setContent(content);
			} catch (Exception ex) {
				LOGGER.warn("Failed to create ClipboardContent for current selection (drag detected)", ex);
			}
		}
	}
	
	private void dragExited(DragEvent e) {
		if (e.getSource() instanceof BibleTreeCell) {
			BibleTreeCell cell = (BibleTreeCell)e.getSource();
			cell.pseudoClassStateChanged(DRAG_OVER_PARENT, false);
			cell.pseudoClassStateChanged(DRAG_OVER_SIBLING_BOTTOM, false);
			cell.pseudoClassStateChanged(DRAG_OVER_SIBLING_TOP, false);
		}
	}
	
	private void dragEntered(DragEvent e) {
		// nothing to do here
	}
	
	private void dragOver(DragEvent e) {
		if (!(e.getSource() instanceof BibleTreeCell)) {
			return;
		}
		
		// don't allow drop onto itself
		BibleTreeCell cell = (BibleTreeCell)e.getSource();
		TreeItem<Object> item = cell.getTreeItem();
		if (this.treeView.getSelectionModel().getSelectedItems().contains(item)) {
			return;
		}
		
		// check for null data
		Object data = item.getValue();
		if (data == null) {
			return;
		}
		
		// don't allow drop onto incorrect locations
		boolean dragBooks = e.getDragboard().hasContent(BOOK_CLIPBOARD_DATA);
		boolean dragChapters = e.getDragboard().hasContent(CHAPTER_CLIPBOARD_DATA);
		boolean dragVerses = e.getDragboard().hasContent(VERSE_CLIPBOARD_DATA);
		
		boolean targetIsBible = data instanceof Bible;
		boolean targetIsBook = data instanceof Book;
		boolean targetIsChapter = data instanceof Chapter;
		boolean targetIsVerse = data instanceof Verse;
		
		boolean isAllowed = 
				(dragBooks && targetIsBible) ||
				(dragBooks && targetIsBook) ||
				(dragChapters && targetIsBook) ||
				(dragChapters && targetIsChapter) ||
				(dragVerses && targetIsChapter) ||
				(dragVerses && targetIsVerse);
		
		if (!isAllowed) {
			return;
		}
		
		// allow the transfer
		e.acceptTransferModes(TransferMode.MOVE);
		
		boolean isParent = 
				(dragBooks && targetIsBible) ||
				(dragChapters && targetIsBook) ||
				(dragVerses && targetIsChapter);

		if (isParent) {
			cell.pseudoClassStateChanged(DRAG_OVER_PARENT, true);
		} else {
			if (e.getY() < cell.getHeight() * 0.75) {
				cell.pseudoClassStateChanged(DRAG_OVER_SIBLING_TOP, true);
				cell.pseudoClassStateChanged(DRAG_OVER_SIBLING_BOTTOM, false);
			} else {
				cell.pseudoClassStateChanged(DRAG_OVER_SIBLING_BOTTOM, true);
				cell.pseudoClassStateChanged(DRAG_OVER_SIBLING_TOP, false);
			}
		}
	}
	
	private void dragDropped(DragEvent e) {
		// make sure the target is a valid target
		if (!(e.getGestureTarget() instanceof BibleTreeCell)) {
			return;
		}
		
		// copy the selected items
		List<TreeItem<Object>> selected = new ArrayList<>(this.treeView.getSelectionModel().getSelectedItems());

		// check for null data
		BibleTreeCell target = (BibleTreeCell)e.getGestureTarget();
		TreeItem<Object> targetItem = target.getTreeItem();
		Object targetValue = targetItem.getValue();
		
		// are we dragging to a parent node?
		boolean dragBooks = e.getDragboard().hasContent(BOOK_CLIPBOARD_DATA);
		boolean dragChapters = e.getDragboard().hasContent(CHAPTER_CLIPBOARD_DATA);
		boolean dragVerses = e.getDragboard().hasContent(VERSE_CLIPBOARD_DATA);
		
		boolean targetIsBible = targetValue instanceof Bible;
		boolean targetIsBook = targetValue instanceof Book;
		boolean targetIsChapter = targetValue instanceof Chapter;
		
		boolean isParent = 
				(dragBooks && targetIsBible) ||
				(dragChapters && targetIsBook) ||
				(dragVerses && targetIsChapter);

		this.undoManager.beginBatch("DragDrop");
		
		// remove the data from its previous location
		List<Object> items = new ArrayList<>();
		for (TreeItem<Object> item : selected) {
			Object child = item.getValue();
			Object parent = item.getParent().getValue();
			if (child instanceof Verse) {
				((Chapter)parent).getVerses().remove(child);
			} else if (child instanceof Chapter) {
				((Book)parent).getChapters().remove(child);
			} else if (child instanceof Book) {
				((Bible)parent).getBooks().remove(child);
			}
			items.add(child);
		}
		
		// now add the data
		Object parent = isParent ? targetValue : targetItem.getParent().getValue();
		int size = targetItem.getChildren().size();
		int index = isParent ? size : targetItem.getParent().getChildren().indexOf(targetItem);
		boolean after = e.getY() >= target.getHeight() * 0.75;
		if (!isParent && after) index++;
		
		if (dragBooks) {
			((Bible)parent).getBooks().addAll(index, items.stream().map(i -> (Book)i).collect(Collectors.toList()));
		} else if (dragChapters) {
			((Book)parent).getChapters().addAll(index, items.stream().map(i -> (Chapter)i).collect(Collectors.toList()));
		} else if (dragVerses) {
			((Chapter)parent).getVerses().addAll(index, items.stream().map(i -> (Verse)i).collect(Collectors.toList()));
		}
		
		int row = (isParent && size > 0 
				? this.treeView.getRow(targetItem.getChildren().get(size - 1))
				: this.treeView.getRow(targetItem)) 
				+ (!isParent && after ? 1 : -items.size());
		this.treeView.getSelectionModel().clearSelection();
		this.treeView.getSelectionModel().selectRange(row, row + items.size());
		
		this.undoManager.completeBatch();
		
		e.setDropCompleted(true);
	}
	
	private void dragDone(DragEvent e) {
		// nothing to do
	}
}
