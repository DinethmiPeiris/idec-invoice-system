package com.idec.invoicesystem.controller;

import com.idec.invoicesystem.model.Invoice;
import com.idec.invoicesystem.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceRepository repo;

    @PostMapping
    public Invoice save(@RequestBody Invoice invoice) {
        return repo.save(invoice);
    }
}