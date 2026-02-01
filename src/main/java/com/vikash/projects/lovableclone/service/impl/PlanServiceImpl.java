package com.vikash.projects.lovableclone.service.impl;

import com.vikash.projects.lovableclone.dto.subscription.PlanResponse;
import com.vikash.projects.lovableclone.service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {
    @Override
    public List<PlanResponse> getAllActivePlans() {
        return List.of();
    }
}
