package org.praisenter.javafx;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.praisenter.javafx.async.AsyncTask;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;

// TODO translate
class MainStatusBar extends HBox {
	
	private final PraisenterContext context;
	
	public MainStatusBar(PraisenterContext context) {
		this.context = context;

		Button button = new Button("Progress");
		button.setContentDisplay(ContentDisplay.RIGHT);
		
		ProgressBar progress = new ProgressBar();
		progress.setProgress(0);
		this.context.getExecutorService().runningProperty().addListener((obs, ov, nv) -> {
			if (nv.intValue() > 0) {
				progress.setProgress(-1);
			} else {
				progress.setProgress(0);
			}
		});
		progress.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			// for whatever reason, mouse events are not propagated
			// from the progress bar to the button so do it manually
			button.getOnAction().handle(new ActionEvent());
		});
		progress.setMaxHeight(10);
		progress.setMinWidth(200);
		progress.setPrefWidth(200);
		progress.setStyle("-fx-indeterminate-bar-escape: false; -fx-indeterminate-bar-flip: true;");
		
		button.setGraphic(progress);
		
		ListView<AsyncTask<?>> view = new ListView<AsyncTask<?>>(context.getExecutorService().tasksProperty());
		view.setPrefSize(200, 300);
		view.setPlaceholder(new Label("No pending or completed tasks"));
		view.setCellFactory(new Callback<ListView<AsyncTask<?>>, ListCell<AsyncTask<?>>>() {
			@Override
			public ListCell<AsyncTask<?>> call(ListView<AsyncTask<?>> view) {
				return new MonitoredTaskListCell();
			}
		});
		BorderPane layout = new BorderPane(view);
		
		PopOver pop = new PopOver(layout);
		pop.setDetachable(false);
		pop.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
		pop.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
		pop.setAutoFix(false);
		
		button.setOnAction(e -> {
			// show task detail view
			pop.show(button);
		});
		
		button.addEventFilter(KeyEvent.ANY, e -> {
			for (ApplicationAction action : ApplicationAction.values()) {
				if (action.getAccelerator() != null && action.getAccelerator().match(e)) {
					e.consume();
					fireEvent(new ApplicationEvent(this, this, ApplicationEvent.ALL, action));
				}
			}
		});
		
		this.setSpacing(2);
		this.getChildren().addAll(button);
	}
}
