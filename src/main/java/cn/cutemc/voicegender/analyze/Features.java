package cn.cutemc.voicegender.analyze;

import lombok.Getter;

@Getter
public enum Features {
    MEAN_FREQ("meanfreq"),
    SD("sd"),
    MEDIAN("median"),
    Q25("Q25"),
    Q75("Q75"),
    IQR("IQR"),
    SKEW("skew"),
    KURT("kurt"),
    SP_ENT("sp_ent"),
    SFM("sfm"),
    MODE("mode"),
    CENTROID("centroid"),
    MEAN_FUN("meanfun"),
    MIN_FUN("minfun"),
    MAX_FUN("maxfun"),
    MEAN_DOM("meandom"),
    MIN_DOM("mindom"),
    MAX_DOM("maxdom"),
    DF_RANGE("dfrange"),
    MODINDX("modindx");

    private final String name;

    Features(String name) {
        this.name = name;
    }

}
