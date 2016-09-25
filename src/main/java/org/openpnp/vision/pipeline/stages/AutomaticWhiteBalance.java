package org.openpnp.vision.pipeline.stages;

import java.awt.Color;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openpnp.vision.FluentCv;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.simpleframework.xml.Attribute;

/**
 * Remove color from an image based on the HSV color space. Pixels that fall between (hueMin,
 * saturationMin, valueMin) and (hueMax, saturationMax, valueMax) are set to black in the output
 * image.
 * 
 * This stage expects the input to be in HSV_FULL format, so you should do a ConvertColor
 * with Bgr2HsvFull before this stage and ConvertColor Hsv2BgrFull after. These are not applied
 * internally as to not complicate the use of multiple instances of this stage in series.
 * 
 * Note that this stage can be used with any 3 channel, 8 bit per channel color space. The order
 * of the filtered channels is hue, saturation, value, but you can use these ranges for other
 * channels.
 */
public class AutomaticWhiteBalance extends CvStage {
    @Attribute
    private int inputMin = 0;

    @Attribute
    private int inputMax = 255;

    @Attribute
    private int outputMin = 0;

    @Attribute
    private int outputMax = 255;


    public int getInputMin() {
		return inputMin;
	}


	public void setInputMin(int inputMin) {
		this.inputMin = inputMin;
	}


	public int getInputMax() {
		return inputMax;
	}


	public void setInputMax(int inputMax) {
		this.inputMax = inputMax;
	}


	public int getOutputMin() {
		return outputMin;
	}


	public void setOutputMin(int outputMin) {
		this.outputMin = outputMin;
	}


	public int getOutputMax() {
		return outputMax;
	}


	public void setOutputMax(int outputMax) {
		this.outputMax = outputMax;
	}


	@Override
    public Result process(CvPipeline pipeline) throws Exception {
        Mat mat = pipeline.getWorkingImage();
        return null;
    }
}
