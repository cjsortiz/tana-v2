package com.tana.tana_auth.functions.places;

import com.tana.tana_auth.functions.collections.dto.*;
import com.tana.tana_auth.functions.collections.repository.*;
import com.tana.tana_auth.functions.collections.service.impl.CollectionServiceImpl;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class HomeContentEditingTest {
    private final CuratorSpotRepository picks = mock(CuratorSpotRepository.class);
    private final TanaStoryRepository stories = mock(TanaStoryRepository.class);
    private final PlacesRepository places = mock(PlacesRepository.class);
    private final CollectionServiceImpl service = new CollectionServiceImpl();

    HomeContentEditingTest() {
        ReflectionTestUtils.setField(service, "curatorSpotRepository", picks);
        ReflectionTestUtils.setField(service, "tanaStoryRepository", stories);
        ReflectionTestUtils.setField(service, "placesRepository", places);
    }

    @Test
    void editPickRetainsIdentityWhenChangingSpot() throws Exception {
        PlaceMaster place = new PlaceMaster(); place.setId(8L); place.setName("New spot");
        CuratorSpot pick = new CuratorSpot(); pick.setCuratorSpotId(3L);
        when(places.findById(8L)).thenReturn(Optional.of(place));
        when(picks.findById(3L)).thenReturn(Optional.of(pick));
        when(picks.save(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.saveCuratorSpot(CuratorSpotRequestDto.builder().curatorSpotId(3L)
            .placeId(8L).displayOrder(2).proofLabel("Edited").active(false).build());
        assertEquals(3L, result.getCuratorSpotId());
        assertEquals(place, pick.getPlace());
        assertEquals("Edited", pick.getProofLabel());
        assertFalse(pick.getActive());
    }

    @Test
    void removalOnlyDeletesTheSelectedHomeContent() throws Exception {
        CuratorSpot pick = new CuratorSpot();
        TanaStory story = new TanaStory();
        when(picks.findById(3L)).thenReturn(Optional.of(pick));
        when(stories.findById(4L)).thenReturn(Optional.of(story));
        service.deleteCuratorSpot(3L);
        service.deleteTanaStory(4L);
        verify(picks).delete(pick);
        verify(stories).delete(story);
        verifyNoInteractions(places);
    }

    @Test
    void deletedRecordsCannotBeRecreatedByAnOldEditForm() {
        assertThrows(TanaException.class, () -> service.saveTanaStory(
            TanaStoryRequestDto.builder().tanaStoryId(99L).title("Old").linkUrl("https://example.com").build()));
        PlaceMaster place = new PlaceMaster(); place.setId(8L);
        when(places.findById(8L)).thenReturn(Optional.of(place));
        assertThrows(TanaException.class, () -> service.saveCuratorSpot(
            CuratorSpotRequestDto.builder().curatorSpotId(99L).placeId(8L).build()));
        verify(stories, never()).save(any());
        verify(picks, never()).save(any());
    }

    @Test
    void storyEditKeepsExistingImageAndIdentity() throws Exception {
        TanaStory story = new TanaStory(); story.setTanaStoryId(4L); story.setImage("existing.jpg");
        when(stories.findById(4L)).thenReturn(Optional.of(story));
        when(stories.save(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.saveTanaStory(TanaStoryRequestDto.builder().tanaStoryId(4L)
            .title("Updated title").linkUrl("https://example.com").image("existing.jpg").active(false).build());
        assertEquals(4L, result.getTanaStoryId());
        assertEquals("existing.jpg", result.getImage());
        assertEquals("Updated title", result.getTitle());
        assertFalse(result.getActive());
    }
}
