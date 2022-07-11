package cantonselector;

import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

/**
 * Dashboard consists of a region selector control, that can select a region on a map
 * and a snowman, which is like a gimmick to show wether there is a funpark.
 *
 * @author Anessollah Ima
 */
public class RegionSnowmanDashboard extends Region {
    // wird gebraucht fuer StyleableProperties
    private static final StyleablePropertyFactory<RegionSnowmanDashboard> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    private static final double ARTBOARD_WIDTH = 1200;
    private static final double ARTBOARD_HEIGHT = 675;
    private static final double ASPECT_RATIO = ARTBOARD_WIDTH / ARTBOARD_HEIGHT;
    private static final double MINIMUM_WIDTH = 25;
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;
    private static final double MAXIMUM_WIDTH = 1200;
    private static final double MAXIMUM_HEIGHT = 675;

    //the custom controls for the dashboard
    private RegionSelectorControl regionSelectorControl;
    private SnowmanControl snowmanControl;

    //all the properties for the underlying custom controls
    private final DoubleProperty value = new SimpleDoubleProperty();
    private final BooleanProperty isFun = new SimpleBooleanProperty();
    private final ObjectProperty<Color> hoverColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> selectColor = new SimpleObjectProperty<>();
    private final StringProperty selectedRegion = new SimpleStringProperty();

    //the three stylable properties for the base color, the selected color and the hover color
    private static final CssMetaData<RegionSnowmanDashboard, Color> BASE_COLOR_META_DATA = FACTORY.createColorCssMetaData("-base-color", s -> s.baseColor);

    private final StyleableObjectProperty<Color> baseColor = new SimpleStyleableObjectProperty<Color>(BASE_COLOR_META_DATA) {
        @Override
        protected void invalidated() {
            setStyle(String.format("%s: %s;", getCssMetaData().getProperty(), colorToCss(get())));
            applyCss();
        }
    };

    private GridPane pane = new GridPane();

    public RegionSnowmanDashboard() {
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

        getStyleClass().add("dashboard");
    }

    private void initializeParts() {
        double center = ARTBOARD_WIDTH * 0.5;

        regionSelectorControl = new RegionSelectorControl();
        snowmanControl = new SnowmanControl();
    }

    private void initializeDrawingPane() {
        pane = new GridPane();
        pane.setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        pane.setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        pane.setPrefSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
    }

    private void initializeAnimations() {
    }

    private void layoutParts() {
        pane.add(regionSelectorControl, 0, 0);
        pane.add(snowmanControl, 1, 0);

        getChildren().add(pane);
    }

    private void setupEventHandlers() {
    }

    private void setupValueChangeListeners() {
    }

    private void setupBindings() {
        snowmanControl.valueProperty().bindBidirectional(isFunProperty());

        regionSelectorControl.baseColorProperty().bindBidirectional(baseColorProperty());
        regionSelectorControl.hoverColorProperty().bindBidirectional(hoverColorProperty());
        regionSelectorControl.selectColorProperty().bindBidirectional(selectColorProperty());
        regionSelectorControl.selectedRegionProperty().bindBidirectional(selectedRegionProperty());

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
            pane.setScaleX(scalingFactor);
            pane.setScaleY(scalingFactor);
        }
    }

    private void relocateDrawingPaneCentered() {
        pane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, (getHeight() - ARTBOARD_HEIGHT) * 0.5);
    }

    private void relocateDrawingPaneCenterBottom(double scaleY, double paddingBottom) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double visualSpace = getHeight() - visualHeight;
        double y = visualSpace + (visualHeight - ARTBOARD_HEIGHT) * 0.5 - paddingBottom;

        pane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
    }

    private void relocateDrawingPaneCenterTop(double scaleY, double paddingTop) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double y = (visualHeight - ARTBOARD_HEIGHT) * 0.5 + paddingTop;

        pane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
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

    //region all getters and setters
    public double getValue() {
        return value.get();
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public void setValue(double value) {
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

    public Color getHoverColor() {
        return hoverColor.get();
    }

    public ObjectProperty<Color> hoverColorProperty() {
        return hoverColor;
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor.set(hoverColor);
    }

    public Color getSelectColor() {
        return selectColor.get();
    }

    public ObjectProperty<Color> selectColorProperty() {
        return selectColor;
    }

    public void setSelectColor(Color selectColor) {
        this.selectColor.set(selectColor);
    }


    public String getSelectedRegion() {
        return selectedRegion.get();
    }

    public StringProperty selectedRegionProperty() {
        return selectedRegion;
    }

    public void setSelectedRegion(String selectedRegion) {
        this.selectedRegion.set(selectedRegion);
    }

    public boolean isIsFun() {
        return isFun.get();
    }

    public BooleanProperty isFunProperty() {
        return isFun;
    }

    public void setIsFun(boolean isFun) {
        this.isFun.set(isFun);
    }
    //endregion
}