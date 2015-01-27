package com.github.gabehamilton.jmeter

import spock.lang.Specification


/**
 * Created by gabe on 1/25/15.
 */
class FailingSpecToBeRunBySamplerSpec extends Specification {

    def "it should fail"() {
        when:
            println 'fail fast and prosper'
        then:
        1 == 2
    }

    def "it should throw an unexpected exception"() {
        when:
            println "throwing an exception"
            throw new RuntimeException("Exception deliberately thrown from test")
        then:
            1 == "we should never get here"
    }

    def setup() {
        println 'setup ran'
    }

}