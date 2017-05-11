package org.marketsuite.framework.model.chart;

//chart canvas related information
public class PlotAttrib {


    //----- variables -----
    private float plotWidth;//full x range available to draw, exclude margins
    private float gridWidth;//width of each quote date
    private int priceIntensity = INTENSITY_PRICE_NORMAL, overlayIntensity = INTENSITY_OVERLAY_ON,
            volumeIntensity = INTENSITY_VOLUME_NORMAL, indicatorIntensity = INTENSITY_PRICE_NORMAL;
    private int topMargin = DEFAULT_MARGIN, bottomMargin = DEFAULT_MARGIN, leftMargin = DEFAULT_MARGIN, riteMargin = 0;

    //----- literals -----
    private static final int DEFAULT_MARGIN = 5;//in pixels
    private static final float DEFAULT_PRICE_HEIGHT_PERCENT = 0.80f;
    private static final float DEFAULT_VOL_HEIGHT_PERCENT = 0.3f;
    private static final int INTENSITY_PRICE_NORMAL = 200;
    private static final int INTENSITY_DIM = 100;//80
    private static final int INTENSITY_OVERLAY_ON = 250;
    private static final int INTENSITY_VOLUME_NORMAL = 120;
}
