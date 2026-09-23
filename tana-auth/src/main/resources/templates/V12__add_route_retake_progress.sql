ALTER TABLE UserCompletedRoute
    ADD COLUMN restartedAt DATETIME(6) NULL,
    ADD COLUMN retakeVisitedPlaceIds JSON NULL,
    ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
