-- Hibernate may already have added these columns before deployment migrations run.
SET @transport_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'PlaceMaster' AND COLUMN_NAME = 'habalHabalTricycle'),
    'SELECT 1',
    'ALTER TABLE PlaceMaster ADD COLUMN habalHabalTricycle TEXT NULL'
);
PREPARE transport_statement FROM @transport_sql;
EXECUTE transport_statement;
DEALLOCATE PREPARE transport_statement;

SET @transport_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'PlaceMaster' AND COLUMN_NAME = 'commute'),
    'SELECT 1',
    'ALTER TABLE PlaceMaster ADD COLUMN commute TEXT NULL'
);
PREPARE transport_statement FROM @transport_sql;
EXECUTE transport_statement;
DEALLOCATE PREPARE transport_statement;

SET @transport_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'PlaceMaster' AND COLUMN_NAME = 'walkFromDropOff'),
    'SELECT 1',
    'ALTER TABLE PlaceMaster ADD COLUMN walkFromDropOff TEXT NULL'
);
PREPARE transport_statement FROM @transport_sql;
EXECUTE transport_statement;
DEALLOCATE PREPARE transport_statement;

SET @transport_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'PlaceMaster' AND COLUMN_NAME = 'privateCarVan'),
    'SELECT 1',
    'ALTER TABLE PlaceMaster ADD COLUMN privateCarVan TEXT NULL'
);
PREPARE transport_statement FROM @transport_sql;
EXECUTE transport_statement;
DEALLOCATE PREPARE transport_statement;

