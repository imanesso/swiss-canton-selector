package cantonselector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;

import java.util.List;

/**
 * Snowman Control can show happiness or sadness based on a boolean value
 *
 * @author Anessollah Ima
 */
public class SnowmanControl extends Region {
    // wird gebraucht fuer StyleableProperties
    private static final StyleablePropertyFactory<SnowmanControl> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    private static final double ARTBOARD_WIDTH = 105;
    private static final double ARTBOARD_HEIGHT = 130;

    private static final double ASPECT_RATIO = ARTBOARD_WIDTH / ARTBOARD_HEIGHT;

    private static final double MINIMUM_WIDTH = 25;
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;

    private static final double MAXIMUM_WIDTH = 800;

    //all parts of the snowmans body
    private Circle buttonDown;
    private Circle buttonMiddle;
    private Circle buttonUp;
    private Ellipse snowLegs;
    private Ellipse snowBelly;
    private Ellipse snowHead;
    private Ellipse eyeLeft;
    private Ellipse eyeRight;
    private Ellipse nose;
    private Line mouth1;
    private Line mouth2;
    private Line armLeft;
    private Line armRight;

    //the boolean property to show sadness or happiness
    private final BooleanProperty value = new SimpleBooleanProperty();

    private static final CssMetaData<SnowmanControl, Color> BASE_COLOR_META_DATA = FACTORY.createColorCssMetaData("-base-color", s -> s.baseColor);

    private final StyleableObjectProperty<Color> baseColor = new SimpleStyleableObjectProperty<Color>(BASE_COLOR_META_DATA) {
        @Override
        protected void invalidated() {
            setStyle(String.format("%s: %s;", getCssMetaData().getProperty(), colorToCss(get())));
            applyCss();
        }
    };

    // fuer Resizing benoetigt
    private Pane drawingPane;

    public SnowmanControl() {
        initializeSelf();
        initializeParts();
        initializeDrawingPane();
        initializeAnimations();
        layoutParts();
        setupEventHandlers();
        setupValueChangeListeners();
        setupBindings();
    }

    private void initializeSelf() {
        loadFonts("/fonts/Lato/Lato-Lig.ttf", "/fonts/Lato/Lato-Reg.ttf");
        addStylesheetFiles("style.css");

        getStyleClass().add("snowman");
    }

    private void initializeParts() {
        double buttonRadius = 3.2 / 2;
        snowHead = new Ellipse(33.08, 14.48, 33.17 / 2, 32.24 / 2);
        snowHead.getStyleClass().add("snow");
        snowHead.getTransforms().add(Transform.affine(1.0, -0.05, 0.05, 1, 0, 0));

        snowBelly = new Ellipse(28.81, 36.61, 43.22 / 2, 44.12 / 2);
        snowBelly.getStyleClass().add("snow");
        snowLegs = new Ellipse(18.94, 66.31, 62.96 / 2, 61.72 / 2);
        snowLegs.getStyleClass().add("snow");

        eyeLeft = new Ellipse(33.22, 16.15, 2.2, 1.5);
        eyeLeft.getStyleClass().add("button");
        eyeLeft.getTransforms().add(Transform.affine(0.98, -0.18, 0.18, 0.98, 0, 0));
        eyeRight = new Ellipse(38.84, 15.04, 2.2, 1.5);
        eyeRight.getStyleClass().add("button");
        eyeRight.getTransforms().add(Transform.affine(0.98, -0.18, 0.18, 0.98, 0, 0));

        buttonDown = new Circle(34.69, 47.00, buttonRadius);
        buttonDown.getStyleClass().add("button");
        buttonMiddle = new Circle(35.44, 43.15, buttonRadius);
        buttonMiddle.getStyleClass().add("button");
        buttonUp = new Circle(34.78, 39.5, buttonRadius);
        buttonUp.getStyleClass().add("button");

        nose = new Ellipse(47.8, 12.55, 3.0, 10.5);
        nose.getStyleClass().add("nose");
        nose.setRotate(85);

        var startX = 30.099;
        var startY = 18.45;

        mouth1 = new Line(startX, startY, startX + 5, startY);
        mouth1.getStyleClass().add("button");

        mouth2 = new Line(startX + 5, startY, startX + 10, startY);
        mouth2.getStyleClass().add("button");

        armLeft = new Line(1, 41, 15, 41);
        armLeft.getStyleClass().add("button");

        armRight = new Line(50, 41, 65, 41);
        armRight.getStyleClass().add("button");
    }

    private void initializeDrawingPane() {
        drawingPane = new Pane();
        drawingPane.getStyleClass().add("drawing-pane");
        drawingPane.setMaxSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setMinSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setPrefSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
    }

    private void initializeAnimations() {
        //initialising animations in future
    }

    private void layoutParts() {
        drawingPane.getChildren().addAll(armRight,
                snowLegs, snowBelly, snowHead,
                nose, eyeLeft, eyeRight,
                buttonDown, buttonUp, buttonMiddle, mouth1, mouth2, armLeft);

        getChildren().add(drawingPane);
    }

    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            setValue(!getValue());
        });
    }

    private void setupValueChangeListeners() {
        valueProperty().addListener((observable, oldValue, newValue) -> {
            switchHappiness(newValue);
        });
    }

    private void setupBindings() {
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        resize();
    }


    private void resize() {
        Insets padding = getPadding();
        double availableWidth = getWidth() - padding.getLeft() - padding.getRight();
        double availableHeight = getHeight() - padding.getTop() - padding.getBottom();

        double width = Math.max(Math.min(Math.min(availableWidth, availableHeight * ASPECT_RATIO), MAXIMUM_WIDTH), MINIMUM_WIDTH);

        double scalingFactor = width / ARTBOARD_WIDTH;

        if (availableWidth > 0 && availableHeight > 0) {
            relocateDrawingPaneCentered();
            drawingPane.setScaleX(scalingFactor);
            drawingPane.setScaleY(scalingFactor);
        }
    }

    private void relocateDrawingPaneCentered() {
        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, (getHeight() - ARTBOARD_HEIGHT) * 0.5);
    }

    private void relocateDrawingPaneCenterBottom(double scaleY, double paddingBottom) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double visualSpace = getHeight() - visualHeight;
        double y = visualSpace + (visualHeight - ARTBOARD_HEIGHT) * 0.5 - paddingBottom;

        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
    }

    private void relocateDrawingPaneCenterTop(double scaleY, double paddingTop) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double y = (visualHeight - ARTBOARD_HEIGHT) * 0.5 + paddingTop;

        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
    }

    // Sammlung nuetzlicher Funktionen
    private void loadFonts(String... font) {
        for (String f : font) {
            Font.loadFont(getClass().getResourceAsStream(f), 0);
        }
    }

    private void addStylesheetFiles(String... stylesheetFile) {
        for (String file : stylesheetFile) {
            String stylesheet = getClass().getResource(file).toExternalForm();
            getStylesheets().add(stylesheet);
        }
    }

    private void switchHappiness(boolean isHappy) {
        if (isHappy) {
            mouth1.setRotate(15);
            mouth2.setRotate(-15);
            armLeft.setStartY(armLeft.getStartY() - 15);
            armRight.setEndY(armRight.getEndY() - 15);
        } else {
            mouth1.setRotate(0);
            mouth2.setRotate(0);
            armLeft.setStartY(armLeft.getStartY() + 15);
            armRight.setEndY(armRight.getEndY() + 15);
        }
    }

    private String colorToCss(final Color color) {
        return color.toString().replace("0x", "#");
    }

    // compute sizes

    @Override
    protected double computeMinWidth(double height) {
        Insets padding = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return MINIMUM_WIDTH + horizontalPadding;
    }

    @Override
    protected double computeMinHeight(double width) {
        Insets padding = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return MINIMUM_HEIGHT + verticalPadding;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets padding = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return ARTBOARD_WIDTH + horizontalPadding;
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets padding = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return ARTBOARD_HEIGHT + verticalPadding;
    }

    //region  all getters and setters
    public boolean getValue() {
        return value.get();
    }

    public BooleanProperty valueProperty() {
        return value;
    }

    public void setValue(boolean value) {
        this.value.set(value);
    }

    public Color getBaseColor() {
        return baseColor.get();
    }

    public StyleableObjectProperty<Color> baseColorProperty() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor.set(baseColor);
    }

    //endregion
}