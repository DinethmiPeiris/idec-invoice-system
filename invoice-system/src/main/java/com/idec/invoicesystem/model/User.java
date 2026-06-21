package com.idec.invoicesystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity representing a User in the invoice system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;
    
    private String username;
    private String password; // hashed BCrypt password
    private String role;     // e.g. ROLE_ADMIN, ROLE_STAFF
}
