package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.ErrorRepository;
import com.github.cimsbioko.server.dao.FormSubmissionRepository;
import com.github.cimsbioko.server.domain.FormSubmission;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.github.cimsbioko.server.util.JDOMUtil.stringFromDoc;

@Controller
public class SubmissionsController {

    private final FormSubmissionRepository submissionsRepo;
    private final ErrorRepository errorRepo;
    private final MessageSource messages;

    public SubmissionsController(FormSubmissionRepository submissionsRepo, ErrorRepository errorRepo, MessageSource messages) {
        this.submissionsRepo = submissionsRepo;
        this.errorRepo = errorRepo;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/submissions")
    @ResponseBody
    public Page<FormSubmission> submissions(
            @RequestParam(name = "p", defaultValue = "0") Integer page,
            @RequestParam(name = "q", defaultValue = "") String query) {
        if (!query.isEmpty()) {
            return submissionsRepo.findBySearch(query, PageRequest.of(page, 10));
        } else {
            return submissionsRepo.findAll(PageRequest.of(page, 10, Sort.Direction.DESC, "submitted"));
        }
    }

    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/submissions/xml/{instanceId}")
    @ResponseBody
    public ResponseEntity<?> submissionXml(@PathVariable("instanceId") String instanceId) {
        return submissionsRepo
                .findById(instanceId)
                .map(fs -> stringFromDoc(fs.getXml(), true))
                .map(xml -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(xml))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/submissions/error/{instanceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> lastSubmissionError(@PathVariable("instanceId") String instanceId) {
        return errorRepo
                .findFirstBySubmissionInstanceIdOrderByCreatedDesc(instanceId)
                .map(msg -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", msg.getMessage());
                    result.put("created", msg.getCreated());
                    return ResponseEntity.ok().body(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('REPROCESS_SUBMISSIONS')")
    @PostMapping("submissions/{instanceId}/reprocess")
    @ResponseBody
    public ResponseEntity<?> reprocessSubmission(@PathVariable("instanceId") String instanceId, Locale locale) {
        return submissionsRepo.findById(instanceId).map(submission -> {
            submission.setProcessed(null);
            submissionsRepo.save(submission);
            return ResponseEntity.ok(new AjaxResult().addMessage(resolveMessage("submissions.msg.reprocessing", locale, instanceId)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('DELETE_SUBMISSIONS')")
    @DeleteMapping("/submissions/{uuid}")
    @ResponseBody
    public ResponseEntity<?> deleteRole(@PathVariable("uuid") String uuid, Locale locale) {
        submissionsRepo.deleteById(uuid);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("submissions.msg.deleted", locale, uuid)));
    }


    private String resolveMessage(String key, Locale locale, Object... args) {
        return messages.getMessage(key, args, locale);
    }

}
