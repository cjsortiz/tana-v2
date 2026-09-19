package com.tana.tana_auth.functions.qr;

import com.tana.tana_auth.config.AuthConfig;
import com.tana.tana_auth.functions.collections.repository.CollectionRepository;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.qr.model.QrScanEvent;
import com.tana.tana_auth.functions.qr.repository.QrScanEventRepository;
import com.tana.tana_auth.functions.qr.service.impl.QrServiceImpl;
import com.tana.tana_auth.functions.routes.repository.RoutePartnerRepository;
import com.tana.tana_auth.functions.routes.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QrPartnerTest {
    private QrScanEventRepository scans;
    private QrServiceImpl service;

    @BeforeEach
    void setup() {
        scans = mock(QrScanEventRepository.class);
        RoutePartnerRepository partners = mock(RoutePartnerRepository.class);
        CollectionRepository collections = mock(CollectionRepository.class);
        PlacesRepository places = mock(PlacesRepository.class);
        RouteRepository routes = mock(RouteRepository.class);
        when(collections.existsById(12L)).thenReturn(true);
        when(places.existsById(12L)).thenReturn(true);
        when(routes.existsById(12L)).thenReturn(true);
        when(partners.existsById(7L)).thenReturn(true);
        service = new QrServiceImpl(scans, collections, places, routes, mock(AuthConfig.class), partners);
        ReflectionTestUtils.setField(service, "iosAppStoreUrl", "https://apps.apple.com/test");
        ReflectionTestUtils.setField(service, "androidPlayStoreUrl", "https://play.google.com/test");
        ReflectionTestUtils.setField(service, "analyticsHashSalt", "test");
    }

    @Test
    void partnerScansKeepAllFourDestinations() {
        for (String type : new String[]{"collection", "spot", "route", "download"}) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter("partnerId", "7");
            String html = service.recordScanAndBuildHandoff(type, type.equals("download") ? null : 12L, null, request);
            assertTrue(html.contains(type.equals("download") ? "Coming soon." : "type=" + type));
        }
        ArgumentCaptor<QrScanEvent> events = ArgumentCaptor.forClass(QrScanEvent.class);
        verify(scans, times(4)).save(events.capture());
        events.getAllValues().forEach(event -> assertEquals(7L, event.getPartnerId()));
    }

    @Test
    void oldLinksRemainGeneral() {
        service.recordScanAndBuildHandoff("download", null, null, new MockHttpServletRequest());
        ArgumentCaptor<QrScanEvent> event = ArgumentCaptor.forClass(QrScanEvent.class);
        verify(scans).save(event.capture());
        assertNull(event.getValue().getPartnerId());
    }

    @Test
    void iosDownloadGoesToAppStoreAndRetainsPartner() {
        String store = "https://apps.apple.com/ph/app/tana/id6769938553";
        ReflectionTestUtils.setField(service, "iosAppStoreUrl", store);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("platform", "ios");
        request.setParameter("partnerId", "7");
        String html = service.recordScanAndBuildHandoff("download", null, "exp://localhost", request);
        assertTrue(html.contains("0;url=" + store));
        assertFalse(html.contains("tanav2://"));
        assertFalse(html.contains("Coming soon"));
        ArgumentCaptor<QrScanEvent> event = ArgumentCaptor.forClass(QrScanEvent.class);
        verify(scans).save(event.capture());
        assertEquals("ios", event.getValue().getDownloadPlatform());
        assertEquals(7L, event.getValue().getPartnerId());
    }

    @Test
    void downloadWaitsForReleaseEvenWithAnExpoGoUrl() throws Exception {
        String html = service.recordScanAndBuildHandoff("download", null,
            "exp://192.168.1.2:8081/--/qr-open?type=download", new MockHttpServletRequest());
        assertTrue(html.contains("Coming soon."));
        assertFalse(html.contains("http-equiv=\"refresh\""));
        assertFalse(html.contains("exp://"));
        assertFalse(html.contains("intent://"));
        assertFalse(html.contains("tanav2://"));
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/qr-download-preview.html"), html);
    }

    @Test
    void sameDownloadEndpointRedirectsAfterReleaseAndStillCountsPartner() {
        ReflectionTestUtils.setField(service, "androidDownloadAvailable", true);
        ReflectionTestUtils.setField(service, "androidPlayStoreUrl",
            "https://play.google.com/store/apps/details?id=com.app.tana");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("partnerId", "7");
        String html = service.recordScanAndBuildHandoff("download", null, null, request);
        assertTrue(html.contains("http-equiv=\"refresh\""));
        assertTrue(html.contains("https://play.google.com/store/apps/details?id=com.app.tana"));
        assertFalse(html.contains("Coming soon."));
        ArgumentCaptor<QrScanEvent> event = ArgumentCaptor.forClass(QrScanEvent.class);
        verify(scans).save(event.capture());
        assertEquals(7L, event.getValue().getPartnerId());
    }

    @Test
    void invalidStoreConfigurationKeepsComingSoonPage() {
        ReflectionTestUtils.setField(service, "androidDownloadAvailable", true);
        ReflectionTestUtils.setField(service, "androidPlayStoreUrl", "javascript:alert(1)");
        String html = service.recordScanAndBuildHandoff("download", null, null, new MockHttpServletRequest());
        assertTrue(html.contains("Coming soon."));
        assertFalse(html.contains("javascript:"));
    }

    @Test
    void invalidPartnersCannotPolluteGeneralCounts() {
        for (String id : new String[]{"", "bad", "0", "-1", "999"}) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setParameter("partnerId", id);
            assertThrows(ResponseStatusException.class,
                () -> service.recordScanAndBuildHandoff("download", null, null, request));
        }
        verify(scans, never()).save(any());
    }
}
