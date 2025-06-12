package com.vivid.sample.repository;

import com.vivid.sample.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {
    Optional<ApiClient> findByApiKeyAndEnabledTrue(String apiKeyFromHeader);
}
