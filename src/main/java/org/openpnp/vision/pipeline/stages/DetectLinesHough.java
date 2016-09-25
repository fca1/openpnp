package org.openpnp.vision.pipeline.stages;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.simpleframework.xml.Attribute;

/**
 * Finds circles in the working image and stores the results as a List<Circle> on the model. 
 */
public class DetectLinesHough extends CvStage {
	
	
	

    @Override
    public Result process(CvPipeline pipeline) throws Exception {
        Mat mat = pipeline.getWorkingImage();
        Mat output = new Mat();
//        Imgproc.HoughCircles(mat, output, Imgproc.CV_HOUGH_GRADIENT, dp, minDistance, param1,
//                param2, minDiameter / 2, maxDiameter / 2);
        List<Result.Circle> circles = new ArrayList<>();
        for (int i = 0; i < output.cols(); i++) {
            double[] circle = output.get(0, i);
            double x = circle[0];
            double y = circle[1];
            double radius = circle[2];
            circles.add(new Result.Circle(x, y, radius * 2.0));
        }
        output.release();

        return new Result(null, circles);
    }
}
