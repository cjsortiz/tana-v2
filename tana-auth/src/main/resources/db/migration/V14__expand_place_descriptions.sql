-- Allow longer spot descriptions and tips, including on existing databases.
ALTER TABLE PlaceMaster
    MODIFY COLUMN overview TEXT NULL,
    MODIFY COLUMN tanaTip TEXT NULL;
