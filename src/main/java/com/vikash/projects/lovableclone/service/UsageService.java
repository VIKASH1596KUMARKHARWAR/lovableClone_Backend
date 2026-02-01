package com.vikash.projects.lovableclone.service;

import com.vikash.projects.lovableclone.dto.subscription.PlanLimitsResponse;
import com.vikash.projects.lovableclone.dto.subscription.UsageTodayResponse;

public interface UsageService {
     UsageTodayResponse getTodayUsageOfUser(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId);
}
