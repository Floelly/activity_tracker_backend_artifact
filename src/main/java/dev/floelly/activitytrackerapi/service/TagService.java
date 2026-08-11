package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.mapper.TagCommandMapper;
import dev.floelly.activitytrackerapi.mapper.TagResponseMapper;
import dev.floelly.activitytrackerapi.repository.TagRepository;
import dev.floelly.activitytrackerapi.repository.TagSpecifications;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TSID.Factory tsidFactory;

    private final TagRepository repository;
    private final TagCommandMapper commandMapper;
    private final TagResponseMapper responseMapper;

    @Transactional(readOnly = true)
    public TagsResponse findAllTags() {
        List<Tag> tags = repository.findAll();
        return responseMapper.toTagsResponse(tags);
    }

    @Transactional(readOnly = true)
    public TagsResponse searchTags(String query, int limit) {
        Sort sort = Sort.by(Sort.Direction.ASC, "label");

        List<Tag> tags;
        if (query == null || query.isBlank()) {
            tags = repository.findAll(PageRequest.of(0, limit, sort)).getContent();
        } else {
            Specification<Tag> spec = TagSpecifications.labelContains(query.trim());
            tags = repository.findAll(spec, PageRequest.of(0, limit, sort)).getContent();
        }

        return responseMapper.toSearchTagsResponse(tags);
    }

    @Transactional
    public TagResponse registerNewTag(CreateTagRequest tagRequest) {
        Tag tag = commandMapper.toEntity(tagRequest);
        tag.setBusinessId(tsidFactory.generate().toString());
        repository.save(tag);
        return responseMapper.toResponse(tag);
    }
}
