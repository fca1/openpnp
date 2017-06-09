/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.feeder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.apache.commons.io.IOUtils;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.feeder.wizards.ReferenceLoosePartFeederConfigurationWizard;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.model.Rectangle;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.OpenCvUtils;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.pipeline.CvPipeline;

import org.simpleframework.xml.Element;
import org.pmw.tinylog.Logger;

import nu.pattern.OpenCV;

/*
 * TODO: We need to be able to determine "normal" angle for the part, which probably means width and
 * height, which is going to be tough when the body is square. Might need to make it more related to
 * bottom vision so that it can determine the correct angle for what it locates.
 * TODO: Remember the idea of using contours or whatever to find the part and then using template
 * matching to get it's orientation. That would solve the above, and make it work for polarized
 * parts too.
 * 
 * And related to that, this might be a good time to play with something like SIFT.
 */
public class ReferenceLoosePartFeeder extends ReferenceFeeder {
	
// FCA the size of package is compared with a rectangle given by the camera. 
// If percent of area is inside an interval of PERCENT_AREA, the rectangle is a good candidate
// The ratio of rectangle (length, with) is compared with the same way.
// @TODO put this datas inside a wizard. 	
	private final static double PERCENT_AREA = 0.25; 
	private final static double PERCENT_RATIO = 0.30;

	

    @Element(required = false)
    private CvPipeline pipeline = createDefaultPipeline();

    private Location pickLocation;

    @Override
    public Location getPickLocation() throws Exception {
    	if (pickLocation==null)
    		{
    		return location;	// by Default
    		}
    	else
    		{
    		return pickLocation;
    		}
        
    }
    
    

    @Override
    public void feed(Nozzle nozzle,Part aPart) throws Exception {
        Camera camera = nozzle.getHead().getDefaultCamera();
        // Move to the feeder pick location
        MovableUtils.moveToLocationAtSafeZ(camera, location);
        for (int i = 0; i < 3; i++) {

            pickLocation = getPickLocation(camera, nozzle,aPart);
            camera.moveTo(pickLocation);
        }
    }
    
    /**
     * 
     * @param results List of RotatedRectangle
     * @param ratio Rectangle filter value, suppress of the returned list if not in the interval:
     * @param areaPixels Area filter 
     * @return The new filtered list. 
     */
    private List<RotatedRect> filterDimensionRotatedRect(List<RotatedRect> results, double ratio, double areaPixels) 
    {
    	List<RotatedRect> filteredResults = new ArrayList<RotatedRect>(results); 
    	filteredResults.removeIf((a) -> {
        		// No dimension, suppress
        		if (a.size.height==0 || a.size.width == 0) 
        			{
        			Logger.debug("Size of footprint detected = 0");
        			return true;
        			}
            double ratioC =Math.min(a.size.width/a.size.height,a.size.height/a.size.width);
        	double pcentRatio = Math.abs((ratioC-ratio)/ratio);
        	if (pcentRatio>PERCENT_RATIO)
        		{
        		Logger.debug("Ratio measured = "+Math.round(100.0*ratioC)/100.0+" waited="+Math.round(100.0*ratio)/100.0+" => candidate suppressed");
        		}

        	return pcentRatio>PERCENT_RATIO;
        });
    	Logger.debug("Nber candidates after ratio filtering : "+filteredResults.size());
    	Logger.debug("Remove all pattern with a maximum  area error of "+PERCENT_AREA*100.0+" %");
    	filteredResults.removeIf((a) -> {
        	double pcent = (a.size.area()-areaPixels)/areaPixels;
        	boolean result =Math.abs(pcent)>PERCENT_AREA;
        	if (result)
        		{
        		Logger.debug("Area filtered error of "+Math.round(pcent*100.0)+" %  (positive = the measured is more bigger than the reference given)");
        		}
        	return result;
        });
    return filteredResults;	
    }
    
     
    
    
    private Location getPickLocation(Camera camera, Nozzle nozzle,Part aPart) throws Exception {
    	org.openpnp.model.Package pack = null;
    	if (aPart == null)
    	{
    		pack = part.getPackage();
    	}
    	else
    	{
    		pack = aPart.getPackage();
    	}
// FCA @TODO if the part has a big value of height, the focal length is not ridiculous and the size of 
// rectangle increase. 
// For the test purpose, a factor of correction is given with an absolute value, but later, a method 
// to find the good constant value (simple thales) must be providen.
// @TODO change the method        
        final double correctionFocale =1.0-pack.getHeight().getValue()*0.01;  
        final double unitsPerSquarePixels = camera.getUnitsPerPixel().getY()*camera.getUnitsPerPixel().getX()*(correctionFocale*correctionFocale);
        final double areaPackagePixels = pack.getFootprint().getBodyArea()/unitsPerSquarePixels;
        final double ratio = pack.getFootprint().getMinusRatio();
// FCA When the rectangle has no area value, give immediatly an error and exit.         
        if (unitsPerSquarePixels==0)
        	{
            throw new Exception("Feeder " + getName() + "no dimension for package"+pack.getId());
        	}

        
        
        
        // Process the pipeline to extract RotatedRect results
        pipeline.setCamera(camera);
        pipeline.setNozzle(nozzle);
        pipeline.setFeeder(this);
        pipeline.process();
        Logger.debug("Search the nearest ratio :");
        // Grab the results
        List<RotatedRect> results = (List<RotatedRect>) pipeline.getResult("results").model;
        Logger.debug("Number of rectangles detected : "+results.size());
        Logger.debug("fillter of part : "+aPart.getName());
        List<RotatedRect> filteredResults =filterDimensionRotatedRect(results,ratio,areaPackagePixels);
        Logger.debug("Number of rectangles  filtered : "+filteredResults.size()+ "            ratio = "+ratio+" sizeArea="+areaPackagePixels);
        
        
        
        
        
        // Find the closest result  (area Near than areaPackagePixels)
        results.sort((a, b) -> {
            Double da = Math.abs(a.size.area()-areaPackagePixels);
            Double db = Math.abs(b.size.area()-areaPackagePixels); 
            return da.compareTo(db);
        });
        
        
        if (filteredResults.isEmpty()) {
        	double errorPcent = 100;
        	if (!results.isEmpty())
        		{
        		errorPcent = Math.round(Math.abs(100.0*(results.get(0).size.area()-areaPackagePixels)/areaPackagePixels));
                Logger.debug("The nearest candidate is near of " + errorPcent + " %");
        		}
        		throw new Exception("Feeder " + getName() + ": No parts found. near Area="+errorPcent+" %");
        		}
        
        
        filteredResults.sort((a, b) -> {
            Double da = Math.abs(a.size.area()-areaPackagePixels);
            Double db = Math.abs(b.size.area()-areaPackagePixels); 
            return da.compareTo(db);
        });
        RotatedRect result = null;
        for(RotatedRect rect : filteredResults )
        	{
            double l2 = VisionUtils.getPixelLocation(camera,rect.center.x  , rect.center.y).getLinearDistanceTo(camera.getLocation());
// FCA @TODO Remove this absolute value. The goal is to keep only the nearest point between camera and rectangle found. 
// Else, the camera can move to an other rectangle with a best area and ration each time of iteration

        	if (l2<0.5)
        		{
        		if (result==null) { result = rect; }
        		}
        	}
        if (result==null)
        	{
        	// No candidate found, take the first (the best) 
        	result = filteredResults.get(0);
        	}
        Location location = VisionUtils.getPixelLocation(camera, result.center.x, result.center.y);
        // Get the result's Location
        // Update the location with the result's rotation
        double angle = result.angle;
        // L'angle peut etre de -90 a 90, 

// FCA  @TODO remove absolute value, use a rectangle object with isSquare method		
        boolean isSquare = Math.abs(ratio-1.0)<0.1;	// Si le ratio est <10%, ne plus considerer le rectangle
		if (isSquare)
		{
			Logger.debug("Detection square Angle = "+angle);
			// A square cannot have an angle high than +/- 45�
			angle = -angle;  // l'angle est toujours negatif
			if (angle>45)
				{
				angle=(90d-angle);
				}
			}
		else
			{
			if ((result.size.width < result.size.height))  // FCA l'angle d'un rectangle 0� = cote long
				{
				angle-=90;
				Logger.debug("Detection rectangle Angle = "+angle);
				}
			angle=Math.abs(angle);
			if ((angle>90))
				{
				angle-=180;
				}
			}
        
        location = location.derive(null, null, null, angle);
        // Update the location with the correct Z, which is the configured Location's Z
        // plus the part height.
        location =
                location.derive(null, null,
                        this.location.convertToUnits(location.getUnits()).getZ()
                                + pack.getHeight().convertToUnits(location.getUnits()).getValue(),
                        null);
        MainFrame.get().getCameraViews().getCameraView(camera)
                .showFilteredImage(OpenCvUtils.toBufferedImage(pipeline.getWorkingImage()), 250);
        return location;
    }
    
    public CvPipeline getPipeline() {
        return pipeline;
    }
    
    public void resetPipeline() {
        pipeline = createDefaultPipeline();
    }

    @Override
    public Wizard getConfigurationWizard() {
        return new ReferenceLoosePartFeederConfigurationWizard(this);
    }

    @Override
    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getName();
    }

    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertySheet[] getPropertySheets() {
        return new PropertySheet[] {new PropertySheetWizardAdapter(getConfigurationWizard())};
    }

    @Override
    public Action[] getPropertySheetHolderActions() {
        // TODO Auto-generated method stub
        return null;
    }

    public static CvPipeline createDefaultPipeline() {
        try {
            String xml = IOUtils.toString(ReferenceLoosePartFeeder.class
                    .getResource("ReferenceLoosePartFeeder-DefaultPipeline.xml"));
            return new CvPipeline(xml);
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

	@Override
	public boolean canHandle(Part candidate) {
		// Always accept all parts because it's the last chance with this feeder. 
		return true;
	}
    
    
}
