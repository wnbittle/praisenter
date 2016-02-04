import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.praisenter.javafx.easing.Easing;
import org.praisenter.javafx.easing.EasingType;
import org.praisenter.javafx.easing.Easings;
import org.praisenter.javafx.transition.CustomTransition;
import org.praisenter.javafx.transition.TransitionType;
import org.praisenter.javafx.transition.Transitions;


public class TransitionTest extends Application {
	public static void main(String[] args) {
		Application.launch(args);
	}

	// for each slide
	// on button click
	// 	1. build node (the slide contents)
	//	2. Add node to scene with offset position (if necessary)
	//  3. perform animation (on correct nodes, ie. smart/stationary backgrounds)
	//  4. remove other node
	
	@Override
	public void start(Stage stage) throws Exception {
		ObservableList<Screen> screens = Screen.getScreens();
		for (Screen screen : screens) {
			System.out.println(screen.getBounds());
		}

		Screen screen = screens.get(1);
//		Rectangle2D bounds = screen.getBounds();
		Rectangle2D bounds = new Rectangle2D(0, 0, 400, 400);
		// creating a new window
		// transparent background
		Stage other = new Stage(StageStyle.TRANSPARENT);
		// size to fill the screen
		other.setWidth(bounds.getWidth());
		other.setHeight(bounds.getHeight());
		other.setX(bounds.getMinX());
		other.setY(bounds.getMinY());
		
		Pane stack = new Pane();

		VBox s1 = new VBox();
		s1.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 255, 0.5), null, null)));
		s1.setMinSize(bounds.getWidth(), bounds.getHeight());
		
		stack.getChildren().add(s1);
		
		// now say we want to transition a new piece
		// TODO scaling
		VBox s2 = new VBox();
		s2.setBackground(new Background(new BackgroundFill(Color.rgb(255, 0, 0, 0.5), null, null)));
		s2.setMinSize(bounds.getWidth(), bounds.getHeight());
		
		stack.getChildren().add(s2);
		
		Transition tx = Transitions.getCircularCollapse(bounds, s1, s2, Duration.millis(500), Easings.getBounce(EasingType.IN));
		
		Scene scene = new Scene(stack, Color.TRANSPARENT);
		other.setScene(scene);
		other.show();
//		stage.setScene(scene);
//		stage.show();
		
//		TranslateTransition tx1 = new TranslateTransition(Duration.millis(500), s1);
//		tx1.setToX(-s1.getMinWidth());
//		tx1.setInterpolator(Easings.getBack(EasingType.IN));
//		TranslateTransition tx2 = new TranslateTransition(Duration.millis(500), s2);
//		tx2.setByX(-s2.getLayoutX());
//		tx2.setInterpolator(Easings.getBack(EasingType.IN));
//		
//		ParallelTransition px = new ParallelTransition(tx1, tx2);
//		
//		px.play();
		

		
		tx.play();
	}
}
