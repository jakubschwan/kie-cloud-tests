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

import java.net.MalformedURLException;
import java.net.URL;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;

import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqImageStreamDeployer {

  private static final Logger logger = LoggerFactory.getLogger(AmqImageStreamDeployer.class);

    public static void deploy(Project project) {
        try {
            logger.debug("Create amq broker image stream (for version up to 7.8)");
            OpenShift openShift = project.getOpenShiftAdmin();
            KubernetesList resourceList = openShift.lists()
                    .inNamespace("openshift")
                    .load(new URL(OpenShiftConstants.getAmqImageStreams()))
                    .get();
            resourceList.getItems().forEach(item -> {
                openShift.imageStreams()
                        .inNamespace("openshift")
                        .withName(item.getMetadata().getName())
                        .delete();
            });
            project.runOcCommandAsAdmin("apply", "-f", OpenShiftConstants.getAmqImageStreams(), "-n", "openshift");

            logger.debug("Create image stream using amq.image.reference property (for version from 7.9). For the new format of amq broker image is not available image stream by default.");
            String amqImageStreamReference = OpenShiftConstants.getAmqImageReference();
            String amqBrokerTag = amqImageStreamReference.split(":")[1];
            String amqBrokerName = amqImageStreamReference.split(":")[0].split("/")[2];

            ImageStream amqImageStream = new ImageStreamBuilder().withNewMetadata().withName(amqBrokerName).withNamespace("openshift").endMetadata()
            .withNewSpec().withNewLookupPolicy().withLocal(true).and().addNewTag().withName(amqBrokerTag).addToAnnotations("version", amqBrokerTag)
            .withNewFrom().withKind("DockerImage").withName(amqImageStreamReference).endFrom().endTag().endSpec().build();
            openShift.imageStreams().createOrReplace(amqImageStream);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        }
    }
}
