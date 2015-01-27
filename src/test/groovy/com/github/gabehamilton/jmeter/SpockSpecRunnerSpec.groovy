package com.github.gabehamilton.jmeter

import org.junit.runner.Result
import spock.lang.Specification

/**
 * Created by gabe on 1/25/15.
 */
class SpockSpecRunnerSpec extends Specification {
    def "Execute should run tests"() {
        when:
            Result r  = SpockSpecRunner.execute(SuccessfulSpecToBeRunBySamplerSpec.class);
        then:
            r != null
            r.getRunCount() == 1
            r.getFailureCount() == 0
    }

    def "it should track failures"() {
        when:
        Result r  = SpockSpecRunner.execute(FailingSpecToBeRunBySamplerSpec.class);
        then:
        r != null
        r.getRunCount() == 2
        r.getFailureCount() == 2
    }

}
