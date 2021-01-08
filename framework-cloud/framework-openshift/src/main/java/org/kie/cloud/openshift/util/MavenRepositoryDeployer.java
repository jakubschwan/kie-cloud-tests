/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.util;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.WaiterException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.deployment.MavenNexusRepositoryDeploymentImpl;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying Docker registry to OpenShift project.
 */
public class MavenRepositoryDeployer {

    private static final Logger logger = LoggerFactory.getLogger(MavenRepositoryDeployer.class);

    private static final String NEXUS_LABEL_NAME = "deploymentConfig";
    private static final String NEXUS_LABEL_VALUE = "maven-nexus";
    private static final String NEXUS_LABEL = NEXUS_LABEL_NAME+"="+NEXUS_LABEL_VALUE;

    public static MavenRepositoryDeployment deploy(Project project, boolean shouldWait) {
        deployMavenRepository(project);

        logger.info("Waiting for Maven repository deployment to become ready.");
        MavenRepositoryDeployment mavenDeployment = new MavenNexusRepositoryDeploymentImpl(project);

        if (shouldWait) {
            mavenDeployment.waitForScale();
        }

        return mavenDeployment;
    }

    private static void deployMavenRepository(Project project) {
        logger.info("Creating internal Maven Repository.");

        // Login is part of binary retrieval
        OpenShiftBinary masterBinary = OpenShifts.masterBinary(project.getName());
        masterBinary.execute("new-app", "sonatype/nexus", "-l", NEXUS_LABEL);

        // TODO add waiter
        logger.info("---------- tmp debug : all pods " + project.getOpenShift().pods().list().getItems());

        logger.info("********** tmp debug : pod info when created in " + project.getName());
        Map<String,String> containerImages = project.getOpenShift().pods()
                      .withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
                      .list()
                      .getItems()
                      .stream()
                      .map(Pod::getSpec)
                      .map(PodSpec::getContainers)
                      .flatMap(List::stream)
                      .collect(Collectors.toMap(Container::getName, Container::getImage));
                      //.forEach(c->{logger.info("tmp.debug... container:" + c.getName() + " from image: " + c.getImage());});
        logger.info("...tmp.debug... container:" + containerImages);
        containerImages.forEach((key, value) -> logger.info(key + ":" + value));
        logger.info("...tmp.debug.over...");

        // wait until operator is ready
        boolean pullFromMirrorNew = false;
        Instant startWaiterTime = Instant.now();
        try {
            project.getOpenShift().waiters().areExactlyNPodsRunning(1, NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE).waitFor();
        } catch (WaiterException e) {
            logger.error("Warning exception was caught. Will try to pull nexus from mirror.", e);
            pullFromMirrorNew = true;
        }
        // debug output
        logger.info("++++++++ tmp debug : pod waiting for running time " + Duration.between(startWaiterTime, Instant.now()));

        String nexusDigestImage = DeploymentConstants.getNexusDockerDigestImage();
        if (nexusDigestImage.isEmpty()) {
            logger.error("Nexus digest image not set");
            throw new RuntimeException("Nexus digest image not set");
        }
        if(pullFromMirrorNew) {
            project.getOpenShift().pods()
                      .withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
                      .list()
                      .getItems()
                      .stream()
                      .map(Pod::getSpec)
                      .map(PodSpec::getContainers)
                      .flatMap(List::stream)
                      .forEach(c->c.setImage(nexusDigestImage));

            logger.info("---------- tmp debug : all pods " + project.getOpenShift().pods().list().getItems());

            logger.info("********** tmp debug : pod info when updated in " + project.getName());
            Map<String,String> containerImagesAfterUpdate = project.getOpenShift().pods()
                        .withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
                        .list()
                        .getItems()
                        .stream()
                        .map(Pod::getSpec)
                        .map(PodSpec::getContainers)
                        .flatMap(List::stream)
                        .collect(Collectors.toMap(Container::getName, Container::getImage));
                        //.forEach(c->{logger.info("tmp.debug... container:" + c.getName() + " from image: " + c.getImage());});
            logger.info("...tmp.debug... container:" + containerImagesAfterUpdate);
            containerImagesAfterUpdate.forEach((key, value) -> logger.info(key + ":" + value));
            logger.info("...tmp.debug.over...");
            // TODO add waiter
            Instant startWaiterTimeAfterChange = Instant.now();
                project.getOpenShift().waiters().areExactlyNPodsRunning(1, NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE).waitFor();
            // debug output
            logger.info("++++++++ tmp debug : pod waiting for running time " + Duration.between(startWaiterTimeAfterChange, Instant.now()));
        }
        

        
        
        //OpenShiftCaller.repeatableCall(project.getOpenShift().getPods().stream());

        //OpenShift openShift = project.getOpenShift();


        //boolean pullFromMirror = waitForNexusPodRunning(openShift);



    /*public List<Instance> getAllInstances() {
        return openShift
                .getPods()
                .stream()
                .filter(this::isScheduledPod)
                .map(pod -> OpenshiftInstanceUtil.createInstance(openShift, getName(), pod))
                .collect(toList());
    } */


    /*protected void logNodeNameOfAllInstances() {
        for (Deployment deployment : getDeployments()) {
            deployment.getInstances().forEach(instance -> {
                Pod pod = project.getOpenShift().getPod(instance.getName());
                String podName = pod.getMetadata().getName();
                String instanceNodeName = pod.getSpec().getNodeName();
                logger.info("Node name of the {}: {} ", podName, instanceNodeName);
            });
        }
    } */

/*
        // Nexus digest image is required when is set mirroring in OCP
        String nexusDigestImage = DeploymentConstants.getNexusDockerDigestImage();
        if (nexusDigestImage.isEmpty()) {
            logger.error("Nexus digest image not set");
            // TODO throw exception!
        }
*/
/*
        logger.info("********** tmp debug : pod info when created in " + project.getName());
        openShift.pods().withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
        .list()
        .getItems().forEach(pod -> {logger.info("Pod infor: " + pod.toString());});
*/
        // if image is not pulled from docker hub, we need to change the image tag to sha.
        // Because image content policy cannot mirror images with tag (only with digest).
/*        if(pullFromMirror) {
            openShift.pods()
                      .withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
                      .list()
                      .getItems()
                      .stream()
                      .map(Pod::getSpec)
                      .map(PodSpec::getContainers)
                      .flatMap(List::stream)
                      .forEach(c->c.setImage(nexusDigestImage));
 */                     //.forEach(list -> list.forEach(c -> c.setImage(nexusDigestImage)));
        /*project.getOpenShiftAdmin().pods().withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE).list().getItems().forEach(
                pod -> {
                    pod.getSpec().getContainers().forEach(container -> {
                        container.setImage(nexusDigestImage);
                    });
                });*/

                // or maybe try to run patch command e.g. ./oc patch imagepruners.imageregistry.operator.openshift.io/cluster --patch '{"spec":{"suspend":false, "keepYoungerThanDuration":"24h"}}' --type=merge

            
//        }

/*        logger.info("********** tmp debug : pod info when updated in " + project.getName());
        openShift.pods().withLabel(NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
        .list()
        .getItems().forEach(pod -> {logger.info("Pod infor: " + pod.toString());});
        // TODO add waiter
        pullFromMirror = waitForNexusPodRunning(openShift);
        if(pullFromMirror) {
            throw new RuntimeException("Deployment of nexus pod failed. See latest warning.");
        }
*/
        masterBinary.execute("expose", "service", "nexus");
    }

    private static boolean waitForNexusPodRunning(OpenShift openShift) {
        try {
            OpenShiftCaller.repeatableCall(() -> openShift.waiters()
                                                          .areExactlyNPodsRunning(1, NEXUS_LABEL_NAME, NEXUS_LABEL_VALUE)
                                                          .timeout(OpenShiftResourceConstants.NEXUS_POD_START_TO_RUNNING)
                                                          .reason("Waiting for " + 1 + " pods of deployment config " + NEXUS_LABEL_VALUE + " to become ready.")
                                                          .waitFor());
        } catch (AssertionError | WaiterException e) {
            logger.warn("Nexus pod was not pulled from docker. Try to change image to digest and pull it from mirror.", e);
            return true;
        }
        return false;
    }
}
