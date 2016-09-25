package org.openpnp.machine.reference.vision.wizards;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.vision.ReferenceBottomVision;
import org.openpnp.machine.reference.vision.ReferenceFiducialLocator;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ReferenceFiducialLocatorWizard extends AbstractConfigurationWizard {
    private final ReferenceFiducialLocator fiducialLocator;

    private JTextField locationX;
    private JTextField locationY;
    private JPanel fiducialLocatorPanel;
    private JPanel panelOffsets;
    
    
	
	
	

    public ReferenceFiducialLocatorWizard(ReferenceFiducialLocator fiducialLocator) {
        this.fiducialLocator = fiducialLocator;
        fiducialLocatorPanel = new JPanel();
        fiducialLocatorPanel.setLayout(new BoxLayout(fiducialLocatorPanel, BoxLayout.Y_AXIS));
        contentPanel.add(fiducialLocatorPanel);

        panelOffsets = new JPanel();
        fiducialLocatorPanel.add(panelOffsets);
        panelOffsets.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
                "Home Location fiducial", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panelOffsets.setLayout(new FormLayout(
                new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,}));

        locationX = new JTextField();
        panelOffsets.add(locationX, "2, 2");
        locationX.setColumns(5);

        locationY = new JTextField();
        panelOffsets.add(locationY, "4, 2");
        locationY.setColumns(5);

    }
    

	@Override
	public void createBindings() {
//        LengthConverter lengthConverter = new LengthConverter();
//        IntegerConverter intConverter = new IntegerConverter();
//
//        MutableLocationProxy headOffsets = new MutableLocationProxy();
//        addWrappedBinding(headOffsets, "locationHomeX", locationX, "text", lengthConverter);
//        addWrappedBinding(headOffsets, "locationHomeY", locationY, "text", lengthConverter);
//        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(locationX);
//        ComponentDecorators.decorateWithAutoSelectAndLengthConversion(locationY);

	}

}
