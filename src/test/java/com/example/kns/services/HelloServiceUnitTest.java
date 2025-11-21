package com.example.kns.services;

import com.example.kns.services.HelloService;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HelloServiceUnitTest {
    private final HelloService service;

    public HelloServiceUnitTest(HelloService service) {
        this.service = service;
    }

    @Test
	void greet_returnsApproval() {
		String result = service.greet();
		assertThat(result).isEqualTo("approval");
	}
}
