-- Curated bibliography: the real published works of each figure, independent of the quotes table.
-- Human-reviewed like seed_figures.sql. Drafted with AI help; every entry reviewed before shipping.
-- Run in the Supabase SQL Editor (after the server has started once, so the works table exists), or locally:
--   sqlite3 $DB_PATH < appServer/src/main/resources/seed_works.sql   (skip the final setval line on SQLite)
--
-- Conventions:
--   * Works the figure authored (wrote, dictated, delivered, or composed) have no recorded_by. Biographies
--     about a figure are never listed as the figure's own work; quotes recorded in one use a "Cited in ..." source.
--   * A book someone else wrote that preserves the figure's words is listed only in the Recorded Words section
--     at the end, with recorded_by naming its writer. See that section for when one is added.
--   * title and year are separate; year only when a single year is well established, else NULL.
--   * id = figure_id * 1000 + n, so editing one figure never renumbers another.
--   * Upsert, not DELETE + INSERT: re-running never wipes columns added later (e.g. a work's content).
--     Removing a work therefore needs an explicit DELETE FROM works WHERE id = ...;
--   * A trailing comment flags a judgment call for review (as-told-to, compilation, disputed, uncertain).


-- 1: Martin Luther
INSERT INTO works (id, figure_id, title, year) VALUES
(1001, 1, 'Ninety-Five Theses', 1517),
(1002, 1, 'Heidelberg Disputation', 1518),
(1003, 1, 'Address to the Christian Nobility of the German Nation', 1520),
(1004, 1, 'On the Babylonian Captivity of the Church', 1520),
(1005, 1, 'The Freedom of a Christian', 1520),
(1006, 1, 'Speech at the Diet of Worms', 1521),
(1007, 1, 'Preface to the Epistle to the Romans', 1522),
(1008, 1, 'German New Testament', 1522), -- known as the September Testament; Luther's translation
(1009, 1, 'On Secular Authority', 1523),
(1010, 1, 'The Bondage of the Will', 1525),
(1011, 1, 'Small Catechism', 1529),
(1012, 1, 'Large Catechism', 1529),
(1013, 1, 'A Mighty Fortress Is Our God', NULL), -- hymn; composed between 1527 and 1529, exact year uncertain
(1014, 1, 'Letter to Jerome Weller', 1530),
(1015, 1, 'A Simple Way to Pray', 1535),
(1016, 1, 'Commentary on Galatians', 1535),
(1017, 1, 'Smalcald Articles', 1537),
(1018, 1, 'Preface to Georg Rhau''s Symphoniae iucundae', 1538),
(1019, 1, 'Table Talk', 1566) -- compilation: Johannes Aurifaber, 1566 (sayings recorded by Luther's table companions)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 2: John Calvin
INSERT INTO works (id, figure_id, title, year) VALUES
(2001, 2, 'Commentary on Seneca''s De Clementia', 1532),
(2002, 2, 'Institutes of the Christian Religion', 1536), -- first edition 1536; definitive Latin edition 1559
(2003, 2, 'Reply to Sadoleto', 1539),
(2004, 2, 'Commentary on Romans', 1540),
(2005, 2, 'Ecclesiastical Ordinances', 1541),
(2006, 2, 'Short Treatise on the Lord''s Supper', 1541),
(2007, 2, 'Treatise on Relics', 1543),
(2008, 2, 'The Necessity of Reforming the Church', 1544),
(2009, 2, 'Letters of John Calvin', NULL) -- compilation: Jules Bonnet, 1855-1858
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 3: Dietrich Bonhoeffer
INSERT INTO works (id, figure_id, title, year) VALUES
(3001, 3, 'Sanctorum Communio', 1930),
(3002, 3, 'Act and Being', 1931),
(3003, 3, 'Creation and Fall', 1933),
(3004, 3, 'The Cost of Discipleship', 1937),
(3005, 3, 'Life Together', 1939),
(3006, 3, 'Psalms: The Prayer Book of the Bible', 1940),
(3007, 3, 'Who Am I?', 1944), -- poem written in Tegel prison
(3008, 3, 'By Gracious Powers', 1944), -- poem written in Gestapo prison, December 1944
(3009, 3, 'Ethics', 1949), -- compilation: Eberhard Bethge, 1949 (unfinished manuscripts)
(3010, 3, 'Letters and Papers from Prison', 1951) -- compilation: Eberhard Bethge, 1951 (German Widerstand und Ergebung); English 1953
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 4: Charles Spurgeon
INSERT INTO works (id, figure_id, title, year) VALUES
(4001, 4, 'The Saint and His Saviour', 1857),
(4002, 4, 'The Metropolitan Tabernacle Pulpit', NULL), -- sermon series published weekly/annually 1861-1917 (continuing the New Park Street Pulpit, 1855-1860)
(4003, 4, 'Morning and Evening', NULL), -- Morning by Morning (1865) and Evening by Evening (1868), later combined
(4004, 4, 'John Ploughman''s Talk', 1869),
(4005, 4, 'The Treasury of David', NULL), -- published in 7 volumes 1869-1885
(4006, 4, 'Lectures to My Students', 1875), -- first series 1875; further series followed
(4007, 4, 'Commenting and Commentaries', 1876),
(4008, 4, 'All of Grace', 1886),
(4009, 4, 'According to Promise', 1887),
(4010, 4, 'Faith''s Checkbook', 1888),
(4011, 4, 'Around the Wicket Gate', 1890),
(4012, 4, 'The Soul Winner', 1895), -- published posthumously from his lectures
(4013, 4, 'C.H. Spurgeon''s Autobiography', NULL) -- compilation: Susannah Spurgeon and J.W. Harrald, 1897-1900 (from his diary, letters, and records)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 5: John Wesley
INSERT INTO works (id, figure_id, title, year) VALUES
(5001, 5, 'Journal of John Wesley', NULL), -- kept 1735-1790; published in extracts from 1740
(5002, 5, 'Salvation by Faith', 1738), -- sermon preached at St Mary's, Oxford, 11 June 1738
(5003, 5, 'The Character of a Methodist', 1742),
(5004, 5, 'The Use of Money', NULL), -- sermon; preached 1744 per seed, printed in Sermons on Several Occasions vol. 4 (1760)
(5005, 5, 'Sermons on Several Occasions', 1746), -- first volume 1746; further volumes followed
(5006, 5, 'Primitive Physic', 1747),
(5007, 5, 'Explanatory Notes upon the New Testament', 1755),
(5008, 5, 'A Plain Account of Christian Perfection', 1766),
(5009, 5, 'Wesley''s Works', NULL), -- compilation: The Works of John Wesley; Wesley's own 32-vol. edition 1771-1774, later Thomas Jackson edition 1829-1831 (volume cites follow Jackson)
(5010, 5, 'Thoughts Upon Slavery', 1774),
(5011, 5, 'A Collection of Hymns for the Use of the People Called Methodists', 1780)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 6: John Wycliffe
INSERT INTO works (id, figure_id, title, year) VALUES
(6001, 6, 'On Divine Dominion', NULL), -- Latin De Dominio Divino; c. 1373-1374
(6002, 6, 'On Civil Dominion', NULL), -- Latin De Civili Dominio; c. 1375-1376
(6003, 6, 'On the Church', NULL), -- Latin De Ecclesia; c. 1378
(6004, 6, 'On the Truth of Holy Scripture', NULL), -- Latin De Veritate Sacrae Scripturae; c. 1378
(6005, 6, 'On the Office of King', NULL), -- Latin De Officio Regis; c. 1379
(6006, 6, 'On the Pastoral Office', NULL), -- Latin De Officio Pastorali; c. 1378-1379
(6007, 6, 'On the Eucharist', NULL), -- Latin De Eucharistia; c. 1379-1380
(6008, 6, 'On Simony', NULL), -- Latin De Simonia; c. 1380
(6009, 6, 'Trialogus', NULL), -- c. 1382-1383
(6010, 6, 'Wycliffe Bible', NULL) -- attribution disputed: English translation produced by Wycliffe's circle (Nicholas of Hereford, John Purvey); Wycliffe's personal share debated
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 7: William Tyndale
INSERT INTO works (id, figure_id, title, year) VALUES
(7001, 7, 'Prologue to the New Testament', 1525), -- prologue to the Cologne fragment of 1525
(7002, 7, 'Tyndale New Testament', 1526), -- first complete printed English New Testament (Worms)
(7003, 7, 'The Parable of the Wicked Mammon', 1528),
(7004, 7, 'The Obedience of a Christian Man', 1528),
(7005, 7, 'The Practice of Prelates', 1530),
(7006, 7, 'Tyndale Pentateuch', 1530),
(7007, 7, 'A Pathway into the Holy Scripture', NULL), -- revised from the 1525 prologue; published c. 1530-1532
(7008, 7, 'An Answer unto Sir Thomas More''s Dialogue', 1531)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 8: Jonathan Edwards
INSERT INTO works (id, figure_id, title, year) VALUES
(8001, 8, 'Resolutions', NULL), -- written 1722-1723
(8002, 8, 'A Divine and Supernatural Light', 1734),
(8003, 8, 'A Faithful Narrative of the Surprising Work of God', 1737),
(8004, 8, 'Personal Narrative', NULL), -- written c. 1739-1740; published posthumously
(8005, 8, 'Sinners in the Hands of an Angry God', 1741),
(8006, 8, 'The Distinguishing Marks of a Work of the Spirit of God', 1741),
(8007, 8, 'Some Thoughts Concerning the Present Revival of Religion in New England', 1742),
(8008, 8, 'A Treatise Concerning Religious Affections', 1746),
(8009, 8, 'An Humble Attempt', 1747),
(8010, 8, 'Freedom of the Will', 1754),
(8011, 8, 'Original Sin', 1758),
(8012, 8, 'The End for Which God Created the World', 1765), -- published posthumously in Two Dissertations
(8013, 8, 'The Nature of True Virtue', 1765), -- published posthumously in Two Dissertations
(8014, 8, 'A History of the Work of Redemption', 1774), -- compilation: John Erskine, 1774 (sermon series preached 1739)
(8015, 8, 'Charity and Its Fruits', 1852) -- compilation: Tryon Edwards, 1852 (sermon series preached 1738)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 9: George Whitefield
INSERT INTO works (id, figure_id, title, year) VALUES
(9001, 9, 'The Nature and Necessity of Our Regeneration or New Birth in Christ Jesus', 1737), -- sermon
(9002, 9, 'Journal of George Whitefield', NULL), -- Journals published in parts 1738-1741
(9003, 9, 'Christ the Believer''s Wisdom, Righteousness, Sanctification, and Redemption', NULL), -- sermon
(9004, 9, 'The Method of Grace', NULL), -- sermon
(9005, 9, 'The Lord Our Righteousness', NULL), -- sermon
(9006, 9, 'A Short Account of God''s Dealings with the Reverend Mr. George Whitefield', 1740),
(9007, 9, 'A Letter to the Inhabitants of Maryland, Virginia, North and South Carolina', 1740), -- open letter on the treatment of slaves
(9008, 9, 'A Further Account of God''s Dealings with the Reverend Mr. George Whitefield', 1747)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 10: John Knox
INSERT INTO works (id, figure_id, title, year) VALUES
(10001, 10, 'First Blast of the Trumpet Against the Monstrous Regiment of Women', 1558),
(10002, 10, 'Appellation to the Nobility', 1558),
(10003, 10, 'Letter to the Commonalty of Scotland', 1558), -- full title: The Copie of an Epistle ... unto the Commonaltie of Scotland
(10004, 10, 'Scots Confession', 1560), -- co-authored with five other ministers
(10005, 10, 'First Book of Discipline', 1560), -- co-authored with five other ministers
(10006, 10, 'Sermon on Isaiah 26', 1565), -- his only sermon printed in his lifetime (published 1566)
(10007, 10, 'History of the Reformation in Scotland', NULL) -- first printing 1587 suppressed; complete edition 1644; records his interviews with Mary Queen of Scots
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 11: Ulrich Zwingli
INSERT INTO works (id, figure_id, title, year) VALUES
(11001, 11, 'Of the Clarity and Certainty of the Word of God', 1522),
(11002, 11, 'On the Choice and Freedom of Foods', 1522),
(11003, 11, 'Sixty-Seven Articles', 1523),
(11004, 11, 'On Divine and Human Righteousness', 1523),
(11005, 11, 'Commentary on True and False Religion', 1525),
(11006, 11, 'Fidei Ratio', 1530),
(11007, 11, 'On the Providence of God', 1530),
(11008, 11, 'Exposition of the Christian Faith', 1531) -- addressed to Francis I; published 1536
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 12: Philip Melanchthon
INSERT INTO works (id, figure_id, title, year) VALUES
(12001, 12, 'On Correcting the Studies of Youth', 1518), -- Latin De corrigendis adolescentiae studiis; inaugural address at Wittenberg
(12002, 12, 'Loci Communes', 1521), -- first edition 1521; revised editions 1535 and 1543
(12003, 12, 'Augsburg Confession', 1530),
(12004, 12, 'Apology of the Augsburg Confession', 1531),
(12005, 12, 'Treatise on the Power and Primacy of the Pope', 1537)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 13: Karl Barth
INSERT INTO works (id, figure_id, title, year) VALUES
(13001, 13, 'The Epistle to the Romans', 1919), -- second, rewritten edition 1922
(13002, 13, 'The Word of God and the Word of Man', 1924), -- German Das Wort Gottes und die Theologie 1924; English 1928
(13003, 13, 'Anselm: Fides Quaerens Intellectum', 1931),
(13004, 13, 'Church Dogmatics', NULL), -- published in 13 part-volumes 1932-1967, unfinished
(13005, 13, 'Barmen Declaration', 1934), -- principal drafter; adopted by the Confessing Church synod
(13006, 13, 'Dogmatics in Outline', 1947), -- lectures of 1946; English 1949
(13007, 13, 'Protestant Theology in the Nineteenth Century', 1947),
(13008, 13, 'The Humanity of God', 1956),
(13009, 13, 'Evangelical Theology', 1962) -- German 1962; English 1963
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 14: John Owen
INSERT INTO works (id, figure_id, title, year) VALUES
(14001, 14, 'The Death of Death in the Death of Christ', 1647),
(14002, 14, 'Of the Death of Christ', 1650),
(14003, 14, 'The Doctrine of the Saints'' Perseverance', 1654),
(14004, 14, 'Vindiciae Evangelicae', 1655),
(14005, 14, 'The Mortification of Sin', 1656), -- original title: Of the Mortification of Sin in Believers
(14006, 14, 'Communion with the Triune God', 1657), -- original title: Of Communion with God the Father, Son, and Holy Ghost
(14007, 14, 'Of Temptation', 1658),
(14008, 14, 'Biblical Theology', 1661), -- Latin original Theologoumena Pantodapa; English title from 1994 translation
(14009, 14, 'Indwelling Sin', 1667),
(14010, 14, 'An Exposition of the Epistle to the Hebrews', NULL), -- published in 4 volumes 1668-1684
(14011, 14, 'Pneumatologia: A Discourse Concerning the Holy Spirit', 1674),
(14012, 14, 'Justification by Faith', 1677),
(14013, 14, 'The Grace and Duty of Being Spiritually Minded', 1681),
(14014, 14, 'The Glory of Christ', 1684) -- original title: Meditations and Discourses on the Glory of Christ
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 15: Richard Baxter
INSERT INTO works (id, figure_id, title, year) VALUES
(15001, 15, 'The Saints'' Everlasting Rest', 1650),
(15002, 15, 'The Reformed Pastor', 1656),
(15003, 15, 'A Call to the Unconverted', 1658),
(15004, 15, 'A Christian Directory', 1673),
(15005, 15, 'The Poor Man''s Family Book', 1674),
(15006, 15, 'Catholick Theologie', 1675),
(15007, 15, 'Methodus Theologiae Christianae', 1681),
(15008, 15, 'Reliquiae Baxterianae', 1696) -- compilation: Matthew Sylvester, 1696 (Baxter's autobiographical narrative)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 16: Thomas Cranmer
INSERT INTO works (id, figure_id, title, year) VALUES
(16001, 16, 'Preface to the Great Bible', 1540),
(16002, 16, 'Exhortation and Litany', 1544),
(16003, 16, 'Book of Homilies', 1547), -- Cranmer wrote several of the homilies
(16004, 16, 'Book of Common Prayer', 1549), -- principal compiler; revised 1552
(16005, 16, 'Defence of the True and Catholic Doctrine of the Sacrament', 1550),
(16006, 16, 'Forty-Two Articles', 1553),
(16007, 16, 'Address before his execution', 1556),
(16008, 16, 'Miscellaneous Writings and Letters of Thomas Cranmer', 1846) -- compilation: Parker Society, ed. John Edmund Cox, 1846 (his surviving letters, incl. those to Henry VIII)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 17: Martin Bucer
INSERT INTO works (id, figure_id, title, year) VALUES
(17001, 17, 'That No One Should Live for Himself but for Others', 1523),
(17002, 17, 'The Ground and Reason of the Articles', 1524), -- German Grund und Ursach
(17003, 17, 'Tetrapolitan Confession', 1530), -- principal drafter, with Wolfgang Capito
(17004, 17, 'Wittenberg Concord', 1536), -- co-drafted with Melanchthon and others
(17005, 17, 'Concerning the True Care of Souls', 1538), -- Latin/German Von der waren Seelsorge
(17006, 17, 'De Regno Christi', 1550) -- written for Edward VI; published 1557
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 18: William Carey
INSERT INTO works (id, figure_id, title, year) VALUES
(18001, 18, 'Enquiry into the Obligations of Christians to Use Means for the Conversion of the Heathens', 1792),
(18002, 18, 'Sermon at Nottingham', 1792), -- the 'Expect great things' sermon, 31 May 1792; text not preserved, only summaries
(18003, 18, 'A Grammar of the Bengalee Language', 1801),
(18004, 18, 'Bengali New Testament', 1801), -- Carey's translation, printed at Serampore
(18005, 18, 'Serampore Form of Agreement', 1805) -- co-authored with Joshua Marshman and William Ward
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 19: A.W. Tozer
INSERT INTO works (id, figure_id, title, year) VALUES
(19001, 19, 'Wingspread', 1943), -- biography of A.B. Simpson written by Tozer
(19002, 19, 'Let My People Go', 1947), -- biography of Robert Jaffray written by Tozer
(19003, 19, 'The Pursuit of God', 1948),
(19004, 19, 'The Divine Conquest', 1950),
(19005, 19, 'The Root of the Righteous', 1955),
(19006, 19, 'Born After Midnight', 1959),
(19007, 19, 'Of God and Men', 1960),
(19008, 19, 'The Knowledge of the Holy', 1961),
(19009, 19, 'That Incredible Christian', 1964),
(19010, 19, 'Man: The Dwelling Place of God', 1966) -- compilation: posthumous collection of Tozer's editorials, 1966
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 20: Jan Hus
INSERT INTO works (id, figure_id, title, year) VALUES
(20001, 20, 'Exposition of the Faith, the Decalogue, and the Lord''s Prayer', 1412),
(20002, 20, 'On the Church', 1413), -- Latin De Ecclesia
(20003, 20, 'On Simony', 1413), -- Czech O svatokupectví
(20004, 20, 'Postil', 1413), -- Czech sermon collection (Postila)
(20005, 20, 'The Letters of John Hus', 1904) -- compilation: English translation by Herbert B. Workman and R. Martin Pope, 1904 (incl. his letters from prison at Constance, 1415)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 21: Francis Schaeffer
INSERT INTO works (id, figure_id, title, year) VALUES
(21001, 21, 'The God Who Is There', 1968),
(21002, 21, 'Escape from Reason', 1968),
(21003, 21, 'The Mark of the Christian', 1970),
(21004, 21, 'Pollution and the Death of Man', 1970),
(21005, 21, 'True Spirituality', 1971),
(21006, 21, 'The Church Before the Watching World', 1971),
(21007, 21, 'He Is There and He Is Not Silent', 1972),
(21008, 21, 'Art and the Bible', 1973),
(21009, 21, 'Two Contents, Two Realities', 1974), -- paper delivered at the Lausanne Congress
(21010, 21, 'No Little People', 1974),
(21011, 21, 'How Should We Then Live?', 1976),
(21012, 21, 'Whatever Happened to the Human Race?', 1979), -- co-authored with C. Everett Koop
(21013, 21, 'A Christian Manifesto', 1981),
(21014, 21, 'The Great Evangelical Disaster', 1984)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 22: Martyn Lloyd-Jones
INSERT INTO works (id, figure_id, title, year) VALUES
(22001, 22, 'The Plight of Man and the Power of God', 1942),
(22002, 22, 'Truth Unchanged, Unchanging', 1951),
(22003, 22, 'Authority', 1958),
(22004, 22, 'Studies in the Sermon on the Mount', NULL), -- published in 2 volumes 1959-1960
(22005, 22, 'Spiritual Depression: Its Causes and Cures', 1965),
(22006, 22, 'Romans: An Exposition', NULL), -- 14-volume sermon series published 1970-2003
(22007, 22, 'Preaching and Preachers', 1971),
(22008, 22, 'God''s Way of Reconciliation: Studies in Ephesians 2', 1972),
(22009, 22, 'Joy Unspeakable', 1984), -- compilation: posthumous edition of his sermons on the baptism of the Spirit
(22010, 22, 'The Puritans: Their Origins and Successors', 1987) -- compilation: posthumous collection of his Puritan Conference addresses
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 23: Watchman Nee
INSERT INTO works (id, figure_id, title, year) VALUES
(23001, 23, 'The Spiritual Man', 1928), -- published in 3 volumes 1927-1928
(23002, 23, 'The Latent Power of the Soul', 1933),
(23003, 23, 'The Normal Christian Life', 1957), -- compilation: Angus Kinnear, 1957 (edited from Nee's spoken messages)
(23004, 23, 'Sit, Walk, Stand', 1957), -- compilation: Angus Kinnear, 1957 (edited from Nee's spoken messages)
(23005, 23, 'What Shall This Man Do?', 1961), -- compilation: Angus Kinnear, 1961 (edited from Nee's spoken messages)
(23006, 23, 'The Release of the Spirit', 1965), -- English edition 1965
(23007, 23, 'Changed into His Likeness', 1967), -- compilation: Angus Kinnear, 1967 (edited from Nee's spoken messages)
(23008, 23, 'Love Not the World', 1968), -- compilation: Angus Kinnear, 1968 (edited from Nee's spoken messages)
(23009, 23, 'Spiritual Authority', 1972) -- English edition 1972
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 24: François Fénelon
INSERT INTO works (id, figure_id, title, year) VALUES
(24001, 24, 'Treatise on the Education of Daughters', 1687),
(24002, 24, 'Maxims of the Saints', 1697), -- French Explication des maximes des saints; his central work on pure love
(24003, 24, 'The Adventures of Telemachus', 1699),
(24004, 24, 'Treatise on the Existence of God', 1712),
(24005, 24, 'Dialogues on Eloquence', 1718),
(24006, 24, 'The Spiritual Letters of Fénelon', NULL), -- compilation: posthumous collections of his spiritual letters from 1718 onward
(24007, 24, 'Christian Perfection', 1947) -- compilation: Mildred Whitney Stillman (ed.), 1947 (selections from his spiritual letters and writings, c. 1685-1715)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 25: Madame Guyon
INSERT INTO works (id, figure_id, title, year) VALUES
(25001, 25, 'Spiritual Torrents', NULL), -- written c. 1682; published 1704
(25002, 25, 'A Short and Easy Method of Prayer', 1685),
(25003, 25, 'Commentary on the Song of Songs', 1688),
(25004, 25, 'Autobiography of Madame Guyon', 1720) -- compilation: edited by Pierre Poiret, 1720 (her own autobiography, published posthumously)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 26: Thomas à Kempis
INSERT INTO works (id, figure_id, title, year) VALUES
(26001, 26, 'The Imitation of Christ', NULL), -- attribution disputed: long debated (Gerard Groote, Jean Gerson proposed); scholarly consensus favors Thomas à Kempis
(26002, 26, 'The Soliloquy of the Soul', NULL),
(26003, 26, 'The Garden of Roses', NULL),
(26004, 26, 'The Valley of Lilies', NULL),
(26005, 26, 'Sermons to the Novices', NULL),
(26006, 26, 'Prayers and Meditations on the Life of Christ', NULL),
(26007, 26, 'Chronicle of the Canons Regular of Mount St. Agnes', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 27: Brother Lawrence
INSERT INTO works (id, figure_id, title, year) VALUES
(27001, 27, 'The Practice of the Presence of God', NULL), -- compilation: Joseph de Beaufort, 1692 (maxims/letters) and 1694 (conversations)
(27002, 27, 'Spiritual Maxims', NULL) -- compilation: Joseph de Beaufort, 1692
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 28: Julian of Norwich
INSERT INTO works (id, figure_id, title, year) VALUES
(28001, 28, 'Revelations of Divine Love', NULL) -- Short Text and Long Text versions; Long Text completed c. 1393 or later
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 29: John of the Cross
INSERT INTO works (id, figure_id, title, year) VALUES
(29001, 29, 'The Ascent of Mount Carmel', NULL),
(29002, 29, 'The Dark Night of the Soul', NULL),
(29003, 29, 'Spiritual Canticle', NULL),
(29004, 29, 'The Living Flame of Love', NULL),
(29005, 29, 'Sayings of Light and Love', NULL),
(29006, 29, 'Precautions', NULL),
(29007, 29, 'Counsels to a Religious on How to Reach Perfection', NULL),
(29008, 29, 'Romances', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 30: Teresa of Ávila
INSERT INTO works (id, figure_id, title, year) VALUES
(30001, 30, 'The Life of Teresa of Jesus', 1565),
(30002, 30, 'The Way of Perfection', NULL),
(30003, 30, 'Interior Castle', 1577),
(30004, 30, 'The Foundations', NULL),
(30005, 30, 'Exclamations of the Soul to God', 1569),
(30006, 30, 'Meditations on the Song of Songs', NULL),
(30007, 30, 'Spiritual Testimonies', NULL),
(30008, 30, 'Letters of St. Teresa', NULL), -- compilation: her collected letters, published posthumously
(30009, 30, 'Poems', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 31: Bernard of Clairvaux
INSERT INTO works (id, figure_id, title, year) VALUES
(31001, 31, 'On Loving God', NULL),
(31002, 31, 'Steps of Humility and Pride', NULL),
(31003, 31, 'On Grace and Free Choice', NULL),
(31004, 31, 'Sermons on the Song of Songs', NULL),
(31005, 31, 'Letter 11', NULL),
(31006, 31, 'On Consideration', NULL),
(31007, 31, 'In Praise of the New Knighthood', NULL),
(31008, 31, 'Apologia to Abbot William', NULL),
(31009, 31, 'On Conversion', NULL),
(31010, 31, 'The Life of St. Malachy', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 32: Francis of Assisi
INSERT INTO works (id, figure_id, title, year) VALUES
(32001, 32, 'The Admonitions', NULL),
(32002, 32, 'The Canticle of the Sun', NULL),
(32003, 32, 'The Earlier Rule (Regula non bullata)', 1221),
(32004, 32, 'The Later Rule', 1223),
(32005, 32, 'The Testament of St. Francis', 1226),
(32006, 32, 'Letter to All the Faithful', NULL),
(32007, 32, 'Letter to Brother Leo', NULL),
(32008, 32, 'The Praises of God', NULL),
(32009, 32, 'A Salutation of the Virtues', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 33: Hildegard of Bingen
INSERT INTO works (id, figure_id, title, year) VALUES
(33001, 33, 'Scivias', NULL),
(33002, 33, 'Liber Vitae Meritorum', NULL),
(33003, 33, 'Liber Divinorum Operum', NULL),
(33004, 33, 'Ordo Virtutum', NULL),
(33005, 33, 'Symphonia', NULL),
(33006, 33, 'Physica', NULL),
(33007, 33, 'Causae et Curae', NULL),
(33008, 33, 'Lingua Ignota', NULL),
(33009, 33, 'Letters of Hildegard of Bingen', NULL) -- compilation: her correspondence, collected in her lifetime (Riesencodex) and in modern editions
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 34: Jean-Pierre de Caussade
INSERT INTO works (id, figure_id, title, year) VALUES
(34001, 34, 'Abandonment to Divine Providence', 1861), -- compilation: Henri Ramière, 1861; attribution disputed: some modern scholarship (e.g. Dominique Salin) questions Caussade's authorship of the core treatise
(34002, 34, 'Spiritual Letters', NULL), -- compilation: Henri Ramière, published with Abandonment
(34003, 34, 'Spiritual Instructions on the Various States of Prayer', 1741)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 35: Richard Rolle
INSERT INTO works (id, figure_id, title, year) VALUES
(35001, 35, 'The Fire of Love', NULL),
(35002, 35, 'Mending of Life', NULL),
(35003, 35, 'The Form of Living', NULL),
(35004, 35, 'Ego Dormio', NULL),
(35005, 35, 'The Commandment', NULL),
(35006, 35, 'English Psalter', NULL),
(35007, 35, 'Meditations on the Passion', NULL) -- attribution disputed: authorship of the English Meditations is not certain
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 36: Augustine of Hippo
INSERT INTO works (id, figure_id, title, year) VALUES
(36001, 36, 'Confessions', NULL),
(36002, 36, 'The City of God', NULL),
(36003, 36, 'On the Trinity', NULL),
(36004, 36, 'Enchiridion', NULL),
(36005, 36, 'On Christian Doctrine', NULL),
(36006, 36, 'On Free Choice of the Will', NULL),
(36007, 36, 'Sermons', NULL),
(36008, 36, 'Tractates on the Gospel of John', NULL),
(36009, 36, 'Expositions on the Psalms', NULL),
(36010, 36, 'Soliloquies', NULL),
(36011, 36, 'Retractations', NULL),
(36012, 36, 'On Nature and Grace', NULL),
(36013, 36, 'On the Spirit and the Letter', NULL),
(36014, 36, 'On Grace and Free Will', NULL),
(36015, 36, 'On the Predestination of the Saints', NULL),
(36016, 36, 'The Literal Meaning of Genesis', NULL),
(36017, 36, 'Against the Academics', NULL),
(36018, 36, 'On the Good of Marriage', NULL),
(36019, 36, 'Letters', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 37: Athanasius
INSERT INTO works (id, figure_id, title, year) VALUES
(37001, 37, 'On the Incarnation', NULL),
(37002, 37, 'Against the Heathen', NULL),
(37003, 37, 'Orations Against the Arians', NULL),
(37004, 37, 'Defence of the Nicene Council', NULL),
(37005, 37, 'Festal Letter 39', 367),
(37006, 37, 'History of the Arians', NULL),
(37007, 37, 'Letters to Serapion', NULL),
(37008, 37, 'Life of Antony', NULL),
(37009, 37, 'Apology Against the Arians', NULL),
(37010, 37, 'Apology to Constantius', NULL),
(37011, 37, 'Apology for His Flight', NULL),
(37012, 37, 'Letter to Marcellinus', NULL),
(37013, 37, 'On the Councils of Ariminum and Seleucia', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 38: John Chrysostom
INSERT INTO works (id, figure_id, title, year) VALUES
(38001, 38, 'On the Priesthood', NULL),
(38002, 38, 'Baptismal Instructions', NULL),
(38003, 38, 'Homilies on Matthew', NULL),
(38004, 38, 'Homilies on John', NULL),
(38005, 38, 'Homilies on Romans', NULL),
(38006, 38, 'Homilies on First Corinthians', NULL),
(38007, 38, 'Homilies on the Epistle to the Hebrews', NULL),
(38008, 38, 'Homilies on Acts', NULL),
(38009, 38, 'Homilies on Genesis', NULL),
(38010, 38, 'Homilies on the Statues', 387),
(38011, 38, 'On Wealth and Poverty', NULL), -- compilation: modern title for his sermons on Lazarus and the Rich Man (e.g. tr. Catharine Roth, 1981)
(38012, 38, 'Letters to Olympias', NULL),
(38013, 38, 'Paschal Homily', NULL) -- attribution disputed: traditionally attributed to Chrysostom
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 39: Origen
INSERT INTO works (id, figure_id, title, year) VALUES
(39001, 39, 'On First Principles', NULL),
(39002, 39, 'Contra Celsum', NULL),
(39003, 39, 'Commentary on John', NULL),
(39004, 39, 'On Prayer', NULL),
(39005, 39, 'Exhortation to Martyrdom', NULL),
(39006, 39, 'Homilies on Genesis', NULL),
(39007, 39, 'Homilies on Exodus', NULL),
(39008, 39, 'Homilies on Leviticus', NULL),
(39009, 39, 'Homilies on Luke', NULL),
(39010, 39, 'Commentary on Matthew', NULL),
(39011, 39, 'Commentary on Romans', NULL),
(39012, 39, 'Commentary on the Song of Songs', NULL),
(39013, 39, 'Dialogue with Heraclides', NULL),
(39014, 39, 'Hexapla', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 40: Polycarp — Only one authored work survives (Letter to the Philippians); Martyrdom of Polycarp is an account by the church of Smyrna, listed only because a quote cites his recorded words from it
INSERT INTO works (id, figure_id, title, year) VALUES
(40001, 40, 'Letter to the Philippians', NULL),
(40002, 40, 'Martyrdom of Polycarp', NULL) -- attribution disputed: written by the church of Smyrna (Marcion/Evarestus), not by Polycarp; records his words at his death
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 41: Ignatius of Antioch
INSERT INTO works (id, figure_id, title, year) VALUES
(41001, 41, 'Letter to the Ephesians', NULL),
(41002, 41, 'Letter to the Magnesians', NULL),
(41003, 41, 'Letter to the Trallians', NULL),
(41004, 41, 'Letter to the Romans', NULL),
(41005, 41, 'Letter to the Philadelphians', NULL),
(41006, 41, 'Letter to the Smyrnaeans', NULL),
(41007, 41, 'Letter to Polycarp', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 42: Irenaeus
INSERT INTO works (id, figure_id, title, year) VALUES
(42001, 42, 'Against Heresies', NULL),
(42002, 42, 'Proof of the Apostolic Preaching', NULL),
(42003, 42, 'Letter to Florinus', NULL), -- fragmentary: survives only in quotations by Eusebius
(42004, 42, 'Letter to Victor', NULL) -- fragmentary: survives only in quotations by Eusebius
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 43: Clement of Alexandria
INSERT INTO works (id, figure_id, title, year) VALUES
(43001, 43, 'Exhortation to the Greeks', NULL),
(43002, 43, 'The Instructor (Paedagogus)', NULL),
(43003, 43, 'Stromateis', NULL),
(43004, 43, 'Who Is the Rich Man That Shall Be Saved?', NULL),
(43005, 43, 'Excerpts from Theodotus', NULL),
(43006, 43, 'Hypotyposes', NULL) -- fragmentary: survives only in fragments
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 44: Cyprian of Carthage
INSERT INTO works (id, figure_id, title, year) VALUES
(44001, 44, 'On the Unity of the Church', 251),
(44002, 44, 'On the Lapsed', 251),
(44003, 44, 'On the Lord''s Prayer', NULL),
(44004, 44, 'On Mortality', NULL),
(44005, 44, 'On Works and Almsgiving', NULL),
(44006, 44, 'To Donatus', NULL),
(44007, 44, 'On the Dress of Virgins', NULL),
(44008, 44, 'On the Advantage of Patience', NULL),
(44009, 44, 'On Jealousy and Envy', NULL),
(44010, 44, 'To Demetrianus', NULL),
(44011, 44, 'Exhortation to Martyrdom, to Fortunatus', NULL),
(44012, 44, 'Letter 55', NULL),
(44013, 44, 'Letter 63', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 45: Basil the Great
INSERT INTO works (id, figure_id, title, year) VALUES
(45001, 45, 'On the Holy Spirit', 375),
(45002, 45, 'Homilies on the Hexameron', NULL),
(45003, 45, 'Letters', NULL),
(45004, 45, 'Moral Rules', NULL),
(45005, 45, 'The Long Rules', NULL),
(45006, 45, 'The Short Rules', NULL),
(45007, 45, 'Against Eunomius', NULL),
(45008, 45, 'Address to Young Men on Greek Literature', NULL),
(45009, 45, 'On Wealth and Poverty', NULL) -- compilation: modern title for his social homilies (e.g. 'I Will Tear Down My Barns', 'To the Rich')
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 46: Gregory of Nyssa
INSERT INTO works (id, figure_id, title, year) VALUES
(46001, 46, 'The Life of Moses', NULL),
(46002, 46, 'On the Soul and the Resurrection', NULL),
(46003, 46, 'Catechetical Oration', NULL),
(46004, 46, 'On the Making of Man', NULL),
(46005, 46, 'Against Eunomius', NULL),
(46006, 46, 'On the Holy Spirit', NULL),
(46007, 46, 'On Not Three Gods', NULL),
(46008, 46, 'On Virginity', NULL),
(46009, 46, 'Life of Macrina', NULL),
(46010, 46, 'Homilies on the Song of Songs', NULL),
(46011, 46, 'Homilies on the Beatitudes', NULL),
(46012, 46, 'Homilies on the Lord''s Prayer', NULL),
(46013, 46, 'Homilies on Ecclesiastes', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 47: Gregory of Nazianzus
INSERT INTO works (id, figure_id, title, year) VALUES
(47001, 47, 'Theological Orations', 380),
(47002, 47, 'Oration 2: In Defence of His Flight', NULL),
(47003, 47, 'Oration 6: On Peace', NULL),
(47004, 47, 'Oration 14: On Love for the Poor', NULL),
(47005, 47, 'Oration 16: On His Father''s Silence', NULL),
(47006, 47, 'Oration 38: On the Theophany', NULL),
(47007, 47, 'Oration 43: Funeral Oration on Basil the Great', NULL),
(47008, 47, 'Oration 45: On Holy Easter', NULL),
(47009, 47, 'Letter 101', NULL),
(47010, 47, 'Concerning His Own Life', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 48: Ambrose of Milan
INSERT INTO works (id, figure_id, title, year) VALUES
(48001, 48, 'On the Duties of the Clergy', NULL),
(48002, 48, 'On the Faith', NULL),
(48003, 48, 'On the Holy Spirit', 381),
(48004, 48, 'On the Mysteries', NULL),
(48005, 48, 'On the Sacraments', NULL), -- attribution disputed: formerly contested, now generally accepted as Ambrose's
(48006, 48, 'Hexameron', NULL),
(48007, 48, 'On Virginity', NULL),
(48008, 48, 'Exposition of the Gospel of Luke', NULL),
(48009, 48, 'On Penance', NULL),
(48010, 48, 'On Naboth', NULL),
(48011, 48, 'On the Death of Satyrus', NULL),
(48012, 48, 'On the Death of Theodosius', 395),
(48013, 48, 'Letters', NULL),
(48014, 48, 'Aeterne Rerum Conditor', NULL),
(48015, 48, 'Deus Creator Omnium', NULL),
(48016, 48, 'Veni Redemptor Gentium', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 49: Jerome
INSERT INTO works (id, figure_id, title, year) VALUES
(49001, 49, 'Vulgate', NULL), -- translation: Latin translation of the Bible, largely Jerome's own work
(49002, 49, 'Commentary on Isaiah', NULL),
(49003, 49, 'Commentary on Matthew', 398),
(49004, 49, 'Commentary on Ezekiel', NULL), -- c. 410-414; the preface to Book I laments the sack of Rome
(49005, 49, 'Lives of Illustrious Men', NULL),
(49006, 49, 'Life of Paul the First Hermit', NULL),
(49007, 49, 'Life of Hilarion', NULL),
(49008, 49, 'Life of Malchus', NULL),
(49009, 49, 'Against Helvidius', NULL),
(49010, 49, 'Against Jovinian', 393),
(49011, 49, 'Dialogue Against the Pelagians', 415),
(49012, 49, 'Letter 1 to Innocent', NULL),
(49013, 49, 'Letter 22 to Eustochium', 384),
(49014, 49, 'Letter 52 to Nepotianus', 394),
(49015, 49, 'Letter 60 to Heliodorus', 396),
(49016, 49, 'Letter 108 on the Death of Paula', 404)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 50: Justin Martyr
INSERT INTO works (id, figure_id, title, year) VALUES
(50001, 50, 'First Apology', NULL),
(50002, 50, 'Second Apology', NULL),
(50003, 50, 'Dialogue with Trypho', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 51: Martin Luther King Jr.
INSERT INTO works (id, figure_id, title, year) VALUES
(51001, 51, 'Stride Toward Freedom', 1958),
(51002, 51, 'Strength to Love', 1963),
(51003, 51, 'Letter from Birmingham Jail', 1963),
(51004, 51, 'I Have a Dream', 1963), -- speech at the March on Washington, 28 August 1963
(51005, 51, 'Why We Can''t Wait', 1964),
(51006, 51, 'Where Do We Go from Here: Chaos or Community?', 1967),
(51007, 51, 'The Trumpet of Conscience', 1967),
(51008, 51, 'Beyond Vietnam: A Time to Break Silence', 1967),
(51009, 51, 'I''ve Been to the Mountaintop', 1968), -- speech at Mason Temple, Memphis, 3 April 1968
(51010, 51, 'Nobel Peace Prize Acceptance Speech', 1964),
(51011, 51, 'Our God Is Marching On', 1965) -- speech at the conclusion of the Selma to Montgomery march; also known as 'How Long, Not Long'
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 52: William Wilberforce
INSERT INTO works (id, figure_id, title, year) VALUES
(52001, 52, 'A Practical View of Christianity', 1797), -- short title; full title 'A Practical View of the Prevailing Religious System of Professed Christians...'; modern abridged editions titled Real Christianity
(52002, 52, 'Speech on the Abolition of the Slave Trade', 1789), -- House of Commons, 12 May 1789
(52003, 52, 'A Letter on the Abolition of the Slave Trade', 1807),
(52004, 52, 'An Appeal to the Religion, Justice, and Humanity of the Inhabitants of the British Empire in Behalf of the Negro Slaves in the West Indies', 1823),
(52005, 52, 'Letter to William Hey', 1801) -- uncertain: specific letter not independently verified; Wilberforce and Hey did correspond
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 53: Harriet Tubman — No authored works; oral sayings recorded in biographies only

-- 54: Sojourner Truth — No written works (Truth could not read or write); dictated narrative and speeches recorded by others
INSERT INTO works (id, figure_id, title, year) VALUES
(54001, 54, 'Narrative of Sojourner Truth', 1850), -- as-told-to: Olive Gilbert
(54002, 54, 'Ain''t I a Woman?', 1851), -- attribution disputed: best-known wording is Frances Dana Gage's 1863 rendering; Marius Robinson's 1851 report differs substantially and lacks the refrain
(54003, 54, 'Speech at the American Equal Rights Association', 1867) -- delivered speech, recorded by others
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 55: Desmond Tutu
INSERT INTO works (id, figure_id, title, year) VALUES
(55001, 55, 'Crying in the Wilderness: The Struggle for Justice in South Africa', 1982), -- compilation: sermons and speeches edited by John Webster, 1982
(55002, 55, 'Hope and Suffering', 1983), -- compilation: sermons and speeches
(55003, 55, 'The Rainbow People of God', 1994), -- compilation: edited by John Allen, 1994
(55004, 55, 'No Future Without Forgiveness', 1999),
(55005, 55, 'God Has a Dream', 2004), -- full title: God Has a Dream: A Vision of Hope for Our Time
(55006, 55, 'Made for Goodness', 2010), -- co-authored: with Mpho Tutu
(55007, 55, 'The Book of Forgiving', 2014), -- co-authored: with Mpho Tutu
(55008, 55, 'The Book of Joy', 2016) -- co-authored: with the Dalai Lama and Douglas Abrams
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 56: Frederick Douglass
INSERT INTO works (id, figure_id, title, year) VALUES
(56001, 56, 'Narrative of the Life of Frederick Douglass', 1845),
(56002, 56, 'My Bondage and My Freedom', 1855),
(56003, 56, 'Life and Times of Frederick Douglass', 1881),
(56004, 56, 'The Heroic Slave', 1853),
(56005, 56, 'What to the Slave Is the Fourth of July?', 1852), -- speech at Corinthian Hall, Rochester, 5 July 1852
(56006, 56, 'Men of Color, to Arms!', 1863),
(56007, 56, 'Oration in Memory of Abraham Lincoln', 1876),
(56008, 56, 'Self-Made Men', NULL), -- lecture delivered many times from 1859 onward
(56009, 56, 'The Significance of Emancipation in the West Indies', 1857) -- address at Canandaigua, N.Y., August 3, 1857 (West India Emancipation)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 57: Abraham Lincoln
INSERT INTO works (id, figure_id, title, year) VALUES
(57001, 57, 'Speech to the Young Men''s Lyceum', 1838), -- commonly called the Lyceum Address
(57002, 57, 'Letter to Joshua Speed', 1855),
(57003, 57, 'House Divided Speech', 1858),
(57004, 57, 'Cooper Union Address', 1860),
(57005, 57, 'Farewell Address at Springfield', 1861),
(57006, 57, 'First Inaugural Address', 1861),
(57007, 57, 'Letter to Horace Greeley', 1862),
(57008, 57, 'Meditation on the Divine Will', NULL), -- undated fragment; traditionally dated September 1862, some scholars suggest 1864
(57009, 57, 'Emancipation Proclamation', 1863),
(57010, 57, 'Proclamation of a National Fast Day', 1863),
(57011, 57, 'Gettysburg Address', 1863),
(57012, 57, 'Letter to Eliza Gurney', 1864),
(57013, 57, 'Letter to Mrs. Bixby', 1864), -- attribution disputed: some historians attribute authorship to John Hay
(57014, 57, 'Second Inaugural Address', 1865)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 58: Corrie ten Boom
INSERT INTO works (id, figure_id, title, year) VALUES
(58001, 58, 'A Prisoner and Yet', 1947),
(58002, 58, 'Amazing Love', 1953),
(58003, 58, 'Not Good If Detached', 1957),
(58004, 58, 'Marching Orders for the End Battle', 1969),
(58005, 58, 'The Hiding Place', 1971), -- as-told-to: John and Elizabeth Sherrill
(58006, 58, 'Tramp for the Lord', 1974), -- as-told-to: Jamie Buckingham
(58007, 58, 'In My Father''s House', 1976), -- as-told-to: Carole C. Carlson
(58008, 58, 'Each New Day', 1977),
(58009, 58, 'He Cares, He Comforts', 1977),
(58010, 58, 'Clippings from My Notebook', 1982)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 59: Eric Liddell — Few authored works; most quotes come from biographies
INSERT INTO works (id, figure_id, title, year) VALUES
(59001, 59, 'The Disciplines of the Christian Life', 1985) -- posthumous: manuscript written at Weihsien internment camp, published 1985
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 60: Lord Shaftesbury — Few authored works beyond parliamentary speeches (collected 1868) and diaries; diaries known through Edwin Hodder's biography (1886)
INSERT INTO works (id, figure_id, title, year) VALUES
(60001, 60, 'Speeches of the Earl of Shaftesbury', 1868) -- full title: Speeches of the Earl of Shaftesbury upon Subjects Having Relation Chiefly to the Claims and Interests of the Labouring Class
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 61: Charles Finney
INSERT INTO works (id, figure_id, title, year) VALUES
(61001, 61, 'Lectures on Revivals of Religion', 1835),
(61002, 61, 'Sermons on Important Subjects', 1836),
(61003, 61, 'Lectures to Professing Christians', 1837),
(61004, 61, 'Lectures on Systematic Theology', 1846),
(61005, 61, 'The Character, Claims and Practical Workings of Freemasonry', 1869),
(61006, 61, 'Memoirs of Charles G. Finney', 1876) -- posthumous: written c. 1868, edited by James H. Fairchild, published 1876
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 62: John Newton
INSERT INTO works (id, figure_id, title, year) VALUES
(62001, 62, 'An Authentic Narrative', 1764),
(62002, 62, 'Twenty-Six Letters on Religious Subjects', 1774), -- published under the pseudonym Omicron
(62003, 62, 'Olney Hymns', 1779), -- co-authored: with William Cowper
(62004, 62, 'Amazing Grace', NULL), -- hymn written for 1 January 1773; first published in Olney Hymns (1779)
(62005, 62, 'Glorious Things of Thee Are Spoken', NULL), -- hymn; first published in Olney Hymns (1779)
(62006, 62, 'How Sweet the Name of Jesus Sounds', NULL), -- hymn; first published in Olney Hymns (1779)
(62007, 62, 'Cardiphonia', 1781),
(62008, 62, 'Messiah: Fifty Expository Discourses', 1786),
(62009, 62, 'Thoughts upon the African Slave Trade', 1788),
(62010, 62, 'Letters of John Newton', NULL) -- compilation: Banner of Truth edition, 1960 (letters edited by Josiah Bull, 1869)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 63: Olaudah Equiano — Single major authored work
INSERT INTO works (id, figure_id, title, year) VALUES
(63001, 63, 'The Interesting Narrative of the Life of Olaudah Equiano', 1789)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 64: Howard Thurman
INSERT INTO works (id, figure_id, title, year) VALUES
(64001, 64, 'The Negro Spiritual Speaks of Life and Death', 1947),
(64002, 64, 'Jesus and the Disinherited', 1949),
(64003, 64, 'Deep is the Hunger', 1951),
(64004, 64, 'Meditations of the Heart', 1953),
(64005, 64, 'The Creative Encounter', 1954),
(64006, 64, 'The Inward Journey', 1961),
(64007, 64, 'Disciplines of the Spirit', 1963),
(64008, 64, 'The Luminous Darkness', 1965),
(64009, 64, 'The Search for Common Ground', 1971),
(64010, 64, 'With Head and Heart: The Autobiography of Howard Thurman', 1979)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 65: John Perkins
INSERT INTO works (id, figure_id, title, year) VALUES
(65001, 65, 'Let Justice Roll Down', 1976),
(65002, 65, 'With Justice for All', 1982),
(65003, 65, 'Beyond Charity', 1993),
(65004, 65, 'Dream with Me', 2017), -- co-authored: with Karen Waddles
(65005, 65, 'One Blood', 2018) -- co-authored: with Karen Waddles
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 66: Isaac Newton
INSERT INTO works (id, figure_id, title, year) VALUES
(66001, 66, 'The Principia: Mathematical Principles of Natural Philosophy', 1687), -- General Scholium added in 2nd edition (1713)
(66002, 66, 'Letter to Richard Bentley', 1692),
(66003, 66, 'Letter to Robert Hooke', 1676), -- dated 5 February 1675 Old Style; source of 'standing on the shoulders of giants'
(66004, 66, 'Opticks', 1704), -- Query 31 appeared in later editions (1706 Latin, 1717 English)
(66005, 66, 'Arithmetica Universalis', 1707),
(66006, 66, 'The Chronology of Ancient Kingdoms Amended', 1728), -- posthumous
(66007, 66, 'Observations upon the Prophecies of Daniel', 1733), -- posthumous
(66008, 66, 'Method of Fluxions', 1736), -- posthumous
(66009, 66, 'An Historical Account of Two Notable Corruptions of Scripture', 1754) -- posthumous
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 67: Blaise Pascal
INSERT INTO works (id, figure_id, title, year) VALUES
(67001, 67, 'Essay on Conics', 1640),
(67002, 67, 'New Experiments Concerning the Vacuum', 1647),
(67003, 67, 'Memorial', 1654), -- private record of his conversion night, 23 November 1654, found sewn into his coat after his death
(67004, 67, 'The Provincial Letters', NULL), -- published 1656-1657
(67005, 67, 'Treatise on the Arithmetical Triangle', 1665), -- posthumous
(67006, 67, 'Pensées', 1670), -- posthumous: unfinished fragments, first published 1670
(67007, 67, 'Prayer to Ask God for the Proper Use of Sickness', NULL),
(67008, 67, 'The Art of Persuasion', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 68: C.S. Lewis
INSERT INTO works (id, figure_id, title, year) VALUES
(68001, 68, 'The Pilgrim''s Regress', 1933),
(68002, 68, 'The Allegory of Love', 1936),
(68003, 68, 'Out of the Silent Planet', 1938),
(68004, 68, 'The Problem of Pain', 1940),
(68005, 68, 'The Weight of Glory', 1941), -- sermon preached 8 June 1941
(68006, 68, 'The Screwtape Letters', 1942),
(68007, 68, 'Perelandra', 1943),
(68008, 68, 'The Abolition of Man', 1943),
(68009, 68, 'That Hideous Strength', 1945),
(68010, 68, 'The Great Divorce', 1945),
(68011, 68, 'Miracles', 1947),
(68012, 68, 'The Lion, the Witch and the Wardrobe', 1950),
(68013, 68, 'Mere Christianity', 1952),
(68014, 68, 'Surprised by Joy', 1955),
(68015, 68, 'Till We Have Faces', 1956),
(68016, 68, 'Reflections on the Psalms', 1958),
(68017, 68, 'The Four Loves', 1960),
(68018, 68, 'A Grief Observed', 1961), -- first published under the pseudonym N.W. Clerk
(68019, 68, 'Letters to Malcolm: Chiefly on Prayer', 1964), -- posthumous
(68020, 68, 'God in the Dock', 1970) -- compilation: essays edited by Walter Hooper, 1970
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 69: G.K. Chesterton
INSERT INTO works (id, figure_id, title, year) VALUES
(69001, 69, 'The Defendant', 1901),
(69002, 69, 'Heretics', 1905),
(69003, 69, 'Charles Dickens', 1906),
(69004, 69, 'Orthodoxy', 1908),
(69005, 69, 'The Man Who Was Thursday', 1908),
(69006, 69, 'The Napoleon of Notting Hill', 1904),
(69007, 69, 'What''s Wrong with the World', 1910),
(69008, 69, 'The Innocence of Father Brown', 1911),
(69009, 69, 'The Ballad of the White Horse', 1911),
(69010, 69, 'St. Francis of Assisi', 1923),
(69011, 69, 'The Everlasting Man', 1925),
(69012, 69, 'Saint Thomas Aquinas', 1933),
(69013, 69, 'Autobiography', 1936) -- posthumous
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 70: Francis Bacon
INSERT INTO works (id, figure_id, title, year) VALUES
(70001, 70, 'Essays', 1597), -- expanded editions 1612 and 1625
(70002, 70, 'Meditationes Sacrae', 1597),
(70003, 70, 'The Advancement of Learning', 1605),
(70004, 70, 'Novum Organum', 1620),
(70005, 70, 'The History of the Reign of King Henry VII', 1622),
(70006, 70, 'De Augmentis Scientiarum', 1623),
(70007, 70, 'New Atlantis', 1627) -- posthumous
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 71: Galileo Galilei
INSERT INTO works (id, figure_id, title, year) VALUES
(71001, 71, 'The Starry Messenger', 1610), -- Sidereus Nuncius
(71002, 71, 'Discourse on Floating Bodies', 1612),
(71003, 71, 'Letters on Sunspots', 1613),
(71004, 71, 'Letter to Benedetto Castelli', 1613),
(71005, 71, 'Letter to the Grand Duchess Christina', 1615),
(71006, 71, 'The Assayer', 1623), -- Il Saggiatore
(71007, 71, 'Dialogue Concerning the Two Chief World Systems', 1632),
(71008, 71, 'Two New Sciences', 1638)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 72: Johannes Kepler
INSERT INTO works (id, figure_id, title, year) VALUES
(72001, 72, 'Mysterium Cosmographicum', 1596), -- title page dated 1596; printed 1597
(72002, 72, 'Astronomia Nova', 1609),
(72003, 72, 'Dioptrice', 1611),
(72004, 72, 'On the Six-Cornered Snowflake', 1611), -- Strena seu de Nive Sexangula
(72005, 72, 'Epitome of Copernican Astronomy', NULL), -- published in parts 1617-1621
(72006, 72, 'Harmonices Mundi', 1619),
(72007, 72, 'Rudolphine Tables', 1627),
(72008, 72, 'Somnium', 1634) -- posthumous
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 73: George Washington Carver — Own writings are primarily agricultural bulletins; quotes come from biographies and a compilation
INSERT INTO works (id, figure_id, title, year) VALUES
(73001, 73, 'How to Grow the Peanut and 105 Ways of Preparing It for Human Consumption', 1916), -- Tuskegee Experiment Station Bulletin No. 31
(73002, 73, 'George Washington Carver: In His Own Words', 1987) -- compilation: Gary R. Kremer, 1987
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 74: Gregor Mendel — Scientific papers and letters only; no theological writings
INSERT INTO works (id, figure_id, title, year) VALUES
(74001, 74, 'Experiments on Plant Hybridization', 1866), -- Versuche über Pflanzen-Hybriden; read 1865, published 1866
(74002, 74, 'Letter to Carl Nägeli', 1867),
(74003, 74, 'On Hieracium-Hybrids Obtained by Artificial Fertilisation', 1869)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 75: Francis Collins
INSERT INTO works (id, figure_id, title, year) VALUES
(75001, 75, 'The Language of God', 2006),
(75002, 75, 'The Language of Life', 2010),
(75003, 75, 'Belief: Readings on the Reason for Faith', 2010), -- editor: anthology of others' writings compiled by Collins
(75004, 75, 'The Language of Science and Faith', 2011), -- co-authored: with Karl Giberson
(75005, 75, 'Is There a God and Does He Care About Me? The Testimony of BioLogos Founder Francis Collins', 2019) -- BioLogos article
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 76: Alexis de Tocqueville
INSERT INTO works (id, figure_id, title, year) VALUES
(76001, 76, 'Democracy in America', NULL),
(76002, 76, 'Recollections', 1893),
(76003, 76, 'The Old Regime and the Revolution', 1856),
(76004, 76, 'Memoir on Pauperism', 1835),
(76005, 76, 'On the Penitentiary System in the United States', 1833) -- co-authored with Gustave de Beaumont
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 77: Søren Kierkegaard
INSERT INTO works (id, figure_id, title, year) VALUES
(77001, 77, 'Either/Or', 1843),
(77002, 77, 'Fear and Trembling', 1843),
(77003, 77, 'Repetition', 1843),
(77004, 77, 'Philosophical Fragments', 1844),
(77005, 77, 'The Concept of Anxiety', 1844),
(77006, 77, 'Stages on Life''s Way', 1845),
(77007, 77, 'Concluding Unscientific Postscript', 1846),
(77008, 77, 'Two Ages', 1846),
(77009, 77, 'Purity of Heart Is to Will One Thing', 1847), -- English title of the first part of Upbuilding Discourses in Various Spirits (1847)
(77010, 77, 'Works of Love', 1847),
(77011, 77, 'Christian Discourses', 1848),
(77012, 77, 'The Sickness unto Death', 1849),
(77013, 77, 'The Lily of the Field and the Bird of the Air', 1849),
(77014, 77, 'Practice in Christianity', 1850),
(77015, 77, 'For Self-Examination', 1851),
(77016, 77, 'The Point of View for My Work as an Author', 1859), -- written 1848; published posthumously
(77017, 77, 'Attack upon Christendom', NULL), -- compilation: pamphlets incl. The Moment (1854-55), collected in English 1944
(77018, 77, 'Journals', NULL) -- compilation: posthumous editions of his journals and papers, e.g. Hong & Hong, Journals and Papers, 1967-78
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 78: William James
INSERT INTO works (id, figure_id, title, year) VALUES
(78001, 78, 'The Principles of Psychology', 1890),
(78002, 78, 'Psychology: Briefer Course', 1892),
(78003, 78, 'The Will to Believe and Other Essays', 1897), -- full title: The Will to Believe and Other Essays in Popular Philosophy
(78004, 78, 'Human Immortality', 1898),
(78005, 78, 'Talks to Teachers on Psychology', 1899),
(78006, 78, 'The Varieties of Religious Experience', 1902),
(78007, 78, 'Pragmatism', 1907),
(78008, 78, 'A Pluralistic Universe', 1909),
(78009, 78, 'The Meaning of Truth', 1909),
(78010, 78, 'Some Problems of Philosophy', 1911), -- unfinished; published posthumously
(78011, 78, 'Essays in Radical Empiricism', 1912) -- compilation: Ralph Barton Perry, 1912
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 79: Dorothy Sayers
INSERT INTO works (id, figure_id, title, year) VALUES
(79001, 79, 'Whose Body?', 1923),
(79002, 79, 'Strong Poison', 1930),
(79003, 79, 'Murder Must Advertise', 1933),
(79004, 79, 'The Nine Tailors', 1934),
(79005, 79, 'Gaudy Night', 1935),
(79006, 79, 'Busman''s Honeymoon', 1937),
(79007, 79, 'The Zeal of Thy House', 1937),
(79008, 79, 'Begin Here', 1940),
(79009, 79, 'Creed or Chaos?', NULL), -- 1940 address; essay collection Creed or Chaos? and Other Essays 1947 (UK), 1949 (US)
(79010, 79, 'The Mind of the Maker', 1941),
(79011, 79, 'The Man Born to Be King', 1943), -- radio play cycle broadcast 1941-42
(79012, 79, 'The Other Six Deadly Sins', 1943),
(79013, 79, 'Unpopular Opinions', 1946),
(79014, 79, 'The Lost Tools of Learning', 1947),
(79015, 79, 'Are Women Human?', NULL), -- compilation: Eerdmans, 1971 (her 1938 address and 1947 essay)
(79016, 79, 'Letters to a Diminished Church', 2004) -- compilation: W Publishing, 2004
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 80: John Lennox
INSERT INTO works (id, figure_id, title, year) VALUES
(80001, 80, 'God''s Undertaker: Has Science Buried God?', 2007),
(80002, 80, 'God and Stephen Hawking', 2011),
(80003, 80, 'Gunning for God', 2011),
(80004, 80, 'Seven Days That Divide the World', 2011),
(80005, 80, 'Against the Flow', 2015),
(80006, 80, 'Determined to Believe?', 2017),
(80007, 80, 'Can Science Explain Everything?', 2019),
(80008, 80, 'Where Is God in a Coronavirus World?', 2020),
(80009, 80, '2084: Artificial Intelligence and the Future of Humanity', 2020)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 81: Mother Teresa — Wrote no books herself; works are her speeches plus compilations of her sayings, talks and letters
INSERT INTO works (id, figure_id, title, year) VALUES
(81001, 81, 'Nobel Prize acceptance speech', 1979),
(81002, 81, 'Words to Love By', 1983), -- compilation: Ave Maria Press, 1983
(81003, 81, 'A Simple Path', 1995), -- compilation: Lucinda Vardey, 1995
(81004, 81, 'In the Heart of the World', 1997), -- compilation: Becky Benenate, 1997
(81005, 81, 'No Greater Love', 1997), -- compilation: Becky Benenate and Joseph Durepos, 1997
(81006, 81, 'Come Be My Light', 2007) -- compilation: Brian Kolodiejchuk, 2007 (private letters)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 82: Hudson Taylor
INSERT INTO works (id, figure_id, title, year) VALUES
(82001, 82, 'China''s Spiritual Need and Claims', 1865),
(82002, 82, 'Retrospect', NULL), -- often titled A Retrospect
(82003, 82, 'Union and Communion', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 83: Amy Carmichael
INSERT INTO works (id, figure_id, title, year) VALUES
(83001, 83, 'Things as They Are', 1903),
(83002, 83, 'Lotus Buds', 1909),
(83003, 83, 'Mimosa', 1924),
(83004, 83, 'Gold Cord', 1932),
(83005, 83, 'Rose from Brier', 1933),
(83006, 83, 'Ploughed Under', 1934),
(83007, 83, 'Gold by Moonlight', 1935),
(83008, 83, 'Toward Jerusalem', 1936),
(83009, 83, 'Windows', 1937),
(83010, 83, 'If', 1938),
(83011, 83, 'Kohila', 1939),
(83012, 83, 'Edges of His Ways', 1955), -- compilation: posthumous, from her devotional notes
(83013, 83, 'Thou Givest... They Gather', 1958), -- compilation: posthumous
(83014, 83, 'Candles in the Dark', 1981) -- compilation: her letters, 1981
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 84: David Livingstone
INSERT INTO works (id, figure_id, title, year) VALUES
(84001, 84, 'Letter to his father', 1838),
(84002, 84, 'Missionary Travels and Researches in South Africa', 1857),
(84003, 84, 'Dr. Livingstone''s Cambridge Lectures', 1858), -- compilation: ed. William Monk, 1858; includes his Senate House address of 4 December 1857
(84004, 84, 'Narrative of an Expedition to the Zambesi and Its Tributaries', 1865), -- co-authored with Charles Livingstone
(84005, 84, 'The Last Journals of David Livingstone', 1874) -- compilation: ed. Horace Waller, 1874 (posthumous)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 85: Jim Elliot — Published nothing in his lifetime; journals and letters published posthumously by Elisabeth Elliot (also quoted in her biography Shadow of the Almighty)
INSERT INTO works (id, figure_id, title, year) VALUES
(85001, 85, 'The Journals of Jim Elliot', 1978) -- compilation: ed. Elisabeth Elliot, 1978
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 86: William Booth
INSERT INTO works (id, figure_id, title, year) VALUES
(86001, 86, 'In Darkest England and the Way Out', 1890),
(86002, 86, 'O Boundless Salvation', 1893), -- hymn
(86003, 86, 'I''ll Fight', 1912), -- final public address, Royal Albert Hall, 9 May 1912; text known from contemporary reports
(86004, 86, 'The Founder Speaks Again', 1960) -- compilation: Cyril Barnes, 1960
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 87: Lottie Moon — No books; writings are letters, many printed in the Foreign Mission Journal, collected in Send the Light (2002)
INSERT INTO works (id, figure_id, title, year) VALUES
(87001, 87, 'Send the Light: Lottie Moon''s Letters and Other Writings', 2002) -- compilation: ed. Keith Harper, 2002 (her letters to the Foreign Mission Board, many first printed in the Foreign Mission Journal)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 88: Adoniram Judson — Few authored works beyond his Burmese translation/lexicography; best known through letters quoted in biographies
INSERT INTO works (id, figure_id, title, year) VALUES
(88001, 88, 'A Letter to the Women of America, on Female Dress', 1831),
(88002, 88, 'The Burmese Bible', NULL), -- translation; completed 1834
(88003, 88, 'A Dictionary, English and Burmese', 1849)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 89: Mary Slessor — No authored published works; her letters were never published as a standalone collection and survive mainly as quoted in W.P. Livingstone's biography Mary Slessor of Calabar (1916)

-- 90: Count Zinzendorf
INSERT INTO works (id, figure_id, title, year) VALUES
(90001, 90, 'Jesus, Still Lead On', NULL), -- hymn (Jesu, geh voran); text later revised by Christian Gregor
(90002, 90, 'Jesus, Thy Blood and Righteousness', 1739), -- hymn; English translation by John Wesley, 1740
(90003, 90, 'Nine Public Lectures on Important Subjects in Religion', 1746),
(90004, 90, 'Maxims', NULL) -- compilation: Maxims, Theological Ideas and Sentences, ed. John Gambold, 1751
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 91: George Müller
INSERT INTO works (id, figure_id, title, year) VALUES
(91001, 91, 'A Narrative of Some of the Lord''s Dealings with George Müller', 1837), -- first part; later parts through the 1880s
(91002, 91, 'Autobiography of George Müller', NULL), -- compilation: abridged from his Narrative by G. Fred Bergin, 1905
(91003, 91, 'Answers to Prayer', NULL) -- compilation: extracts from his Narrative, compiled by A.E.C. Brooks
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 92: Gladys Aylward — Only authored work is a told-to autobiography; most quotes come from biographies by others
INSERT INTO works (id, figure_id, title, year) VALUES
(92001, 92, 'The Little Woman', 1970) -- as-told-to: Christine Hunter
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 93: C.T. Studd
INSERT INTO works (id, figure_id, title, year) VALUES
(93001, 93, 'The Chocolate Soldier', NULL), -- pamphlet; commonly dated c. 1912
(93002, 93, 'Only One Life, ''Twill Soon Be Past', NULL) -- attribution disputed: poem widely attributed to Studd; authorship not firmly documented
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 94: Jonathan Goforth
INSERT INTO works (id, figure_id, title, year) VALUES
(94001, 94, 'By My Spirit', NULL),
(94002, 94, 'When the Spirit''s Fire Swept Korea', NULL) -- published posthumously
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 95: Elisabeth Elliot
INSERT INTO works (id, figure_id, title, year) VALUES
(95001, 95, 'Through Gates of Splendor', 1957),
(95002, 95, 'Shadow of the Almighty', 1958),
(95003, 95, 'The Savage My Kinsman', 1961),
(95004, 95, 'No Graven Image', 1966),
(95005, 95, 'A Slow and Certain Light', 1973),
(95006, 95, 'These Strange Ashes', 1975),
(95007, 95, 'Let Me Be a Woman', 1976),
(95008, 95, 'Love Has a Price Tag', 1979),
(95009, 95, 'Discipline: The Glad Surrender', 1982),
(95010, 95, 'Passion and Purity', 1984),
(95011, 95, 'A Chance to Die', 1987),
(95012, 95, 'A Path Through Suffering', 1990),
(95013, 95, 'Keep a Quiet Heart', 1995),
(95014, 95, 'Quest for Love', 1996),
(95015, 95, 'Secure in the Everlasting Arms', 2002),
(95016, 95, 'Suffering Is Never for Nothing', 2019) -- compilation: B&H, 2019, from her recorded talks
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 96: Frank Laubach
INSERT INTO works (id, figure_id, title, year) VALUES
(96001, 96, 'Letters by a Modern Mystic', 1937),
(96002, 96, 'Prayer: The Mightiest Force in the World', 1946),
(96003, 96, 'Teaching the World to Read', 1947),
(96004, 96, 'Wake Up or Blow Up', 1951),
(96005, 96, 'The Game with Minutes', NULL),
(96006, 96, 'Channels of Spiritual Power', 1954),
(96007, 96, 'Toward World Literacy', 1960), -- co-authored with Robert S. Laubach
(96008, 96, 'Forty Years with the Silent Billion', 1970)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 97: Samuel Zwemer
INSERT INTO works (id, figure_id, title, year) VALUES
(97001, 97, 'Arabia: The Cradle of Islam', 1900),
(97002, 97, 'Raymund Lull: First Missionary to the Moslems', 1902),
(97003, 97, 'The Moslem Doctrine of God', 1905),
(97004, 97, 'Islam: A Challenge to Faith', 1907),
(97005, 97, 'The Unoccupied Mission Fields of Africa and Asia', 1911),
(97006, 97, 'The Disintegration of Islam', 1916),
(97007, 97, 'The Glory of the Cross', 1928),
(97008, 97, 'The Cross Above the Crescent', 1941)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 98: Nate Saint — Only one short authored piece is openly available; his journals were published only within Hitt's Jungle Pilot (1959) and Elliot's Through Gates of Splendor (1957)
INSERT INTO works (id, figure_id, title, year) VALUES
(98001, 98, 'Letter to the Christian Airmen''s Missionary Fellowship', 1945) -- handwritten letter, transcribed by Wheaton College Archives
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 99: John G. Paton
INSERT INTO works (id, figure_id, title, year) VALUES
(99001, 99, 'John G. Paton: An Autobiography', 1889) -- as-told-to: edited by his brother James Paton
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- 100: Andrew Murray
INSERT INTO works (id, figure_id, title, year) VALUES
(100001, 100, 'Abide in Christ', NULL),
(100002, 100, 'Like Christ', 1884),
(100003, 100, 'With Christ in the School of Prayer', 1885),
(100004, 100, 'Holy in Christ', 1887),
(100005, 100, 'The Spirit of Christ', 1888),
(100006, 100, 'The Holiest of All', 1894),
(100007, 100, 'Absolute Surrender', NULL),
(100008, 100, 'Humility', NULL),
(100009, 100, 'Waiting on God', 1896),
(100010, 100, 'The Ministry of Intercession', NULL),
(100011, 100, 'The Two Covenants', 1898),
(100012, 100, 'Working for God', 1901),
(100013, 100, 'The Full Blessing of Pentecost', 1908),
(100014, 100, 'The Prayer Life', NULL)
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year;

-- Recorded Words: books by someone else that preserve a figure's own words, so every figure has a source to cite.
-- A briefing reaches for one only when the figure has fewer than five works of their own to fill its sources, so
-- only those figures are listed here. Each is drawn from that figure's "Cited in ..." quote sources, keeping just the
-- earliest one, since an early or eyewitness record is the one the model knows best.
-- id = figure_id * 1000 + 901, clear of the figure's own works. Needs the recorded_by column, which the server
-- adds on start.
INSERT INTO works (id, figure_id, title, year, recorded_by) VALUES
(53901, 53, 'Scenes in the Life of Harriet Tubman', 1869, 'Sarah Bradford'),
(59901, 59, 'The Flying Scotsman', 1981, 'Sally Magnusson'),
(60901, 60, 'The Life and Work of the Seventh Earl of Shaftesbury', 1886, 'Edwin Hodder'), -- authorised biography built on his diaries
(73901, 73, 'George Washington Carver: An American Biography', 1943, 'Rackham Holt'),
(74901, 74, 'Life of Mendel', 1932, 'Hugo Iltis'), -- English translation; German original 1924
(82901, 82, 'Hudson Taylor''s Spiritual Secret', 1932, 'Howard Taylor and Geraldine Taylor'),
(85901, 85, 'Shadow of the Almighty', 1958, 'Elisabeth Elliot'),
(86901, 86, 'The Life of General Booth', 1912, 'Hulda Friederichs'),
(87901, 87, 'Lottie Moon', 1927, 'Una Roberts Lawrence'),
(88901, 88, 'A Memoir of the Life and Labors of the Rev. Adoniram Judson', 1853, 'Francis Wayland'),
(89901, 89, 'Mary Slessor of Calabar', 1915, 'W.P. Livingstone'),
(90901, 90, 'Zinzendorf the Ecumenical Pioneer', 1962, 'A.J. Lewis'), -- the only record cited; modern
(92901, 92, 'The Small Woman', 1957, 'Alan Burgess'), -- written from interviews with Aylward
(93901, 93, 'C.T. Studd: Cricketer and Pioneer', 1933, 'Norman Grubb'),
(94901, 94, 'Goforth of China', 1937, 'Rosalind Goforth'), -- by his wife
(98901, 98, 'Through Gates of Splendor', 1957, 'Elisabeth Elliot') -- quotes his journals
ON CONFLICT (id) DO UPDATE SET figure_id = excluded.figure_id, title = excluded.title, year = excluded.year, recorded_by = excluded.recorded_by;

-- Reset Postgres sequence after explicit ID inserts
SELECT setval('works_id_seq', (SELECT MAX(id) FROM works));
