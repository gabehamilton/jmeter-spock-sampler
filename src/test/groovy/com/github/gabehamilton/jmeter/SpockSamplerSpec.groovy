package com.github.gabehamilton.jmeter

import org.apache.jmeter.samplers.SampleResult
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Created by gabe on 1/23/15.
 */
class SpockSamplerSpec extends Specification {


	def "sample runs a spec"() {
		when:
			Locale.setDefault(Locale.US)
			SpockSampler sampler = new SpockSampler() // warns of NPE due to locale
			sampler.setClassname(SuccessfulSpecToBeRunBySamplerSpec.getCanonicalName())
//			sampler.setMethod('it should pretend to test something')
			sampler.threadStarted()
			SampleResult result = sampler.sample(null)
			sampler.threadFinished()
		then:
			result != null
			result.isSuccessful()
			!result.responseMessage.startsWith('Failed')
	}

	def "getSpockTestMethod finds Spock Tests"() {

		when:
			SpockSampler sampler = new SpockSampler() // warns of NPE due to locale
			Method m = sampler.getSpockTestMethod(new SuccessfulSpecToBeRunBySamplerSpec(), "")
		then:
			m == null

		when:
			 m = sampler.getSpockTestMethod(new SuccessfulSpecToBeRunBySamplerSpec(), "it should pretend to test something")
		then:
			m != null
			m.getName().equals('$spock_feature_0_0')
	}

}
