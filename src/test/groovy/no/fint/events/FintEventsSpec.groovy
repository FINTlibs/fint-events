package no.fint.events

import no.fint.events.config.FintEventsProps
import no.fint.events.listener.Listener
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestListener
import org.redisson.api.RBlockingQueue
import org.redisson.api.RedissonClient
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.TaskScheduler
import spock.lang.Specification

class FintEventsSpec extends Specification {
    private FintEvents fintEvents
    private RedissonClient client
    private TaskScheduler taskScheduler
    private ApplicationContext applicationContext

    void setup() {
        client = Mock(RedissonClient)
        taskScheduler = Mock(TaskScheduler)
        applicationContext = Mock(ApplicationContext)
        def props = new FintEventsProps(defaultDownstreamQueue: '%s.downstream', defaultUpstreamQueue: '%s.upstream')
        fintEvents = new FintEvents(client: client, props: props, taskScheduler: taskScheduler, applicationContext: applicationContext)
    }

    def "Get downstream queue"() {
        when:
        def downstream = fintEvents.getDownstream('rogfk.no')

        then:
        1 * client.getBlockingQueue('rogfk.no.downstream') >> Mock(RBlockingQueue)
        downstream != null
    }

    def "Get upstream queue"() {
        when:
        def upstream = fintEvents.getUpstream('rogfk.no')

        then:
        1 * client.getBlockingQueue('rogfk.no.upstream') >> Mock(RBlockingQueue)
        upstream != null
    }

    def "Send object to queue"() {
        given:
        def queue = Mock(RBlockingQueue)
        def testDto = new TestDto()

        when:
        fintEvents.send('test-queue', testDto)

        then:
        1 * client.getBlockingQueue('test-queue') >> queue
        1 * queue.offer(testDto)
    }

    def "Send object to downstream queue"() {
        given:
        def queue = Mock(RBlockingQueue)
        def testDto = new TestDto()

        when:
        fintEvents.sendDownstream('rogfk.no', testDto)

        then:
        1 * client.getBlockingQueue('rogfk.no.downstream') >> queue
        1 * queue.offer(testDto)
    }

    def "Send object to upstream queue"() {
        given:
        def queue = Mock(RBlockingQueue)
        def testDto = new TestDto()

        when:
        fintEvents.sendUpstream('rogfk.no', testDto)

        then:
        1 * client.getBlockingQueue('rogfk.no.upstream') >> queue
        1 * queue.offer(testDto)
    }

    def "Register listener"() {
        when:
        fintEvents.registerListener('test-listener-queue', TestListener)

        then:
        1 * applicationContext.getBean(TestListener) >> new TestListener()
        1 * client.getBlockingQueue('test-listener-queue')
        1 * taskScheduler.scheduleWithFixedDelay(_ as Listener, 10)
    }

    def "Register downstream listener"() {
        when:
        fintEvents.registerDownstreamListener('rogfk.no', TestListener)

        then:
        1 * applicationContext.getBean(TestListener) >> new TestListener()
        1 * client.getBlockingQueue('rogfk.no.downstream')
        1 * taskScheduler.scheduleWithFixedDelay(_ as Listener, 10)
    }

    def "Register upstream listener"() {
        when:
        fintEvents.registerUpstreamListener('rogfk.no', TestListener)

        then:
        1 * applicationContext.getBean(TestListener) >> new TestListener()
        1 * client.getBlockingQueue('rogfk.no.upstream')
        1 * taskScheduler.scheduleWithFixedDelay(_ as Listener, 10)
    }
}
