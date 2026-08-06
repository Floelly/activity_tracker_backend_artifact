package dev.floelly.activitytrackerapi.feature.activityexport.dto;

public record ActivityExportFilterDTO(
        String categoryId,
        String startDate,
        String endDate
) {
}