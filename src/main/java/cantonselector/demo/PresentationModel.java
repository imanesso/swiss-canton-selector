package cantonselector.demo;

import cantonselector.SkiRegion;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class PresentationModel {
    private final DoubleProperty        pmValue   = new SimpleDoubleProperty();
    private final BooleanProperty       isFun   = new SimpleBooleanProperty();
    private final ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> hoverColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> selectColor = new SimpleObjectProperty<>();
    private final StringProperty        region = new SimpleStringProperty();

    public ObservableList<String> getCantonStringList() {
        ArrayList<String> list = new ArrayList<>();
        var vals = SkiRegion.RegionName.values();
        for (var x: vals){
            list.add(x.getName());
        }
        return FXCollections.observableArrayList(list);
    }
    //region getter and setter
    public double getPmValue() {
        return pmValue.get();
    }

    public DoubleProperty pmValueProperty() {
        return pmValue;
    }

    public void setPmValue(double pmValue) {
        this.pmValue.set(pmValue);
    }

    public Color getBaseColor() {
        return baseColor.get();
    }

    public ObjectProperty<Color> baseColorProperty() {
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

    public String getRegion() {
        return region.get();
    }

    public StringProperty regionProperty() {
        return region;
    }

    public void setRegion(String region) {
        this.region.set(region);
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
