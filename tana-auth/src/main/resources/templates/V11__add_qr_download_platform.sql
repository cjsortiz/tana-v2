-- Run once only if Hibernate ddl-auto=update is not used.
ALTER TABLE QrScanEvent ADD COLUMN downloadPlatform VARCHAR(16) NULL;
-- Historical downloads without a platform remain in the legacy Android bucket.
