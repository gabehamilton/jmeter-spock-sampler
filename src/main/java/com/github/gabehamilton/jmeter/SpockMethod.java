package com.github.gabehamilton.jmeter;

/**
 * Created by gabe on 1/27/15.
 *
 * Once compiled spock test methods get names like $spock_feature_0_0.
 * This object holds that name plus the "real" name from the @FeatureMetaData.name
 */
public class SpockMethod {
	String methodName;

	String specName;

	public SpockMethod(String methodName, String specName)
	{
		this.methodName = methodName; this.specName = specName;
	}

	/** What to display in JComboBox */
	public String toString(){return specName;}


	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getSpecName() {
		return specName;
	}

	public void setSpecName(String specName) {
		this.specName = specName;
	}
}
