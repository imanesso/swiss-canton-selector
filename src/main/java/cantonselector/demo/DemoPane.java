package cantonselector.demo;

import cantonselector.RegionSnowmanDashboard;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DemoPane extends BorderPane {
    private final PresentationModel pm;

    // declare the custom control
    private RegionSnowmanDashboard regionSnowmanDashboard;

    // all controls
    private Slider slider;
    private CheckBox isFun;
    private ColorPicker colorPicker;
    private ColorPicker colorPicker2;
    private ColorPicker colorPicker3;
    private ComboBox<String> cbRegion;

    public DemoPane(PresentationModel pm) {
        this.pm = pm;
        initializeControls();
        layoutControls();
        setupBindings();
    }

    private void initializeControls() {
        setPadding(new Insets(10));
        regionSnowmanDashboard = new RegionSnowmanDashboard();

        slider = new Slider();
        slider.setShowTickLabels(true);

        isFun = new CheckBox();

        colorPicker = new ColorPicker();
        colorPicker2 = new ColorPicker();
        colorPicker3 = new ColorPicker();

        cbRegion = new ComboBox<>(pm.getCantonStringList());
    }

    private void layoutControls() {

        VBox controlPane = new VBox(new Label("Base Color"),
                colorPicker,
                new Label("Hover Color"), colorPicker2,
                new Label("Select Color"), colorPicker3,
                new Label("Gebiet"), cbRegion,
                new Label("Funpark ist offen"), isFun);
        controlPane.setPadding(new Insets(0, 50, 0, 50));
        controlPane.setSpacing(10);
        var pane = new Pane();
        pane.setPrefWidth(100);
        pane.setStyle("-fx-border-color: black");
        setLeft(pane);
        setCenter(regionSnowmanDashboard);
        setRight(controlPane);
    }

    private void setupBindings() {
        slider.valueProperty().bindBidirectional(pm.pmValueProperty());
        colorPicker.valueProperty().bindBidirectional(pm.baseColorProperty());
        colorPicker2.valueProperty().bindBidirectional(pm.hoverColorProperty());
        colorPicker3.valueProperty().bindBidirectional(pm.selectColorProperty());
        isFun.selectedProperty().bindBidirectional(pm.isFunProperty());
        cbRegion.valueProperty().bindBidirectional(pm.regionProperty());

        regionSnowmanDashboard.isFunProperty().bindBidirectional(pm.isFunProperty());
        regionSnowmanDashboard.baseColorProperty().bindBidirectional(pm.baseColorProperty());
        regionSnowmanDashboard.hoverColorProperty().bindBidirectional(pm.hoverColorProperty());
        regionSnowmanDashboard.selectColorProperty().bindBidirectional(pm.selectColorProperty());
        regionSnowmanDashboard.selectedRegionProperty().bindBidirectional(pm.regionProperty());
    }
}
