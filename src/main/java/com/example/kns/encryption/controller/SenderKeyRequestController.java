package com.example.kns.encryption.controller;

import com.example.kns.encryption.dto.SenderKeyRequestRowDTO;
import com.example.kns.encryption.service.SenderKeyRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/encryption/sender-keys/requests")
public class SenderKeyRequestController {

	private final SenderKeyRequestService requestService;

	@GetMapping("/pending")
	public ResponseEntity<List<SenderKeyRequestRowDTO>> getPendingRequests(@RequestParam String chatId) {
		List<SenderKeyRequestRowDTO> dtos = requestService.getPendingRequests(chatId);
		return dtos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dtos);
	}
}
