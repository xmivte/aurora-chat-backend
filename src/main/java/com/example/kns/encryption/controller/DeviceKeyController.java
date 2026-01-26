package com.example.kns.encryption.controller;

import com.example.kns.encryption.dto.DeviceKeyDTO;
import com.example.kns.encryption.dto.RegisterDeviceKeyDTO;
import com.example.kns.encryption.service.DeviceKeyService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/encryption/keys")
public class DeviceKeyController {

	@SuppressFBWarnings(value = "EI2", justification = "SpotBugs false positive for DI")
	private final DeviceKeyService deviceKeyService;

	@PostMapping
	public ResponseEntity<Void> registerDeviceKey(@Valid @RequestBody RegisterDeviceKeyDTO dto) {
		deviceKeyService.registerDeviceKey(dto);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{userId}/devices")
	public ResponseEntity<List<DeviceKeyDTO>> getUserDevices(@PathVariable String userId) {
		return ResponseEntity.ok(deviceKeyService.getUserDevices(userId));
	}
}
