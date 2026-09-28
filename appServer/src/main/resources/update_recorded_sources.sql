-- One-time correction of the recorded-words sources added with seed_works.sql's Recorded Words section, and
-- of the "Cited in ..." quote sources that name the same books. All 16 recorded works were checked against library
-- catalogues; the two errors are fixed below, and quote sources that named a recorded book in a shortened or
-- different form now name it exactly as its works entry does. seed_works.sql and seed_quotes.sql already carry these
-- values. Run once in the Supabase SQL Editor, after update_quote_sources.sql. Re-running changes nothing.
BEGIN;

-- Booth: the author is Hulda Friederichs, and the book was first published in 1912.
-- https://openlibrary.org/books/OL13765437M (The life of General Booth, Hulda Friederichs, Nelson, 1912)
UPDATE works SET year = 1912, recorded_by = 'Hulda Friederichs' WHERE id = 86901;
UPDATE quotes SET source = 'Cited in Hulda Friederichs, The Life of General Booth (1912)' WHERE figure_id = 86 AND source = 'Cited in Hulda Friedrichs, The Life of General Booth (1913)';

-- Slessor: first published in 1915; the 1916 copies are later editions (the Internet Archive scan is the seventh).
-- https://openlibrary.org/books/OL18451816M (Mary Slessor of Calabar, W. P. Livingstone, Hodder and Stoughton, 1915)
UPDATE works SET year = 1915 WHERE id = 89901;
UPDATE quotes SET source = 'Cited in W.P. Livingstone, Mary Slessor of Calabar (1915)' WHERE figure_id = 89 AND source = 'Cited in W.P. Livingstone, Mary Slessor of Calabar (1916)';

-- Carver, Studd, Judson: the same book cited under a shortened title.
UPDATE quotes SET source = 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)' WHERE figure_id = 73 AND source = 'Cited in Rackham Holt, George Washington Carver (1943)';
UPDATE quotes SET source = 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)' WHERE figure_id = 93 AND source = 'Cited in Norman Grubb, C.T. Studd (1933)';
UPDATE quotes SET source = 'Cited in Francis Wayland, A Memoir of the Life and Labors of the Rev. Adoniram Judson (1853)' WHERE figure_id = 88 AND source = 'Cited in Francis Wayland, A Memoir (1853)';

-- Liddell: the writer first, like every other "Cited in" source.
UPDATE quotes SET source = 'Cited in Sally Magnusson, The Flying Scotsman (1981)' WHERE figure_id = 59 AND source = 'Cited in The Flying Scotsman by Sally Magnusson (1981)';

COMMIT;
