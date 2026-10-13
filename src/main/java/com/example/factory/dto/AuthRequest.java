package com.example.factory.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String identifier; // Can be either username or email
    private String password;
}
