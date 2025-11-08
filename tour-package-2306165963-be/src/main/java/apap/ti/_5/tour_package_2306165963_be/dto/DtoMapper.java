package apap.ti._5.tour_package_2306165963_be.dto;

import apap.ti._5.tour_package_2306165963_be.dto.activity.*;
import apap.ti._5.tour_package_2306165963_be.dto.orderedquantity.*;
import apap.ti._5.tour_package_2306165963_be.dto.packagedto.*;
import apap.ti._5.tour_package_2306165963_be.dto.plan.*;
import apap.ti._5.tour_package_2306165963_be.model.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public DtoMapper() {}

    // ========== Activity ==========
    public Activity toEntity(CreateActivityDto dto) {
        return Activity.builder()
                .activityName(dto.getActivityName())
                .activityType(dto.getActivityType())
                .activityItem(dto.getActivityItem())
                .capacity(dto.getCapacity())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .startLocation(dto.getStartLocation())
                .endLocation(dto.getEndLocation())
                .build();
    }

    public Activity toEntity(UpdateActivityDto dto) {
        return Activity.builder()
                .id(dto.getId())
                .activityName(dto.getActivityName())
                .activityType(dto.getActivityType())
                .activityItem(dto.getActivityItem())
                .capacity(dto.getCapacity())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .startLocation(dto.getStartLocation())
                .endLocation(dto.getEndLocation())
                .build();
    }

    public ReadActivityDto toReadDto(Activity entity) {
        if (entity == null) return null;
        
        return ReadActivityDto.builder()
                .id(entity.getId())
                .activityName(entity.getActivityName())
                .activityType(entity.getActivityType())
                .activityItem(entity.getActivityItem())
                .capacity(entity.getCapacity())
                .price(entity.getPrice())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startLocation(entity.getStartLocation())
                .endLocation(entity.getEndLocation())
                .build();
    }

    public UpdateActivityDto toUpdateDto(Activity entity) {
        if (entity == null) return null;
        
        return UpdateActivityDto.builder()
                .id(entity.getId())
                .activityName(entity.getActivityName())
                .activityType(entity.getActivityType())
                .activityItem(entity.getActivityItem())
                .capacity(entity.getCapacity())
                .price(entity.getPrice())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startLocation(entity.getStartLocation())
                .endLocation(entity.getEndLocation())
                .build();
    }

    // ========== Package ==========
    public Package toEntity(CreatePackageDto dto) {
        return Package.builder()
                .userId(dto.getUserId())
                .packageName(dto.getPackageName())
                .quota(dto.getQuota())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status("Pending")
                .plans(new ArrayList<>())
                .build();
    }

    public Package toEntity(UpdatePackageDto dto) {
        return Package.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .packageName(dto.getPackageName())
                .quota(dto.getQuota())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .plans(new ArrayList<>())
                .build();
    }

    public ReadPackageDto toReadDto(Package entity) {
        if (entity == null) return null;
        
        // ✅ ENSURE plans is NEVER null
        if (entity.getPlans() == null) {
            entity.setPlans(new ArrayList<>());
        }
        
        return ReadPackageDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .packageName(entity.getPackageName())
                .quota(entity.getQuota())
                .price(entity.getPrice() != null ? entity.getPrice() : 0L)
                .status(entity.getStatus() != null ? entity.getStatus() : "Pending")
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .plans(entity.getPlans().stream()
                        .map(this::toReadDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public UpdatePackageDto toUpdateDto(Package entity) {
        if (entity == null) return null;
        
        return UpdatePackageDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .packageName(entity.getPackageName())
                .quota(entity.getQuota())
                .price(entity.getPrice())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }

    // ========== Plan ==========
    public Plan toEntity(CreatePlanDto dto) {
        return Plan.builder()
                .activityType(dto.getActivityType())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .startLocation(dto.getStartLocation())
                .endLocation(dto.getEndLocation())
                .status("Unfinished")
                .orderedQuantities(new ArrayList<>())
                .build();
    }

    public Plan toEntity(UpdatePlanDto dto) {
        return Plan.builder()
                .id(dto.getId())
                .activityType(dto.getActivityType())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .startLocation(dto.getStartLocation())
                .endLocation(dto.getEndLocation())
                .orderedQuantities(new ArrayList<>())
                .build();
    }

    public ReadPlanDto toReadDto(Plan entity) {
        if (entity == null) return null;
        
        // ✅ ENSURE orderedQuantities is NEVER null
        if (entity.getOrderedQuantities() == null) {
            entity.setOrderedQuantities(new ArrayList<>());
        }
        
        return ReadPlanDto.builder()
                .id(entity.getId())
                .packageId(entity.getPackageId())
                .activityType(entity.getActivityType())
                .price(entity.getPrice() != null ? entity.getPrice() : 0L)
                .status(entity.getStatus() != null ? entity.getStatus() : "Unfinished")
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startLocation(entity.getStartLocation())
                .endLocation(entity.getEndLocation())
                .orderedQuantities(entity.getOrderedQuantities().stream()
                        .map(this::toReadDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public UpdatePlanDto toUpdateDto(Plan entity) {
        if (entity == null) return null;
        
        return UpdatePlanDto.builder()
                .id(entity.getId())
                .activityType(entity.getActivityType())
                .price(entity.getPrice())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startLocation(entity.getStartLocation())
                .endLocation(entity.getEndLocation())
                .build();
    }

    // ========== OrderedQuantity ==========
    public OrderedQuantity toEntity(CreateOrderedQuantityDto dto) {
        return OrderedQuantity.builder()
                .activityId(dto.getActivityId())
                .orderedQuota(dto.getOrderedQuota())
                .build();
    }

    public ReadOrderedQuantityDto toReadDto(OrderedQuantity entity) {
        if (entity == null) return null;
        
        return ReadOrderedQuantityDto.builder()
                .id(entity.getId())
                .planId(entity.getPlanId())
                .activityId(entity.getActivityId())
                .orderedQuota(entity.getOrderedQuota())
                .quota(entity.getQuota())
                .price(entity.getPrice() != null ? entity.getPrice() : 0L)
                .activityName(entity.getActivityName())
                .activityItem(entity.getActivityItem())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }

    public UpdateOrderedQuantityDto toUpdateDto(OrderedQuantity entity) {
        if (entity == null) return null;
        
        return UpdateOrderedQuantityDto.builder()
                .id(entity.getId())
                .orderedQuota(entity.getOrderedQuota())
                .build();
    }
}