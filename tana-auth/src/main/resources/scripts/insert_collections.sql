INSERT IGNORE INTO CollectionsMaster (
    collectionName,
    overview,
    badge,
    spot,
    helperText,
    badgeOverview,
    collectionImage,
    moodType,
    moodPriority
) VALUES

-- Coastal / Nature
('Coastal Walks', 'Salty air and scenic paths made for slow afternoon strolls.', 'The Sand Collector', 1, 'SEA-FACING', 'You know the difference between a beach and a good beach. Not by the amenities, by the feeling. You''ve found the ones worth returning to, and you keep them close.', 'Coastal Walks.png', 4, 4),

('Golden Hour', 'Not all sunsets are loud. Some are quiet, grounding, and meant to be felt slowly.', 'The Sunset Chaser', 1, 'SUNSET SPOTS', 'You watch the light. Not everyone slows down enough to notice it, the way it softens before it disappears. You''ve learned to time your days around it.', 'Golden Hour.png', 1, 1),

('Hill Climber', 'For those who do not mind the steep road if it means getting the best view.', 'The Hill Folk', 1, 'HILLTOP VIEWS', 'You know the hills don''t ask anything of you except the climb. You show up anyway, usually early, usually alone, and the view is always worth it.', 'Hill Climber.png', NULL, NULL),

('From the Farm', 'Honoring the hands that feed us through farm-to-table finds and green spaces.', 'The Farm Believer', 1, 'FARM TO TABLE', 'You understand where things come from. Not just food, but effort, patience, and time. You''ve stood in a working farm and felt the weight of how much goes into something simple.', 'From the Farm.png', 4, 7),

('Higher Ground', 'For the brave who seek the highest points and the most rewarding summits.', 'The Summit Seeker', 1, 'MOUNTAINS', 'You don''t mind the climb. The steep road, the uneven trail, the moment your legs say stop. You keep going anyway because you already know what''s waiting at the top.', 'Higher Ground.png', 4, 2),

('Blue Deep', 'From hidden waterfalls to free diving spots—immerse yourself in the island''s waters.', 'The Deep Explorer', 1, 'DIVE & SNORKEL', 'You go under while everyone else stays on the surface. The reef, the current, the quiet below. You know the island goes deeper than most people ever see.', 'Blue Deep.png', 4, 3),

('Falls Chase', 'Chasing cascades tucked away from the main road. These ones you have to earn.', 'The Water Bender', 1, 'WATERFALL SPOTS', 'You follow the sound of water down unmarked trails. Most people walk past the turnoff. You''re the one who stops and asks where it leads.', 'Falls Chase.png', NULL, NULL),

-- Community / Culture
('Tambayan Circuit', 'Finding those in-between spaces where you can just sit, stay, and be.', 'The Tambay King', 2, 'HANGOUT', 'You know how to stay. Not everyone does. You''ve found the spaces where time moves slower and nobody rushes you out, and those are your favorite places in the world.', 'Tambayan Circuit.png', 1, 2),

('Heritage Walks', 'Stepping into the stories and stone walls that have stood the test of time.', 'The History Buff', 2, 'HISTORIC SITES', 'You read the walls. Every old building, every faded marker, every church that''s survived an earthquake. You''re the one who actually stops and looks.', 'Heritage Walks.png', 2, 1),

('Quiet Devotion', 'Serene spaces for reflection, prayer, and finding a moment of inner peace.', 'The Devoted', 2, 'REFLECTION SPACES', 'You find something in the stillness of these spaces that''s hard to explain. Whether it''s faith, reflection, or just the quiet, you keep coming back to it.', 'Quiet Devotion.png', 1, 3),

('Collective Pulse', 'Community-driven spaces where local energy and creativity come together.', 'The Social Butterfly', 2, 'COMMUNITY SPACES', 'You find the pulse wherever you go. The gathering spaces, the creative corners, the spots where local energy is highest. You don''t just visit a city, you plug into it.', 'Collective Pulse.png', 2, 2),

('Fiesta Frenzy', 'Diving headfirst into the colorful, crowded, and joyful heart of local celebrations.', 'The Festival Blood', 2, 'FESTIVALS', 'You show up for the city when it comes alive. The dancing, the noise, the crowd pressing in from all sides. You don''t just watch the fiesta. You''re in it.', 'Fiesta Frenzy.png', 2, 3),

('Nomad Regular', 'The go-to spots for a solid connection, good coffee, and a productive flow.', 'The WiFi Warrior', 2, 'WORK-FRIENDLY CAFES', 'You work from anywhere and you''ve found the spots that actually make it possible. Good signal, good coffee, a table that''s yours for as long as you need it.', 'Nomad Regular.png', NULL, NULL),

-- Food & Drink
('Matcha Hunt', 'A curated hunt for the finest green whisks and cozy corners in the city.', 'The Matcha Head', 3, 'SPECIALTY CAFES', 'You know the difference between earthy and bitter, between ceremonial and just green. You chase the right cup in the right space, and you don''t settle until the flavor is exactly what it should be.', 'Matcha Hunt.png', 3, 8),

('Delicacy Trail', 'A journey through the flavors that define us—from heritage recipes to local favorites.', 'The Pasalubong Pro', 3, 'LOCAL SWEETS', 'You never leave empty-handed. You know which delicacies are worth the detour, which recipes haven''t changed in decades, and exactly who to bring them home to.', 'Delicacy Trail.png', 3, 4),

('Tabo-an Canteens', 'Authentically local, no-frills dining where the best stories are shared over a meal.', 'The Canteen King', 3, 'LOCAL EATERIES', 'You eat where the locals eat. No menus with photos, no air conditioning, no pretense. Just real food, honest prices, and the best stories shared over a meal.', 'Tabo-an Canteens.png', 3, 1),

('Sisig Mayhem', 'A loud, sizzling tribute to the island''s most iconic comfort food.', 'The Sisig Master', 3, 'SIZZLING RESTOS', 'You respect the sizzle. You know a great sisig by smell before it even hits the table. Loud, hot, unapologetic. Exactly how it should be.', 'Sisig Mayhem.png', 3, 5),

('Chicken Run', 'The ultimate shortlist for the crispiest, juiciest, and most reliable poultry fix.', 'The Chicken Major', 3, 'FRIED CHICKEN', 'You have opinions about fried chicken. Strong ones. You''ve done the research, made the trips, and you know exactly which version of crispy is worth driving to.', 'Chicken Run.png', 3, 3),

('Sugar Rush', 'A sweet mission to find the best desserts, treats, and ice cream on the map.', 'The Sweet Tooth', 3, 'SWEET TREATS', 'You plan your day around dessert. The ice cream, the kakanin, the thing you spotted on someone else''s table. You always save room, always find it, always finish it.', 'Sugar Rush.png', 3, 6),

('Scenic Table', 'Good food tastes better when paired with a river view or a mountain breeze.', 'The View Eater', 3, 'FOOD & A VIEW', 'You believe a great view makes everything taste better. You''ve found the spots where good food and good scenery come together, and you''ll go back just to prove the point.', 'Scenic Table.png', 1, 6),

('Early Fuel', 'Warm meals and strong coffee to jumpstart your morning adventure.', 'The Early Bird', 3, 'MORNING EATS', 'You''re up before the city. First coffee, first meal, first light. You know the best version of the day belongs to the ones who show up early enough to claim it.', 'Early Fuel.png', 3, 2),

('Transit Treats', 'The best roadside stops for a quick bite while you are on the move.', 'The Road Muncher', 3, 'LOCAL STOPS', 'You eat on the move. The best stops aren''t on any map, they''re the ones you find by slowing down on the right road at the right time.', 'Transit Treats.png', 3, 7),

('After Hours', 'Dim lights and smooth drinks for when the night is just getting started.', 'The Smooth Operator', 3, 'BARS & SPEAKEASY''S', 'You know where the night goes. The right bar, the right drink, the right company. You move through the evening like you''ve done it a hundred times, because you have.', 'After Hours.png', 2, 4),

-- Sports & Wellness
('Afternoon Reset', 'That mid-day pause designed to recharge your spirit before the sun goes down.', 'The Soft Lander', 4, 'MIDDAY REST', 'You''ve mastered the art of the afternoon pause. The mid-day reset that everyone needs but few actually take. You protect that window and you always come out better for it.', 'Afternoon Reset.png', 1, 4),

('Active Ritual', 'Movement that feels like a reward—from morning jogs to evening drills.', 'The Fitness Grinder', 4, 'MOVE & SWEAT', 'You move on purpose. Court, studio, gym, wherever the work happens. You''ve built a routine out of it and you protect it like it matters, because it does.', 'Active Ritual.png', 4, 1),

('Glow State', 'Chasing the golden hour and the high-energy vibes that make the island feel alive.', 'The Glow Getter', 4, 'SELF-CARE', 'You invest in yourself without apology. The appointment, the treatment, the hour that''s entirely yours. You know that taking care of yourself is not a luxury, it''s a practice.', 'Glow State.png', 1, 5),

('Wild Rush', 'Bungee, zip lines, cliff jumping — anything that gets the heart going.', 'The Adrenaline Junkie', 4, 'ADRENALINE ADVENTURES', 'You don''t slow down for warning signs. You read them, acknowledge them, and jump anyway. Bohol''s wildest spots were made for people like you.', 'Wild Rush.png', NULL, NULL);