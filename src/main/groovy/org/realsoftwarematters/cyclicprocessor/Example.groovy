package org.realsoftwarematters.cyclicprocessor

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import groovy.grape.Grape
import groovyx.gpars.GParsExecutorsPool

class CyclicProcessor {

	def processQueue
	def aggregateValuesQueue
	def aggregateResults = new SimpleAggregateResults()

	def executorConsumeRequestsService
	def executorAggregateRequestsService

	AtomicInteger numReq = new AtomicInteger(0)
	AtomicInteger numAgg = new AtomicInteger(0)
	final int numRequests

	CyclicProcessor(int queueSize , int reqNum){
	
		this.numRequests = reqNum

		this.processQueue = new ArrayBlockingQueue<RunRequest>(queueSize+5)
		this.aggregateValuesQueue = new ArrayBlockingQueue(queueSize+5)
	
		executorConsumeRequestsService = Executors.newFixedThreadPool(queueSize);
		executorAggregateRequestsService = Executors.newFixedThreadPool(1);
	}

	def setup(){
		
		for(i in 1..10)	
			executorConsumeRequestsService.submit({executeRequest()} as Runnable)

		executorAggregateRequestsService.submit({aggregateRequests()} as Runnable)
	}

	def run(){
		for(i in 1..12) {
			println "Adding ${i}"
			processQueue.put(new SimpleRunRequest(i))
		}
	}

	def shutdown(){
		executorAggregateRequestsService.shutdown()
		executorConsumeRequestsService.shutdown()
		executorAggregateRequestsService.awaitTermination(1000, TimeUnit.SECONDS)
		executorConsumeRequestsService.awaitTermination(1000, TimeUnit.SECONDS)
	}

	def executeRequest(){
		while(numReq.intValue() < numRequests){
			numReq.getAndAdd(1)
			println "Executing Request"
			def a = processQueue.poll(10, TimeUnit.SECONDS);
			println "Request -> ${a}"
			def result = a.makeRequest()
			aggregateValuesQueue.put(result)
			
		}
	}

	def aggregateRequests(){
		while(numAgg.intValue() < numRequests){
			numAgg.getAndAdd(1)
			def a = aggregateValuesQueue.poll(10, TimeUnit.SECONDS)
			aggregateResults.add(a.getResult())
			processQueue.put(a)
		}
	}

	def getAggregatorResult(){
		return aggregateResults
	}
}


/*
* Interface for Making a request
*/
interface RunRequest<T>{
	def makeRequest()
	def T getResult()
}


/*
* Sample implementation for Making a request
*/
class SimpleRunRequest implements RunRequest<Integer>{
	Integer numThread

	SimpleRunRequest(Integer numThread){
		this.numThread = numThread
	}

	def makeRequest(){
		println "making the request from number ${numThread}"
		return this
	}

	def getResult(){
		return 10
	}
}

/*
* Factory to create RuRequest Objects
*/
interface RunRequestFactory<R,T implements RunRequest<R>>{
	def T getObject()
}

/*
* Implementing RunRequest Factory
*/
class SimpleRunRequestFactory implements RunRequestFactory<SimpleRunRequest>{

	def SingleRunRequest getObject(){
		return null
	}

}



interface AggregateResults<T>{
	def add(T value)
}

class SimpleAggregateResults implements AggregateResults<Integer>{
	Integer value = 0

	def add(Integer i){
		println "Adding ${i} to agregator"
		value = value + i
		println "Value ${value}"
	}
}

def p = new CyclicProcessor(10,300)
p.setup()
p.run()
p.shutdown()
println("### Result #### : ${p.getAggregatorResult().value}")

//Thread.sleep(50000)