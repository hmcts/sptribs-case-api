package uk.gov.hmcts.sptribs.systemupdate.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;

@Service
@Slf4j
public class CcdSearchService {

    public static final String DUE_DATE = "data.dueDate";
    public static final String STATE = "state";

    @Value("${core_case_data.search.page_size}")
    private int pageSize;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    public List<CaseDetails> searchForAllCasesWithQuery(
        final BoolQueryBuilder query, User user, String serviceAuth, final State... states) {

        final List<CaseDetails> allCaseDetails = new ArrayList<>();
        int from = 0;
        int totalResults = pageSize;

        try {
            while (totalResults == pageSize) {
                final SearchResult searchResult =
                    searchForCasesWithQuery(from, pageSize, query, user, serviceAuth);

                allCaseDetails.addAll(searchResult.getCases());

                from += pageSize;
                totalResults = searchResult.getTotal();
            }
        } catch (final FeignException e) {
            final String message = String.format("Failed to complete search for Cases with state of %s", Arrays.toString(states));
            log.info(message, e);
            throw new CcdSearchCaseException(message, e);
        }
        return allCaseDetails;
    }


    public List<CaseDetails> searchForCasesWithVersionLessThan(int latestVersion, User user, String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(boolQuery()
                        .must(rangeQuery("data.dataVersion").lt(latestVersion)))
            )
            .from(0)
            .size(500);
        log.info("Query:" + sourceBuilder.toString());
        log.info("CaseTypeName:" + CcdCaseType.CIC.getCaseTypeName());
        final List<CaseDetails> cases = coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()
        ).getCases();
        log.info("Cases:" + cases.size());
        return cases;
    }

    public List<CaseDetails> searchForCases(
        final List<String> caseReferences,
        final User user,
        final String serviceAuth) {

        final QueryBuilder bulkCaseDetailsExist = termsQuery("reference", caseReferences);
        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .query(
                boolQuery()
                    .must(bulkCaseDetailsExist)
            )
            .from(0)
            .size(50);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString()
        ).getCases();
    }

    private SearchResult searchForCasesWithQuery(final int from,
                                                 final int size,
                                                 final BoolQueryBuilder query,
                                                 final User user,
                                                 final String serviceAuth) {

        final SearchSourceBuilder sourceBuilder = SearchSourceBuilder
            .searchSource()
            .sort(DUE_DATE, ASC)
            .query(query)
            .from(from)
            .size(size);

        return coreCaseDataApi.searchCases(
            user.getAuthToken(),
            serviceAuth,
            CcdCaseType.CIC.getCaseTypeName(),
            sourceBuilder.toString());
    }
}
