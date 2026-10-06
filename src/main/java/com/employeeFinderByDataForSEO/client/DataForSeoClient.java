package com.employeeFinderByDataForSEO.client;

import com.employeeFinderByDataForSEO.dto.DataForSeoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

@Component
public class DataForSeoClient {

    private static final Logger log =
            LoggerFactory.getLogger(DataForSeoClient.class);

    private final RestClient restClient;

    /**
     * Running total of DataForSEO cost incurred by this
     * running instance. Not persisted - just a sanity
     * check / early warning signal in logs. For real
     * spend tracking, persist this per-call to the DB
     * instead.
     */
    private final DoubleAdder totalCost = new DoubleAdder();

    public DataForSeoClient(
            RestClient.Builder builder,
            @Value("${dataforseo.login}") String login,
            @Value("${dataforseo.password}") String password) {

        this.restClient = builder
                .baseUrl("https://api.dataforseo.com")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(login, password))
                .build();
    }

    /**
     * Searches DataForSEO's Google organic SERP endpoint for
     * LinkedIn profile results matching the given company
     * (optionally narrowed by a job-title search group).
     *
     * Never throws on remote failure - returns an empty list
     * and logs the problem instead, so a single bad call
     * doesn't 500 the whole request.
     */
    public List<DataForSeoResponse.Item> search(
            String company,
            String searchGroup) {

        String keyword = buildKeyword(company, searchGroup);

        log.info("DataForSEO keyword: {}", keyword);

        Map<String, Object> task = Map.of(
                "keyword", keyword,
                "location_name", "India",
                "language_code", "en",
                "device", "desktop",
                "depth", 100
        );

        DataForSeoResponse response;

        try {

            response = restClient.post()
                    .uri("/v3/serp/google/organic/live/regular")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(task))
                    .retrieve()
                    .body(DataForSeoResponse.class);

        } catch (RestClientException ex) {

            log.error(
                    "DataForSEO call failed for company='{}' group='{}': {}",
                    company,
                    searchGroup,
                    ex.getMessage()
            );

            return Collections.emptyList();
        }

        if (response == null) {

            log.warn("DataForSEO returned a null response body");
            return Collections.emptyList();
        }

        if (response.getStatus_code() != 20000) {

            log.warn(
                    "DataForSEO returned non-success status {} ({})",
                    response.getStatus_code(),
                    response.getStatus_message()
            );

            return Collections.emptyList();
        }

        totalCost.add(response.getCost());

        log.info(
                "DataForSEO call cost: {} | running total: {}",
                response.getCost(),
                totalCost.sum()
        );

        if (response.getTasks() == null
                || response.getTasks().isEmpty()) {

            return Collections.emptyList();
        }

        DataForSeoResponse.Task firstTask =
                response.getTasks().get(0);

        if (firstTask.getResult() == null
                || firstTask.getResult().isEmpty()) {

            return Collections.emptyList();
        }

        DataForSeoResponse.Result firstResult =
                firstTask.getResult().get(0);

        if (firstResult.getItems() == null) {

            return Collections.emptyList();
        }

        return firstResult.getItems()
                .stream()
                .filter(item ->
                        "organic".equals(item.getType()))
                .filter(item ->
                        item.getUrl() != null)
                .filter(item ->
                        item.getUrl().contains("linkedin.com/in/"))
                .toList();
    }

    private String buildKeyword(String company, String searchGroup) {

        if (searchGroup == null || searchGroup.isBlank()) {

            return "site:linkedin.com/in/ \"" + company + "\"";
        }

        return "site:linkedin.com/in/ \""
                + company
                + "\" \""
                + searchGroup
                + "\"";
    }
}
