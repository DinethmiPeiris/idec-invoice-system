package com.idec.invoicesystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "companies")
public class Company {

    @Id
    private String id;

    private String name;
    private String contactPerson;
    private String phone;
    private String tinNo;
    private String address;
    private String email;
    private boolean active = true;
}
