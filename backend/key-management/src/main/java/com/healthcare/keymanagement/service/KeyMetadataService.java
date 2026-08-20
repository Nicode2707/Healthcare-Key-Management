package com.healthcare.keymanagement.service;

import com.healthcare.keymanagement.entity.KeyMetadata;
import com.healthcare.keymanagement.repository.KeyMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyMetadataService {

    private final KeyMetadataRepository repository;

    public KeyMetadata create(KeyMetadata keyMetadata) {
        return repository.save(keyMetadata);
    }

    public List<KeyMetadata> getAll() {
        return repository.findAll();
    }
}