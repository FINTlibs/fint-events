package no.fint.events.config

import org.springframework.core.env.Environment
import spock.lang.Specification

class FintEventsPropsSpec extends Specification {
    private FintEventsProps props
    private Environment environment

    void setup() {
        environment = Mock(Environment)
        props = new FintEventsProps(environment: environment)
    }

    def "Load default redisson config when no yml file is found"() {
        when:
        props.init()

        then:
        1 * environment.getActiveProfiles() >> ['unknown-profile']
        props.getRedissonConfig() != null
    }

    def "Read redisson config file"() {
        when:
        props.init()

        then:
        1 * environment.getActiveProfiles() >> ['test']
        props.getRedissonConfig() != null
    }

    def "Load default redisson config when test-mode is enabled"() {
        given:
        props = new FintEventsProps(testMode: 'true')

        when:
        props.init()

        then:
        props.getRedissonConfig() != null
    }

    def "Load configured redisson config file"() {
        given:
        props = new FintEventsProps(redissonConfigFile: 'src/test/resources/redisson-test.yml')

        when:
        props.init()

        then:
        props.getRedissonConfig() != null
    }

    def "Throw IllegalStateException when the configured redisson config file is not found"() {
        given:
        props = new FintEventsProps(redissonConfigFile: 'invalid-file-path')

        when:
        props.init()

        then:
        thrown(IllegalStateException)
    }
}
