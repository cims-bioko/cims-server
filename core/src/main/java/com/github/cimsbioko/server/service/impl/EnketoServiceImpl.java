package com.github.cimsbioko.server.service.impl;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.cimsbioko.server.dao.FormSubmissionRepository;
import com.github.cimsbioko.server.service.EnketoService;
import com.github.cimsbioko.server.util.JDOMUtil;
import com.github.cimsbioko.server.webapi.odk.EndpointHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;

public class EnketoServiceImpl implements EnketoService {

    public static final String SERVER_URL = "server_url";
    public static final String FORM_ID = "form_id";
    public static final String INSTANCE_ID = "instance_id";
    public static final String INSTANCE = "instance";

    private final RestTemplate template;

    private final URI enketoApiUri;

    private final EndpointHelper endpointHelper;

    private final FormSubmissionRepository submissionDao;

    public EnketoServiceImpl(URI enketoApiUri, String enketoApiKey, EndpointHelper endpointHelper, RestTemplate template,
                             FormSubmissionRepository submissionDao) {
        this.template = template;
        template.getInterceptors().add(new BasicAuthenticationInterceptor(enketoApiKey, ""));
        this.enketoApiUri = enketoApiUri;
        this.endpointHelper = endpointHelper;
        this.submissionDao = submissionDao;
    }

    static class Response {

        @JsonAlias("edit_url")
        private String url;
        private int code;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    @Override
    public Optional<URI> editSubmission(HttpServletRequest req, String instanceId) {
        return submissionDao
                .findById(instanceId)
                .flatMap(submission -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
                    data.add(SERVER_URL, endpointHelper.contextRelativeUrl(req));
                    data.add(FORM_ID, submission.getFormId());
                    data.add(INSTANCE_ID, submission.getInstanceId());
                    data.add(INSTANCE, JDOMUtil.stringFromDoc(submission.getXml(), false, false));
                    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(data, headers);
                    Response response = template.postForObject(enketoApiUri, request, Response.class);
                    return Optional.ofNullable(response).map(f -> URI.create(f.url));
                });
    }
}