package org.openpnp.machine.reference.camera.wizards;

import javax.swing.JColorChooser;
import javax.swing.JLabel;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.machine.reference.camera.OpenCvCamera;
import org.openpnp.vision.pipeline.stages.convert.ColorConverter;

public class OpenCvCameraLedConfigurationWizard extends OpenCvCameraConfigurationWizard {
	JColorChooser colorLed;
	JLabel 			lblColor;

	public OpenCvCameraLedConfigurationWizard(OpenCvCamera camera) {
		super(camera);
        lblColor = new JLabel("(Lighting color)");
        panelGeneral.add(lblColor, "2, 10");
		
        colorLed = new JColorChooser();
        panelGeneral.add(colorLed, "4, 10");
		
	}
	
    @Override
    public void createBindings() {
        super.createBindings();
        ColorConverter cv = new ColorConverter();
        bind(UpdateStrategy.READ_WRITE, camera, "color", cv, "ColorConverter");
        
//        ColorConverter colorConvert = new ColorConverter();
//        addWrappedBinding(camera, "color", colorLed, "text", colorConvert);
}

}