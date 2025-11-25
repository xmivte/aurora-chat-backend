package com.example.kns.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HelloServiceUnitTest {

	// creation of a mock
	@Mock
	private MockService mockService;

	// injection of mock into our service
	@InjectMocks
	private HelloService service;

	// mock test
	@Test
	void getText_WithMockDependency_ReturnsMockValue(){
		// creating what the mock should return
		when(mockService.getText()).thenReturn("response");

		// checking if the mock was called
		String result = service.getText();
		assertThat(result).isEqualTo("response");
		verify(mockService).getText();
	}

	// normal unit test - no mocked service is used
	@Test
	void greet_ReturnsApproval() {
		HelloService service = new HelloService(null, null);
		String result = service.greet();
		assertThat(result).isEqualTo("approval");
	}
}
