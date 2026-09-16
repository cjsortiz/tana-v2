CREATE TABLE IF NOT EXISTS UserCompletedRoute (
    userCompletedRouteId BIGINT NOT NULL AUTO_INCREMENT,
    accountId BIGINT NOT NULL,
    routeId BIGINT NOT NULL,
    completedAt DATETIME(6) NOT NULL,
    createdDate DATETIME(6) NULL,
    createdBy VARCHAR(128) NULL,
    PRIMARY KEY (userCompletedRouteId),
    UNIQUE KEY uk_completed_route_account_route (accountId, routeId),
    KEY idx_completed_route_account (accountId, completedAt),
    CONSTRAINT fk_completed_route_account FOREIGN KEY (accountId) REFERENCES AccountMaster (id),
    CONSTRAINT fk_completed_route_route FOREIGN KEY (routeId) REFERENCES RouteMaster (routeId)
);
