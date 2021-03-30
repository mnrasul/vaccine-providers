package com.example.vaccine.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.drools.core.management.DroolsManagementAgent;
import org.kie.api.event.KieRuntimeEventManager;

/**
 * See https://kie.zulipchat.com/#narrow/stream/232676-kogito for context. This resolves a mysterious build failure
 * where Drools Core brings in a JMX mbean, which are not supported in Graal Native Image builds.
 */
@TargetClass(value = org.drools.core.management.DroolsManagementAgent.class)
final class DroolsManagementAgentSubstitution {

    @Substitute
    private void unregisterKnowledgeSessionUnderName(DroolsManagementAgent.CBSKey cbsKey, KieRuntimeEventManager ksession) {
        System.out.println("WARN: NOOP DroolsSubstitution.unregisterKnowledgeSessionUnderName");
    }

}
