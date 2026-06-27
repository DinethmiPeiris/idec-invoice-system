package com.idec.invoicesystem.repository;
import com.idec.invoicesystem.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
}