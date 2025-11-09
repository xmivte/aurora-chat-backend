package com.example.kns.services;

import com.example.kns.services.HelloService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HelloServiceUnitTest {
	@Test
	void greet_returnsApproval() {
		HelloService service = new HelloService();
		String result = service.greet();
		assertThat(result).isEqualTo("approval");
	}
}
