package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType.CREATE_AND_SEND_NEW_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_TO_ANONYMISE_CASE;

@RequiredArgsConstructor
@Component
public class ApplyAnonymity implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "LabelCaseworkerApplyAnonymity!=\"\"";
    private final AnonymisationService anonymisationService;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerApplyAnonymity", this::midEvent)
                .pageLabel("Anonymity")
                .label("LabelCaseworkerApplyAnonymity", "")
                .complex(CaseData::getCicCase)
                    .readonly(CicCase::getFullName, ALWAYS_HIDE)
                    .readonly(CicCase::getAnonymisedAppellantName, ALWAYS_HIDE)
                    .readonly(CicCase::getAnonymityAlreadyApplied, ALWAYS_HIDE)
                    .readonly(CicCase::getAnonymisationDate, ALWAYS_HIDE)
                    .mandatory(CicCase::getAnonymiseYesOrNo)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDetails,
                                                                   CaseDetails<CaseData, State> caseDetailsBefore) {
        final CaseData caseData = caseDetails.getData();
        final CicCase cicCase = caseData.getCicCase();
        final List<String> errors = new ArrayList<>();
        boolean firstTimeAnonymisationJourney = isFirstTimeAnonymisationJourney(cicCase);
        applyAnonymitySelection(cicCase, errors, !firstTimeAnonymisationJourney);
        updateIssuingAndTemplateOptions(caseData, firstTimeAnonymisationJourney);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

    private static boolean isFirstTimeAnonymisationJourney(CicCase cicCase) {
        return !YesOrNo.YES.equals(cicCase.getAnonymityAlreadyApplied())
            && YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo());
    }

    private static void updateIssuingAndTemplateOptions(CaseData caseData, boolean firstTimeAnonymisationJourney) {
        CicCase cicCase = caseData.getCicCase();
        if (firstTimeAnonymisationJourney) {
            DynamicList restrictedIssueOptions = DynamicListUtil.createDynamicListFromEnumSet(
                EnumSet.of(CREATE_AND_SEND_NEW_ORDER),
                OrderIssuingType::getLabel,
                CREATE_AND_SEND_NEW_ORDER);
            cicCase.setOrderIssuingDynamicRadioList(restrictedIssueOptions);

            DynamicList restrictedTemplateOptions = DynamicListUtil.createDynamicListFromEnumSet(
                EnumSet.of(OrderTemplate.CIC6_GENERAL_DIRECTIONS),
                OrderTemplate::getLabel,
                OrderTemplate.CIC6_GENERAL_DIRECTIONS);
            cicCase.setTemplateDynamicList(restrictedTemplateOptions);
            return;
        }

        DynamicList allIssueOptions = DynamicListUtil.createDynamicListFromEnumSet(
            EnumSet.of(CREATE_AND_SEND_NEW_ORDER, UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER),
            OrderIssuingType::getLabel,
            cicCase.getOrderIssuingType());
        cicCase.setOrderIssuingDynamicRadioList(allIssueOptions);

        DynamicList allTemplateOptions = DynamicListUtil.createDynamicListFromEnumSet(
            EnumSet.allOf(OrderTemplate.class),
            OrderTemplate::getLabel,
            caseData.getDraftOrderContentCIC().getOrderTemplate());
        cicCase.setTemplateDynamicList(allTemplateOptions);
    }

    public void applyAnonymitySelection(CicCase cicCase, List<String> errors) {
        applyAnonymitySelection(cicCase, errors, true);
    }

    public void applyAnonymitySelection(CicCase cicCase, List<String> errors, boolean updateAnonymityAlreadyApplied) {
        if (YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo())) {
            if (cicCase.getAnonymisedAppellantName() == null) {
                String anonymisedName = anonymisationService.getOrCreateAnonymisation();

                if (anonymisedName == null) {
                    errors.add(FAILED_TO_ANONYMISE_CASE);
                    return;
                }
                cicCase.setAnonymisedAppellantName(anonymisedName);
            }
            if (cicCase.getAnonymisationDate() == null) {
                cicCase.setAnonymisationDate(LocalDate.now());
            }
            if (updateAnonymityAlreadyApplied) {
                cicCase.setAnonymityAlreadyApplied(YesOrNo.YES);
            }
            return;
        }

        if (YesOrNo.NO.equals(cicCase.getAnonymiseYesOrNo())) {
            cicCase.setAnonymisationDate(null);
            if (updateAnonymityAlreadyApplied) {
                cicCase.setAnonymityAlreadyApplied(YesOrNo.NO);
            }
        }
    }
}
