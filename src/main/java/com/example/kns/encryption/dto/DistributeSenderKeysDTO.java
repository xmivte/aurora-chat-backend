package com.example.kns.encryption.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DistributeSenderKeysDTO {
	@NotBlank
	private String chatId;

	@NotEmpty
	@Valid
	private List<SenderKeyEnvelopeItemDTO> items;

	private static <T> List<T> copyList(List<T> in) {
		return in == null ? null : List.copyOf(in);
	}

	public void setItems(List<SenderKeyEnvelopeItemDTO> items) {
		this.items = copyList(items);
	}

	public List<SenderKeyEnvelopeItemDTO> getItems() {
		return copyList(this.items);
	}
}
