package com.github.gabehamilton.jmeter

import org.spockframework.runtime.model.FeatureMetadata
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Created by gabe on 1/27/15.
 */
class ClassUtilsTest extends Specification {
	def "test getTestMethods"() {
		when:
			List<Method> methods = ClassUtils.getTestMethods(SuccessfulSpecToBeRunBySamplerSpec.class);
		then:
			methods.size() == 2
	}

	def "test getMethodsWithAnnotation"() {
		when:
			List<Method> methods = ClassUtils.getMethodsWithAnnotation(SuccessfulSpecToBeRunBySamplerSpec.class, FeatureMetadata.class);
		then:
			methods.size() == 2
	}

	def "getSpockTestMethod finds Spock Tests"() {

		when:
		SpockSampler sampler = new SpockSampler() // warns of NPE due to locale
		Method m = ClassUtils.getSpockTestMethod(SuccessfulSpecToBeRunBySamplerSpec.class, "")
		then:
		m == null

		when:
		m = ClassUtils.getSpockTestMethod(SuccessfulSpecToBeRunBySamplerSpec.class, "it should pretend to test something")
		then:
		m != null
		m.getName().equals('$spock_feature_0_0')
	}

}
