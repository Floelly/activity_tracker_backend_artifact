package dev.floelly.activitytrackerapi.service;

import dev.floelly.activitytrackerapi.dto.request.CreateTagRequest;
import dev.floelly.activitytrackerapi.dto.response.TagResponse;
import dev.floelly.activitytrackerapi.dto.response.TagsResponse;
import dev.floelly.activitytrackerapi.entity.Tag;
import dev.floelly.activitytrackerapi.mapper.TagCommandMapper;
import dev.floelly.activitytrackerapi.mapper.TagResponseMapper;
import dev.floelly.activitytrackerapi.repository.TagRepository;
import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

        when(repository.findAll(any(Sort.class))).thenReturn(tags);
        when(responseMapper.toTagsResponse(anyList())).thenReturn(expectedResponse);

        TagsResponse result = service.findAllTags();

        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findAll(any(Sort.class));
        verify(responseMapper).toTagsResponse(anyList());
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
}