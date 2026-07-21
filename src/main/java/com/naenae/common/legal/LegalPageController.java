package com.naenae.common.legal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalPageController {

    private final String serviceName;
    private final String operatorName;
    private final String contactEmail;

    public LegalPageController(
            @Value("${app.legal.service-name}") String serviceName,
            @Value("${app.legal.operator-name}") String operatorName,
            @Value("${app.legal.contact-email}") String contactEmail
    ) {
        this.serviceName = serviceName;
        this.operatorName = operatorName;
        this.contactEmail = contactEmail;
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        common(model, LegalDocumentVersions.TERMS);
        return "legal/terms";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        common(model, LegalDocumentVersions.PRIVACY);
        return "legal/privacy";
    }

    private void common(Model model, String version) {
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("operatorName", operatorName);
        model.addAttribute("contactEmail", contactEmail);
        model.addAttribute("documentVersion", version);
    }
}
