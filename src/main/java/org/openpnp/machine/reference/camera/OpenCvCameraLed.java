package org.openpnp.machine.reference.camera;

import java.awt.Color;
import java.io.IOException;

import org.openpnp.CameraListener;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.camera.wizards.OpenCvCameraLedConfigurationWizard;
import org.openpnp.vision.pipeline.stages.convert.ColorConverter;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

public class OpenCvCameraLed extends OpenCvCamera {
    @Element(required = false)
    @Convert(ColorConverter.class)
    private Color color = null;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

	
	public OpenCvCameraLed() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void setLightingColor(Color color) {
		try {
			machine.getDriver().setLightingColor(color);
			setColor(color);
		} catch (Exception e) {

			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public Color getLightingColor() {
		// TODO Auto-generated method stub
		return getColor();
	}

	@Override
    public Wizard getConfigurationWizard() {
        return new OpenCvCameraLedConfigurationWizard(this);
    }
	

    @Override
    public synchronized void startContinuousCapture(CameraListener listener, int maximumFps) {
        super.startContinuousCapture(listener, maximumFps);
        setLightingColor(color);
    }
    @Override
    public void close() throws IOException {
        super.close();
        setLightingColor(Color.black);
    }
}
