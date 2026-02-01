package com.vikash.projects.lovableclone.repository;

import com.vikash.projects.lovableclone.entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatEventRepository extends JpaRepository<ChatEvent, Long> {
}
