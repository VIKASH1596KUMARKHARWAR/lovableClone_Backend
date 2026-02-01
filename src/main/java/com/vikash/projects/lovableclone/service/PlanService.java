package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.subscription.PlanResponse;

import java.util.List;

public interface PlanService {
     List<PlanResponse> getAllActivePlans();
}
