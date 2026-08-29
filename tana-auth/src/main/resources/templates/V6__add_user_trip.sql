CREATE TABLE IF NOT EXISTS UserTripSettings (
    userTripSettingsId BIGINT NOT NULL AUTO_INCREMENT,
    accountId BIGINT NOT NULL,
    startDate DATE NULL,
    endDate DATE NULL,
    dateLabel VARCHAR(128) NULL,
    createdDate DATETIME(6) NULL,
    createdBy VARCHAR(128) NULL,
    PRIMARY KEY (userTripSettingsId),
    UNIQUE KEY uk_user_trip_settings_account (accountId),
    CONSTRAINT fk_user_trip_settings_account
        FOREIGN KEY (accountId)
        REFERENCES AccountMaster (id)
);

CREATE TABLE IF NOT EXISTS UserTripItem (
    userTripItemId BIGINT NOT NULL AUTO_INCREMENT,
    accountId BIGINT NOT NULL,
    itemType VARCHAR(32) NOT NULL,
    itemId VARCHAR(255) NOT NULL,
    saved BIT NOT NULL DEFAULT 1,
    going BIT NOT NULL DEFAULT 0,
    createdDate DATETIME(6) NULL,
    createdBy VARCHAR(128) NULL,
    PRIMARY KEY (userTripItemId),
    UNIQUE KEY uk_user_trip_item (accountId, itemType, itemId),
    KEY idx_user_trip_item_account_saved (accountId, saved),
    CONSTRAINT fk_user_trip_item_account
        FOREIGN KEY (accountId)
        REFERENCES AccountMaster (id)
);
