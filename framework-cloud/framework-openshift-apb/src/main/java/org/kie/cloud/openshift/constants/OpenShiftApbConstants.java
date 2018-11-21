/*
 * Copyright 2018 JBoss by Red Hat.
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
package org.kie.cloud.openshift.constants;

public class OpenShiftApbConstants {

    //extra-vars for APB image
    public static final String APB_PLAN_ID = "_apb_plan_id";
    public static final String APB_KIESERVER_DB_TYPE = "apb_kieserver_db_type";

    // Pre-provisioned users
    public static final String KIE_ADMIN_USER = "apb_kie_admin_user";
    public static final String KIE_ADMIN_PWD = "apb_kie_admin_pwd";
    public static final String KIE_SERVER_USER = "apb_kieserver_user";
    public static final String KIE_SERVER_PWD = "apb_kieserver_pwd";
    public static final String KIE_CONTROLLER_USER = "apb_controller_user";
    public static final String KIE_CONTROLLER_PWD = "apb_controller_pwd";

    public static final String BUSINESS_CENTRAL_HOSTNAME_HTTP = "";
    public static final String BUSINESS_CENTRAL_HOSTNAME_HTTPS = "apb_businesscentral_hostname";
    public static final String APB_KIESERVER_HOSTNAME = "apb_kieserver_hostname"; // TODO remove this after added HTTP prop.
    public static final String APB_KIESERVER_HOSTNAME_HTTP = "";
    public static final String APB_KIESERVER_HOSTNAME_HTTPS = "apb_kieserver_hostname";
    public static final String APB_SMART_ROUTER_HOSTNAME_HTTPS = "apb_smartrouter_hostname";
    public static final String APB_KIESERVER_IMAGE_STREAM_NAME = "apb_kieserver_image_stream_name";
    public static final String APB_BUSINESSCENTRAL_SECRET_NAME = "apb_businesscentral_secret_name";
    public static final String APB_KIESERVER_SECRET_NAME = "apb_kieserver_secret_name";

    // RH-SSO
    public static final String SSO_URL = "apb_sso_url";
    public static final String SSO_REALM = "apb_sso_realm";
    public static final String SSO_CLIENT = "apb_sso_client";
    public static final String SSO_CLIENT_SECRET = "apb_sso_client_secret";
    public static final String SSO_USER = "apb_sso_user";
    public static final String SSO_PWD = "apb_sso_pwd";
    public static final String SSO_DISABLE_SSL_CERT_VALIDATION = "apb_sso_disable_ssl_cert_validation";

    // Maven repositories
    public static final String MAVEN_REPO_URL = "apb_maven_repo_url";
    public static final String MAVEN_REPO_USER = "apb_maven_repo_user";
    public static final String MAVEN_REPO_PWD = "apb_maven_repo_pwd";
    public static final String BUSINESS_CENTRAL_MAVEN_USERNAME = "apb_businesscentral_maven_repo_user";
    public static final String BUSINESS_CENTRAL_MAVEN_PASSWORD = "apb_businesscentral_maven_repo_pwd";
    public static final String BUSINESS_CENTRAL_MAVEN_SERVICE = "";

    // ImageStreams
    public static final String IMAGE_STREAM_NAMESPACE = "image_stream_namespace"; // Need to hard replace this value
    public static final String APB_IMAGE_STREAM_TAG = "apb_image_stream_tag";

    public static final String DEFAULT_PASSWORD = "";

    public static final String KIE_SERVER_ID = "";
    public static final String KIE_SERVER_ROUTER_ID = "";
    public static final String TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL = ""; // ??

    // HA
    public static final String APB_REPLICAS = "apb_replicas";
    public static final String APB_KIESERVER_SETS = "apb_kieserver_sets";
    public static final String APB_KIESERVER_REPLICAS_STRING = "apb_kieserver_replicas";
    public static final String APB_SMARTROUTER_REPLICAS_STRING = "apb_smartrouter_replicas";

    // External DB
    public static final String APB_KIESERVER_EXTERNAL_DB_DRIVER="apb_kieserver_external_db_driver";
    public static final String APB_KIESERVER_EXTERNAL_DB_DIALECT = "apb_kieserver_external_db_dialect";
    public static final String APB_KIESERVER_EXTERNAL_DB_HOST = "apb_kieserver_external_db_host";
    public static final String APB_KIESERVER_EXTERNAL_DB_PORT = "apb_kieserver_external_db_port";
    public static final String APB_KIESERVER_EXTERNAL_DB_NAME = "apb_kieserver_external_db_name";
    public static final String APB_KIESERVER_EXTERNAL_DB_URL = "apb_kieserver_external_db_url";

    // SSO
    public static final String APB_SSO_URL = "apb_sso_url";
    public static final String APB_SSO_REALM = "apb_sso_realm";
    public static final String APB_SSO_CLIENT = "apb_sso_client";
    public static final String APB_SSO_CLIENT_SECRET = "apb_sso_client_secret";
    public static final String APB_SSO_USER = "apb_sso_user";
    public static final String APB_SSO_PWD = "apb_sso_pwd";
    public static final String APB_SSO_DISABLE_SSL_CERT_VALIDATION = "apb_sso_disable_ssl_cert_validation";

    //Artifact source (S2I)
    public static final String KIE_SERVER_CONTAINER_DEPLOYMENT = "apb_kieserver_container_deployment";
    public static final String SOURCE_REPOSITORY_URL = "apb_kieserver_source_url";
    public static final String SOURCE_REPOSITORY_REF = "apb_kieserver_source_ref";
    public static final String CONTEXT_DIR = "apb_kieserver_source_context";
    public static final String ARTIFACT_DIR = "apb_kieserver_artifact_dir";
}
