package com.vikash.projects.lovableclone.service.impl;

import com.vikash.projects.lovableclone.dto.subscription.PlanLimitsResponse;
import com.vikash.projects.lovableclone.dto.subscription.UsageTodayResponse;
import com.vikash.projects.lovableclone.service.UsageService;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {

    @Override
    public UsageTodayResponse getTodayUsageOfUser(Long userId) {
        return null;
    }

    @Override
    public PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId) {
        return null;
    }
}
