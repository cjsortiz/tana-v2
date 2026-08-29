CREATE TABLE IF NOT EXISTS TanaStory (
    tanaStoryId BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    tag VARCHAR(255),
    category VARCHAR(255),
    description VARCHAR(1000),
    image VARCHAR(255),
    linkUrl VARCHAR(1000) NOT NULL,
    displayOrder INT NOT NULL DEFAULT 1,
    active BIT NOT NULL DEFAULT 1,
    createdBy VARCHAR(255),
    createdDate DATETIME(6),
    modifiedBy VARCHAR(255),
    modifiedDate DATETIME(6),
    PRIMARY KEY (tanaStoryId),
    KEY idx_tana_story_active_order (active, displayOrder)
);
