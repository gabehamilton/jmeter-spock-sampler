package com.github.gabehamilton.jmeter

import org.apache.jmeter.samplers.SampleResult
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Created by gabe on 1/23/15.
 */
class SpockSamplerTest extends Specification {


	def "sampler runs a spec"() {
		when:
			Locale.setDefault(Locale.US)
			SpockSampler sampler = new SpockSampler() // warns of NPE due to locale
			sampler.setClassname(SuccessfulSpecToBeRunBySamplerSpec.getCanonicalName())
			sampler.threadStarted()
			SampleResult result = sampler.sample(null)
			sampler.threadFinished()
		then:
			result != null
			result.isSuccessful()
			!result.responseMessage.startsWith('Failed')
	}

	def "sampler runs a single method in spec"() {
		when:
		Locale.setDefault(Locale.US)
		SpockSampler sampler = new SpockSampler() // warns of NPE due to locale
		sampler.setClassname(SuccessfulSpecToBeRunBySamplerSpec.getCanonicalName())
		sampler.setMethod('$spock_feature_0_0')// aka 'it should pretend to test something'
		sampler.threadStarted()
		SampleResult result = sampler.sample(null)
		sampler.threadFinished()
		then:
		result != null
		result.isSuccessful()
		!result.responseMessage.startsWith('Failed')
	}
}
