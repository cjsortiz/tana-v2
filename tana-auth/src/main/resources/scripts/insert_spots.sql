INSERT INTO spot (spotName, description)
SELECT 'Nature & Scenery', 'Beautiful landscapes'
WHERE NOT EXISTS (
    SELECT 1 FROM spot WHERE spotName = 'Nature & Scenery'
);

INSERT INTO spot (spotName, description)
SELECT 'Community & Culture', 'Local traditions'
WHERE NOT EXISTS (
    SELECT 1 FROM spot WHERE spotName = 'Community & Culture'
);

INSERT INTO spot (spotName, description)
SELECT 'Food & Drink', 'Food and drinks'
WHERE NOT EXISTS (
    SELECT 1 FROM spot WHERE spotName = 'Food & Drink'
);

INSERT INTO spot (spotName, description)
SELECT 'Sports & Wellness', 'Sports and wellness'
WHERE NOT EXISTS (
    SELECT 1 FROM spot WHERE spotName = 'Sports & Wellness'
);

INSERT INTO spot (spotName, description)
SELECT 'Events', 'Events'
WHERE NOT EXISTS (
    SELECT 1 FROM spot WHERE spotName = 'Events'
);