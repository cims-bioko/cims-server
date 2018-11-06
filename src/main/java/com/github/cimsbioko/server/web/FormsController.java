package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.service.FormService;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView forms(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return new ModelAndView("forms", "forms", repo.findAll(PageRequest.of(page, 10)));
    }

    @PreAuthorize("hasAuthority('FORM_UPLOAD')")
    @PostMapping("/uploadXmlForm")
    @ResponseBody
    public void uploadForm(@RequestParam(FORM_DEF_FILE) MultipartFile xmlForm,
                           @RequestParam(value = XLSFORM_DEF_FILE, required = false) MultipartFile xlsform,
                           MultipartHttpServletRequest req) throws JDOMException, IOException, NoSuchAlgorithmException {
        service.uploadForm(xmlForm, xlsform, req.getMultiFileMap());
    }

    @PreAuthorize("hasAuthority('FORM_UPLOAD_XLS')")
    @PostMapping(value = "/uploadXlsForm")
    @ResponseBody
    public void uploadForm(@RequestParam(value = XLSFORM_DEF_FILE) MultipartFile xlsform, MultipartHttpServletRequest req)
            throws JDOMException, IOException, NoSuchAlgorithmException {
        service.uploadXlsform(xlsform, req.getMultiFileMap());
    }
}
