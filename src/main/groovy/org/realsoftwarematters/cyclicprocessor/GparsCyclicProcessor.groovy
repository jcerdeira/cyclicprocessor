package org.realsoftwarematters.cyclicprocessor


import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.DefaultActor

class GparsCyclicProcessor {



}

final class WorkToDo {}
final class NeedMoreWork {}

final class LoadBalancer extends DefaultActor {
    int workers = 0
    List taskQueue = []
    private static final QUEUE_SIZE_TRIGGER = 10

    void act() {
        loop {
            react { message ->
                switch (message) {
                    case NeedMoreWork:
                        if (taskQueue.size() == 0) {
                            println 'No more tasks in the task queue. Terminating the worker.'
                            reply DemoWorker.EXIT
                            workers -= 1
                        } else reply taskQueue.remove(0)
                        break
                    case WorkToDo:
                        taskQueue << message
                        if ((workers >= 0) && (workers < QUEUE_SIZE_TRIGGER)) {
                            println 'Need more workers. Starting one.'
                            workers += 1
                            new DemoWorker(this).start()
                        }
                }
                println "Active workers=${workers}tTasks in queue=${taskQueue.size()}"
            }
        }
    }
}

final class DemoWorker extends DefaultActor {

    final static Object EXIT = new Object()
    private static final Random random = new Random()

    Actor balancer

    def DemoWorker(balancer) {
        this.balancer = balancer
    }

    void act() {
        loop {
            this.balancer << new NeedMoreWork()
            react {
                switch (it) {
                    case WorkToDo:
                        processMessage(it)
                        break
                    case EXIT: terminate()
                }
            }
        }

    }

    private void processMessage(message) {
        synchronized (random) {
            Thread.sleep random.nextInt(5000)
        }
    }
}



