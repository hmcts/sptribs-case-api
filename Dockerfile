# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.6

FROM hmctspublic.azurecr.io/base/java:21-distroless
USER hmcts

COPY build/libs/sptribs-case-api.jar /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/cftlib/definition-snapshots /opt/app/build/cftlib/definition-snapshots

EXPOSE 4013
CMD [ "sptribs-case-api.jar" ]
