package com.example.kns.services;

import org.springframework.stereotype.Service;

@Service
public class DefaultMockService implements MockService {
	@Override
	public String getText() {
		return "aa";
	}
}
