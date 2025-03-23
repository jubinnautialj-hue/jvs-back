package cn.bctools.report.model.univer.plugin;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SheetDrawingPlugin {

    private String unitId;

    private String subUnitId;

    private String drawingId;

    private Integer drawingType;

    private String imageSourceType;

    private String source;

    private SheetTransform sheetTransform;

    private Transform transform;

    @Data
    @Accessors(chain = true)
    public static class SheetTransform{

        private Boolean flipY;

        private Boolean flipX;

        private Integer angle;

        private Integer skewX;

        private Integer skewY;

        private Location from;

        private Location to;

    }

    @Data
    @Accessors(chain = true)
    public static class Location{
        private Integer column;

        private Integer columnOffset;

        private Integer row;

        private Integer rowOffset;
    }

    @Data
    @Accessors(chain = true)
    public static class Transform{

        private Boolean flipY;

        private Boolean flipX;

        private Integer angle;

        private Integer skewX;

        private Integer skewY;

        private Integer left;

        private Integer top;

        private Integer width;

        private Integer height;

    }


}
