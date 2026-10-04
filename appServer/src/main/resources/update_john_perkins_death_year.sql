-- One-time correction of John Perkins's death year. He died on March 13, 2026, at 95, not in 2023.
-- Fixes his lifespan and the one sentence of his biography that names the year. seed_figures.sql already carries these values.
-- Bumps updated_at so devices pick the change up on their next figure sync rather than at the weekly full sync.
-- Run once in the Supabase SQL Editor. The UPDATE matches on id and name, so a row whose name differs changes nothing.
-- Re-running changes nothing but updated_at.
BEGIN;

UPDATE figures SET lifespan = '1930-2026', bio = REPLACE(bio, 'He died in 2023,', 'He died in 2026,'), updated_at = (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT WHERE id = 65 AND name = 'John Perkins';

COMMIT;
