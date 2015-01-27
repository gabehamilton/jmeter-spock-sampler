package com.github.gabehamilton.jmeter;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.FileEditor;
import java.beans.PropertyDescriptor;

/**
 * This class defines the bean properties that will be visually exposed for this JMeter plugin.
 * <p/>
 * Created by gabe on 1/22/15.
 */
public class SpockSamplerBeanInfo extends BeanInfoSupport {
    public SpockSamplerBeanInfo() {
        super(SpockSampler.class);

        createPropertyGroup("which_spec", new String[]{"classname"});

        PropertyDescriptor classname = property("classname");
        classname.setValue(NOT_UNDEFINED, Boolean.FALSE);
        classname.setValue(DEFAULT, "com.github.gabehamilton.jmeter.SuccessfulSpecToBeRunBySamplerSpec");
        classname.setValue(NOT_EXPRESSION, Boolean.TRUE);
//        classname.setPropertyEditorClass(FileEditor.class);
    }
}