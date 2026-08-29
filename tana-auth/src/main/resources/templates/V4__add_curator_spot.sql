CREATE TABLE IF NOT EXISTS CuratorSpot (
    curatorSpotId BIGINT NOT NULL AUTO_INCREMENT,
    placeId BIGINT NOT NULL,
    displayOrder INT NOT NULL DEFAULT 1,
    proofLabel VARCHAR(255) NULL,
    active BIT NOT NULL DEFAULT 1,
    createdDate DATETIME(6) NULL,
    createdBy VARCHAR(128) NULL,
    PRIMARY KEY (curatorSpotId),
    UNIQUE KEY uk_curator_spot_place (placeId),
    KEY idx_curator_spot_place (placeId),
    KEY idx_curator_spot_active_order (active, displayOrder),
    CONSTRAINT fk_curator_spot_place
        FOREIGN KEY (placeId)
        REFERENCES PlaceMaster (id)
);
