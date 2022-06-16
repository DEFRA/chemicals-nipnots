@Library('jenkins-shared-library')_
def helper = new helpers.PipelineHelper(this);

node(label: 'autoSlaveLive') {
    def URL = "reach-nipnots"
    def RESOURCE = "SNDCHMINFRGP001-${URL}-${helper.getEnvSuffix()}"
    def APP = "${URL}-${helper.getEnvSuffix()}"
    def AI = "SNDCHMINFRGP001-${helper.getEnvSuffix()}"
    def DATABASE_DB_USER = "SA-AZURE-CHM-SQL-SND@Defra.onmicrosoft.com"
    def KEY_VAULT_NAME = "SECCHMINFKVT001"
    def DATABASE_HOST_NAME = "${helper.getDatabaseServerName()}.database.windows.net"
    def DATABASE_DB_PORT = 1433
    def DATABASE_ADMIN_PASS_NAME_KEY_LOOKUP = "sndSqlDBAdminPass"
    def DB_URL = "jdbc:sqlserver://${DATABASE_HOST_NAME}:${DATABASE_DB_PORT};database=$APP;encrypt=true;schema=dbo;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;authentication=ActiveDirectoryPassword;loginTimeout=30"

    def environmentVariables = [
            "APP_NAME=${APP}",
            "SERVICE_NAME=REACH Nipnots",
            "URL_PATH=${URL}",
            "ACR_REPO=reach-nipnots/reach-nipnots",
            "DB_URL=${DB_URL}",
            "DB_USER=${DATABASE_DB_USER}",
            "DB_PASSWORD=${helper.getKeyVaultPassword(KEY_VAULT_NAME, DATABASE_ADMIN_PASS_NAME_KEY_LOOKUP)}",
            "CONNECTION_STRING=HTTP_REACH_NIPNOTS_PORT='8100' REACH_MONITORING_URL='https://reach-monitoring-dev.azurewebsites.net' CHEMICAL_REGULATIONS_URL='https://comply-chemical-regulations.service.gov.uk/' REACH_NOTIFY_URL='https://reach-notify-dev.azurewebsites.net' DB_REACH_NIPNOTS_URL='${DB_URL}' DB_USER='${DATABASE_DB_USER}' DB_PASSWORD='${helper.getKeyVaultPassword(KEY_VAULT_NAME, DATABASE_ADMIN_PASS_NAME_KEY_LOOKUP)}' AUDIT_API='https://reach-audit-dev.azurewebsites.net' API='https://reach-develop.azurewebsites.net' FILE_SERVICE_URL='https://reach-file-service-dev.azurewebsites.net' JWT_SECRET_KEY='MySecretKey'",
            "REACH_SUBMISSION_ID_PREFIX=TEST",
            "AI_NAME=${AI}",
            "RESOURCE_GROUP=${RESOURCE}",
            "BACKEND_PLAN=SNDCHMINFRGP001-${URL}-${helper.getEnvSuffix()}-service-plan",
            "PROJECT_REPO_URL=https://giteux.azure.defra.cloud/chemicals/reach-nipnots.git",
            "SET_APP_LOGGING=false",
            "RUN_SONAR=true",
            "API=https://reach-develop.azurewebsites.net"
    ]

    withEnv(environmentVariables) {
        def databases = [APP]
        def STORAGE_CONTAINERS = []
        def runIntegrationTests = {
            withMaven(
                    options: [artifactsPublisher(disabled: true), jacocoPublisher(disabled: true)], mavenOpts: helper.getMavenOpts()
            ) {
                sh(label: "Run e2e tests", script: "mvn verify -P e2e-tests -DREACH_MONITORING_URL=https://reach-monitoring-dev.azurewebsites.net -DCHEMICAL_REGULATIONS_URL=https://comply-chemical-regulations.service.gov.uk/ -DREACH_NOTIFY_URL=https://reach-notify-dev.azurewebsites.net -DFILE_SERVICE_URL=https://reach-file-service-dev.azurewebsites.net -DNIPNOTS_SERVICE_URL=https://${APP_NAME}.${APPLICATION_URL_SUFFIX}")
            }
        }
        reachPipeline(databases, STORAGE_CONTAINERS, runIntegrationTests)
    }
}
