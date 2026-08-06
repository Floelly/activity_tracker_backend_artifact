package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.response.AppReferenceDataResponse;
import dev.floelly.activitytrackerapi.dto.response.CategoryResponse;
import dev.floelly.activitytrackerapi.entity.Category;
import dev.floelly.activitytrackerapi.mapper.CategoryResponseMapper;
import dev.floelly.activitytrackerapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppReferenceDataService {


    private final CategoryRepository categoryRepository;
    private final CategoryResponseMapper categoryResponseMapper;

    @Transactional(readOnly = true)
    public AppReferenceDataResponse getAppReferenceData() {
        List<Category> categories = categoryRepository.findAllByDeletedAtIsNull();
        List<CategoryResponse> categoryResponses = categoryResponseMapper.toCategoryResponseList(categories);
        return new AppReferenceDataResponse(categoryResponses);
    }
}