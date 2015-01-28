package com.github.gabehamilton.jmeter

import spock.lang.Specification


/**
 * Created by gabe on 1/25/15.
 */
class SuccessfulSpecToBeRunBySamplerSpec extends Specification {


    def "it should pretend to test something"() {
        when:
            println 'live long and test'
        then:
            1 == 1
    }

    def "one more time"() {
        when:
            println 'prosper through testing'
        then:
            2 == 2
    }

    def setup() {
        println 'setup ran'
    }

}