CREATE TABLE IF NOT EXISTS RoutePartners (
    routePartnerId BIGINT NOT NULL AUTO_INCREMENT,
    partnerName VARCHAR(160) NOT NULL,
    description TEXT NULL,
    logoImage VARCHAR(512) NULL,
    helperText VARCHAR(255) NULL,
    active BIT NOT NULL DEFAULT 1,
    createdDate DATETIME(6) NULL,
    createdBy VARCHAR(128) NULL,
    PRIMARY KEY (routePartnerId),
    UNIQUE KEY uk_route_partner_name (partnerName),
    KEY idx_route_partner_name (partnerName),
    KEY idx_route_partner_active (active)
);

ALTER TABLE RouteMaster
    ADD COLUMN routePartnerId BIGINT NULL;

ALTER TABLE RouteMaster
    ADD CONSTRAINT fk_route_master_route_partner
        FOREIGN KEY (routePartnerId)
        REFERENCES RoutePartners (routePartnerId);

CREATE INDEX idx_route_partner
    ON RouteMaster (routePartnerId);
