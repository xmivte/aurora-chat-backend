package com.example.kns.encryption.service;

import com.example.kns.encryption.dto.SenderKeyRequestRowDTO;
import com.example.kns.encryption.model.SenderKeyRequest;
import com.example.kns.encryption.repository.SenderKeyRequestRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI beans")
public class SenderKeyRequestService {

	private final SenderKeyRequestRepository requestRepo;

	public List<SenderKeyRequestRowDTO> getPendingRequests(String chatId) {
		List<SenderKeyRequest> rows = requestRepo.findPendingByChatId(chatId);

		return rows.stream()
				.map(r -> SenderKeyRequestRowDTO.builder().id(r.getId()).chatId(r.getChatId())
						.requesterUserId(r.getRequesterUserId()).requesterDeviceId(r.getRequesterDeviceId())
						.createdAt(r.getCreatedAt()).build())
				.toList();
	}
}
