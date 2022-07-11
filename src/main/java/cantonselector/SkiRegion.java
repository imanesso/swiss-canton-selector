package cantonselector;

import javafx.scene.shape.SVGPath;

import java.util.List;

public class SkiRegion {
    private final RegionName regionName;
    private final List<SVGPath> cantonSvgs;

    public SkiRegion(RegionName regionName, List<SVGPath> cantonSvgs) {
        this.regionName = regionName;
        this.cantonSvgs = cantonSvgs;
    }

    public RegionName getRegionName() {
        return regionName;
    }

    public List<SVGPath> getCantonSvgs() {
        return cantonSvgs;
    }

    public enum RegionName {
        WW("WW", "Waadt und Wallis"),
        BE("BE", "Berner Oberland"),
        ZS("ZS", "Zentralschweiz"),
        GR("GR", "Graubünden"),
        OS("OS", "Ostschweiz"),
        TI("TI", "Tessin");

        private final String abbreviation;
        private final String longName;
        private final String name;

        RegionName(String abbreviation, String longName) {
            this.abbreviation = abbreviation;
            this.longName = longName;
            //TODO 1) change to abbreviation if that's needed
            this.name = longName;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public String getLongName() {
            return longName;
        }

        public String getName() {
            return name;
        }
    }

    public enum Canton {
        ZH("ZH", "Zürich"),
        BE("BE", "Bern"),
        LU("LU", "Luzern"),
        UR("UR", "Uri"),
        SZ("SZ", "Schwyz"),
        OW("OW", "Obwalden"),
        NW("NW", "Nidwalden"),
        GL("GL", "Glarus"),
        ZG("ZG", "Zug"),
        FR("FR", "Freiburg"),
        SO("SO", "Solothurn"),
        BS("BS", "Basel-Stadt"),
        BL("BL", "Basel-Land"),
        SH("SH", "Schaffhausen"),
        AR("AR", "Appenzell-Ausserrhoden"),
        AI("AI", "Appenzell Innerrhoden"),
        SG("SG", "St. Gallen"),
        GR("GR", "Graubünden"),
        AG("AG", "Aargau"),
        TG("TG", "Thurgau"),
        TI("TI", "Tessin"),
        VD("VD", "Waadt"),
        VS("VS", "Wallis"),
        NE("NE", "Neuenburg"),
        GE("GE", "Genf"),
        JU("JU", "Jura");

        private final String shortName;
        private final String name;

        Canton(String shortName, String name) {
            this.shortName = shortName;
            this.name = name;
        }

        public String getShortName() {
            return shortName;
        }

        public String getName() {
            return name;
        }
    }
}

