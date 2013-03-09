//
// Generated from archetype; please customize.
//

package org.realsoftwarematters.cyclicprocessor

import org.realsoftwarematters.cyclicprocessor.CyclicProcessor

/**
 * Tests for the {@link Example} class.
 */
class CyclicProcessorTest
    extends GroovyTestCase
{
    void testSimpleRun() {
        def p = new CyclicProcessor(10,300)

        p.aggregateResults = new TestAggregateResults()
        p.factoryRequest = new TestSimpleFactoryRequest()

        p.setup()
        p.run()
        p.shutdown()
        println("### Result #### : ${p.getAggregatorResult().value}")

        assertEquals(3000,p.getAggregateResults().value)
    }
}

class TestAggregateResults implements AggregateResults<Integer>{
    Integer value = 0

    def add(Integer i){
        println "Adding ${i} to agregator"
        value = value + i
        println "Value ${value}"
    }
}


/*
* Sample implementation for Making a request
*/
class TestRunRequest implements RunRequest<Integer>{
    Integer numThread

    TestRunRequest(){
        this.numThread = 0
    }

    TestRunRequest(Integer numThread){
        this.numThread = numThread
    }

    def makeRequest(){
        println "making the request from number ${numThread}"
        return this
    }

    def Integer getResult(){
        return 10
    }
}

class TestSimpleFactoryRequest implements FactoryRequest{

    def RunRequest getRunRequest(){
        return new TestRunRequest();
    }
}