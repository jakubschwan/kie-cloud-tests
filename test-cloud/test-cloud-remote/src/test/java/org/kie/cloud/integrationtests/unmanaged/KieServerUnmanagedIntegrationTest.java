/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.integrationtests.unmanaged;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.KieServerScenario;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.TemplateNotSupported;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

@Category({TemplateNotSupported.class})
public class KieServerUnmanagedIntegrationTest extends AbstractCloudIntegrationTest {

    // TODO set in before class replicas to 0, to let Nexus started and create projects there. Then in before scale Kie Server to 1 (with if to skip if it is already scaled)

    private static KieServerScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static OptaplannerTestProvider optaplannerTestProvider;
    private static ProcessTestProvider processTestProvider;

    @BeforeClass
    public static void initializeDeployment() {
        try {
            deploymentScenario = deploymentScenarioFactory.getKieServerScenarioBuilder()
                    .withInternalMavenRepo(true)
                    .withContainerDeployment(new StringBuilder().append(getHelloRulesProjectContainerDeployment()).append("|")
                            .append(getCloudBalanceProjectContainerDeployment()).append("|").append(getDefinitionProjectContainerDeployment()).toString())
                    .withKieServerMgmtDisabled(true)
                    .withKieServerReplicas(0) // Replicas are set to 0 to let the scenario deploy Nexus. As project are added to the registry later it's required to have set Kie Servers to 0 to let the Scenario deployer pass.
                    .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }
        deploymentScenario.setLogFolderName(KieServerUnmanagedIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        optaplannerTestProvider = OptaplannerTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);

        deploymentScenario.getKieServerDeployment().scale(1);
        deploymentScenario.getKieServerDeployment().waitForScale();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testFireRules(deploymentScenario.getKieServerDeployment(), HELLO_RULES_PROJECT_NAME);
    }

    @Test
    public void testSolverFromMavenRepo() {
        optaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerDeployment(), CLOUD_BALANCE_PROJECT_SNAPSHOT_NAME);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), DEFINITION_PROJECT_SNAPSHOT_NAME);
    }
}
