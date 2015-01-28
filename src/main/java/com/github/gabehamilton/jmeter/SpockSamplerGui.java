/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
*/

package com.github.gabehamilton.jmeter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.VerticalPanel;
import com.github.gabehamilton.jmeter.SpockSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spockframework.runtime.model.FeatureMetadata;
import org.spockframework.runtime.model.SpecMetadata;
import spock.lang.Specification;

/**
 * The <code>SpockSamplerGui</code> class provides the user interface
 * for the {@link SpockSampler}.
 *
 */
public class SpockSamplerGui extends AbstractSamplerGui
        implements ChangeListener, ActionListener, ItemListener
{
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    // Names of standard Spock methods
    private static final String ONETIMESETUP = "setup"; //$NON-NLS-1$
    private static final String ONETIMETEARDOWN = "cleanup"; //$NON-NLS-1$

    //todo Can we support suites?

    private static final String[] SPATHS;

    static {
        String paths[];
        String ucp = JMeterUtils.getProperty("user.classpath");
        if (ucp!=null){
            String parts[] = ucp.split(File.pathSeparator);
            paths = new String[parts.length+1];
            paths[0] = JMeterUtils.getJMeterHome() + "/lib/junit/"; //$NON-NLS-1$
            System.arraycopy(parts, 0, paths, 1, parts.length);
        } else {
            paths = new String[]{
                    JMeterUtils.getJMeterHome() + "/lib/junit/" //$NON-NLS-1$
            };
        }
        SPATHS = paths;
    }

    private JLabeledTextField constructorLabel =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_constructor_string")); //$NON-NLS-1$

    private JLabel methodLabel =
            new JLabel(
                    JMeterUtils.getResString("junit_test_method")); //$NON-NLS-1$

    private JLabeledTextField successMsg =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_success_msg")); //$NON-NLS-1$

    private JLabeledTextField failureMsg =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_failure_msg")); //$NON-NLS-1$

    private JLabeledTextField errorMsg =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_error_msg")); //$NON-NLS-1$

    private JLabeledTextField successCode =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_success_code")); //$NON-NLS-1$

    private JLabeledTextField failureCode =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_failure_code")); //$NON-NLS-1$

    private JLabeledTextField errorCode =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_error_code")); //$NON-NLS-1$

    private JLabeledTextField filterpkg =
            new JLabeledTextField(
                    JMeterUtils.getResString("junit_pkg_filter")); //$NON-NLS-1$

    private JCheckBox doSetup = new JCheckBox(JMeterUtils.getResString("junit_do_setup_teardown")); //$NON-NLS-1$
    private JCheckBox appendError = new JCheckBox(JMeterUtils.getResString("junit_append_error")); //$NON-NLS-1$
    private JCheckBox appendExc = new JCheckBox(JMeterUtils.getResString("junit_append_exception")); //$NON-NLS-1$
    private JCheckBox createInstancePerSample = new JCheckBox(JMeterUtils.getResString("junit_create_instance_per_sample")); //$NON-NLS-1$

    /** A combo box allowing the user to choose a test class. */
    private JComboBox classnameCombo;
    private JComboBox methodName;

    private final transient ClassLoader contextClassLoader =
            Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once

    /**
     * Constructor for SpockSamplerGui
     */
    public SpockSamplerGui()
    {
        super();
        init();
    }

    @Override
    public String getStaticLabel() {
        ResourceBundle rb = ResourceBundle.getBundle("com.github.gabehamilton.jmeter.SpockSamplerResources", Locale.getDefault());
        return rb.getString(this.getLabelResource());
    }

    @Override
    public String getLabelResource() { return "sampler_name"; }//$NON-NLS-1$

    /**
     * Initialize the GUI components and layout.
     */
    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);


        add(createClassPanel(), BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    private void setupClasslist(){
        classnameCombo.removeAllItems();
        methodName.removeAllItems();
        try
        {
            List<String> classList;
            classList = ClassFinder.findClassesThatExtend(SPATHS, new Class [] {Specification.class});
            ClassFilter filter = new ClassFilter();
            filter.setPackages(JOrphanUtils.split(filterpkg.getText(), ",")); //$NON-NLS-1$
            // change the classname drop down
            Object[] clist = filter.filterArray(classList);
            for (int idx=0; idx < clist.length; idx++) {
                classnameCombo.addItem(clist[idx]);
            }
        }
        catch (IOException e)
        {
            log.error("Exception getting interfaces.", e);
        }
    }

    private JPanel createClassPanel()
    {
        JLabel label =
                new JLabel(JMeterUtils.getResString("protocol_java_classname")); //$NON-NLS-1$

        classnameCombo = new JComboBox();
        classnameCombo.addActionListener(this);
        classnameCombo.setEditable(false);
        label.setLabelFor(classnameCombo);

        methodName = new JComboBox();
        methodName.addActionListener(this);
        methodLabel.setLabelFor(methodName);

        setupClasslist();

        VerticalPanel panel = new VerticalPanel();
        panel.add(filterpkg);
        filterpkg.addChangeListener(this);

        panel.add(label);
        panel.add(classnameCombo);

        constructorLabel.setText("");
        panel.add(constructorLabel);
        panel.add(methodLabel);
        panel.add(methodName);

        panel.add(successMsg);
        panel.add(successCode);
        panel.add(failureMsg);
        panel.add(failureCode);
        panel.add(errorMsg);
        panel.add(errorCode);
        panel.add(doSetup);
        panel.add(appendError);
        panel.add(appendExc);
        panel.add(createInstancePerSample);
        return panel;
    }

    private void initGui(){
        appendError.setSelected(false);
        appendExc.setSelected(false);
        createInstancePerSample.setSelected(false);
        doSetup.setSelected(false);
        filterpkg.setText(""); //$NON-NLS-1$
        constructorLabel.setText(""); //$NON-NLS-1$
        successCode.setText(JMeterUtils.getResString("junit_success_default_code")); //$NON-NLS-1$
        successMsg.setText(JMeterUtils.getResString("junit_success_default_msg")); //$NON-NLS-1$
        failureCode.setText(JMeterUtils.getResString("junit_failure_default_code")); //$NON-NLS-1$
        failureMsg.setText(JMeterUtils.getResString("junit_failure_default_msg")); //$NON-NLS-1$
        errorMsg.setText(JMeterUtils.getResString("junit_error_default_msg")); //$NON-NLS-1$
        errorCode.setText(JMeterUtils.getResString("junit_error_default_code")); //$NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void clearGui() {
        super.clearGui();
        initGui();
    }

    /** {@inheritDoc} */
    @Override
    public TestElement createTestElement()
    {
        SpockSampler sampler = new SpockSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /** {@inheritDoc} */
    @Override
    public void modifyTestElement(TestElement el)
    {
        SpockSampler sampler = (SpockSampler)el;
        configureTestElement(sampler);
        if (classnameCombo.getSelectedItem() != null &&
                classnameCombo.getSelectedItem() instanceof String) {
            sampler.setClassname((String)classnameCombo.getSelectedItem());
        } else {
            sampler.setClassname(null);
        }
        sampler.setConstructorString(constructorLabel.getText());
        if (methodName.getSelectedItem() != null) {
            String mobj = ((SpockMethod)methodName.getSelectedItem()).getMethodName();
            sampler.setMethod(mobj);
        } else {
            sampler.setMethod(null);
        }
        sampler.setFilterString(filterpkg.getText());
        sampler.setSuccess(successMsg.getText());
        sampler.setSuccessCode(successCode.getText());
        sampler.setFailure(failureMsg.getText());
        sampler.setFailureCode(failureCode.getText());
        sampler.setError(errorMsg.getText());
        sampler.setErrorCode(errorCode.getText());
        sampler.setDoNotSetUpTearDown(doSetup.isSelected());
        sampler.setAppendError(appendError.isSelected());
        sampler.setAppendException(appendExc.isSelected());
        sampler.setCreateOneInstancePerSample(createInstancePerSample.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public void configure(TestElement el)
    {
        super.configure(el);
        SpockSampler sampler = (SpockSampler)el;
        filterpkg.setText(sampler.getFilterString());
        classnameCombo.setSelectedItem(sampler.getClassname());
        setupMethods();
        methodName.setSelectedItem(sampler.getMethod());
        constructorLabel.setText(sampler.getConstructorString());
        if (sampler.getSuccessCode().length() > 0) {
            successCode.setText(sampler.getSuccessCode());
        } else {
            successCode.setText(JMeterUtils.getResString("junit_success_default_code")); //$NON-NLS-1$
        }
        if (sampler.getSuccess().length() > 0) {
            successMsg.setText(sampler.getSuccess());
        } else {
            successMsg.setText(JMeterUtils.getResString("junit_success_default_msg")); //$NON-NLS-1$
        }
        if (sampler.getFailureCode().length() > 0) {
            failureCode.setText(sampler.getFailureCode());
        } else {
            failureCode.setText(JMeterUtils.getResString("junit_failure_default_code")); //$NON-NLS-1$
        }
        if (sampler.getFailure().length() > 0) {
            failureMsg.setText(sampler.getFailure());
        } else {
            failureMsg.setText(JMeterUtils.getResString("junit_failure_default_msg")); //$NON-NLS-1$
        }
        if (sampler.getError().length() > 0) {
            errorMsg.setText(sampler.getError());
        } else {
            errorMsg.setText(JMeterUtils.getResString("junit_error_default_msg")); //$NON-NLS-1$
        }
        if (sampler.getErrorCode().length() > 0) {
            errorCode.setText(sampler.getErrorCode());
        } else {
            errorCode.setText(JMeterUtils.getResString("junit_error_default_code")); //$NON-NLS-1$
        }
        doSetup.setSelected(sampler.getDoNotSetUpTearDown());
        appendError.setSelected(sampler.getAppendError());
        appendExc.setSelected(sampler.getAppendException());
        createInstancePerSample.setSelected(sampler.getCreateOneInstancePerSample());
    }

    private void setupMethods(){
        String className =
                ((String) classnameCombo.getSelectedItem());
        methodName.removeAllItems();
        if (className != null) {
            try {
                // Don't instantiate class
                Class<?> testClass = Class.forName(className, false, contextClassLoader);
                List<Method> methods = ClassUtils.getTestMethods(testClass);
                for(Method m : methods) {
                    String specName = ((FeatureMetadata)m.getAnnotation(FeatureMetadata.class)).name();
                    methodName.addItem(new SpockMethod(m.getName(), specName));
                }
                methodName.repaint();
            } catch (ClassNotFoundException e) {
            }
        }
    }


    private String[] getMethodNames(Class<?> clazz)
    {
        Method[] meths = clazz.getMethods();
        List<String> list = new ArrayList<String>();
        for (int idx=0; idx < meths.length; idx++){
            final Method method = meths[idx];
            final String name = method.getName();
                if (method.isAnnotationPresent(FeatureMetadata.class) ||
                        method.isAnnotationPresent(BeforeClass.class) ||
                        method.isAnnotationPresent(AfterClass.class) ||
                        name.equals(ONETIMESETUP) ||
                        name.equals(ONETIMETEARDOWN)
                        ) {
                    list.add(name);
                }
        }
        if (list.size() > 0){
            return list.toArray(new String[list.size()]);
        }
        return new String[0];
    }

    /**
     * Handle action events for this component.  This method currently handles
     * events for the classname combo box, and sets up the associated method names.
     *
     * @param evt  the ActionEvent to be handled
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == classnameCombo)
        {
            setupMethods();
        }
    }

    /**
     * Handle change events: currently handles events for the JUnit4
     * checkbox, and sets up the relevant class names.
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
//        if (event.getItem() == junit4){
//            setupClasslist();
//        }
    }

    /**
     * the current implementation checks to see if the source
     * of the event is the filterpkg field.
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        if ( event.getSource() == filterpkg) {
            setupClasslist();
        }
    }
}
