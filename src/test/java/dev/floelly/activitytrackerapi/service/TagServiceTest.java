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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TSID.Factory tsidFactory;
    @Mock
    private TagRepository repository;
    @Mock
    private TagCommandMapper commandMapper;
    @Mock
    private TagResponseMapper responseMapper;

    @InjectMocks
    private TagService service;

    @Test
    void findAllTags_shouldReturnMappedResponse() {
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();
        List<Tag> tags = List.of(tag1, tag2);
        TagsResponse expectedResponse = mock(TagsResponse.class);

        when(repository.findAll()).thenReturn(tags);
        when(responseMapper.toTagsResponse(tags)).thenReturn(expectedResponse);

        TagsResponse result = service.findAllTags();

        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findAll();
        verify(responseMapper).toTagsResponse(tags);
        verifyNoMoreInteractions(repository, responseMapper);
    }

    @Test
    void registerNewTag_shouldMapSaveAndReturnResponse() {
        CreateTagRequest request = mock(CreateTagRequest.class);
        Tag mappedTag = new Tag();
        TagResponse expectedResponse = mock(TagResponse.class);

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("TSID-123");

        when(commandMapper.toEntity(request)).thenReturn(mappedTag);
        when(responseMapper.toResponse(mappedTag)).thenReturn(expectedResponse);

        TagResponse result = service.registerNewTag(request);

        assertThat(mappedTag.getBusinessId()).isEqualTo("TSID-123");
        assertThat(result).isSameAs(expectedResponse);

        verify(commandMapper).toEntity(request);
        verify(tsidFactory).generate();
        verify(repository).save(mappedTag);
        verify(responseMapper).toResponse(mappedTag);
    }

    @Test
    void registerNewTag_shouldSaveTagWithGeneratedBusinessId() {
        CreateTagRequest request = mock(CreateTagRequest.class);
        Tag mappedTag = new Tag();
        TagResponse response = mock(TagResponse.class);

        TSID tsid = mock(TSID.class);
        when(tsidFactory.generate()).thenReturn(tsid);
        when(tsid.toString()).thenReturn("generated-id");

        when(commandMapper.toEntity(request)).thenReturn(mappedTag);
        when(responseMapper.toResponse(any(Tag.class))).thenReturn(response);

        service.registerNewTag(request);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(repository).save(captor.capture());

        Tag savedTag = captor.getValue();
        assertThat(savedTag.getBusinessId()).isEqualTo("generated-id");
    }

    @Test
    void searchTags_shouldReturnAllTags_whenQueryIsNull() {
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();
        List<Tag> tags = List.of(tag1, tag2);
        TagsResponse expectedResponse = mock(TagsResponse.class);

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "label");
        PageRequest pageRequest = PageRequest.of(0, 10, expectedSort);
        Page<Tag> page = new PageImpl<>(tags);

        when(repository.findAll(pageRequest)).thenReturn(page);
        when(responseMapper.toTagsResponsePreserveOrder(tags)).thenReturn(expectedResponse);

        TagsResponse result = service.searchTags(null, 10);

        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findAll(pageRequest);
        verify(responseMapper).toTagsResponsePreserveOrder(tags);
    }

    @Test
    void searchTags_shouldReturnAllTags_whenQueryIsBlank() {
        Tag tag1 = new Tag();
        List<Tag> tags = List.of(tag1);
        TagsResponse expectedResponse = mock(TagsResponse.class);

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "label");
        PageRequest pageRequest = PageRequest.of(0, 10, expectedSort);
        Page<Tag> page = new PageImpl<>(tags);

        when(repository.findAll(pageRequest)).thenReturn(page);
        when(responseMapper.toTagsResponsePreserveOrder(tags)).thenReturn(expectedResponse);

        TagsResponse result = service.searchTags("   ", 10);

        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findAll(pageRequest);
        verify(responseMapper).toTagsResponsePreserveOrder(tags);
    }

    @Test
    void searchTags_shouldSearchByLabel_whenQueryIsProvided() {
        Tag tag1 = new Tag();
        List<Tag> tags = List.of(tag1);
        TagsResponse expectedResponse = mock(TagsResponse.class);

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "label");
        PageRequest pageRequest = PageRequest.of(0, 10, expectedSort);

        when(repository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(new PageImpl<>(tags));
        when(responseMapper.toTagsResponsePreserveOrder(tags)).thenReturn(expectedResponse);

        TagsResponse result = service.searchTags("proj", 10);

        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findAll(any(Specification.class), eq(pageRequest));
        verify(responseMapper).toTagsResponsePreserveOrder(tags);
    }

    @Test
    void searchTags_shouldRespectLimit() {
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();
        Tag tag3 = new Tag();
        List<Tag> tags = List.of(tag1, tag2, tag3);

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "label");
        PageRequest pageRequest = PageRequest.of(0, 2, expectedSort);

        when(repository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(new PageImpl<>(tags.subList(0, 2)));

        service.searchTags("proj", 2);

        verify(repository).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    void searchTags_shouldTrimQueryBeforeSearch() {
        Tag tag1 = new Tag();
        List<Tag> tags = List.of(tag1);
        TagsResponse expectedResponse = mock(TagsResponse.class);

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "label");
        PageRequest pageRequest = PageRequest.of(0, 10, expectedSort);

        when(repository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(new PageImpl<>(tags));
        when(responseMapper.toTagsResponsePreserveOrder(tags)).thenReturn(expectedResponse);

        TagsResponse result = service.searchTags("  proj  ", 10);

        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findAll(any(Specification.class), eq(pageRequest));
    }
}