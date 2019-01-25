package org.praisenter.ui.slide;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.praisenter.data.Tag;
import org.praisenter.data.TextType;
import org.praisenter.data.TextVariant;
import org.praisenter.data.configuration.Resolution;
import org.praisenter.data.media.MediaType;
import org.praisenter.data.slide.Slide;
import org.praisenter.data.slide.SlideComponent;
import org.praisenter.data.slide.animation.SlideAnimation;
import org.praisenter.data.slide.effects.SlideShadow;
import org.praisenter.data.slide.graphics.SlideColor;
import org.praisenter.data.slide.graphics.SlidePadding;
import org.praisenter.data.slide.graphics.SlidePaint;
import org.praisenter.data.slide.graphics.SlideStroke;
import org.praisenter.data.slide.media.MediaComponent;
import org.praisenter.data.slide.media.MediaObject;
import org.praisenter.data.slide.text.CountdownComponent;
import org.praisenter.data.slide.text.DateTimeComponent;
import org.praisenter.data.slide.text.FontScaleType;
import org.praisenter.data.slide.text.HorizontalTextAlignment;
import org.praisenter.data.slide.text.SlideFont;
import org.praisenter.data.slide.text.TextComponent;
import org.praisenter.data.slide.text.TextPlaceholderComponent;
import org.praisenter.data.slide.text.VerticalTextAlignment;
import org.praisenter.ui.GlobalContext;
import org.praisenter.ui.Glyphs;
import org.praisenter.ui.MappedList;
import org.praisenter.ui.Option;
import org.praisenter.ui.bind.BindingHelper;
import org.praisenter.ui.controls.DateTimePicker;
import org.praisenter.ui.controls.IntegerSpinnerValueFactory;
import org.praisenter.ui.controls.IntegerStringConverter;
import org.praisenter.ui.controls.LastValueDoubleStringConverter;
import org.praisenter.ui.controls.LongSpinnerValueFactory;
import org.praisenter.ui.controls.SimpleDateFormatConverter;
import org.praisenter.ui.controls.TagListView;
import org.praisenter.ui.controls.TextInputFieldEventFilter;
import org.praisenter.ui.controls.TimeStringConverter;
import org.praisenter.ui.document.DocumentContext;
import org.praisenter.ui.document.DocumentSelectionEditor;
import org.praisenter.ui.slide.controls.MediaPicker;
import org.praisenter.ui.slide.controls.PaintPicker;
import org.praisenter.ui.slide.controls.SlideFontPicker;
import org.praisenter.ui.slide.controls.SlideShadowPicker;
import org.praisenter.ui.slide.convert.TimeFormatConverter;
import org.praisenter.ui.translations.Translations;
import org.praisenter.ui.undo.UndoManager;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

// TODO font paint selection control
// TODO border selection control
// TODO padding selection control
// TODO animation control
public final class SlideSelectionEditor extends VBox implements DocumentSelectionEditor<Slide> {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final GlobalContext context;
	private final ObjectProperty<DocumentContext<Slide>> documentContext;
	
	private final ObjectProperty<Slide> slide;
	private final ObjectProperty<Object> selectedItem;

	// slide
	private final StringProperty name;
	private final ObjectProperty<Integer> width;
	private final ObjectProperty<Integer> height;
	private final LongProperty time;
	private final ObjectProperty<Long> timeAsObject;
	private final ObjectProperty<SlidePaint> background;
	private final ObjectProperty<SlideStroke> border;
	private final DoubleProperty opacity;
	private final ObservableSet<Tag> tags;
	private final ObservableList<SlideAnimation> animations;
	
	private final ObservableList<Option<Resolution>> resolutions;
	
	// general slide component
	private final ObjectProperty<SlideComponent> selectedComponent;
	private final ObjectProperty<SlidePaint> componentBackground;
	private final ObjectProperty<SlideStroke> componentBorder;
	private final DoubleProperty componentOpacity;
	private final ObjectProperty<SlideShadow> componentShadow;
	private final ObjectProperty<SlideShadow> componentGlow;
	
	// media component
	private final ObjectProperty<MediaComponent> selectedMediaComponent;
	private final ObjectProperty<MediaObject> componentMedia;
	
	// generic/basic text component
	private final ObjectProperty<TextComponent> selectedTextComponent;
	private final StringProperty componentText;
	private final ObjectProperty<SlidePaint> componentTextPaint;
	private final ObjectProperty<SlideStroke> componentTextBorder;
	private final ObjectProperty<SlideFont> componentFont;
	private final ObjectProperty<FontScaleType> componentFontScaleType;
	private final ObjectProperty<VerticalTextAlignment> componentVerticalTextAlignment;
	private final ObjectProperty<HorizontalTextAlignment> componentHorizontalTextAlignment;
	private final ObjectProperty<SlidePadding> componentPadding;
	private final DoubleProperty componentLineSpacing;
	private final ObjectProperty<Double> componentLineSpacingAsObject;
	private final BooleanProperty componentTextWrapping;
	private final ObjectProperty<SlideShadow> componentTextShadow;
	private final ObjectProperty<SlideShadow> componentTextGlow;
	
	// text placeholder component
	private final ObjectProperty<TextPlaceholderComponent> selectedTextPlaceholderComponent;
	private final ObjectProperty<TextType> componentPlaceholderType;
	private final ObjectProperty<TextVariant> componentPlaceholderVariant;
	
	// date/time component
	private final ObjectProperty<DateTimeComponent> selectedDateTimeComponent;
	private final ObjectProperty<SimpleDateFormat> componentDateTimeFormat;
	
	// countdown component
	private final ObjectProperty<CountdownComponent> selectedCountdownComponent;
	private final ObjectProperty<LocalDateTime> componentCountdownTarget;
	private final BooleanProperty componentCountdownTimeOnly;
	private final StringProperty componentCountdownFormat;
	
	// visible bindings
	private final BooleanBinding componentSelected;
	private final BooleanBinding mediaComponentSelected;
	private final BooleanBinding textComponentSelected;
	private final BooleanBinding placeholderComponentSelected;
	private final BooleanBinding dateTimeComponentSelected;
	private final BooleanBinding countdownComponentSelected;
	
	// helpers
	
	public SlideSelectionEditor(GlobalContext context) {
		this.getStyleClass().add("p-selection-properties");
		
		this.context = context;
		this.documentContext = new SimpleObjectProperty<>();
		
		this.slide = new SimpleObjectProperty<>();
		this.selectedItem = new SimpleObjectProperty<>();
		
		this.name = new SimpleStringProperty();
		this.width = new SimpleObjectProperty<>();
		this.height = new SimpleObjectProperty<>();
		this.time = new SimpleLongProperty();
		this.timeAsObject = this.time.asObject();
		this.background = new SimpleObjectProperty<>();
		this.border = new SimpleObjectProperty<>();
		this.opacity = new SimpleDoubleProperty();
		this.tags = FXCollections.observableSet(new HashSet<>());
		this.animations = FXCollections.observableArrayList();
		
		ObservableList<Resolution> resolutions = FXCollections.observableArrayList();
		resolutions.addAll(context.getConfiguration().getResolutions());
		this.updateResolutions(resolutions);
		
		Screen.getScreens().addListener((InvalidationListener)(obs -> this.updateResolutions(resolutions)));
		
		this.resolutions = new MappedList<Option<Resolution>, Resolution>(resolutions.sorted(), r -> {
			Option<Resolution> option = new Option<>(null, r);
			option.nameProperty().bind(Bindings.createStringBinding(() -> {
				boolean isNative = this.isNativeResolution(r);
				// TODO lookup the screen assignment so we can show something other than the index
				return Translations.get("slide.resolution", r.getWidth(), r.getHeight(), isNative ? " (Native)" : "");
			}, Screen.getScreens()));
			return option;
		});
		
		this.selectedComponent = new SimpleObjectProperty<>();
		this.componentBackground = new SimpleObjectProperty<>();
		this.componentBorder = new SimpleObjectProperty<>();
		this.componentOpacity = new SimpleDoubleProperty();
		this.componentShadow = new SimpleObjectProperty<>();
		this.componentGlow = new SimpleObjectProperty<>();
		
		this.selectedMediaComponent = new SimpleObjectProperty<>();
		this.componentMedia = new SimpleObjectProperty<>();
		
		this.selectedTextComponent = new SimpleObjectProperty<>();
		this.componentText = new SimpleStringProperty();
		this.componentTextPaint = new SimpleObjectProperty<>();
		this.componentTextBorder = new SimpleObjectProperty<>();
		this.componentFont = new SimpleObjectProperty<>();
		this.componentFontScaleType = new SimpleObjectProperty<>();
		this.componentHorizontalTextAlignment = new SimpleObjectProperty<>();
		this.componentVerticalTextAlignment = new SimpleObjectProperty<>();
		this.componentPadding = new SimpleObjectProperty<>();
		this.componentLineSpacing = new SimpleDoubleProperty();
		this.componentLineSpacingAsObject = this.componentLineSpacing.asObject();
		this.componentTextWrapping = new SimpleBooleanProperty();
		this.componentTextShadow = new SimpleObjectProperty<>();
		this.componentTextGlow = new SimpleObjectProperty<>();
		
		this.selectedTextPlaceholderComponent = new SimpleObjectProperty<>();
		this.componentPlaceholderType = new SimpleObjectProperty<>();
		this.componentPlaceholderVariant = new SimpleObjectProperty<>();
		
		this.selectedDateTimeComponent = new SimpleObjectProperty<>();
		this.componentDateTimeFormat = new SimpleObjectProperty<>();
		
		this.selectedCountdownComponent = new SimpleObjectProperty<>();
		this.componentCountdownFormat = new SimpleStringProperty();
		this.componentCountdownTarget = new SimpleObjectProperty<>();
		this.componentCountdownTimeOnly = new SimpleBooleanProperty();
		
		this.componentSelected = this.selectedComponent.isNotNull();
		this.mediaComponentSelected = this.selectedMediaComponent.isNotNull();
		this.textComponentSelected = this.selectedTextComponent.isNotNull();
		this.placeholderComponentSelected = this.selectedTextPlaceholderComponent.isNotNull();
		this.dateTimeComponentSelected = this.selectedDateTimeComponent.isNotNull();
		this.countdownComponentSelected = this.selectedCountdownComponent.isNotNull();
		
		this.documentContext.addListener((obs, ov, nv) -> {
			this.slide.unbind();
			this.selectedItem.unbind();
			if (nv != null) {
				this.slide.bind(nv.documentProperty());
				this.selectedItem.bind(nv.selectedItemProperty());
			} else {
				this.slide.set(null);
				this.selectedItem.set(null);
			}
		});
		
		this.selectedItem.addListener((obs, ov, nv) -> {
			if (nv != null && nv instanceof EditNode) {
				EditNode node = (EditNode)nv;
				this.selectedComponent.set(node.getComponent());
			} else {
				this.selectedComponent.set(null);
			}
		});
		
		this.selectedComponent.addListener((obs, ov, nv) -> {
			if (nv != null) {
				if (nv instanceof TextComponent) {
					this.selectedMediaComponent.set(null);
					this.selectedTextComponent.set((TextComponent)nv);
				} else {
					this.selectedTextComponent.set(null);
					this.selectedCountdownComponent.set(null);
					this.selectedDateTimeComponent.set(null);
					this.selectedTextPlaceholderComponent.set(null);
				}
				
				if (nv instanceof MediaComponent) {
					this.selectedMediaComponent.set((MediaComponent)nv);
				} else if (nv instanceof TextPlaceholderComponent) {
					this.selectedTextPlaceholderComponent.set((TextPlaceholderComponent)nv);
					this.selectedCountdownComponent.set(null);
					this.selectedDateTimeComponent.set(null);
				} else if (nv instanceof DateTimeComponent) {
					this.selectedDateTimeComponent.set((DateTimeComponent)nv);
					this.selectedCountdownComponent.set(null);
					this.selectedTextPlaceholderComponent.set(null);
				} else if (nv instanceof CountdownComponent) {
					this.selectedCountdownComponent.set((CountdownComponent)nv);
					this.selectedDateTimeComponent.set(null);
					this.selectedTextPlaceholderComponent.set(null);
				}
			}
		});
		
		this.slide.addListener((obs, ov, nv) -> {
			if (ov != null) {
				this.name.unbindBidirectional(ov.nameProperty());
				this.time.unbindBidirectional(ov.timeProperty());
				this.background.unbindBidirectional(ov.backgroundProperty());
				this.border.unbindBidirectional(ov.borderProperty());
				this.opacity.unbindBidirectional(ov.opacityProperty());
				
				Bindings.unbindContentBidirectional(this.tags, ov.getTags());
				Bindings.unbindContentBidirectional(this.animations, ov.getAnimations());
				
				this.tags.clear();
				this.animations.clear();
			}
			
			if (nv != null) {
				this.width.set((int)Math.ceil(nv.getWidth()));
				this.height.set((int)Math.ceil(nv.getHeight()));
				
				this.name.bindBidirectional(nv.nameProperty());
				this.time.bindBidirectional(nv.timeProperty());
				this.background.bindBidirectional(nv.backgroundProperty());
				this.border.bindBidirectional(nv.borderProperty());
				this.opacity.bindBidirectional(nv.opacityProperty());
				
				Bindings.bindContentBidirectional(this.tags, nv.getTags());
				Bindings.bindContentBidirectional(this.animations, nv.getAnimations());
			}
		});
		
		this.selectedComponent.addListener((obs, ov, nv) -> {
			if (ov != null) {
				this.componentBackground.unbindBidirectional(ov.backgroundProperty());
				this.componentBorder.unbindBidirectional(ov.borderProperty());
				this.componentGlow.unbindBidirectional(ov.glowProperty());
				this.componentOpacity.unbindBidirectional(ov.opacityProperty());
				this.componentShadow.unbindBidirectional(ov.shadowProperty());
			}
			if (nv != null) {
				this.componentBackground.bindBidirectional(nv.backgroundProperty());
				this.componentBorder.bindBidirectional(nv.borderProperty());
				this.componentGlow.bindBidirectional(nv.glowProperty());
				this.componentOpacity.bindBidirectional(nv.opacityProperty());
				this.componentShadow.bindBidirectional(nv.shadowProperty());
			}
		});
		
		this.selectedMediaComponent.addListener((obs, ov, nv) -> {
			if (ov != null) {
				this.componentMedia.unbindBidirectional(ov.mediaProperty());
			}
			if (nv != null) {
				this.componentMedia.bindBidirectional(nv.mediaProperty());
			}
		});
		
		this.selectedTextComponent.addListener((obs, ov, nv) -> {
			if (ov != null) {
				if (ov.getClass() == TextComponent.class) {
					this.componentText.unbindBidirectional(ov.textProperty());
				}
				this.componentTextPaint.unbindBidirectional(ov.textPaintProperty());
				this.componentTextBorder.unbindBidirectional(ov.textBorderProperty());
				this.componentFont.unbindBidirectional(ov.fontProperty());
				this.componentFontScaleType.unbindBidirectional(ov.fontScaleTypeProperty());
				this.componentHorizontalTextAlignment.unbindBidirectional(ov.horizontalTextAlignmentProperty());
				this.componentVerticalTextAlignment.unbindBidirectional(ov.verticalTextAlignmentProperty());
				this.componentLineSpacing.unbindBidirectional(ov.lineSpacingProperty());
				this.componentTextWrapping.unbindBidirectional(ov.textWrappingEnabledProperty());
				this.componentPadding.unbindBidirectional(ov.paddingProperty());
				this.componentLineSpacing.unbindBidirectional(ov.lineSpacingProperty());
				this.componentTextGlow.unbindBidirectional(ov.textGlowProperty());
				this.componentTextShadow.unbindBidirectional(ov.textShadowProperty());
			}
			if (nv != null) {
				if (nv.getClass() == TextComponent.class) {
					this.componentText.bindBidirectional(nv.textProperty());
				}
				this.componentTextPaint.bindBidirectional(nv.textPaintProperty());
				this.componentTextBorder.bindBidirectional(nv.textBorderProperty());
				this.componentFont.bindBidirectional(nv.fontProperty());
				this.componentFontScaleType.bindBidirectional(nv.fontScaleTypeProperty());
				this.componentHorizontalTextAlignment.bindBidirectional(nv.horizontalTextAlignmentProperty());
				this.componentVerticalTextAlignment.bindBidirectional(nv.verticalTextAlignmentProperty());
				this.componentLineSpacing.bindBidirectional(nv.lineSpacingProperty());
				this.componentTextWrapping.bindBidirectional(nv.textWrappingEnabledProperty());
				this.componentPadding.bindBidirectional(nv.paddingProperty());
				this.componentLineSpacing.bindBidirectional(nv.lineSpacingProperty());
				this.componentTextGlow.bindBidirectional(nv.textGlowProperty());
				this.componentTextShadow.bindBidirectional(nv.textShadowProperty());
			}
		});
		
		this.selectedTextPlaceholderComponent.addListener((obs, ov, nv) -> {
			if (ov != null) {
				this.componentPlaceholderType.unbindBidirectional(ov.placeholderTypeProperty());
				this.componentPlaceholderVariant.unbindBidirectional(ov.placeholderVariantProperty());
			}
			if (nv != null) {
				this.componentPlaceholderType.bindBidirectional(nv.placeholderTypeProperty());
				this.componentPlaceholderVariant.bindBidirectional(nv.placeholderVariantProperty());
			}
		});
		
		this.selectedDateTimeComponent.addListener((obs, ov, nv) -> {
			if (ov != null) {
				this.componentDateTimeFormat.unbindBidirectional(ov.dateTimeFormatProperty());
			}
			if (nv != null) {
				this.componentDateTimeFormat.bindBidirectional(nv.dateTimeFormatProperty());
			}
		});
		
		this.selectedCountdownComponent.addListener((obs, ov, nv) -> {
			if (ov != null) {
				this.componentCountdownFormat.unbindBidirectional(ov.countdownFormatProperty());
				this.componentCountdownTarget.unbindBidirectional(ov.countdownTargetProperty());
				this.componentCountdownTimeOnly.unbindBidirectional(ov.countdownTimeOnlyProperty());
			}
			if (nv != null) {
				this.componentCountdownFormat.bindBidirectional(nv.countdownFormatProperty());
				this.componentCountdownTarget.bindBidirectional(nv.countdownTargetProperty());
				this.componentCountdownTimeOnly.bindBidirectional(nv.countdownTimeOnlyProperty());
			}
		});
		
		// UI
		
		Label lblName = new Label(Translations.get("slide.name"));
		TextField txtName = new TextField();
		txtName.textProperty().bindBidirectional(this.name);
		
		Label lblTargetSize = new Label("slide.target");
		ComboBox<Option<Resolution>> cmbTargetSize = new ComboBox<>(this.resolutions);
		
		Spinner<Integer> spnWidth = new Spinner<>();
		IntegerSpinnerValueFactory spnWidthVF = new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE);
		spnWidthVF.setConverter(new IntegerStringConverter(originalValueText -> {
			Platform.runLater(() -> {
				spnWidth.getEditor().setText(originalValueText);
			});
		}));
		spnWidth.setValueFactory(spnWidthVF);
		spnWidth.setEditable(true);
		spnWidth.getValueFactory().valueProperty().bindBidirectional(this.width);

		Spinner<Integer> spnHeight = new Spinner<>();
		IntegerSpinnerValueFactory spnHeightVF = new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE);
		spnHeightVF.setConverter(new IntegerStringConverter(originalValueText -> {
			Platform.runLater(() -> {
				spnHeight.getEditor().setText(originalValueText);
			});
		}));
		spnHeight.setValueFactory(spnHeightVF);
		spnHeight.setEditable(true);
		spnHeight.getValueFactory().valueProperty().bindBidirectional(this.height);
		
		cmbTargetSize.valueProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				Resolution r = nv.getValue();
				if (r != null) {
					spnWidth.getValueFactory().setValue(r.getWidth());
					spnHeight.getValueFactory().setValue(r.getHeight());
				}
			}
		});
		
		Button btnApplySize = new Button("", Glyphs.WARN.duplicate());
		Button btnResetSize = new Button("", Glyphs.UNDO.duplicate());
		Button btnSaveSize = new Button("", Glyphs.SAVE.duplicate());
		
		btnApplySize.setOnAction(e -> {
			DocumentContext<Slide> document = this.documentContext.get();
			if (document != null) {
				Slide slide = document.getDocument();
				if (slide != null) {
					int cw = (int)Math.ceil(slide.getWidth());
					int ch = (int)Math.ceil(slide.getHeight());
					int tw = spnWidth.getValue();
					int th = spnHeight.getValue();
					
					if (cw != tw || ch != th) {
						UndoManager undoManager = document.getUndoManager();
						undoManager.addEdit(new SlideTargetSizeEdit(
								this.width,
								this.height,
								cw, ch, tw, th,
								(w, h) -> {
									slide.fit(w, h);
								}));
						// update the slide
						slide.fit(tw, th);
					}
				}
			}
		});
		
		btnResetSize.setOnAction(e -> {
			DocumentContext<Slide> document = this.documentContext.get();
			if (document != null) {
				Slide slide = document.getDocument();
				if (slide != null) {
					int cw = (int)Math.ceil(slide.getWidth());
					int ch = (int)Math.ceil(slide.getHeight());
					
					spnWidth.getValueFactory().setValue(cw);
					spnHeight.getValueFactory().setValue(ch);
				}
			}
		});
		
		btnSaveSize.setOnAction(e -> {
			int tw = spnWidth.getValue();
			int th = spnHeight.getValue();
			Resolution nr = new Resolution(tw, th);
			if (resolutions.indexOf(nr) < 0) {
				resolutions.add(nr);
				this.context.getConfiguration().getResolutions().add(nr);
				this.context.saveConfiguration();
			}
		});
		
		PaintPicker pkrBackground = new PaintPicker(context, true, true, true, true, true);
		pkrBackground.valueProperty().bindBidirectional(this.background);
		
		Button btnInsertText = new Button("t");
		Button btnInsertPlaceholder = new Button("p");
		Button btnInsertDateTime = new Button("d");
		Button btnInsertCountdown = new Button("c");
		Button btnInsertMedia = new Button("m");
		
		btnInsertText.setOnAction(e -> {
			TextComponent tc = new TextComponent();
			tc.setWidth(300);
			tc.setHeight(100);
			tc.setText("New text");
			tc.setX(100);
			tc.setY(100);
			tc.setTextWrappingEnabled(true);
			Slide slide = this.slide.get();
			if (slide != null) {
				slide.getComponents().add(tc);
			}
		});
		
		btnInsertPlaceholder.setOnAction(e -> {
			TextPlaceholderComponent tpc = new TextPlaceholderComponent();
			tpc.setWidth(300);
			tpc.setHeight(100);
			tpc.setX(100);
			tpc.setY(100);
			tpc.setTextWrappingEnabled(true);
			tpc.setPlaceholderType(TextType.TEXT);
			tpc.setPlaceholderVariant(TextVariant.PRIMARY);
			Slide slide = this.slide.get();
			if (slide != null) {
				slide.getComponents().add(tpc);
			}
		});
		
		btnInsertDateTime.setOnAction(e -> {
			DateTimeComponent dtc = new DateTimeComponent();
			dtc.setWidth(300);
			dtc.setHeight(100);
			dtc.setX(100);
			dtc.setY(100);
			dtc.setTextWrappingEnabled(true);
			Slide slide = this.slide.get();
			if (slide != null) {
				slide.getComponents().add(dtc);
			}
		});
		
		btnInsertCountdown.setOnAction(e -> {
			CountdownComponent cc = new CountdownComponent();
			cc.setWidth(300);
			cc.setHeight(100);
			cc.setX(100);
			cc.setY(100);
			cc.setTextWrappingEnabled(true);
			cc.setCountdownTarget(LocalDateTime.now().plusHours(1));
			Slide slide = this.slide.get();
			if (slide != null) {
				slide.getComponents().add(cc);
			}
		});
		
		btnInsertMedia.setOnAction(e -> {
			MediaComponent mc = new MediaComponent();
			mc.setWidth(300);
			mc.setHeight(100);
			mc.setX(100);
			mc.setY(100);
			mc.setBackground(new SlideColor());
			Slide slide = this.slide.get();
			if (slide != null) {
				slide.getComponents().add(mc);
			}
		});
		
		Label lblTime = new Label(Translations.get("slide.time"));
		Spinner<Long> spnTime = new Spinner<>(0, Long.MAX_VALUE, 0, 5);
		spnTime.setEditable(true);
		LongSpinnerValueFactory spnTimeVF = new LongSpinnerValueFactory(0, Long.MAX_VALUE, 0, 5);
		spnTimeVF.setConverter(new TimeStringConverter((originalValueText) -> {
			Platform.runLater(() -> {
				spnTime.getEditor().setText(originalValueText);
			});
		}));
		spnTime.setValueFactory(spnTimeVF);
		spnTime.getValueFactory().valueProperty().bindBidirectional(this.timeAsObject);
		
		Label lblOpacity = new Label(Translations.get("slide.opacity"));
		Slider sldOpacity = new Slider(0, 1, 1);
		sldOpacity.valueProperty().bindBidirectional(this.opacity);

		TagListView viewTags = new TagListView(this.context.getDataManager().getTagsUmodifiable());
		Bindings.bindContentBidirectional(viewTags.getTags(), this.tags);
		
		// generic component
		
		Label lblComponentOrder = new Label(Translations.get("slide.component.order"));
		Button btnComponentMoveUp = new Button("", this.buildMoveUpStackingGraphic());
		Button btnComponentMoveDown = new Button("", this.buildMoveDownStackingGraphic());
		Button btnComponentMoveFront = new Button("", this.buildMoveFrontStackingGraphic());
		Button btnComponentMoveBack = new Button("", this.buildMoveBackStackingGraphic());
		
		btnComponentMoveUp.setOnAction(e -> {
			Slide slide = this.slide.get();
			SlideComponent component = this.selectedComponent.get();
			if (slide != null && component != null) {
				DocumentContext<Slide> document = this.documentContext.get();
				if (document != null) {
					UndoManager undoManager = document.getUndoManager();
					undoManager.beginBatch("moveup");
					slide.moveComponentUp(component);
					undoManager.completeBatch();
				}
			}
		});
		btnComponentMoveDown.setOnAction(e -> {
			Slide slide = this.slide.get();
			SlideComponent component = this.selectedComponent.get();
			if (slide != null && component != null) {
				DocumentContext<Slide> document = this.documentContext.get();
				if (document != null) {
					UndoManager undoManager = document.getUndoManager();
					undoManager.beginBatch("movedown");
					slide.moveComponentDown(component);
					undoManager.completeBatch();
				}
			}
		});
		btnComponentMoveFront.setOnAction(e -> {
			Slide slide = this.slide.get();
			SlideComponent component = this.selectedComponent.get();
			if (slide != null && component != null) {
				DocumentContext<Slide> document = this.documentContext.get();
				if (document != null) {
					UndoManager undoManager = document.getUndoManager();
					undoManager.beginBatch("movefront");
					slide.moveComponentFront(component);
					undoManager.completeBatch();
				}
			}
		});
		btnComponentMoveBack.setOnAction(e -> {
			Slide slide = this.slide.get();
			SlideComponent component = this.selectedComponent.get();
			if (slide != null && component != null) {
				DocumentContext<Slide> document = this.documentContext.get();
				if (document != null) {
					UndoManager undoManager = document.getUndoManager();
					undoManager.beginBatch("moveback");
					slide.moveComponentBack(component);
					undoManager.completeBatch();
				}
			}
		});
		
		Label lblComponentBackground = new Label(Translations.get("slide.background"));
		PaintPicker pkrComponentBackground = new PaintPicker(context, true, true, true, true, true);
		pkrComponentBackground.valueProperty().bindBidirectional(this.componentBackground);
		
		Label lblComponentOpacity = new Label(Translations.get("slide.opacity"));
		Slider sldComponentOpacity = new Slider(0, 1, 1);
		sldComponentOpacity.valueProperty().bindBidirectional(this.componentOpacity);
		
		Label lblComponentShadow = new Label(Translations.get("slide.shadow"));
		SlideShadowPicker pkrComponentShadow = new SlideShadowPicker();
		pkrComponentShadow.valueProperty().bindBidirectional(this.componentShadow);

		Label lblComponentGlow = new Label(Translations.get("slide.glow"));
		SlideShadowPicker pkrComponentGlow = new SlideShadowPicker();
		pkrComponentGlow.valueProperty().bindBidirectional(this.componentGlow);
		
		// media component
		
		Label lblComponentMedia = new Label(Translations.get("slide.media"));
		MediaPicker pkrComponentMedia = new MediaPicker(context, MediaType.AUDIO, MediaType.VIDEO, MediaType.IMAGE);
		pkrComponentMedia.valueProperty().bindBidirectional(this.componentMedia);
		
		// basic text component
		
		Label lblComponentText = new Label(Translations.get("slide.text"));
		TextArea txtComponentText = new TextArea();
		txtComponentText.setWrapText(true);
		txtComponentText.textProperty().bindBidirectional(this.componentText);
		
		Label lblComponentTextPaint = new Label(Translations.get("slide.text.paint"));
		PaintPicker pkrComponentTextPaint = new PaintPicker(context, false, true, true, false, false);
		pkrComponentTextPaint.valueProperty().bindBidirectional(this.componentTextPaint);
		
		Label lblComponentFont = new Label(Translations.get("slide.font"));
		SlideFontPicker pkrComponentFont = new SlideFontPicker();
		pkrComponentFont.valueProperty().bindBidirectional(this.componentFont);
		
		Label lblComponentFontScaleType = new Label(Translations.get("slide.font.scale"));
		ObservableList<Option<FontScaleType>> fontScaleOptions = FXCollections.observableArrayList();
		for (FontScaleType type : FontScaleType.values()) {
			fontScaleOptions.add(new Option<FontScaleType>(Translations.get("slide.font.scale." + type), type));
		}
		ChoiceBox<Option<FontScaleType>> cbComponentFontScaleType = new ChoiceBox<>(fontScaleOptions);
		BindingHelper.bindBidirectional(cbComponentFontScaleType.valueProperty(), this.componentFontScaleType);
		
		Label lblComponentTextHAlignment = new Label(Translations.get("slide.text.halignment"));
//		ObservableList<Option<HorizontalTextAlignment>> hAlignmentOptions = FXCollections.observableArrayList();
//		for (HorizontalTextAlignment alignment : HorizontalTextAlignment.values()) {
//			hAlignmentOptions.add(new Option<HorizontalTextAlignment>(Translations.get("slide.text.halignment" + alignment), alignment));
//		}
//		ChoiceBox<Option<HorizontalTextAlignment>> cbComponentTextHAlignment = new ChoiceBox<>(hAlignmentOptions);
//		BindingHelper.bindBidirectional(cbComponentTextHAlignment.valueProperty(), this.componentHorizontalTextAlignment);
		
		ToggleButton tglHAlignLeft = new ToggleButton("", Glyphs.HALIGN_LEFT.duplicate());
		ToggleButton tglHAlignRight = new ToggleButton("", Glyphs.HALIGN_RIGHT.duplicate());
		ToggleButton tglHAlignCenter = new ToggleButton("", Glyphs.HALIGN_CENTER.duplicate());
		ToggleButton tglHAlignJustify = new ToggleButton("", Glyphs.HALIGN_JUSTIFY.duplicate());
		BindingHelper.bindBidirectional(this.componentHorizontalTextAlignment, tglHAlignLeft.selectedProperty(), HorizontalTextAlignment.LEFT);
		BindingHelper.bindBidirectional(this.componentHorizontalTextAlignment, tglHAlignRight.selectedProperty(), HorizontalTextAlignment.RIGHT);
		BindingHelper.bindBidirectional(this.componentHorizontalTextAlignment, tglHAlignCenter.selectedProperty(), HorizontalTextAlignment.CENTER);
		BindingHelper.bindBidirectional(this.componentHorizontalTextAlignment, tglHAlignJustify.selectedProperty(), HorizontalTextAlignment.JUSTIFY);
		// NOTE: SegmentedButton has an issue when running on Java 9+, the Controls FX 9.0.0 version printed a nasty error about reflecting on com.sun code (but worked)
//		SegmentedButton segComponentHAlignment = new SegmentedButton(tglHAlignLeft, tglHAlignCenter, tglHAlignRight, tglHAlignJustify);
		HBox segComponentHAlignment = new HBox(1, tglHAlignLeft, tglHAlignCenter, tglHAlignRight, tglHAlignJustify);

		Label lblComponentTextVAlignment = new Label(Translations.get("slide.text.valignment"));
//		ObservableList<Option<VerticalTextAlignment>> vAlignmentOptions = FXCollections.observableArrayList();
//		for (VerticalTextAlignment alignment : VerticalTextAlignment.values()) {
//			vAlignmentOptions.add(new Option<VerticalTextAlignment>(Translations.get("slide.text.valignment." + alignment), alignment));
//		}
//		ChoiceBox<Option<VerticalTextAlignment>> cbComponentTextVAlignment = new ChoiceBox<>(vAlignmentOptions);
//		BindingHelper.bindBidirectional(cbComponentTextVAlignment.valueProperty(), this.componentVerticalTextAlignment);
		
		ToggleButton tglVAlignTop = new ToggleButton("", Glyphs.VALIGN_TOP.duplicate());
		ToggleButton tglVAlignCenter = new ToggleButton("", Glyphs.VALIGN_CENTER.duplicate());
		ToggleButton tglVAlignBottom = new ToggleButton("", Glyphs.VALIGN_BOTTOM.duplicate());
		BindingHelper.bindBidirectional(this.componentVerticalTextAlignment, tglVAlignTop.selectedProperty(), VerticalTextAlignment.TOP);
		BindingHelper.bindBidirectional(this.componentVerticalTextAlignment, tglVAlignCenter.selectedProperty(), VerticalTextAlignment.CENTER);
		BindingHelper.bindBidirectional(this.componentVerticalTextAlignment, tglVAlignBottom.selectedProperty(), VerticalTextAlignment.BOTTOM);
		HBox segComponentVAlignment = new HBox(1, tglVAlignTop, tglVAlignCenter, tglVAlignBottom);
		
		Label lblComponentLineSpacing = new Label(Translations.get("slide.text.linespacing"));
		Spinner<Double> spnComponentLineSpacing = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, 1, 0.1);
		spnComponentLineSpacing.setEditable(true);
		spnComponentLineSpacing.getValueFactory().setConverter(new LastValueDoubleStringConverter((originalValueText) -> {
			Platform.runLater(() -> {
				spnComponentLineSpacing.getEditor().setText(originalValueText);
			});
		}));
		spnComponentLineSpacing.getValueFactory().valueProperty().bindBidirectional(this.componentLineSpacingAsObject);
		
//		Label lblComponentTextWrapping = new Label(Translations.get("slide.text.wrapping"));
		CheckBox chkComponentTextWrapping = new CheckBox(Translations.get("slide.text.wrapping"));
		chkComponentTextWrapping.selectedProperty().bindBidirectional(this.componentTextWrapping);
		
		Label lblComponentTextShadow = new Label(Translations.get("slide.shadow"));
		SlideShadowPicker pkrComponentTextShadow = new SlideShadowPicker();
		pkrComponentTextShadow.valueProperty().bindBidirectional(this.componentTextShadow);

		Label lblComponentTextGlow = new Label(Translations.get("slide.glow"));
		SlideShadowPicker pkrComponentTextGlow = new SlideShadowPicker();
		pkrComponentTextGlow.valueProperty().bindBidirectional(this.componentTextGlow);
		
		// placeholder component
		
		Label lblComponentPlaceholderVariant = new Label(Translations.get("slide.placeholder.variant"));
		ObservableList<Option<TextVariant>> placeholderVariantOptions = FXCollections.observableArrayList();
		for (TextVariant variant : TextVariant.values()) {
			placeholderVariantOptions.add(new Option<TextVariant>(Translations.get("slide.text.variant." + variant), variant));
		}
		ChoiceBox<Option<TextVariant>> cbComponentTextVariant = new ChoiceBox<>(placeholderVariantOptions);
		BindingHelper.bindBidirectional(cbComponentTextVariant.valueProperty(), this.componentPlaceholderVariant);
		
		Label lblComponentPlaceholderType = new Label(Translations.get("slide.placeholder.type"));
		ObservableList<Option<TextType>> placeholderTypeOptions = FXCollections.observableArrayList();
		for (TextType textType : TextType.values()) {
			placeholderTypeOptions.add(new Option<TextType>(Translations.get("slide.text.type." + textType), textType));
		}
		ChoiceBox<Option<TextType>> cbComponentTextType = new ChoiceBox<>(placeholderTypeOptions);
		BindingHelper.bindBidirectional(cbComponentTextType.valueProperty(), this.componentPlaceholderType);

		// datetime component
		
		Label lblComponentDateTimeFormat = new Label(Translations.get("slide.datetime.format"));
		ObservableList<SimpleDateFormat> dateTimeFormatOptions = FXCollections.observableArrayList();
		dateTimeFormatOptions.add(new SimpleDateFormat("EEEE MMMM, d yyyy"));
		dateTimeFormatOptions.add(new SimpleDateFormat("EEEE MMMM, d yyyy h:mm a"));
		dateTimeFormatOptions.add(new SimpleDateFormat("EEEE MMMM, d yyyy h:mm a z"));
		dateTimeFormatOptions.add(new SimpleDateFormat("EEE MMM, d yyyy"));
		dateTimeFormatOptions.add(new SimpleDateFormat("EEE MMM, d yyyy h:mm a"));
		dateTimeFormatOptions.add(new SimpleDateFormat("EEE MMM, d yyyy h:mm a z"));
		dateTimeFormatOptions.add(new SimpleDateFormat("M/d/yyyy"));
		dateTimeFormatOptions.add(new SimpleDateFormat("M/d/yyyy h:mm a"));
		dateTimeFormatOptions.add(new SimpleDateFormat("M/d/yyyy h:mm a z"));
		ComboBox<SimpleDateFormat> cmbComponentDateTimeFormat = new ComboBox<>(dateTimeFormatOptions);
		cmbComponentDateTimeFormat.setConverter(new SimpleDateFormatConverter());
		cmbComponentDateTimeFormat.setEditable(true);
		cmbComponentDateTimeFormat.valueProperty().bindBidirectional(this.componentDateTimeFormat);
		
		// countdown component
		
		Label lblComponentCountdownTarget = new Label(Translations.get("slide.countdown.target"));
		DateTimePicker pkrComponentCountdownTarget = new DateTimePicker();
		pkrComponentCountdownTarget.valueProperty().bindBidirectional(this.componentCountdownTarget);

		Label lblComponentCountdownFormat = new Label(Translations.get("slide.countdown.format"));
		ObservableList<String> countdownFormatOptions = FXCollections.observableArrayList();
		countdownFormatOptions.add("YY:MM:DD:hh:mm:ss");
		countdownFormatOptions.add("MM:DD:hh:mm:ss");
		countdownFormatOptions.add("DD:hh:mm:ss");
		countdownFormatOptions.add("hh:mm:ss");
		countdownFormatOptions.add("mm:ss");
		countdownFormatOptions.add("ss");
		ComboBox<String> cmbComponentCountdownFormat = new ComboBox<>(countdownFormatOptions);
		cmbComponentCountdownFormat.setConverter(new TimeFormatConverter());
		cmbComponentCountdownFormat.setEditable(true);
		cmbComponentCountdownFormat.valueProperty().bindBidirectional(this.componentCountdownFormat);
		
		CheckBox chkComponentCountdownTimeOnly = new CheckBox(Translations.get("slide.countdown.timeonly"));
		chkComponentCountdownTimeOnly.selectedProperty().bindBidirectional(this.componentCountdownTimeOnly);
		
		TextInputFieldEventFilter.applyTextInputFieldEventFilter(
				txtName,
				spnTime.getEditor(),
				txtComponentText,
				spnComponentLineSpacing.getEditor());
		
		TitledPane ttlSlide = new TitledPane(Translations.get("slide"), new VBox(
				lblName, txtName,
				lblTargetSize, 
				new HBox(spnWidth, spnHeight, btnApplySize, btnResetSize, btnSaveSize),
				cmbTargetSize,
				lblTime, spnTime,
				lblOpacity, sldOpacity,
				viewTags,
				pkrBackground,
				new HBox(btnInsertText, btnInsertMedia, btnInsertDateTime, btnInsertCountdown, btnInsertPlaceholder)));
		
		VBox layoutTextComponent = new VBox(
				lblComponentText, txtComponentText,
				lblComponentTextPaint, pkrComponentTextPaint,
				lblComponentFont, pkrComponentFont,
				lblComponentFontScaleType, cbComponentFontScaleType,
				lblComponentTextHAlignment, segComponentHAlignment,
				lblComponentTextVAlignment, segComponentVAlignment,
				lblComponentLineSpacing, spnComponentLineSpacing,
				chkComponentTextWrapping,
				lblComponentTextShadow, pkrComponentTextShadow,
				lblComponentTextGlow, pkrComponentTextGlow);
		layoutTextComponent.visibleProperty().bind(this.textComponentSelected);
		layoutTextComponent.managedProperty().bind(layoutTextComponent.visibleProperty());
		
		VBox layoutPlaceholderComponent = new VBox(
				lblComponentPlaceholderType, cbComponentTextType,
				lblComponentPlaceholderVariant, cbComponentTextVariant);
		layoutPlaceholderComponent.visibleProperty().bind(this.placeholderComponentSelected);
		layoutPlaceholderComponent.managedProperty().bind(layoutPlaceholderComponent.visibleProperty());
		
		VBox layoutDateTimeComponent = new VBox(
				lblComponentDateTimeFormat, cmbComponentDateTimeFormat);
		layoutDateTimeComponent.visibleProperty().bind(this.dateTimeComponentSelected);
		layoutDateTimeComponent.managedProperty().bind(layoutDateTimeComponent.visibleProperty());
		
		VBox layoutCountdownComponent = new VBox(
				lblComponentCountdownTarget, pkrComponentCountdownTarget,
				lblComponentCountdownFormat, cmbComponentCountdownFormat,
				chkComponentCountdownTimeOnly);
		layoutCountdownComponent.visibleProperty().bind(this.countdownComponentSelected);
		layoutCountdownComponent.managedProperty().bind(layoutCountdownComponent.visibleProperty());
		
		VBox layoutMediaComponent = new VBox(
				lblComponentMedia,
				pkrComponentMedia);
		layoutMediaComponent.visibleProperty().bind(this.mediaComponentSelected);
		layoutMediaComponent.managedProperty().bind(layoutMediaComponent.visibleProperty());
		
		TitledPane ttlComponent = new TitledPane(Translations.get("slide.component"), new VBox(
				lblComponentOrder,
				new HBox(btnComponentMoveBack, btnComponentMoveDown, btnComponentMoveUp, btnComponentMoveFront),
				lblComponentBackground, pkrComponentBackground,
				lblComponentOpacity, sldComponentOpacity,
				lblComponentShadow, pkrComponentShadow,
				lblComponentGlow, pkrComponentGlow,
				layoutMediaComponent,
				layoutTextComponent,
				layoutPlaceholderComponent,
				layoutDateTimeComponent,
				layoutCountdownComponent));
		
		ScrollPane scroller = new ScrollPane(new VBox(
				ttlSlide,
				ttlComponent));
		scroller.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroller.setFitToWidth(true);
		
		this.getChildren().addAll(scroller);
		
		// hide/show
		
		ttlComponent.visibleProperty().bind(this.componentSelected);
		ttlComponent.managedProperty().bind(ttlComponent.visibleProperty());
	}
	
	private Pane buildMoveUpStackingGraphic() {
		Rectangle upr1 = new Rectangle(1, 1, 10, 10);
		Rectangle upr2 = new Rectangle(5, 5, 10, 10);
		upr1.setFill(Color.GRAY);
		upr2.setFill(Color.BLUE);
		upr1.setSmooth(false);
		return new Pane(upr1, upr2);
	}
	
	private Pane buildMoveDownStackingGraphic() {
		Rectangle downr1 = new Rectangle(1, 1, 10, 10);
		Rectangle downr2 = new Rectangle(5, 5, 10, 10);
		downr1.setFill(Color.BLUE);
		downr2.setFill(Color.GRAY);
		downr2.setSmooth(false);
		return new Pane(downr1, downr2);
	}
	
	private Pane buildMoveFrontStackingGraphic() {
		Rectangle front1 = new Rectangle(1, 1, 10, 10);
		Rectangle front2 = new Rectangle(3, 3, 10, 10);
		Rectangle front3 = new Rectangle(5, 5, 10, 10);
		front1.setFill(Color.DARKGRAY);
		front2.setFill(Color.GRAY);
		front3.setFill(Color.BLUE);
		front1.setSmooth(false);
		front2.setSmooth(false);
		front3.setSmooth(false);
		return new Pane(front1, front2, front3);
	}
	
	private Pane buildMoveBackStackingGraphic() {
		Rectangle back1 = new Rectangle(5, 5, 10, 10);
		Rectangle back2 = new Rectangle(3, 3, 10, 10);
		Rectangle back3 = new Rectangle(1, 1, 10, 10);
		back1.setFill(Color.DARKGRAY);
		back2.setFill(Color.GRAY);
		back3.setFill(Color.BLUE);
		back1.setSmooth(false);
		back2.setSmooth(false);
		back3.setSmooth(false);
		return new Pane(back3, back2, back1);
	}
	
	private void updateResolutions(ObservableList<Resolution> resolutions) {
		List<Resolution> nr = new ArrayList<>();
		for (Screen screen : Screen.getScreens()) {
			Rectangle2D bounds = screen.getBounds();
			Resolution r = new Resolution((int)Math.ceil(bounds.getWidth()), (int)Math.ceil(bounds.getHeight()));
			int index = resolutions.indexOf(r);
			if (index < 0) {
				// doesn't exist
				nr.add(r);
			}
		}
		resolutions.addAll(nr);
	}
	
	private boolean isNativeResolution(Resolution r) {
		for (Screen screen : Screen.getScreens()) {
			Rectangle2D bounds = screen.getBounds();
			if ((int)bounds.getWidth() == r.getWidth() && (int)bounds.getHeight() == r.getHeight()) {
				return true;
			}
		}
		return false;
	}
	
	private Resolution getClosestResolution(Resolution resolution, List<Resolution> resolutions) {
		Resolution closest = null;
		final double target = resolution.getDiagonalLength();
		double comp = Double.MAX_VALUE;
		for (Resolution r : resolutions) {
			if (Objects.equals(r, resolution)) continue;
			double rd = r.getDiagonalLength();
			double diff = rd - target;
			if (Math.abs(diff) < comp) {
				closest = r;
				comp = diff;
			}
		}
		return closest;
	}
	
	public DocumentContext<Slide> getDocumentContext() {
		return this.documentContext.get();
	}
	
	public void setDocumentContext(DocumentContext<Slide> ctx) {
		this.documentContext.set(ctx);
	}
	
	public ObjectProperty<DocumentContext<Slide>> documentContextProperty() {
		return this.documentContext;
	}
}