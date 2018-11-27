package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.service.FormService;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static com.github.cimsbioko.server.webapi.odk.Constants.FORM_DEF_FILE;
import static com.github.cimsbioko.server.webapi.odk.Constants.XLSFORM_DEF_FILE;

@Controller
public class FormsController {

    private FormRepository repo;
    private FormService service;

    @Autowired
    public FormsController(FormRepository repo, FormService service) {
        this.repo = repo;
        this.service = service;
    }

    @PreAuthorize("hasAuthority('VIEW_FORMS')")
    @GetMapping("/forms")
    @ResponseBody
    public Page<Form> forms(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return repo.findAll(PageRequest.of(page, 10, Sort.by("formId")));
    }

    @PreAuthorize("hasAuthority('FORM_UPLOAD')")
    @PostMapping("/uploadXmlForm")
    @ResponseBody
    public ResponseEntity uploadForm(@RequestParam(FORM_DEF_FILE) MultipartFile xmlForm,
                           @RequestParam(value = XLSFORM_DEF_FILE, required = false) MultipartFile xlsform,
                           MultipartHttpServletRequest req) throws JDOMException, IOException, NoSuchAlgorithmException {
        service.uploadForm(xmlForm, xlsform, req.getMultiFileMap());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('FORM_UPLOAD_XLS')")
    @PostMapping(value = "/uploadXlsForm")
    @ResponseBody
    public ResponseEntity uploadForm(@RequestParam(value = XLSFORM_DEF_FILE) MultipartFile xlsform, MultipartHttpServletRequest req)
            throws JDOMException, IOException, NoSuchAlgorithmException {
        service.uploadXlsform(xlsform, req.getMultiFileMap());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('EXPORT_FORMS')")
    @GetMapping("/forms/export/{id}/{version}")
    public void exportForm(@PathVariable("id") String id, @PathVariable("version") String version,
                           HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s-%s.zip", id, version));
        service.exportToStream(id, version, response.getOutputStream());
    }

    @PreAuthorize("hasAuthority('MANAGE_FORMS')")
    @PatchMapping("/forms/manage/{id}/{version}")
    @ResponseBody
    public ResponseEntity manageForm(@PathVariable("id") String id, @PathVariable("version") String version, @RequestBody ManageForm form) {
        service.manageForm(id, version, form.isDownloads(), form.isSubmissions());
        return ResponseEntity.noContent().build();
    }

    static class ManageForm {

        boolean downloads;
        boolean submissions;

        public boolean isDownloads() {
            return downloads;
        }

        public void setDownloads(boolean downloads) {
            this.downloads = downloads;
        }

        public boolean isSubmissions() {
            return submissions;
        }

        public void setSubmissions(boolean submissions) {
            this.submissions = submissions;
        }
    }
}
