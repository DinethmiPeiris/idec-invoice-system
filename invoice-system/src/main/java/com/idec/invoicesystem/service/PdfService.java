package com.idec.invoicesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generatePdf(String templateName, Context context) throws IOException {
        String htmlContent = templateEngine.process(templateName, context);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            
            // Register Castelar system font for premium outlined serif typography
            try {
                renderer.getFontResolver().addFont("C:/Windows/Fonts/CASTELAR.TTF", "Identity-H", true);
            } catch (Exception e) {
                System.out.println("Warning: Could not register CASTELAR.TTF font. Fallback will be used. Error: " + e.getMessage());
            }
            
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        }
    }
}
