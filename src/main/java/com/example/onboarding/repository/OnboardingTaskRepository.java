package com.example.onboarding.repository;

import com.example.onboarding.entity.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {

    long countByCompletedFalse();
}
