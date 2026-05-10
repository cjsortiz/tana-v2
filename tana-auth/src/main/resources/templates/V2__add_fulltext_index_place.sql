ALTER TABLE PlaceMaster
ADD FULLTEXT idx_place_search (name, town);