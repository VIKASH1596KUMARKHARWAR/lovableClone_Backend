package com.vikash.projects.lovableclone.mapper;

import com.vikash.projects.lovableclone.dto.subscription.PlanResponse;
import com.vikash.projects.lovableclone.dto.subscription.SubscriptionResponse;
import com.vikash.projects.lovableclone.entity.Plan;
import com.vikash.projects.lovableclone.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanResponse toPlanResponse(Plan plan);
}
