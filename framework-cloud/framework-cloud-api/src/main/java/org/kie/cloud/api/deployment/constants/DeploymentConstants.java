/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.api.deployment.constants;

import org.kie.cloud.api.constants.Constants;
import org.kie.cloud.api.constants.TestInfoPrinter;

public class DeploymentConstants implements Constants {

    static {
        TestInfoPrinter.printTestConstants();
    }

    public static final String HIBERNATE_PERSISTENCE_DIALECT = "hibernate.dialect";

    public static final String APP_USER = "kie.app.user";
    public static final String APP_PASSWORD = "kie.app.password";
    public static final String APP_CREDENTIALS_SECRET_NAME = "kie.app.credentials-secret-name";

    public static final String AMQ_USERNAME = "amq.username";
    public static final String AMQ_PASSWORD = "amq.password";

    public static final String DATABASE_HOST = "db.hostname";
    public static final String DATABASE_PORT = "db.port";
    public static final String DATABASE_DRIVER = "db.driver";
    public static final String DATABASE_NAME = "database.name";
    public static final String EXTERNAL_DATABASE_NAME = "db.name";
    public static final String DATABASE_USERNAME = "db.username";
    public static final String DATABASE_PASSWORD = "db.password";
    public static final String DATABASE_URL = "db.jdbc_url";

    public static final String SSO_ADMIN_USERNAME = "sso.admin.username";
    public static final String SSO_ADMIN_PASSWORD = "sso.admin.password";
    public static final String SSO_SERVICE_USERNAME =  "sso.service.username";
    public static final String SSO_SERVICE_PASSWORD = "sso.service.password";

    public static final String SSO_REALM = "sso.app.realm";

    public static final String DEFAULT_DOMAIN_SUFFIX = "default.domain.suffix";

    public static final String KIE_ARTIFACT_VERSION = "kie.artifact.version";

    public static final String TRUSTED_KEYSTORE_ALIAS = "trusted.keystore.alias";
    public static final String TRUSTED_KEYSTORE_PWD = "trusted.keystore.pwd";
    public static final String CERTIFICATE_DIR = "certificate.dir";

    public static String getAppUser() {
        return System.getProperty(APP_USER);
    }

    public static String getAppPassword() {
        return System.getProperty(APP_PASSWORD);
    }

    public static String getAppCredentialsSecretName() {
        return System.getProperty(APP_CREDENTIALS_SECRET_NAME);
    }

    public static String getAmqUsername() {
        return System.getProperty(AMQ_USERNAME);
    }

    public static String getAmqPassword() {
        return System.getProperty(AMQ_PASSWORD);
    }

    public static String getDatabaseHost() {
        return System.getProperty(DATABASE_HOST);
    }

    public static String getDatabasePort() {
        return System.getProperty(DATABASE_PORT);
    }

    public static String getDatabaseDriver() {
        return System.getProperty(DATABASE_DRIVER);
    }

    public static String getDatabaseName() {
        return System.getProperty(DATABASE_NAME);
    }

    public static String getExternalDatabaseName() {
        return System.getProperty(EXTERNAL_DATABASE_NAME);
    }

    public static String getDatabaseUsername() {
        return System.getProperty(DATABASE_USERNAME);
    }

    public static String getDatabasePassword() {
        return System.getProperty(DATABASE_PASSWORD);
    }

    public static String getDatabaseUrl() {
        return System.getProperty(DATABASE_URL);
    }

    public static String getHibernatePersistenceDialect() {
        return System.getProperty(HIBERNATE_PERSISTENCE_DIALECT);
    }

    public static String getSsoAdminUser() {
        return System.getProperty(SSO_ADMIN_USERNAME);
    }

    public static String getSsoAdminPassword() {
        return System.getProperty(SSO_ADMIN_PASSWORD);
    }

    public static String getSsoServiceUser() {
        return System.getProperty(SSO_SERVICE_USERNAME);
    }

    public static String getSsoServicePassword() {
        return System.getProperty(SSO_SERVICE_PASSWORD);
    }

    public static String getSsoRealm() {
        return System.getProperty(SSO_REALM);
    }
    public static String getDefaultDomainSuffix() {
        return System.getProperty(DEFAULT_DOMAIN_SUFFIX);
    }

    public static String getKieArtifactVersion() {
        return System.getProperty(KIE_ARTIFACT_VERSION);
    }

    public static String getTrustedKeystoreAlias() {
        return System.getProperty(TRUSTED_KEYSTORE_ALIAS);
    }

    public static String getTrustedKeystorePwd() {
        return System.getProperty(TRUSTED_KEYSTORE_PWD);
    }

    public static String getCertificateDir() {
        return System.getProperty(CERTIFICATE_DIR);
    }

    @Override
    public void initConfigProperties() {
        // Nothing to init here.
    }
}
