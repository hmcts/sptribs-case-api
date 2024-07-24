package uk.gov.hmcts.sptribs.dmn.service;

import feign.FeignException;
import io.restassured.http.Headers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.dmn.Scenario;
import uk.gov.hmcts.sptribs.dmn.client.RoleAssignmentServiceApiClient;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.UserInfo;
import uk.gov.hmcts.sptribs.dmn.domain.entities.role.RoleAssignment;
import uk.gov.hmcts.sptribs.dmn.domain.entities.role.RoleAssignmentResource;
import uk.gov.hmcts.sptribs.dmn.domain.entities.role.enums.RoleType;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.dmn.service.MapValueExtractor.extractOrDefault;
import static uk.gov.hmcts.sptribs.dmn.service.MapValueExtractor.extractOrThrow;
import static uk.gov.hmcts.sptribs.testutil.JsonUtil.toJsonString;

@Component
@Slf4j
public class RoleAssignmentService {

    public static final DateTimeFormatter ROLE_ASSIGNMENT_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

    private static final String DEFAULT_ROLE_ASSIGNMENT_TEMPLATE;

    private final RoleAssignmentServiceApiClient roleAssignmentServiceApi;

    static {
        try {
            Map<String, String> templates = StringResourceLoader.load(
                "/templates/wa/roleAssignment/*.json"
            );
            DEFAULT_ROLE_ASSIGNMENT_TEMPLATE = templates.get("set-organisational-role-assignment-request.json");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default role assignment template", e);
        }
    }

    public RoleAssignmentService(RoleAssignmentServiceApiClient roleAssignmentServiceApi) throws IOException {
        this.roleAssignmentServiceApi = roleAssignmentServiceApi;
    }

    public void processRoleAssignments(Scenario scenario,
                                       Map<String, Object> postRoleAssignmentValues,
                                       String userToken,
                                       String serviceToken,
                                       UserInfo userInfo) throws IOException {

        String caseId = scenario.getAssignedCaseId("defaultCaseId");

        Map<String, Object> roleDataValues = extractOrThrow(postRoleAssignmentValues, "roleData");
        Map<String, Object> replacementsValues = extractOrThrow(roleDataValues, "replacements");

        String jurisdiction = extractOrThrow(replacementsValues, "jurisdiction");
        Map<String, String> templatesByFilename =
            StringResourceLoader.load(
                "/templates/" + jurisdiction.toLowerCase(Locale.ENGLISH) + "/roleAssignment/*.json"
            );
        String templateFilename = extractOrThrow(roleDataValues, "template");
        String template = templatesByFilename.get(templateFilename);

        String roleName = extractOrThrow(replacementsValues, "roleName");
        String caseType = extractOrThrow(replacementsValues, "caseType");
        String grantType = extractOrDefault(replacementsValues, "grantType", "STANDARD");
        String roleType = extractOrThrow(replacementsValues, "roleType");
        String classification = extractOrDefault(replacementsValues, "classification", "PUBLIC");
        String roleCategory = extractOrDefault(replacementsValues, "roleCategory", "LEGAL_OPERATIONS");

        postRoleAssignment(
            caseId,
            userToken,
            serviceToken,
            userInfo.getUid(),
            roleName,
            toJsonString(Map.of(
                "caseId", caseId,
                "caseType", caseType,
                "jurisdiction", jurisdiction,
                "substantive", "Y"
            )),
            template,
            grantType,
            roleCategory,
            toJsonString(List.of()),
            roleType,
            classification,
            "staff-organisational-role-mapping",
            userInfo.getUid(),
            false,
            false,
            null,
            "2020-01-01T00:00:00Z",
            null,
            userInfo.getUid()
        );
    }

    public void setupRoleAssignment(Headers headers, UserInfo userInfo, String roleName) {
        postRoleAssignment(
            null,
            headers.getValue(AUTHORIZATION),
            headers.getValue(SERVICE_AUTHORIZATION),
            userInfo.getUid(),
            roleName,
            toJsonString(Map.of(
                "caseType", "WaCaseType",
                "jurisdiction", "WA",
                "primaryLocation", "765324"
            )),
            DEFAULT_ROLE_ASSIGNMENT_TEMPLATE,
            "STANDARD",
            "LEGAL_OPERATIONS",
            toJsonString(List.of()),
            "ORGANISATION",
            "PUBLIC",
            "staff-organisational-role-mapping",
            userInfo.getUid(),
            false,
            false,
            null,
            "2020-01-01T00:00:00Z",
            null,
            userInfo.getUid()
        );
    }

    public void clearAllRoleAssignments(Headers headers, UserInfo userInfo) {
        String userToken = headers.getValue(AUTHORIZATION);
        String serviceToken = headers.getValue(SERVICE_AUTHORIZATION);

        RoleAssignmentResource response = null;

        try {
            //Retrieve All role assignments
            response = roleAssignmentServiceApi.getRolesForUser(userInfo.getUid(), userToken, serviceToken);

        } catch (FeignException ex) {
            if (ex.status() == HttpStatus.NOT_FOUND.value()) {
                System.out.println("No roles found, nothing to delete.");
            } else {
                ex.printStackTrace();
            }
        }

        if (response != null) {
            //Delete All role assignments
            List<RoleAssignment> organisationalRoleAssignments = response.getRoleAssignmentResponse().stream()
                .filter(assignment -> RoleType.ORGANISATION.equals(assignment.getRoleType()))
                .collect(toList());

            List<RoleAssignment> caseRoleAssignments = response.getRoleAssignmentResponse().stream()
                .filter(assignment -> RoleType.CASE.equals(assignment.getRoleType()))
                .collect(toList());

            //Check if there are 'orphaned' restricted roles
            if (organisationalRoleAssignments.isEmpty() && !caseRoleAssignments.isEmpty()) {
                log.info("Orphaned Restricted role assignments were found.");
                log.info("Creating a temporary role assignment to perform cleanup");
                //Create a temporary organisational role
                setupRoleAssignment(headers, userInfo, "case-allocator");
                //Recursive
                clearAllRoleAssignments(headers, userInfo);
            }

            log.info("Deleting role assignment for user {}", userInfo.getEmail());

            caseRoleAssignments.forEach(
                assignment -> roleAssignmentServiceApi.deleteRoleAssignmentById(assignment.getId(),
                                                                                userToken,
                                                                                serviceToken)
            );

            organisationalRoleAssignments.forEach(
                assignment -> roleAssignmentServiceApi.deleteRoleAssignmentById(assignment.getId(),
                                                                               userToken,
                                                                               serviceToken)
            );
        }
    }

    private void postRoleAssignment(String caseId,
                                    String bearerUserToken,
                                    String s2sToken,
                                    String actorId,
                                    String roleName,
                                    String attributes,
                                    String resourceFile,
                                    String grantType,
                                    String roleCategory,
                                    String authorisations,
                                    String roleType,
                                    String classification,
                                    String process,
                                    String reference,
                                    boolean replaceExisting,
                                    Boolean readOnly,
                                    String notes,
                                    String beginTime,
                                    String endTime,
                                    String assignerId) {

        String body = getBody(caseId, actorId, roleName, resourceFile, attributes, grantType, roleCategory,
                              authorisations, roleType, classification, process, reference, replaceExisting,
                              readOnly, notes, beginTime, endTime, assignerId);

        roleAssignmentServiceApi.createRoleAssignment(
            body,
            bearerUserToken,
            s2sToken
        );
    }

    private String getBody(final String caseId,
                           String actorId,
                           final String roleName,
                           final String resourceFile,
                           final String attributes,
                           final String grantType,
                           String roleCategory,
                           String authorisations,
                           String roleType,
                           String classification,
                           String process,
                           String reference,
                           boolean replaceExisting,
                           Boolean readOnly,
                           String notes,
                           String beginTime,
                           String endTime,
                           String assignerId) {

        String assignmentRequestBody = null;

        assignmentRequestBody = resourceFile;
        assignmentRequestBody = assignmentRequestBody.replace("{ACTOR_ID_PLACEHOLDER}", actorId);
        assignmentRequestBody = assignmentRequestBody.replace("{ROLE_NAME_PLACEHOLDER}", roleName);
        assignmentRequestBody = assignmentRequestBody.replace("{GRANT_TYPE}", grantType);
        assignmentRequestBody = assignmentRequestBody.replace("{ROLE_CATEGORY}", roleCategory);
        assignmentRequestBody = assignmentRequestBody.replace("{ROLE_TYPE}", roleType);
        assignmentRequestBody = assignmentRequestBody.replace("{CLASSIFICATION}", classification);
        assignmentRequestBody = assignmentRequestBody.replace("{PROCESS}", process);
        assignmentRequestBody = assignmentRequestBody.replace("{ASSIGNER_ID_PLACEHOLDER}", assignerId);

        assignmentRequestBody = assignmentRequestBody.replace(
            "\"replaceExisting\": \"{REPLACE_EXISTING}\"",
            String.format("\"replaceExisting\": %s", replaceExisting)
        );

        if (beginTime != null) {
            assignmentRequestBody = assignmentRequestBody.replace(
                "{BEGIN_TIME_PLACEHOLDER}",
                beginTime
            );
        } else {
            assignmentRequestBody = assignmentRequestBody
                .replace(",\n" + "      \"beginTime\": \"{BEGIN_TIME_PLACEHOLDER}\"", "");
        }

        if (endTime != null) {
            assignmentRequestBody = assignmentRequestBody.replace(
                "{END_TIME_PLACEHOLDER}",
                endTime
            );
        } else {
            assignmentRequestBody = assignmentRequestBody.replace(
                "{END_TIME_PLACEHOLDER}",
                ZonedDateTime.now().plusHours(2).format(ROLE_ASSIGNMENT_DATA_TIME_FORMATTER)
            );
        }

        if (attributes != null) {
            assignmentRequestBody = assignmentRequestBody
                .replace("\"{ATTRIBUTES_PLACEHOLDER}\"", attributes);
        }

        if (caseId != null) {
            assignmentRequestBody = assignmentRequestBody.replace("{CASE_ID_PLACEHOLDER}", caseId);
        }

        assignmentRequestBody = assignmentRequestBody.replace("{REFERENCE}", reference);


        if (notes != null) {
            assignmentRequestBody = assignmentRequestBody.replace(
                "\"notes\": \"{NOTES}\"",
                String.format("\"notes\": [%s]", notes)
            );
        } else {
            assignmentRequestBody = assignmentRequestBody
                .replace(",\n" + "      \"notes\": \"{NOTES}\"", "");
        }

        if (readOnly != null) {
            assignmentRequestBody = assignmentRequestBody.replace(
                "\"readOnly\": \"{READ_ONLY}\"",
                String.format("\"readOnly\": %s", readOnly)
            );
        } else {
            assignmentRequestBody = assignmentRequestBody
                .replace(",\n" + "      \"readOnly\": \"{READ_ONLY}\"", "");
        }

        if (authorisations != null) {
            assignmentRequestBody = assignmentRequestBody.replace("\"{AUTHORISATIONS}\"", authorisations);
        } else {
            assignmentRequestBody = assignmentRequestBody
                .replace(",\n" + "      \"authorisations\": \"{AUTHORISATIONS}\"", "");
        }

        return assignmentRequestBody;
    }
}
