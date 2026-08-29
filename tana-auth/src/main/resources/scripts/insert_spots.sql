INSERT INTO Spot (spotName, description)
SELECT 'Nature & Scenery', 'Beautiful landscapes'
WHERE NOT EXISTS (
    SELECT 1 FROM Spot WHERE spotName = 'Nature & Scenery'
);

INSERT INTO Spot (spotName, description)
SELECT 'Community & Culture', 'Local traditions'
WHERE NOT EXISTS (
    SELECT 1 FROM Spot WHERE spotName = 'Community & Culture'
);

INSERT INTO Spot (spotName, description)
SELECT 'Food & Drink', 'Food and drinks'
WHERE NOT EXISTS (
    SELECT 1 FROM Spot WHERE spotName = 'Food & Drink'
);

INSERT INTO Spot (spotName, description)
SELECT 'Sports & Wellness', 'Sports and wellness'
WHERE NOT EXISTS (
    SELECT 1 FROM Spot WHERE spotName = 'Sports & Wellness'
);

INSERT INTO Spot (spotName, description)
SELECT 'Events', 'Events'
WHERE NOT EXISTS (
    SELECT 1 FROM Spot WHERE spotName = 'Events'
);