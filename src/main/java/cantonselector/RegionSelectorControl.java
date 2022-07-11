package cantonselector;

import javafx.beans.property.*;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.*;

import static cantonselector.SkiRegion.Canton;
import static cantonselector.SkiRegion.RegionName;

/**
 * RegionSelector can be used to select a Region on the swiss map.
 * A Region consists of a number of cantons.
 *
 * @author Anessollah Ima
 */
public class RegionSelectorControl extends Region {
    private static final StyleablePropertyFactory<RegionSelectorControl> FACTORY = new StyleablePropertyFactory<>(Region.getClassCssMetaData());

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    private static final double ARTBOARD_WIDTH = 1100;
    private static final double ARTBOARD_HEIGHT = 675;
    private static final double ASPECT_RATIO = ARTBOARD_WIDTH / ARTBOARD_HEIGHT;
    private static final double MINIMUM_WIDTH = 100;
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;
    private static final double MAXIMUM_WIDTH = 2000;
    private final String filepath = getClass().getResource("/images/Suisse_cantons.svg").toExternalForm();
    //TODO 1) change this to true if you want to use set of cantons called regions
    private final boolean useRegions = false;

    // Parts of which the region selector consists of
    private Text display;
    private Map<String, SVGPath> cantonSvgs;
    private Map<String, SkiRegion> regions;

    //all needed properties for the region selector
    private final StringProperty selectedRegion = new SimpleStringProperty();
    private final StringProperty hoverRegion = new SimpleStringProperty();
    private final BooleanProperty displayVisibility = new SimpleBooleanProperty();
    private final DoubleProperty xMouse = new SimpleDoubleProperty();
    private final DoubleProperty yMouse = new SimpleDoubleProperty();

    //the three stylable properties for the base color, the selected color and the hover color
    private static final CssMetaData<RegionSelectorControl, Color> BASE_COLOR_META_DATA = FACTORY.createColorCssMetaData("-base-color", s -> s.baseColor);
    private static final CssMetaData<RegionSelectorControl, Color> HOVER_COLOR_META_DATA = FACTORY.createColorCssMetaData("-hover-color", s -> s.hoverColor);
    private static final CssMetaData<RegionSelectorControl, Color> SELECT_COLOR_META_DATA = FACTORY.createColorCssMetaData("-select-color", s -> s.selectColor);

    private final StyleableObjectProperty<Color> baseColor = new SimpleStyleableObjectProperty<Color>(BASE_COLOR_META_DATA, this, "baseColor") {
        @Override
        protected void invalidated() {
            setStyle(BASE_COLOR_META_DATA.getProperty() + ": " + colorToCss(getBaseColor()));
            applyCss();
        }
    };
    private final StyleableObjectProperty<Color> hoverColor = new SimpleStyleableObjectProperty<Color>(HOVER_COLOR_META_DATA, this, "hoverColor");
    private final StyleableObjectProperty<Color> selectColor = new SimpleStyleableObjectProperty<Color>(SELECT_COLOR_META_DATA, this, "selectColor");

    // fuer Resizing benoetigt
    private Pane drawingPane;

    public RegionSelectorControl() {
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

        getStyleClass().add("regionselector");
    }


    private void initializeParts() {
        cantonSvgs = new HashMap<>();
        loadCantonSvgs();
        regions = new HashMap<>();
        addAllRegions();

        display = new Text();
        display.setMouseTransparent(true);
        display.getStyleClass().add("display");
    }

    private void initializeDrawingPane() {
        drawingPane = new Pane();
        drawingPane.getStyleClass().add("drawing-pane");
        drawingPane.setMaxSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setMinSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setPrefSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
    }

    private void initializeAnimations() {
    }

    private void layoutParts() {
        drawingPane.getChildren().addAll(cantonSvgs.values());
        drawingPane.getChildren().addAll(display);

        getChildren().add(drawingPane);
    }

    //Event Handlers change properties
    private void setupEventHandlers() {
        for (var region : regions.values()) {
            for (var svg : region.getCantonSvgs()) {
                svg.setOnMouseEntered(event -> {
                    setHoverRegion(region.getRegionName().getLongName());
                });
                svg.setOnMouseClicked(event -> {
                    setSelectedRegion(region.getRegionName().getName());
                });
                svg.setOnMouseMoved(event -> {
                    setxMouse(event.getX());
                    setyMouse(event.getY());
                });
            }
        }
        this.setOnMouseEntered(event -> {
            setDisplayVisibility(true);
        });
        this.setOnMouseExited(event -> {
            setDisplayVisibility(false);
        });
    }

    //GUI updates based on changed property values
    private void setupValueChangeListeners() {
        //colors selected region
        selectedRegionProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && !oldValue.equals("")) {
                giveRegionColor(regions.get(oldValue), baseColor.get());
            }
            giveRegionColor(regions.get(newValue), selectColor.get());
        });

        //colors hover region
        hoverRegionProperty().addListener((observable, oldValue, newValue) -> {
            var oldRegion = regions.get(oldValue);
            var newRegion = regions.get(newValue);
            if (oldValue != null && !oldValue.equals("")) {
                //don't change if selected
                if (!oldRegion.getRegionName().getName().equals(getSelectedRegion()))
                    giveRegionColor(oldRegion, baseColor.get());
            }
            if (!newRegion.getRegionName().getName().equals(getSelectedRegion()))
                giveRegionColor(newRegion, hoverColor.get());
        });

        displayVisibilityProperty().addListener((observable, oldValue, newValue) -> {
            display.setVisible(newValue);
            //resets hover region to base color when mouse is gone
            if (getHoverRegion() != null && !getHoverRegion().equals("")
                    && !getHoverRegion().equals(getSelectedRegion()))
                giveRegionColor(regions.get(getHoverRegion()), baseColor.get());
        });

        xMouseProperty().addListener((observable, oldValue, newValue) -> {
            display.setX(newValue.doubleValue());
        });
        yMouseProperty().addListener((observable, oldValue, newValue) -> {
            display.setY(newValue.doubleValue());
        });
    }

    private void setupBindings() {
        display.textProperty().bind(hoverRegionProperty());
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        resize();
    }

    private void loadCantonSvgs() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(filepath);

            String xpathExpression = "//path";
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            XPathExpression expression = xpath.compile(xpathExpression);
            NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            svgPaths.getLength();

            for (int i = 0; i < 26; i++) {
                var id = svgPaths.item(i);
                var map = id.getAttributes();
                var d = map.item(0);
                var dname = map.item(2).getNodeValue();
                var dValue = d.getNodeValue();
                SVGPath svg = new SVGPath();
                svg.setContent(dValue);
                svg.getStyleClass().add("defaultregion");
                cantonSvgs.put(dname, svg);
            }
        } catch (SAXException | IOException | XPathExpressionException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void addAllRegions() {
        if (useRegions) {
            addCantonsToRegion(new SkiRegion.RegionName("WW", "Waadt und Wallis"), getListOfCantonSVGs(Canton.VD, Canton.VS));
            addCantonsToRegion(new SkiRegion.RegionName("BE", "Berner Oberland"), getListOfCantonSVGs(Canton.BE));
            addCantonsToRegion(new SkiRegion.RegionName("ZS", "Zentralschweiz"), getListOfCantonSVGs(Canton.LU, Canton.UR, Canton.SZ, Canton.OW, Canton.NW, Canton.ZG));
            addCantonsToRegion(new SkiRegion.RegionName("GR", "Graubünden"), getListOfCantonSVGs(Canton.GR));
            addCantonsToRegion(new SkiRegion.RegionName("OS", "Ostschweiz"), getListOfCantonSVGs(Canton.SG, Canton.GL));
            addCantonsToRegion(new SkiRegion.RegionName("TI", "Tessin"), getListOfCantonSVGs(Canton.TI));
        } else {
            useCantonsAsRegions();
        }
    }

    private List<SVGPath> getListOfCantonSVGs(Canton... cantons) {
        var result = new ArrayList<SVGPath>();
        for (var canton : cantons) {
            result.add(cantonSvgs.get(canton.getShortName()));
        }
        return result;
    }

    private void addCantonsToRegion(RegionName regionName, List<SVGPath> cantons) {
        var region = new SkiRegion(regionName, cantons);
        regions.put(region.getRegionName().getName(), region);
    }

    private void useCantonsAsRegions() {
        for (var item :
                Canton.values()) {
            addCantonsToRegion(new RegionName(item.getShortName(), item.getName()), getListOfCantonSVGs(Arrays.stream(Canton.values())
                    .filter(t -> t.getName().equals(item.getName()))
                    .findFirst().get()));
        }
    }

    private void giveRegionColor(SkiRegion skiRegion, Color color) {
        for (var svg : skiRegion.getCantonSvgs()) {
            svg.setFill(color);
        }
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

    private String colorToCss(final Color color) {
        return color.toString().replace("0x", "#");
    }

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
    public Color getHoverColor() {
        return hoverColor.get();
    }

    public StyleableObjectProperty<Color> hoverColorProperty() {
        return hoverColor;
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor.set(hoverColor);
    }

    public Color getSelectColor() {
        return selectColor.get();
    }

    public StyleableObjectProperty<Color> selectColorProperty() {
        return selectColor;
    }

    public void setSelectColor(Color selectColor) {
        this.selectColor.set(selectColor);
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

    public String getSelectedRegion() {
        return selectedRegion.get();
    }

    public StringProperty selectedRegionProperty() {
        return selectedRegion;
    }

    public void setSelectedRegion(String selectedRegion) {
        this.selectedRegion.set(selectedRegion);
    }

    public String getHoverRegion() {
        return hoverRegion.get();
    }

    public StringProperty hoverRegionProperty() {
        return hoverRegion;
    }

    public void setHoverRegion(String hoverRegion) {
        this.hoverRegion.set(hoverRegion);
    }

    public boolean isDisplayVisibility() {
        return displayVisibility.get();
    }

    public BooleanProperty displayVisibilityProperty() {
        return displayVisibility;
    }

    public void setDisplayVisibility(boolean displayVisibility) {
        this.displayVisibility.set(displayVisibility);
    }

    public double getxMouse() {
        return xMouse.get();
    }

    public DoubleProperty xMouseProperty() {
        return xMouse;
    }

    public void setxMouse(double xMouse) {
        this.xMouse.set(xMouse);
    }

    public double getyMouse() {
        return yMouse.get();
    }

    public DoubleProperty yMouseProperty() {
        return yMouse;
    }

    public void setyMouse(double yMouse) {
        this.yMouse.set(yMouse);
    }
    //endregion
}
