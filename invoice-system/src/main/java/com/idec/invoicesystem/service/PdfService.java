package com.idec.invoicesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generatePdf(String templateName, Context context) throws IOException {
        String htmlContent = templateEngine.process(templateName, context);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // Try loading Castelar font from classpath (fonts/CASTELAR.TTF) first,
            // then fall back to the Windows system font path.
            boolean fontLoaded = false;
            try {
                ClassPathResource fontResource = new ClassPathResource("fonts/CASTELAR.TTF");
                if (fontResource.exists()) {
                    try (InputStream fontStream = fontResource.getInputStream()) {
                        byte[] fontBytes = fontStream.readAllBytes();
                        java.io.File tempFont = java.io.File.createTempFile("CASTELAR", ".TTF");
                        tempFont.deleteOnExit();
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFont)) {
                            fos.write(fontBytes);
                        }
                        renderer.getFontResolver().addFont(tempFont.getAbsolutePath(), "Identity-H", true);
                        fontLoaded = true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not load Castelar from classpath: " + e.getMessage());
            }

            if (!fontLoaded) {
                // Fall back to system font (only available on Windows machines that have it)
                try {
                    renderer.getFontResolver().addFont("C:/Windows/Fonts/CASTELAR.TTF", "Identity-H", true);
                } catch (Exception e) {
                    System.out.println("Warning: Could not register CASTELAR.TTF font. Fallback will be used.");
                }
            }

            // Provide an explicit base URL to prevent Flying Saucer from trying
            // to resolve relative resource paths against null (causes network errors
            // on machines with restricted network access).
            renderer.setDocumentFromString(htmlContent, "about:blank");
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        }
    }
}
