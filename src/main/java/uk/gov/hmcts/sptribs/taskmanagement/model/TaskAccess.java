package uk.gov.hmcts.sptribs.taskmanagement.model;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPermission;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static uk.gov.hmcts.sptribs.taskmanagement.model.RoleCategory.ADMIN;
import static uk.gov.hmcts.sptribs.taskmanagement.model.RoleCategory.JUDICIAL;
import static uk.gov.hmcts.sptribs.taskmanagement.model.RoleCategory.LEGAL_OPERATIONS;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Assign;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Cancel;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Claim;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Complete;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Execute;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Manage;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Own;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Read;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Unassign;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.Unclaim;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskOperation.UnclaimAssign;

@Getter
public enum TaskAccess {
    REGIONAL_CENTRE_ADMIN(
        Set.of(Read,Own,Claim,Unclaim,Manage,Complete), ADMIN,
        false, 1,
        Authorisations.NONE),
    REGIONAL_CENTRE_TEAM_LEADER(
        Set.of(Read,Own,Claim,Unclaim,Manage,UnclaimAssign,Assign,Unassign,Cancel,Complete),
        ADMIN, false, 2,
        Authorisations.NONE),
    REGIONAL_CENTRE_TEAM_LEADER_SPECIFIC_ACCESS(
        Set.of(Read,Own,Claim,Manage,Assign,Unassign,Complete,Cancel),
        ADMIN, false,1,
        Authorisations.NONE),
    HEARING_CENTRE_ADMIN(
        Set.of(Read,Own,Claim,Unclaim,Manage,Complete),
        ADMIN, false,1,
        Authorisations.NONE),
    HEARING_CENTRE_TEAM_LEADER(
        Set.of(Read,Own,Claim,Unclaim,Manage,UnclaimAssign,Assign,Unassign,Cancel,Complete),
        ADMIN, false, 2,
        Authorisations.NONE),
    HEARING_CENTRE_TEAM_LEADER_SPECIFIC_ACCESS(
        Set.of(Read,Own,Claim,Manage,Assign,Unassign,Complete,Cancel),
        ADMIN, false,1,
        Authorisations.NONE),
    SENIOR_TRIBUNAL_CASEWORKER(
        Set.of(Read,Own,Claim,Manage,Assign,Unassign,Complete,Cancel),
        LEGAL_OPERATIONS, false, 1,
        Authorisations.NONE),
    TRIBUNAL_CASEWORKER(
        Set.of(Read,Own,Claim,Assign,Unassign,Complete,Cancel),
        LEGAL_OPERATIONS, false, 2,
        Authorisations.NONE),
    SENIOR_JUDGE(
        Set.of(Read,Execute,Claim,Manage,Assign,Unassign,Complete,Cancel),
        JUDICIAL,
        false, 1,
        Authorisations.JUDICIAL),
    JUDGE(
        Set.of(Read,Own,Claim,Assign,Unassign,Complete,Cancel),
        JUDICIAL, false, 2,
        Authorisations.JUDICIAL),
    LEADERSHIP_JUDGE(
        Set.of(Read,Own,Claim,Manage,Assign,Unassign,Complete,Cancel),
        JUDICIAL, false, 1,
        Authorisations.JUDICIAL),
    CTSC(
        Set.of(Read,Own,Claim,Unclaim,Manage,Complete),
        RoleCategory.CTSC, false, 1,
        Authorisations.NONE),
    CTSC_TEAM_LEADER(
        Set.of(Read,Own,Claim,Unclaim,Manage,UnclaimAssign,Assign,Unassign,Cancel,Complete),
        RoleCategory.CTSC, false, 2,
        Authorisations.NONE),
    CTSC_TEAM_LEADER_SPECIFIC_ACCESS(
        Set.of(Read,Own,Claim,Manage,Assign,Unassign,Complete,Cancel),
        RoleCategory.CTSC, false, 1,
        Authorisations.NONE);

    private final Set<TaskOperation> permissions;
    private final RoleCategory roleCategory;
    private final boolean autoAssignable;
    private final int assignmentPriority;
    private final Authorisations authorisations;

    TaskAccess(
        Set<TaskOperation> permissions,
        RoleCategory roleCategory,
        boolean autoAssignable,
        int assignmentPriority,
        Authorisations authorisations
    ) {
        this.permissions = permissions;
        this.roleCategory = roleCategory;
        this.autoAssignable = autoAssignable;
        this.assignmentPriority = assignmentPriority;
        this.authorisations = authorisations;
    }

    public TaskPermission toTaskPermission() {
        String roleName = name()
            .toLowerCase(Locale.UK)
            .replace("_specific_access", "")
            .replace('_', '-');
        List<String> authorisationsList = authorisations == Authorisations.NONE
            ? Collections.emptyList()
            : List.of(authorisations.getAuthorisation());

        return TaskPermission.builder()
            .roleName(roleName)
            .roleCategory(roleCategory.name())
            .permissions(permissions.stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(TaskOperation::name)
                .toList())
            .authorisations(authorisationsList)
            .assignmentPriority(assignmentPriority)
            .autoAssignable(autoAssignable)
            .build();
    }
}


