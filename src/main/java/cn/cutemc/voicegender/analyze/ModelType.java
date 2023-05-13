package cn.cutemc.voicegender.analyze;

import cn.cutemc.voicegender.analyze.engine.EngineType;
import lombok.Getter;

@Getter
public enum ModelType {

    CART("Classification and Regression Tree", "CART", EngineType.TENSORFLOW),
    GBT("Generalized Boosted Tree Regression", "GBT", EngineType.TENSORFLOW),
    RF("Random Forest", "RF", EngineType.TENSORFLOW),
    XGBOOST("eXtreme Gradient Boosting", "XGBOOST", EngineType.XGBOOST);

    private final String fullName;
    private final String id;
    private final EngineType engineType;

    ModelType(String fullName, String id, EngineType engineType) {
        this.fullName = fullName;
        this.id = id;
        this.engineType = engineType;
    }

    public static ModelType getModel(String model) {
        for (ModelType value : values()) {
            if (value.name().equalsIgnoreCase(model)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return id;
    }
}
