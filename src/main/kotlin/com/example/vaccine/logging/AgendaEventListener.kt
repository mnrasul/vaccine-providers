package com.example.vaccine.logging

import org.kie.api.event.rule.AfterMatchFiredEvent
import org.kie.api.event.rule.DefaultAgendaEventListener
import javax.enterprise.context.ApplicationScoped

/**
 * The "Agenda" is a fundamental concept to all pattern matching engines that derive from the original
 * [RETE algorithm](https://en.wikipedia.org/wiki/Rete_algorithm). The current incarnation of drools uses an algorithm
 * called [Phreak](https://docs.jboss.org/kogito/release/latest/html_single/#_decision_engine_in_kogito).
 *
 * This class can subscribe to various events that occur on the "Agenda". We log those events
 */
@ApplicationScoped
class AgendaEventListener : DefaultAgendaEventListener() {
    /**
     * After match fired is semantically equivalent to a rule firing.
     */
    override fun afterMatchFired(event: AfterMatchFiredEvent?) {
        logger.info("Rule Fired: ${event?.match?.rule?.name}")
    }

    companion object {
        private val logger = getLogger()
    }
}
