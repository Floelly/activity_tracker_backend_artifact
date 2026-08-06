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
import org.springframework.data.domain.Sort;
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
        return findAllTags(10);
    }

    @Transactional(readOnly = true)
    public TagsResponse findAllTags(int limit) {
        List<Tag> tags = repository.findAll(Sort.by("label"));
        return responseMapper.toTagsResponse(tags.stream().limit(limit).toList());
    }

    @Transactional(readOnly = true)
    public TagsResponse searchTags(String query, int limit) {
        List<Tag> tags = repository.findAll(TagSpecifications.labelContains(query), Sort.by("label"));
        return responseMapper.toTagsResponse(tags.stream().limit(limit).toList());
    }

    @Transactional
    public TagResponse registerNewTag(CreateTagRequest tagRequest) {
        Tag tag = commandMapper.toEntity(tagRequest);
        tag.setBusinessId(tsidFactory.generate().toString());
        repository.save(tag);
        return responseMapper.toResponse(tag);
    }
}
