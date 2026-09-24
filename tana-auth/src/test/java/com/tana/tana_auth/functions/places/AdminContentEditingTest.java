package com.tana.tana_auth.functions.places;

import com.tana.tana_auth.functions.collections.dto.*;
import com.tana.tana_auth.functions.collections.repository.*;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.collections.service.impl.CollectionServiceImpl;
import com.tana.tana_auth.functions.places.dto.PlacesRequestDto;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.service.impl.PlacesServiceImpl;
import com.tana.tana_auth.functions.spot.repository.SpotRepository;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.*;
import com.tana.tana_common.util.CommonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminContentEditingTest {
    @Test
    void transportOptionsRoundTripPreserveOmissionsAndAllowClearing() throws Exception {
        PlacesRepository places = mock(PlacesRepository.class);
        CollectionsCategorySelectionRepository selections = mock(CollectionsCategorySelectionRepository.class);
        PlacesServiceImpl service = new PlacesServiceImpl();
        ReflectionTestUtils.setField(service, "repository", places);
        ReflectionTestUtils.setField(service, "collectionsCategorySelectionRepository", selections);
        PlaceMaster place = new PlaceMaster();
        place.setId(7L);
        when(places.findById(7L)).thenReturn(Optional.of(place));
        when(places.save(any())).thenAnswer(call -> call.getArgument(0));
        when(selections.findAllByPlaceId(7L)).thenReturn(List.of());
        for (String field : List.of("habalHabalTricycle", "commute", "walkFromDropOff", "privateCarVan")) {
            PlacesRequestDto request = PlacesRequestDto.builder().placeId(7L).name("Island").build();
            ReflectionTestUtils.setField(request, field, " Directions ");
            service.createPlaces(request);
            assertEquals("Directions", ReflectionTestUtils.getField(service.getAdminPlace(7L), field));
            service.createPlaces(PlacesRequestDto.builder().placeId(7L).name("Island").build());
            assertEquals("Directions", ReflectionTestUtils.getField(place, field));
            ReflectionTestUtils.setField(request, field, " ");
            service.createPlaces(request);
            assertEquals("", ReflectionTestUtils.getField(place, field));
        }
    }

    @Test
    void spotTextLimitsApplyBeforePersistenceForCreateEditAndPhotoRequests() {
        PlacesRepository places = mock(PlacesRepository.class);
        PlacesServiceImpl service = new PlacesServiceImpl();
        ReflectionTestUtils.setField(service, "repository", places);
        for (String field : List.of("googleAddress", "overview", "tanaTip", "habalHabalTricycle", "commute", "walkFromDropOff", "privateCarVan")) {
            for (Long id : Arrays.asList(null, 7L)) {
                for (boolean photo : new boolean[]{false, true}) {
                    PlacesRequestDto request = PlacesRequestDto.builder().placeId(id).name("Spot").build();
                    ReflectionTestUtils.setField(request, field, "a".repeat(1001));
                    TanaException error = assertThrows(TanaException.class, () -> {
                        if (photo) service.createPlaces(request, new MockMultipartFile("file", new byte[]{1}));
                        else service.createPlaces(request);
                    });
                    assertEquals(400, error.getCode());
                    assertEquals("textTooLong", error.getMessageCode());
                    assertTrue(error.getErrorMessage().contains("1,000"));
                }
            }
        }
        verifyNoInteractions(places);
    }

    @Test
    void spotTextAcceptsExactlyOneThousandCharacters() throws Exception {
        PlacesRepository places = mock(PlacesRepository.class);
        PlacesServiceImpl service = new PlacesServiceImpl();
        ReflectionTestUtils.setField(service, "repository", places);
        when(places.save(any())).thenAnswer(call -> call.getArgument(0));
        String text = "a".repeat(1000);
        service.createPlaces(PlacesRequestDto.builder().name("Spot")
            .googleAddress(text).overview(text).tanaTip(text).build());
        verify(places).save(argThat(place -> text.equals(place.getGoogleAddress())
            && text.equals(place.getOverview()) && text.equals(place.getTanaTip())));
    }

    @Test
    void editingPlacePreservesIdentityPhotosAndRetainedCollectionOrder() throws Exception {
        PlacesRepository places = mock(PlacesRepository.class);
        CollectionsCategorySelectionRepository selections = mock(CollectionsCategorySelectionRepository.class);
        CollectionService collections = mock(CollectionService.class);
        PlacesServiceImpl service = new PlacesServiceImpl();
        ReflectionTestUtils.setField(service, "repository", places);
        ReflectionTestUtils.setField(service, "collectionsCategorySelectionRepository", selections);
        ReflectionTestUtils.setField(service, "collectionService", collections);
        PlaceMaster place = new PlaceMaster();
        place.setId(7L);
        place.setImageStrings(List.of("existing.jpg"));
        place.setPriceRange("unchanged");
        CollectionsMaster retained = new CollectionsMaster();
        retained.setCollectionName("Keep");
        CollectionsMaster removed = new CollectionsMaster();
        removed.setCollectionName("Remove");
        CollectionsCategorySelections keep = CollectionsCategorySelections.builder()
            .collection(retained).place(place).displayOrder(8).build();
        CollectionsCategorySelections remove = CollectionsCategorySelections.builder()
            .collection(removed).place(place).build();
        when(places.findById(7L)).thenReturn(Optional.of(place));
        when(places.save(any())).thenAnswer(call -> call.getArgument(0));
        when(selections.findAllByPlaceId(7L)).thenReturn(List.of(keep, remove));
        when(collections.getAllCollections()).thenReturn(List.of(retained, removed));
        service.createPlaces(PlacesRequestDto.builder().placeId(7L).name("Renamed")
            .collections(List.of("Keep")).subCategoryTypeEnum(List.of()).build());
        verify(places).save(same(place));
        assertEquals("Renamed", place.getName());
        assertEquals(List.of("existing.jpg"), place.getImageStrings());
        assertEquals("unchanged", place.getPriceRange());
        assertEquals(8, keep.getDisplayOrder());
        verify(selections).deleteAll(List.of(remove));
        verify(selections).saveAll(List.of());
        assertEquals(List.of(), place.getSubCategoryTypeEnum());
    }

    @Test
    void replacingCoverKeepsTheRestOfTheGallery() throws Exception {
        PlacesRepository places = mock(PlacesRepository.class);
        CommonUtils images = mock(CommonUtils.class);
        PlacesServiceImpl service = new PlacesServiceImpl();
        ReflectionTestUtils.setField(service, "repository", places);
        ReflectionTestUtils.setField(service, "commonUtils", images);
        PlaceMaster place = new PlaceMaster();
        place.setId(7L);
        place.setImageStrings(List.of("old-cover.jpg", "gallery.jpg"));
        when(places.findById(7L)).thenReturn(Optional.of(place));
        when(places.save(any())).thenAnswer(call -> call.getArgument(0));
        when(images.uploadImage(anyString(), anyLong(), anyString(), any(), anyString())).thenReturn("new-cover.jpg");
        service.createPlaces(PlacesRequestDto.builder().placeId(7L).name("Spot").build(),
            new MockMultipartFile("file", new byte[]{1}));
        assertEquals(List.of("new-cover.jpg", "gallery.jpg"), place.getImageStrings());
    }

    @Test
    void unknownPlaceCannotSilentlyCreateADuplicate() {
        PlacesRepository places = mock(PlacesRepository.class);
        PlacesServiceImpl service = new PlacesServiceImpl();
        ReflectionTestUtils.setField(service, "repository", places);
        when(places.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TanaException.class, () -> service.createPlaces(
            PlacesRequestDto.builder().placeId(99L).name("Missing").build()));
        verify(places, never()).save(any());
    }

    @Test
    void collectionEditsReplaceMembershipAndReturnFullEditableDetailsWithOrWithoutPhotos() throws Exception {
        for (boolean withPhoto : new boolean[]{false, true}) {
            CollectionRepository collections = mock(CollectionRepository.class);
            CollectionsCategorySelectionRepository selections = mock(CollectionsCategorySelectionRepository.class);
            PlacesRepository places = mock(PlacesRepository.class);
            SpotRepository spots = mock(SpotRepository.class);
            CommonUtils images = mock(CommonUtils.class);
            CollectionServiceImpl service = new CollectionServiceImpl();
            ReflectionTestUtils.setField(service, "collectionRepository", collections);
            ReflectionTestUtils.setField(service, "categorySelectionRepository", selections);
            ReflectionTestUtils.setField(service, "placesRepository", places);
            ReflectionTestUtils.setField(service, "spotRepository", spots);
            ReflectionTestUtils.setField(service, "commonUtils", images);
            CollectionsMaster collection = new CollectionsMaster();
            collection.setCollectionId(3L);
            Spot segment = new Spot();
            segment.setSpotName("Nature & Scenery");
            PlaceMaster place = new PlaceMaster();
            place.setId(7L);
            when(collections.findById(3L)).thenReturn(Optional.of(collection));
            when(collections.save(any())).thenAnswer(call -> call.getArgument(0));
            when(spots.findBySpotNameIgnoreCase("Nature & Scenery")).thenReturn(Optional.of(segment));
            when(places.findById(7L)).thenReturn(Optional.of(place));
            when(images.uploadImage(anyString(), anyLong(), anyString(), any(), anyString())).thenReturn("new.jpg");
            when(selections.findAllByCollectionCollectionIdOrderByDisplayOrderAscCollectionSelectionIdAsc(3L))
                .thenReturn(List.of(CollectionsCategorySelections.builder().place(place).displayOrder(4).build()));
            CollectionCreateRequestDto request = CollectionCreateRequestDto.builder()
                .collectionId(3L).collectionName("Renamed collection").segment("Nature & Scenery")
                .overview("Overview").badge("Badge").badgeOverview("Badge copy").helperText("Helper")
                .collectionImage("existing.jpg").badgeImage("badge.png").moodType(2).moodPriority(5)
                .spots(List.of(CollectionSpotRequestDto.builder().placeId(7L).displayOrder(4).build())).build();
            CollectionAdminOptionDto result = withPhoto
                ? service.createCollection(request, new MockMultipartFile("collectionImage", new byte[]{1}), null)
                : service.createCollection(request);
            assertEquals(3L, result.getCollectionId());
            assertEquals("Overview", result.getOverview());
            assertEquals("Badge copy", result.getBadgeOverview());
            assertEquals("badge.png", result.getBadgeImage());
            assertEquals(withPhoto ? "new.jpg" : "existing.jpg", result.getCollectionImage());
            assertEquals(5, result.getMoodPriority());
            assertEquals(4, result.getSpots().get(0).getDisplayOrder());
            verify(selections).deleteAllByCollectionCollectionId(3L);
            verify(selections).saveAll(argThat(rows -> {
                var row = rows.iterator().next();
                return row.getCollection() == collection && row.getPlace() == place && row.getDisplayOrder() == 4;
            }));
        }
    }
}
