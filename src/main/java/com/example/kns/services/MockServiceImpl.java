package com.example.kns.services;

import org.springframework.stereotype.Service;

@Service
public class MockServiceImpl implements MockService {
    @Override
    public String getText(){
        return "aa";
    }
}
