package com.idec.invoicesystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.templateresolver.ITemplateResolver;
import com.idec.invoicesystem.service.PdfService;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class InvoiceSystemApplicationTests {

    @Autowired
    private ITemplateResolver templateResolver;

    @Autowired
    private PdfService pdfService;

    @Test
    void contextLoads() {
    }

    @Test
    void testProfitReportTemplateRendering() {
        SpringTemplateEngine testEngine = new SpringTemplateEngine();
        testEngine.setTemplateResolver(templateResolver);
        testEngine.setLinkBuilder(new ILinkBuilder() {
            @Override
            public String getName() { return "mock"; }
            @Override
            public Integer getOrder() { return 1; }
            @Override
            public String buildLink(IExpressionContext context, String base, Map<String, Object> parameters) {
                return base;
            }
        });

        Context context = new Context();
        context.setVariable("isAdmin", true);
        context.setVariable("username", "admin");
        context.setVariable("generated", true);
        context.setVariable("fromDate", "2026-06-01");
        context.setVariable("toDate", "2026-06-30");
        context.setVariable("jobs", Collections.emptyList());
        context.setVariable("totalInvoiceAmount", 1000.0);
        context.setVariable("totalExpensesAmount", 400.0);
        context.setVariable("totalProfitAmount", 600.0);

        String html = testEngine.process("jobs/report-profit", context);
        assertNotNull(html);
    }

    @Test
    void testProfitReportPdfTemplateRendering() {
        SpringTemplateEngine testEngine = new SpringTemplateEngine();
        testEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("fromDate", "2026-06-01");
        context.setVariable("toDate", "2026-06-30");
        context.setVariable("jobs", Collections.emptyList());
        context.setVariable("totalInvoiceAmount", 1000.0);
        context.setVariable("totalExpensesAmount", 400.0);
        context.setVariable("totalProfitAmount", 600.0);

        String html = testEngine.process("jobs/report-profit-pdf", context);
        assertNotNull(html);
    }

    @Test
    void testProfitReportPdfGeneration() throws Exception {
        Context context = new Context();
        context.setVariable("fromDate", "2026-06-01");
        context.setVariable("toDate", "2026-06-30");
        context.setVariable("jobs", Collections.emptyList());
        context.setVariable("totalInvoiceAmount", 1000.0);
        context.setVariable("totalExpensesAmount", 400.0);
        context.setVariable("totalProfitAmount", 600.0);

        byte[] pdfBytes = pdfService.generatePdf("jobs/report-profit-pdf", context);
        assertNotNull(pdfBytes);
    }

    @Test
    void testSummaryReportPdfGeneration() throws Exception {
        Context context = new Context();
        context.setVariable("fromDate", "2026-06-01");
        context.setVariable("toDate", "2026-06-30");

        com.idec.invoicesystem.model.Job job1 = new com.idec.invoicesystem.model.Job();
        job1.setJobNo("1007");
        job1.setDate(java.time.LocalDate.of(2026, 6, 16));
        job1.setCompanyName("ABC");
        job1.setVesselName("dsd");
        job1.setDescription("smnd");
        job1.setChassisContainerNo("smnds");
        job1.setBlNumber("m,nm");
        job1.setInvoiceNo("s md");
        job1.setRemarks("—");
        job1.setCustomsRegister("dbwjdgwdsd");
        job1.setEntrySubmit("ndjksh");
        job1.setDuty("smdnm");
        job1.setDeliveryDate(java.time.LocalDate.of(2026, 6, 13));
        job1.setDoCharges(15000.0);
        job1.setEntryPassing(0.0);
        job1.setDeliveryExpenses(0.0);
        job1.setCommission(0.0);
        job1.setHipgCharges(0.0);
        job1.setHandlingExpenses(0.0);
        job1.setOther(0.0);
        job1.setAgencyFee(0.0);
        job1.setBlAmount(0.0);
        job1.setAdvance(0.0);
        job1.setBalance(0.0);

        com.idec.invoicesystem.model.Job job2 = new com.idec.invoicesystem.model.Job();
        job2.setJobNo("1003");
        job2.setDate(java.time.LocalDate.of(2026, 6, 18));
        job2.setCompanyName("ABC");
        job2.setVesselName("nndnd");
        job2.setDescription("nbrnbh");
        job2.setChassisContainerNo("bjj");
        job2.setBlNumber("nnr");
        job2.setInvoiceNo("rrr");
        job2.setRemarks("bbb");
        job2.setCustomsRegister("rrrr");
        job2.setEntrySubmit("ded");
        job2.setDuty("dede");
        job2.setDoCharges(1000.0);
        job2.setEntryPassing(12000.0);
        job2.setDeliveryExpenses(2000.0);
        job2.setCommission(200000.0);
        job2.setHipgCharges(10000.0);
        job2.setHandlingExpenses(25000.0);
        job2.setOther(25000.0);
        job2.setAgencyFee(250000.0);
        job2.setBlAmount(311000.0);
        job2.setAdvance(20000.0);
        job2.setBalance(291000.0);

        java.util.List<com.idec.invoicesystem.model.Job> jobs = java.util.Arrays.asList(job1, job2);
        context.setVariable("jobs", jobs);

        context.setVariable("totalDoCharges", 122615.28);
        context.setVariable("totalEntryPassing", 40430.00);
        context.setVariable("totalDeliveryExpenses", 3250.00);
        context.setVariable("totalCommission", 243560.00);
        context.setVariable("totalHipgCharges", 78291.00);
        context.setVariable("totalHandlingExpenses", 92500.00);
        context.setVariable("totalOther", 27500.00);
        context.setVariable("totalAgencyFee", 280500.00);
        context.setVariable("totalBl", 260701.16);
        context.setVariable("totalAdvance", 14500.00);
        context.setVariable("totalBalance", 246201.14);

        byte[] pdfBytes = pdfService.generatePdf("jobs/report-pdf", context);
        assertNotNull(pdfBytes);

        // Ensure the directory exists and write it to target
        java.io.File targetDir = new java.io.File("invoice-system/target");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        java.nio.file.Files.write(java.nio.file.Paths.get("invoice-system/target/test_summary_report.pdf"), pdfBytes);
    }
}












