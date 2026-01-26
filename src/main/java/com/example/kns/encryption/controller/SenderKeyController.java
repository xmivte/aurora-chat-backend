package com.example.kns.encryption.controller;

import com.example.kns.encryption.dto.DistributeSenderKeysDTO;
import com.example.kns.encryption.dto.SenderKeyEnvelopeDTO;
import com.example.kns.encryption.service.SenderKeyEnvelopeService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/encryption/sender-keys")
public class SenderKeyController {

	@SuppressFBWarnings(value = "EI2", justification = "SpotBugs false positive for DI")
	private final SenderKeyEnvelopeService envelopeService;

	@GetMapping("/pending")
	public ResponseEntity<SenderKeyEnvelopeDTO> getPending(@RequestParam String chatId, @RequestParam String userId,
			@RequestParam String deviceId) {
		return envelopeService.getPending(chatId, userId, deviceId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PostMapping("/{id}/consume")
	public ResponseEntity<Void> consume(@PathVariable Long id) {
		envelopeService.consume(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/distribute")
	public ResponseEntity<Void> distribute(@Valid @RequestBody DistributeSenderKeysDTO dto) {
		envelopeService.distribute(dto);
		return ResponseEntity.ok().build();
	}
}
