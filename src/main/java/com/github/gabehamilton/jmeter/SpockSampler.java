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

import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.junit.runner.Result;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;
import org.spockframework.runtime.model.FeatureMetadata;
import spock.lang.Specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

/**
 *
 * This is a basic implementation that runs a single test method of
 * a Spock spec. The current implementation will use the string
 * constructor first. If the test class does not declare a string
 * constructor, the sampler will try empty constructor.
 *
 * todo Invoking single methods is not implemented
 *
 * Derived from Jmeter 2.12 JUnit support:
 * @see org.apache.jmeter.protocol.java.sampler.JunitSampler
 * @link http://svn.apache.org/repos/asf/jmeter/trunk/src/junit/org/apache/jmeter/protocol/java/sampler/JUnitSampler.java
 */
public class SpockSampler extends AbstractSampler implements ThreadListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 1L; // Remember to change this when the class changes

    // BeanInfo properties looked up in the core org.apache.jmeter.resources messages.properties
    private static final String CLASSNAME = "junit.classname";
    private static final String CONSTRUCTORSTRING = "junit.constructorstring";
    private static final String METHOD = "junit.method";
    private static final String ERROR = "junit.error";
    private static final String ERRORCODE = "junit.error.code";
    private static final String FAILURE = "junit.failure";
    private static final String FAILURECODE = "junit.failure.code";
    private static final String SUCCESS = "junit.success";
    private static final String SUCCESSCODE = "junit.success.code";
    private static final String FILTER = "junit.pkg.filter";
    private static final String DOSETUP = "junit.exec.setup";
    private static final String APPEND_ERROR = "junit.append.error";
    private static final String APPEND_EXCEPTION = "junit.append.exception";
    private static final String CREATE_INSTANCE_PER_SAMPLE="junit.createinstancepersample";
    private static final boolean CREATE_INSTANCE_PER_SAMPLE_DEFAULT = false;
    //--

    // Method names to look for in spec
    private static final String SETUP = "setup";
    private static final String TEARDOWN = "cleanup";

    // the Method objects for setUp (@Before) and tearDown (@After) methods
    // Will be null if not provided or not required
    private transient Method setUpMethod;
    private transient Method tearDownMethod;

    // The TestCase to run
    private transient Specification testCase;

    // The method name to be invoked
    private transient String methodName;
    // The name of the class containing the method
    private transient String className;

    public SpockSampler(){
        super();
    }

    /** {@inheritDoc} */
    @Override
    public SampleResult sample(Entry entry) {
        if(getCreateOneInstancePerSample()) {
            initializeTestObject();
        }
        SampleResult sresult = new SampleResult();
        sresult.setSampleLabel(getName());// Bug 41522 - don't use rlabel here
        sresult.setSamplerData(className + "." + methodName);
        sresult.setDataType(SampleResult.TEXT);
        // Assume success
        sresult.setSuccessful(true);
        sresult.setResponseMessage(getSuccess());
        sresult.setResponseCode(getSuccessCode());

        if (this.testCase != null){
            // create a new TestResult

            Result result = null;
            final Specification spec = this.testCase;
            try {
                if (setUpMethod != null){
//                    SpockSpecRunner.execute(spec.getClass(), SETUP);
                    setUpMethod.invoke(this.testCase,new Object[0]);
                }
                sresult.sampleStart();

                log.info("Running " + spec.getClass().getName() + "." + methodName);
                result = SpockSpecRunner.execute(spec.getClass(), methodName);

                // todo avoid calling setUp and tearDown.
                // Doing that will result in calling
                // the setUp and tearDown method twice and the elapsed time
                // will include setup and teardown.
                sresult.sampleEnd();
                if (tearDownMethod != null){
                    tearDownMethod.invoke(testCase,new Object[0]);
                }
            }

            catch(InitializationError ie) {
                throw new RuntimeException(ie);
            }
            catch(NoTestsRemainException ntre) {
                log.error("No tests remain:: " + ntre.getMessage());
            }

            catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof AssertionFailedError){
//                  tr.addFailure(spec, (AssertionFailedError) cause); //  todo with Result
                } else if (cause instanceof AssertionError) {
                    // Convert JUnit4 failure to Junit3 style
                    AssertionFailedError afe = new AssertionFailedError(cause.toString());
                    // copy the original stack trace
                    afe.setStackTrace(cause.getStackTrace());
//                  tr.addFailure(spec, afe);   //  todo with Result
                } else if (cause != null) {
//                  tr.addError(spec, cause);  //  todo with Result
                } else {
//                  tr.addError(spec, e); // todo with Result
                }
            } catch (IllegalAccessException e) {
//              tr.addError(spec, e);   // todo with Result
            } catch (IllegalArgumentException e) {
//              tr.addError(spec, e);  // todo with Result
            }
            if (result == null || !result.wasSuccessful() ){
                sresult.setSuccessful(false);
                StringBuilder buf = new StringBuilder();
                StringBuilder buftrace = new StringBuilder();
                Iterator<Failure> it;
                if (getAppendError()) {
                    it = result.getFailures().iterator();
                    if (it.hasNext()){
                        sresult.setResponseCode(getFailureCode());
                        buf.append( getFailure() );
                        buf.append("\n");
                    }
                    while (it.hasNext()){
                        Failure item = it.next();
                        buf.append( "Failure -- ");
                        buf.append( item.toString() );
                        buf.append("\n");
                        buftrace.append( "Failure -- ");
                        buftrace.append( item.toString() );
                        buftrace.append("\n");
                        buftrace.append( "Trace -- ");
                        buftrace.append( item.getTrace() );
                    }
// todo track errors
//                    it = result.getErrors();
//                    if (it.hasNext()){
//                        sresult.setResponseCode(getErrorCode());
//                        buf.append( getError() );
//                        buf.append("\n");
//                    }
                    while (it.hasNext()){
                        Failure item = it.next();
                        buf.append( "Error -- ");
                        buf.append( item.toString() );
                        buf.append("\n");
                        buftrace.append( "Error -- ");
                        buftrace.append( item.toString() );
                        buftrace.append("\n");
                        buftrace.append( "Trace -- ");
                        buftrace.append( item.getTrace() );
                    }
                }
                sresult.setResponseMessage(buf.toString());
                sresult.setResponseData(buftrace.toString(), null);
            }
        } else {
            // we should log a warning, but allow the test to keep running
            sresult.setSuccessful(false);
            // this should be externalized to the properties
            sresult.setResponseMessage("Failed to create an instance of the class:"+getClassname()
                    +", reasons may be missing both empty constructor and one "
                    + "String constructor or failure to instantiate constructor,"
                    + " check warning messages in jmeter log file");
            sresult.setResponseCode(getErrorCode());
        }
        return sresult;
    }

    /**
     * Method tries to get the setUp and tearDown method for the class
     * @param testCase
     */
    private void initMethodObjects(Object testCase){
        setUpMethod = null;
        tearDownMethod = null;
        if (!getDoNotSetUpTearDown()) {
            // todo setup and cleanup are not being found
            setUpMethod = getMethod(testCase, SETUP);
            tearDownMethod = getMethod(testCase, TEARDOWN);
        }
    }

    /**
     * Sets the Classname attribute of the JavaConfig object
     *
     * @param classname
     *            the new Classname value
     */
    public void setClassname(String classname)
    {
        setProperty(CLASSNAME, classname);
    }

    /**
     * Gets the Classname attribute of the JavaConfig object
     *
     * @return  the Classname value
     */
    public String getClassname()
    {
        return getPropertyAsString(CLASSNAME);
    }

    /**
     * Set the string label used to create an instance of the
     * test with the string constructor.
     * @param constr the string passed to the constructor
     */
    public void setConstructorString(String constr)
    {
        setProperty(CONSTRUCTORSTRING,constr);
    }

    /**
     * @return the string passed to the string constructor
     */
    public String getConstructorString()
    {
        return getPropertyAsString(CONSTRUCTORSTRING);
    }

    /**
     * @return the name of the method to test
     */
    public String getMethod(){
        return getPropertyAsString(METHOD);
    }

    /**
     * Method should add the JUnit <em>testXXX</em> method to the list at
     * the end, since the sequence matters.
     * @param methodName name of the method to test
     */
    public void setMethod(String methodName){
        setProperty(METHOD,methodName);
    }

    /**
     * @return the success message
     */
    public String getSuccess(){
        String value = getPropertyAsString(SUCCESS);
        if("".equals(value))
            value = "successful";
        return value;
    }

    /**
     * set the success message
     * @param success message to be used for success
     */
    public void setSuccess(String success){
        setProperty(SUCCESS,success);
    }

    /**
     * @return the success code defined by the user
     */
    public String getSuccessCode(){
        String value = getPropertyAsString(SUCCESSCODE);
        if("".equals(value))
            value = "1000";
        return value;
    }

    /**
     * Set the success code. The success code should
     * be unique.
     * @param code unique success code
     */
    public void setSuccessCode(String code){
        setProperty(SUCCESSCODE,code);
    }

    /**
     * @return the failure message
     */
    public String getFailure(){
        String value = getPropertyAsString(FAILURE);
        if("".equals(value))
            value = "failed";
        return value;
    }

    /**
     * set the failure message
     * @param fail the failure message
     */
    public void setFailure(String fail){
        setProperty(FAILURE,fail);
    }

    /**
     * @return The failure code that is used by other components
     */
    public String getFailureCode(){
        String value = getPropertyAsString(FAILURECODE);
        if("".equals(value))
            value = "0001";
        return value;
    }

    /**
     * Provide some unique code to denote a type of failure
     * @param code unique code to denote the type of failure
     */
    public void setFailureCode(String code){
        setProperty(FAILURECODE,code);
    }

    /**
     * @return the descriptive error for the test
     */
    public String getError(){
        String value = getPropertyAsString(ERROR);
        if("".equals(value))
            value = "An unexpected error occurred";
        return value;
    }

    /**
     * provide a descriptive error for the test method. For
     * a description of the difference between failure and
     * error, please refer to the
     * <a href="http://junit.sourceforge.net/doc/faq/faq.htm#tests_9">junit faq</a>
     * @param error the description of the error
     */
    public void setError(String error){
        setProperty(ERROR,error);
    }

    /**
     * @return the error code for the test method. It should
     * be an unique error code.
     */
    public String getErrorCode(){
        String value = getPropertyAsString(ERRORCODE);
        if("".equals(value))
            value = "9999";
        return value;
    }

    /**
     * Provide an unique error code for when the test
     * does not pass the assert test.
     * @param code unique error code
     */
    public void setErrorCode(String code){
        setProperty(ERRORCODE,code);
    }

    /**
     * @return the comma separated string for the filter
     */
    public String getFilterString(){
        return getPropertyAsString(FILTER);
    }

    /**
     * set the filter string in comma separated format
     * @param text comma separated filter
     */
    public void setFilterString(String text){
        setProperty(FILTER,text);
    }

    /**
     * if the sample shouldn't call setup/teardown, the
     * method returns true. It's meant for onetimesetup
     * and onetimeteardown.
     *
     * @return flag whether setup/teardown methods should not be called
     */
    public boolean getDoNotSetUpTearDown(){
        return getPropertyAsBoolean(DOSETUP);
    }

    /**
     * set the setup/teardown option
     *
     * @param setup flag whether the setup/teardown methods should not be called
     */
    public void setDoNotSetUpTearDown(boolean setup){
        setProperty(DOSETUP,String.valueOf(setup));
    }

    /**
     * If append error is not set, by default it is set to false,
     * which means users have to explicitly set the sampler to
     * append the assert errors. Because of how junit works, there
     * should only be one error
     *
     * @return flag whether errors should be appended
     */
    public boolean getAppendError() {
        return getPropertyAsBoolean(APPEND_ERROR,false);
    }

    /**
     * Set whether to append errors or not.
     *
     * @param error the setting to apply
     */
    public void setAppendError(boolean error) {
        setProperty(APPEND_ERROR,String.valueOf(error));
    }

    /**
     * If append exception is not set, by default it is set to <code>false</code>.
     * Users have to explicitly set it to <code>true</code> to see the exceptions
     * in the result tree.
     *
     * @return flag whether exceptions should be appended to the result tree
     */
    public boolean getAppendException() {
        return getPropertyAsBoolean(APPEND_EXCEPTION,false);
    }

    /**
     * Set whether to append exceptions or not.
     *
     * @param exc the setting to apply.
     */
    public void setAppendException(boolean exc) {
        setProperty(APPEND_EXCEPTION,String.valueOf(exc));
    }

    /**
     * If the method is not able to create a new instance of the
     * class, it returns null and logs all the exceptions at
     * warning level.
     */
    private static Object getClassInstance(String className, String label){
        Object testclass = null;
        if (className != null){
            Constructor<?> con = null;
            Constructor<?> strCon = null;
            Class<?> theclazz = null;
            Object[] strParams = null;
            Object[] params = null;

            try
            {
                theclazz =
                        Thread.currentThread().getContextClassLoader().loadClass(className.trim());
            } catch (ClassNotFoundException e) {
                log.warn("ClassNotFoundException::" + className.trim() + " :: " + e.getMessage());
            }
            if (theclazz != null) {
                // first we see if the class declares a string
                // constructor. if it is doesn't we look for
                // empty constructor.
                try {
                    strCon = theclazz.getDeclaredConstructor(
                            new Class[] {String.class});
                    // we have to check and make sure the constructor is
                    // accessible. if we didn't it would throw an exception
                    // and cause a NPE.
                    if (label == null || label.length() == 0) {
                        label = className;
                    }
                    if (strCon.getModifiers() == Modifier.PUBLIC) {
                        strParams = new Object[]{label};
                    } else {
                        strCon = null;
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("Trying to find constructor with one String parameter returned error: " + e.getMessage());
                }
                try {
                    con = theclazz.getDeclaredConstructor(new Class[0]);
                    if (con != null){
                        params = new Object[]{};
                    }
                } catch (NoSuchMethodException e) {
                    log.info("Trying to find empty constructor returned error: " + e.getMessage());
                }
                try {
                    // if the string constructor is not null, we use it.
                    // if the string constructor is null, we use the empty
                    // constructor to get a new instance
                    if (strCon != null) {
                        testclass = strCon.newInstance(strParams);
                    } else if (con != null){
                        testclass = con.newInstance(params);
                    } else {
                        log.error("No empty constructor nor string constructor found for class:"+theclazz);
                    }
                } catch (InvocationTargetException e) {
                    log.error("Error instantiating class:"+theclazz+":"+e.getMessage(), e);
                } catch (InstantiationException e) {
                    log.error("Error instantiating class:"+theclazz+":"+e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error("Error instantiating class:"+theclazz+":"+e.getMessage(), e);
                }
            }
        }
        return testclass;
    }

    /**
     * Get a method.
     * @param clazz the classname (may be null)
     * @param method the method name (may be null)
     * @return the method or null if an error occurred
     * (or either parameter is null)
     */
    private Method getMethod(Object clazz, String method){
        if (clazz != null && method != null){
            try {
                return clazz.getClass().getMethod(method,new Class[0]);
            } catch (NoSuchMethodException e) {
                log.warn(e.getMessage());
            }
        }
        return null;
    }

    private Method getMethodWithAnnotation(Object clazz, Class<? extends Annotation> annotation) {
        if(null != clazz && null != annotation) {
            for(Method m : clazz.getClass().getMethods()) {
                if(m.isAnnotationPresent(annotation)) {
                    return m;
                }
            }
        }
        return null;
    }

    @Override
    public void threadFinished() {
    }

    /**
     * Set up all variables that don't change between samples.
     */
    @Override
    public void threadStarted() {
        testCase = null;
        methodName = getMethod();
        className = getClassname();
        if(!getCreateOneInstancePerSample()) {
            // NO NEED TO INITIALIZE WHEN getCreateOneInstancePerSample 
            // is true cause it will be done in sample
            initializeTestObject();
        }
    }

    /**
     * Initialize test object
     */
    private void initializeTestObject() {
        String rlabel = getConstructorString();
        if (rlabel.length()== 0) {
            rlabel = SpockSampler.class.getName();
        }
        this.testCase = (Specification) getClassInstance(className, rlabel);
        if (this.testCase != null){
            initMethodObjects(this.testCase);

        }
    }

    /**
     *
     * @param createOneInstancePerSample
     *            flag whether a new instance for each call should be created
     */
    public void setCreateOneInstancePerSample(boolean createOneInstancePerSample) {
        this.setProperty(CREATE_INSTANCE_PER_SAMPLE, createOneInstancePerSample, CREATE_INSTANCE_PER_SAMPLE_DEFAULT);
    }

    /**
     *
     * @return boolean create New Instance For Each Call
     */
    public boolean getCreateOneInstancePerSample() {
        return getPropertyAsBoolean(CREATE_INSTANCE_PER_SAMPLE, CREATE_INSTANCE_PER_SAMPLE_DEFAULT);
    }
}