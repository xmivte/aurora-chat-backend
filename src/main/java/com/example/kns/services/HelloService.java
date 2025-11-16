package com.example.kns.services;

import com.example.kns.entities.mockUser;
import com.example.kns.repository.HelloRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class HelloService {

    private final HelloRepository repository;

	public String greet() {
		return "approval";
	}

    public List<mockUser> findAll()
    {
        return repository.findAll();
    }

    public void save(mockUser user)
    {
        repository.save(user);
    }
}
