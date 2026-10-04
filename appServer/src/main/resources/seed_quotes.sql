DELETE FROM quotes;

-- 1: Martin Luther
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(1, 'Speech at the Diet of Worms (1521)', 'My conscience is captive to the Word of God, I neither can nor will recant anything, since it is neither right nor safe to act against conscience.', 'conscience,truth,moral_courage,reformation', true),
(1, 'Preface to the Epistle to the Romans (1522)', 'Faith is a living, daring confidence in God''s grace, so sure and certain that a man would stake his life on it a thousand times.', 'faith,grace,confidence,assurance', true),
(1, 'A Mighty Fortress Is Our God, hymn (1529)', 'A mighty fortress is our God, a bulwark never failing; our helper He amid the flood of mortal ills prevailing.', 'sovereignty,protection,refuge,hope', true),
(1, 'Ninety-Five Theses, Thesis 1 (1517)', 'Our Lord and Master Jesus Christ, when He said Poenitentiam agite, willed that the whole life of believers should be repentance.', 'repentance,discipleship,obedience,reformation', true),
(1, 'The Freedom of a Christian (1520)', 'A Christian man is the most free lord of all, and subject to none; a Christian man is the most dutiful servant of all, and subject to every one.', 'freedom,service,paradox,discipleship', true),
(1, 'Table Talk (1566)', 'Prayer is a strong wall, and a fort of the church; it is a godly Christian''s weapon, which no man knoweth nor findeth, but only he who hath the spirit of grace and of prayer.', 'prayer,spiritual_warfare,faith,protection', true),
(1, 'Large Catechism, First Commandment (1529)', 'That now, I say, upon which you set your heart and put your trust is properly your god.', 'idolatry,heart,worship,truth', true),
(1, 'The Bondage of the Will (1525)', 'But a man cannot be thoroughly humbled, until he comes to know that his salvation is utterly beyond his own powers, counsel, endeavours, will, and works, and absolutely depending on the will, counsel, pleasure, and work of another, that is, of God only.', 'humility,grace,sovereignty,salvation', true),
(1, 'Letter to Jerome Weller (1530)', 'You should not believe your conscience and your feelings more than the word which the Lord who receives sinners preaches to you.', 'conscience,scripture,assurance,grace', false),
(1, 'Preface to Georg Rhau''s Symphoniae iucundae (1538)', 'Next to the Word of God, music deserves the highest praise.', 'worship,beauty,word_of_god,gratitude', true);

-- 2: John Calvin
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(2, 'Institutes of the Christian Religion, I.1.1 (1559)', 'Our wisdom, in so far as it ought to be deemed true and solid wisdom, consists almost entirely of two parts: the knowledge of God and of ourselves.', 'wisdom,knowledge_of_god,self-knowledge,theology', true),
(2, 'Institutes of the Christian Religion, III.7.1 (1559)', 'We are not our own; therefore, neither is our own reason or will to rule our acts and counsels. We are not our own; therefore, let us not make it our end to seek what may be agreeable to our carnal nature.', 'self-denial,sovereignty,discipleship,humility', true),
(2, 'Institutes of the Christian Religion, III.2.36 (1559)', 'The word is not received in faith when it merely flutters in the brain, but when it has taken deep root in the heart.', 'faith,scripture,heart,transformation', true),
(2, 'Institutes of the Christian Religion, IV.1.9 (1559)', 'Wherever we see the word of God sincerely preached and heard, wherever we see the sacraments administered according to the institution of Christ, there we cannot have any doubt that the Church of God has some existence.', 'church,word_of_god,sacraments,ecclesiology', true),
(2, 'Institutes of the Christian Religion, III.20, chapter title (1559)', 'Prayer, the principal exercise of faith, and the medium of our daily reception of divine blessings.', 'prayer,faith,grace,devotion', true),
(2, 'Institutes of the Christian Religion, I.1.2 (1559)', 'It is evident that man never attains to a true self-knowledge until he has previously contemplated the face of God, and come down after such contemplation to look into himself.', 'self-knowledge,humility,contemplation,theology', true),
(2, 'Institutes of the Christian Religion, III.2.34 (1559)', 'Here human discernment is so defective and lost, that the first step of advancement in the school of Christ is to renounce it. Like a veil interposed, it prevents us from beholding divine mysteries, which are revealed only to babes.', 'humility,pride,spirit,wisdom', true),
(2, 'Institutes of the Christian Religion, III.6.4 (1559)', 'Doctrine is not an affair of the tongue, but of the life; is not apprehended by the intellect and memory merely, like other branches of learning; but is received only when it possesses the whole soul, and finds its seat and habitation in the inmost recesses of the heart.', 'gospel,transformation,discipleship,heart', true),
(2, 'Letters of John Calvin (circa 1550)', 'There is not one blade of grass, there is no color in this world that is not intended to make men rejoice.', 'joy,creation,gratitude,beauty', false),
(2, 'Institutes of the Christian Religion, III.20.16 (1559)', 'Even our stammering is tolerated by God, and pardon is granted to our ignorance as often as any thing rashly escapes us: indeed, without this indulgence, we should have no freedom to pray.', 'prayer,grace,mercy,forgiveness', true);

-- 3: Dietrich Bonhoeffer
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(3, 'The Cost of Discipleship (1937)', 'When Christ calls a man, he bids him come and die.', 'discipleship,sacrifice,obedience,cross', true),
(3, 'The Cost of Discipleship (1937)', 'Cheap grace is the deadly enemy of our church. We are fighting today for costly grace.', 'grace,discipleship,repentance,cost', true),
(3, 'Life Together (1939)', 'The first service that one owes to others in the fellowship consists in listening to them.', 'service,community,love,humility', true),
(3, 'Letters and Papers from Prison (1953)', 'We must learn to regard people less in the light of what they do or omit to do, and more in the light of what they suffer.', 'compassion,suffering,empathy,love', true),
(3, 'Letters and Papers from Prison (1953)', 'God lets himself be pushed out of the world on to the cross. He is weak and powerless in the world, and that is precisely the way, the only way, in which he is with us and helps us.', 'cross,suffering,incarnation,presence', true),
(3, 'Letters and Papers from Prison (1953)', 'I believe that God can and will bring good out of evil, even out of the greatest evil.', 'hope,sovereignty,suffering,providence', true),
(3, 'Letters and Papers from Prison, After Ten Years (1953)', 'The ultimate question for a responsible man to ask is not how he is to extricate himself heroically from the affair, but how the coming generation is to live.', 'responsibility,justice,sacrifice,future', true),
(3, 'Letters and Papers from Prison, A Wedding Sermon from a Prison Cell (1953)', 'It is not your love that sustains the marriage, but from now on, the marriage that sustains your love.', 'covenant,faithfulness,love,commitment', true),
(3, 'The Cost of Discipleship (1937)', 'Silence in the face of evil is itself evil: God will not hold us guiltless. Not to speak is to speak. Not to act is to act.', 'justice,moral_courage,truth,responsibility', false),
(3, 'Letters and Papers from Prison (1953)', 'I''m still discovering right up to this moment, that it is only by living completely in this world that one learns to have faith.', 'faith,incarnation,worldliness,discipleship', true);

-- 4: Charles Spurgeon
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(4, 'John Ploughman''s Talk (1869)', 'It is not how much we have, but how much we enjoy, that makes happiness.', 'joy,contentment,gratitude,simplicity', true),
(4, 'All of Grace (1886)', 'No man can come to Christ unless the Father draws him, and yet it is equally certain that every man who will come to Christ may come.', 'salvation,grace,sovereignty,calling', false),
(4, 'The Treasury of David, Vol. I (1865)', 'Prayer pulls the rope down below and the great bell rings above in the ears of God.', 'prayer,faith,intercession,devotion', false),
(4, 'Lectures to My Students, Vol. I (1875)', 'A sorrow shared is a sorrow halved; a joy shared is a joy doubled.', 'community,compassion,joy,service', false),
(4, 'Metropolitan Tabernacle Pulpit, Vol. 34, She Was Not Hid (1888)', 'Have you no wish for others to be saved? Then you are not saved yourself. Be sure of that.', 'evangelism,love,compassion,mission', true),
(4, 'Morning and Evening (1865)', 'By perseverance the snail reached the ark.', 'perseverance,faithfulness,patience,hope', false),
(4, 'Morning and Evening (1865)', 'Our anxiety does not empty tomorrow of its sorrows, but only empties today of its strengths.', 'trust,anxiety,faith,peace', false),
(4, 'Lectures to My Students, Vol. I (1875)', 'Visit many good books, but live in the Bible.', 'scripture,wisdom,devotion,truth', false),
(4, 'Metropolitan Tabernacle Pulpit, Vol. 17, The Talking Book (1871)', 'Nobody ever outgrows Scripture; the book widens and deepens with our years.', 'scripture,growth,wisdom,word_of_god', true),
(4, 'Metropolitan Tabernacle Pulpit, Vol. 37, The Best Strengthening Medicine (1891)', 'God does not need your strength: he has more than enough of power of his own. He asks your weakness: he has none of that himself.', 'grace,humility,weakness,dependence', true);

-- 5: John Wesley
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(5, 'Letter to Various Friends (collected in Works, Vol. 13)', 'Do all the good you can, by all the means you can, in all the ways you can, in all the places you can, at all the times you can, to all the people you can, as long as ever you can.', 'service,love,compassion,diligence', false),
(5, 'Journal of John Wesley (May 24, 1738)', 'I felt my heart strangely warmed. I felt I did trust in Christ, Christ alone, for salvation; and an assurance was given me that He had taken away my sins, even mine, and saved me from the law of sin and death.', 'conversion,faith,assurance,salvation', true),
(5, 'The Use of Money, sermon (1744)', 'Having, First, gained all you can, and, Secondly saved all you can, Then "give all you can."', 'stewardship,generosity,justice,service', true),
(5, 'Wesley''s Works, Vol. 14: Preface to Hymns and Sacred Poems (1739)', 'The gospel of Christ knows of no religion, but social; no holiness but social holiness.', 'holiness,community,social_justice,love', true),
(5, 'Wesley''s Works, Vol. 5: Sermon — Catholic Spirit (1750)', 'Though we cannot think alike, may we not love alike? May we not be of one heart, though we are not of one opinion?', 'unity,love,tolerance,community', true),
(5, 'Wesley''s Works, Vol. 7: On Perfection (1784)', 'The longer I live, the larger allowances I make for human infirmities.', 'grace,compassion,humility,patience', false),
(5, 'Sermon: Salvation by Faith (1738)', 'Faith is the only condition of justification. There is, therefore, no merit in man: merit is in Christ alone.', 'faith,salvation,grace,justification', false),
(5, 'Journal of John Wesley (June 11, 1739)', 'I look upon all the world as my parish; thus far I mean, that, in whatever part of it I am, I judge it meet, right, and my bounden duty to declare unto all that are willing to hear, the glad tidings of salvation.', 'mission,evangelism,calling,boldness', true),
(5, 'Letter to a Friend (1791)', 'I have no more fear of death than of going to sleep at night.', 'hope,eternal_life,peace,faith', false),
(5, 'Sermons on Several Occasions, Preface, §5 (1746)', 'I want to know one thing,—the way to heaven; how to land safe on that happy shore. God himself has condescended to teach the way.', 'scripture,truth,salvation,simplicity', true),
(5, 'Salvation by Faith, sermon, Introduction §3 (1738)', 'Grace is the source, faith the condition, of salvation.', 'grace,faith,salvation,assurance', true),
(5, 'The Character of a Methodist, §8 (1742)', 'In retirement or company, in leisure, business, or conversation, his heart is ever with the Lord. Whether he lie down or rise up, God is in all his thoughts; he walks with God continually, having the loving eye of his mind still fixed upon him, and everywhere "seeing Him that is invisible."', 'prayer,devotion,presence_of_god,faithfulness', true),
(5, 'The Character of a Methodist, §9 (1742)', 'And he accordingly loves his neighbour as himself; he loves every man as his own soul. His heart is full of love to all mankind, to every child of "the Father of the spirits of all flesh."', 'love,neighbor,compassion,community', true);

-- 6: John Wycliffe
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(6, 'On the Truth of Holy Scripture (De Veritate Sacrae Scripturae, 1378)', 'Holy Scripture is the highest authority for every Christian and the standard of faith and of all human perfection.', 'scripture,authority,truth,faith', true),
(6, 'On the Church (De Ecclesia, 1378)', 'The pope is not above Scripture; a man is not to preach the sayings of a man but the Word of God.', 'scripture,truth,authority,reformation', false),
(6, 'Trialogus (1383)', 'I believe that in the end truth will conquer.', 'truth,hope,faith,courage', false),
(6, 'On Simony (De Simonia, 1380)', 'The clergy have too great riches and power, contrary to Christ''s example.', 'justice,reformation,humility,truth', false),
(6, 'Sermon preached at Oxford (circa 1376)', 'Christ is the head of the Church, not the pope. We must follow Christ''s teaching, not the decrees of men.', 'truth,authority,reformation,obedience', false);

-- 7: William Tyndale
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(7, 'Cited in John Foxe, Actes and Monuments (1563)', 'I defy the Pope and all his laws… if God spare my life ere many years, I will cause a boy that driveth the plough, shall know more of the scripture than thou doest.', 'scripture,courage,mission,truth', true),
(7, 'Tyndale New Testament, W.T. unto the Reader (1534)', 'The nature of God''s word is, that whosoever read it or hear it reasoned and disputed before him, it will begin immediately to make him every day better and better.', 'scripture,transformation,word_of_god,faith', true),
(7, 'The Obedience of a Christian Man (1528)', 'Nay I had rather be so poor that I had not wherewith to buy me bread, than to have all the world, so I might do good unto no man therewithal.', 'service,humility,generosity,love', false),
(7, 'Tyndale Pentateuch, A Prologue Shewing the Use of the Scripture (1530)', 'The scripture is a light, and sheweth us the true way, both what to do and what to hope for; and a defence from all error, and a comfort in adversity that we despair not, and feareth us in prosperity that we sin not.', 'scripture,hope,truth,guidance', true),
(7, 'The Obedience of a Christian Man (1528)', 'The key of understanding Scripture is charity; love is its key.', 'love,scripture,truth,wisdom', false);

-- 8: Jonathan Edwards
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(8, 'Resolutions (1722)', 'Resolved, to live with all my might, while I do live.', 'diligence,purpose,faith,obedience', true),
(8, 'Resolutions (1722)', 'Resolved, never to do anything, which I should be afraid to do, if it were the last hour of my life.', 'holiness,conscience,obedience,eternity', true),
(8, 'Sinners in the Hands of an Angry God (1741)', 'The God that holds you over the pit of hell, much as one holds a spider or some loathsome insect over the fire, abhors you.', 'judgment,sin,repentance,fear_of_god', true),
(8, 'A Treatise Concerning Religious Affections (1746)', 'True religion, in great part, consists in holy affections.', 'holiness,heart,affections,worship', true),
(8, 'A Treatise Concerning Religious Affections (1746)', 'Hence gracious affections do not tend to make men bold, forward, noisy, and boisterous; but rather to speak trembling.', 'humility,grace,holiness,character', true),
(8, 'A Treatise Concerning Religious Affections (1746)', 'The devil never rejoices more than when he sees a Christian neglect his Bible.', 'scripture,spiritual_warfare,devotion,vigilance', false),
(8, 'Personal Narrative (circa 1740)', 'God''s excellency, his wisdom, his purity and love, seemed to appear in everything; in the sun, moon, and stars; in the clouds and blue sky.', 'beauty,creation,worship,sovereignty', true),
(8, 'The End for Which God Created the World (1765)', 'The happiness of the creature consists in rejoicing in God, by which also God is magnified and exalted.', 'joy,worship,purpose,glory_of_god', true),
(8, 'A Faithful Narrative of the Surprising Work of God (1737)', 'Conversion is a great and glorious work of God''s power, at once changing the heart, and infusing life into the dead soul.', 'salvation,grace,conversion,sovereignty', true),
(8, 'Personal Narrative (circa 1740)', 'The heaven I desired was a heaven of holiness; to be with God, and to spend my eternity in divine love, and holy communion with Christ.', 'love,holiness,hope,eternity', true);

-- 9: George Whitefield
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(9, 'Sermon: The Method of Grace (1739)', 'Father Abraham, whom have you in heaven? Any Episcopalians? No! Any Presbyterians? No! Any Independents or Methodists? No, no, no! Whom have you there? We don''t know those names here. All who are here are Christians.', 'unity,salvation,church,grace', false),
(9, 'Journal of George Whitefield (1740)', 'I am persuaded that the generality of preachers talk of an unknown and unfelt Christ.', 'evangelism,truth,boldness,preaching', true),
(9, 'Letter to Friends (1741)', 'God forbid that I should travel with anybody a quarter of an hour without speaking of Christ to them.', 'evangelism,boldness,mission,love', false),
(9, 'Sermon: Regeneration (1737)', 'You must be born again. Though you give all your goods to feed the poor, though you should give your body to be burned, and have not this faith that works by love, it will profit you nothing.', 'regeneration,salvation,faith,love', false),
(9, 'Journal of George Whitefield (1738)', 'I am never better than when I am on the full stretch for God.', 'zeal,devotion,calling,service', true),
(9, 'Sermon: The Lord Our Righteousness (1741)', 'God the Father gave to Christ a certain number; and all that the Father gives to Him shall come; and whosoever comes, Christ will in no wise cast out.', 'salvation,grace,sovereignty,hope', false),
(9, 'Letter to the Inhabitants of Maryland, Virginia (1740)', 'I am willing to go to prison and to death for you, but I am not willing to come short of you in heaven.', 'service,sacrifice,love,mission', false),
(9, 'Sermon: Christ the Believer''s Wisdom (1739)', 'Let a man go to the grammar school of faith and repentance before he goes to the university of election and predestination.', 'faith,repentance,wisdom,discipleship', false),
(9, 'The Method of Grace, sermon (1741)', 'Such as have got peace with God, if you are under trials, fear not, all things shall work for your good; if you are under temptations, fear not, if he has spoken peace to your hearts, all these things shall be for your good.', 'peace,trials,hope,providence', true),
(9, 'The Method of Grace, sermon (1741)', 'Christians should be singularly good, bold for their Lord, that all who are with you may take notice that you have been with Jesus.', 'boldness,witness,holiness,discipleship', true),
(9, 'The Lord Our Righteousness, sermon (1741)', 'For why should I lean upon a broken reed, when I can have the rock of ages to stand upon, that never can be moved?', 'faith,assurance,trust,righteousness', true),
(9, 'The Lord Our Righteousness, sermon (1741)', 'O think of his dying love! Let that love constrain you to obedience! Having much forgiven, love much.', 'love,gratitude,obedience,forgiveness', true),
(9, 'Christ the Believer''s Wisdom, Righteousness, Sanctification, and Redemption, sermon', 'Fear not, therefore, O believers, to look into the grave: for to you it is not other than a consecrated dormitory, where your bodies shall sleep quietly until the morning of the resurrection;', 'hope,resurrection,death,comfort', true),
(9, 'The Nature and Necessity of Our Regeneration or New Birth in Christ Jesus, sermon (1737)', 'It is not enough to turn from profaneness to civility; but thou must turn from civility to godliness.', 'regeneration,repentance,holiness,conversion', true);

-- 10: John Knox
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(10, 'History of the Reformation in Scotland (1587)', 'A man with God is always in the majority.', 'courage,faith,sovereignty,truth', false),
(10, 'History of the Reformation in Scotland (1587)', 'Resistance to tyranny is obedience to God.', 'justice,moral_courage,obedience,truth', false),
(10, 'Letter to the Commonality of Scotland (1558)', 'I have promised by God that I will not rest from this calling, not because it is pleasant or easy, but because the Lord God demands it.', 'calling,obedience,courage,faithfulness', false),
(10, 'Sermon at St. Giles'' Cathedral, Edinburgh (circa 1559)', 'Give me Scotland or I die.', 'prayer,intercession,zeal,mission', false),
(10, 'History of the Reformation in Scotland (1587)', 'You cannot antagonize and influence at the same time.', 'wisdom,truth,service,humility', false),
(10, 'History of the Reformation in Scotland, interview with Mary Queen of Scots (1563)', 'Madam, I am not master of myself, but must obey him who commands me to speak plain, and to flatter no flesh upon the face of the earth.', 'truth,moral_courage,obedience,integrity', true),
(10, 'Sermon on Isaiah 26 (circa 1565)', 'The more the Word of God is spread, the more it will take root.', 'scripture,mission,hope,word_of_god', false),
(10, 'Sermon on Isaiah 26 (1565)', 'This, I say, is the victory of faith, when to the midst of death, through the light of God’s word, the afflicted see life.', 'faith,hope,resurrection,perseverance', true),
(10, 'Scots Confession, Preface (1560)', 'And therefore, by the assistance of the mighty Spirit of our Lord Jesus, we firmly promise to abide to the end in the Confession of this our Faith.', 'perseverance,faithfulness,holy_spirit,courage', true);

-- 11: Ulrich Zwingli
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(11, 'Of the Clarity and Certainty of the Word of God (1522)', 'The word of God is sure and cannot fail.', 'scripture,faith,truth,sovereignty', true),
(11, 'Exposition of the Christian Faith (1531)', 'The glory of God is at stake. For this, everything else must yield.', 'worship,sovereignty,holiness,truth', false),
(11, 'Commentary on True and False Religion (1525)', 'No man can give himself faith. Faith is the work of God.', 'faith,grace,sovereignty,salvation', false),
(11, 'Exposition of the Christian Faith (1531)', 'Where God''s word is preached faithfully, there the church is.', 'church,scripture,truth,reformation', false),
(11, 'Sixty-Seven Articles (1523)', 'The sum and substance of the Gospel is that our Lord Jesus Christ, the true Son of God, has made known to us the will of his heavenly Father, and has with his innocence released us from death and reconciled God.', 'gospel,salvation,grace,atonement', true),
(11, 'Letter to Francis I of France (1531)', 'I believe, yea, I know that God governs this world.', 'sovereignty,faith,hope,trust', false),
(11, 'On the Providence of God, p. 229 (1530)', 'You are God''s tool. He wills to wear you out by use, not by idleness. Oh, happy man, whom He calls to His work!', 'calling,providence,perseverance,service', false);

-- 12: Philip Melanchthon
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(12, 'Loci Communes (1521)', 'To know Christ is to know his benefits, and not to contemplate his natures and the mode of the incarnation.', 'christ,salvation,theology,knowledge', true),
(12, 'Augsburg Confession, Article IV (1530)', 'Also they teach that men cannot be justified before God by their own strength, merits, or works, but are freely justified for Christ''s sake, through faith, when they believe that they are received into favor, and that their sins are forgiven for Christ''s sake…', 'salvation,grace,faith,justification', true),
(12, 'Loci Communes (1543)', 'The knowledge of sin is the beginning of salvation.', 'repentance,salvation,truth,humility', false),
(12, 'Letter to Luther (1530)', 'In these matters of which I am uncertain, I follow you as my teacher.', 'humility,wisdom,discipleship,learning', false),
(12, 'On Correcting the Studies of Youth, inaugural address at Wittenberg (1518)', 'Without knowledge of letters, we are all blind.', 'education,wisdom,truth,calling', false),
(12, 'Augsburg Confession, Article XX (1530)', 'Now he that knows that he has a Father gracious to him through Christ, truly knows God; he knows also that God cares for him, and calls upon God; in a word, he is not without God, as the heathen.', 'faith,grace,assurance,prayer', true);

-- 13: Karl Barth
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(13, 'Church Dogmatics, I/1 (1932)', 'To clasp the hands in prayer is the beginning of an uprising against the disorder of the world.', 'prayer,justice,hope,transformation', false),
(13, 'The Epistle to the Romans, on Romans 1:16 (1922)', 'The Gospel is not a truth among other truths. Rather, it sets a question-mark against all truths.', 'gospel,truth,revelation,theology', true),
(13, 'The Humanity of God (1956)', 'In Him the fact is once for all established that God does not exist without man.', 'incarnation,love,covenant,theology', true),
(13, 'Church Dogmatics, IV/3 (1959)', 'Laughter is the closest thing to the grace of God.', 'joy,grace,humor,gratitude', false),
(13, 'Church Dogmatics, II/1 (1940)', 'The theologian who has no joy in his work is not a theologian at all.', 'joy,calling,theology,vocation', true),
(13, 'Church Dogmatics, III/4 (1951)', 'The command of God is permission — permission to live as human beings.', 'freedom,grace,obedience,humanity', false),
(13, 'Dogmatics in Outline (1949)', 'Faith is never identical with ''piety'', even if it were the purest and finest.', 'faith,humility,truth,theology', true),
(13, 'The Humanity of God (1956)', 'We have no theological right to set any sort of limits to the loving-kindness of God which has appeared in Jesus Christ.', 'love,grace,hope,sovereignty', true);

-- 14: John Owen
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(14, 'The Mortification of Sin (1656)', 'Be killing sin or it will be killing you.', 'holiness,repentance,spiritual_warfare,obedience', true),
(14, 'Communion with the Triune God (1657)', 'A man preacheth that sermon only well unto others which preacheth itself in his own soul.', 'preaching,integrity,truth,heart', false),
(14, 'The Mortification of Sin, Chapter 2 (1656)', 'Sin doth so remain, so act and work in the best of believers, whilst they live in this world, that the constant daily mortification of it is all their days incumbent on them.', 'holiness,repentance,discipleship,obedience', true),
(14, 'The Glory of Christ, Chapter 2 (1684)', 'We behold "the glory of God in the face of Jesus Christ"… Otherwise we know it not, we see it not, we see nothing of it.', 'christ,worship,glory,faith', true),
(14, 'Communion with the Triune God, Part 1, Chapter 3 (1657)', 'As your great trouble is about the Father''s love, so you can no way more trouble or burden him, than by your unkindness in not believing of it.', 'love,faith,assurance,grace', true),
(14, 'The Mortification of Sin, Chapter 14 (1656)', 'The Spirit is the author and finisher of our sanctification; gives new supplies and influences of grace for holiness and sanctification…', 'holy_spirit,grace,sanctification,perseverance', true),
(14, 'Biblical Theology (1661)', 'The Scripture is not given to us to supersede thought, but to direct it.', 'scripture,wisdom,truth,theology', false),
(14, 'The Mortification of Sin (1656)', 'Do you mortify? Do you make it your daily work? Be always at it whilst you live; cease not a day from this work.', 'holiness,diligence,obedience,discipleship', true),
(14, 'Of the Death of Christ (1650)', 'Christ took our sin that we might take his righteousness.', 'atonement,salvation,grace,substitution', false);

-- 15: Richard Baxter
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(15, 'The Reformed Pastor (1656)', 'Take heed to yourselves, lest you should be void of that saving grace of God which you offer to others.', 'integrity,holiness,warning,ministry', true),
(15, 'The Saints'' Everlasting Rest (1650)', 'The end of sorrow is near, when the end of sin is near.', 'hope,holiness,eternity,suffering', false),
(15, 'The Reformed Pastor (1656)', 'If God would have had none but scholars and rabbins to read his word, he would have written it in a more learned manner.', 'scripture,humility,equality,word_of_god', false),
(15, 'A Christian Directory (1673)', 'The principal use of prayer is not to inform God, who knows our wants better than we ourselves do, but to acknowledge our dependence on him.', 'prayer,humility,dependence,devotion', false),
(15, 'The Saints'' Everlasting Rest (1650)', 'O how sweet would it be, if that sweet land were near! If it were but a step or two further.', 'hope,eternity,longing,faith', false),
(15, 'The Reformed Pastor (1656)', 'We must love men enough to tell them the truth.', 'love,truth,courage,integrity', false),
(15, 'Reliquiae Baxterianae (1696)', 'In necessary things, unity; in uncertain things, liberty; in all things, charity.', 'unity,love,wisdom,tolerance', false),
(15, 'A Christian Directory, Part 2, Chapter 17 (1673)', 'Keep up a high esteem of time; and be every day more careful that you lose none of your time, than you are that you lose none of your gold or silver.', 'diligence,purpose,calling,stewardship', true);

-- 16: Thomas Cranmer
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(16, 'Book of Common Prayer (1549)', 'Almighty God, unto whom all hearts be open, all desires known, and from whom no secrets are hid: Cleanse the thoughts of our hearts by the inspiration of thy Holy Spirit.', 'prayer,holiness,heart,worship', true),
(16, 'Book of Common Prayer, Morning Prayer (1552)', 'We have erred and strayed from thy ways like lost sheep. We have followed too much the devices and desires of our own hearts.', 'repentance,humility,sin,confession', true),
(16, 'Defence of the True and Catholic Doctrine of the Sacrament (1550)', 'The Word of God is the rule and guide of faith.', 'scripture,truth,faith,authority', false),
(16, 'Address before his execution, Oxford (1556)', 'And as for the pope, I refuse him as Christ''s enemy and Antichrist, with all his false doctrine.', 'truth,courage,reformation,obedience', true),
(16, 'Book of Common Prayer (1549)', 'Grant us therefore, gracious Lord, so to eat the flesh of thy dear Son Jesus Christ, and to drink his blood in these holy Mysteries, that we may continually dwell in him, and he in us, that our sinful bodies may be made clean by his body, and our souls washed through his most precious blood.', 'communion,grace,holiness,love', true),
(16, 'Letter to Henry VIII (1537)', 'Whatever will be most for the glory of God and the wealth of the realm, that will I defend to my power.', 'justice,obedience,calling,truth', false),
(16, 'Book of Homilies, A Short Declaration of the True, Lively, and Christian Faith, Part 3 (1547)', 'If all the world were on our side, and God against us, what could the world avail us? Therefore let us set our whole faith and trust in God, and neither the world, the devil, nor all the power of them, shall prevail against us.', 'faith,trust,courage,sovereignty', true);

-- 17: Martin Bucer
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(17, 'Concerning the True Care of Souls (De Vera Cura Animarum, 1538)', 'The true care of souls is to seek out the lost and lead them back to Christ.', 'service,love,mission,calling', false),
(17, 'Concerning the True Care of Souls (1538)', 'We must not abandon the wretched and the weak, but must draw them forward little by little.', 'compassion,service,patience,love', false),
(17, 'The Ground and Reason of the Articles (1524)', 'The entire purpose of all the ordinances of God is that we should love one another and serve one another.', 'love,service,community,obedience', false),
(17, 'De Regno Christi (1550)', 'The kingdom of Christ is not advanced by human craft or the sword of princes, but by the Spirit of God through the proclamation of his Word.', 'sovereignty,word_of_god,mission,truth', false),
(17, 'Letter to Calvin (1538)', 'God is never more truly honored than when those he has called serve one another in love.', 'love,service,humility,worship', false),
(17, 'De Regno Christi, Book 2, provision for the poor (1550)', 'For it is not ynough for the Congregations off Christ to prouyde, that men may onlie lyue, but that thei may lyue to the Lorde, for a certeyn and mutual profet betwene them selfs, and of the Churche, and commen welthe.', 'service,community,love,justice', true);

-- 18: William Carey
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(18, 'Sermon at Nottingham (1792)', 'Expect great things from God; attempt great things for God.', 'faith,mission,boldness,calling', true),
(18, 'Enquiry into the Obligations of Christians (1792)', 'Our God is great; the church of God is great; let us go forward.', 'faith,mission,courage,calling', false),
(18, 'Letter to his son Jabez (1796)', 'I am not yet dead; I am still able to preach the gospel, still able to travel.', 'perseverance,mission,calling,faithfulness', false),
(18, 'Letter to Andrew Fuller (1794)', 'I can plod. That is my only genius. I can persevere in any definite pursuit. To this I owe everything.', 'perseverance,diligence,calling,faithfulness', false),
(18, 'Sermon at Nottingham (1792)', 'Expect great things; attempt great things. Is anything too hard for the Lord?', 'faith,hope,mission,sovereignty', false),
(18, 'Letter to his sister (1800)', 'The gospel is making its way. Men are coming to the knowledge of Christ.', 'hope,mission,gospel,faith', false),
(18, 'Cited in George Smith, The Life of William Carey, Shoemaker and Missionary (1885)', 'When I am gone, say nothing about Dr. Carey — speak about Dr. Carey''s Saviour.', 'humility,worship,service,calling', true),
(18, 'Enquiry into the Obligations of Christians to Use Means for the Conversion of the Heathens, Section 5 (1792)', 'Many can do nothing but pray, and prayer is perhaps the only thing in which Christians of all denominations can cordially, and unreservedly unite; but in this we may all be one, and in this the strictest unanimity ought to prevail.', 'prayer,unity,faith,community', true),
(18, 'Enquiry into the Obligations of Christians to Use Means for the Conversion of the Heathens, Section 5 (1792)', 'We must not be contented however with praying, without exerting ourselves in the use of means for the obtaining of those things we pray for.', 'prayer,diligence,faith,calling', true),
(18, 'Enquiry into the Obligations of Christians to Use Means for the Conversion of the Heathens, Section 5 (1792)', 'Let then every one in his station consider himself as bound to act with all his might, and in every possible way for God.', 'calling,service,diligence,mission', true),
(18, 'Enquiry into the Obligations of Christians to Use Means for the Conversion of the Heathens, Section 5 (1792)', 'Surely a crown of rejoicing like this is worth aspiring to. Surely it is worth while to lay ourselves out with all our might, in promoting the cause, and kingdom of Christ.', 'hope,mission,perseverance,calling', true);

-- 19: A.W. Tozer
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(19, 'The Knowledge of the Holy, Chapter 1 (1961)', 'What comes into our minds when we think about God is the most important thing about us.', 'worship,theology,truth,heart', true),
(19, 'The Pursuit of God (1948)', 'God is so vastly wonderful, so utterly and completely delightful that He can, without anything other than Himself, meet and overflow the deepest demands of our total nature.', 'joy,worship,sovereignty,love', true),
(19, 'The Knowledge of the Holy (1961)', 'A right conception of God is basic not only to systematic theology but to practical Christian living as well.', 'theology,truth,worship,wisdom', true),
(19, 'The Knowledge of the Holy (1961)', 'We tend by a secret law of the soul to move toward our mental image of God.', 'worship,heart,transformation,theology', true),
(19, 'The Pursuit of God (1948)', 'The man who has God for his treasure has all things in one.', 'joy,worship,contentment,faith', true),
(19, 'The Divine Conquest (1950)', 'The man who would know God must give time to Him.', 'prayer,devotion,calling,discipline', true),
(19, 'The Root of the Righteous (1955)', 'A scared world needs a fearless church.', 'courage,faith,justice,calling', false),
(19, 'The Root of the Righteous (1955)', 'It is doubtful whether God can bless a man greatly until He has hurt him deeply.', 'suffering,grace,sovereignty,transformation', true),
(19, 'The Pursuit of God (1948)', 'Sound Bible exposition is an imperative must in the Church of the Living God. Without it no church can be a New Testament church in any strict meaning of that term.', 'scripture,truth,word_of_god,wisdom', true),
(19, 'Man: The Dwelling Place of God (1966)', 'The worst thing that can happen to a man is to succeed before he is ready.', 'humility,wisdom,character,grace', false);

-- 20: Jan Hus
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(20, 'Letter from Prison, Constance (1415)', 'I am willing to die today. Seek the truth, listen to the truth, learn the truth, love the truth, speak the truth, hold the truth, defend the truth till death.', 'truth,courage,moral_courage,martyrdom', false),
(20, 'On the Church (De Ecclesia, 1413)', 'I firmly hope to abide in the truth of God''s law, which I have preached, written, and taught.', 'truth,faith,obedience,perseverance', false),
(20, 'Letter to his congregation in Bohemia (1415)', 'Stand firm in the truth you have heard me preach.', 'truth,courage,perseverance,faith', false),
(20, 'On the Church (1413)', 'No one who is without repentance can be absolved by any pope.', 'repentance,truth,authority,reformation', false),
(20, 'Cited in Peter of Mladoňovice, John Hus at the Council of Constance, tr. Matthew Spinka (1965)', 'God is my witness that the things charged against me I have never preached. In the same truth of the Gospel which I have written, taught, and preached, drawing upon the sayings and positions of the holy doctors, I am ready to die today.', 'truth,courage,integrity,martyrdom', true),
(20, 'The Letters of John Hus, Letter 61, To Henry Skopek de Duba, 9 June 1415 (1904)', 'Fear Him as the Lord Almighty, love Him as the Father most holy, ever aim at Him in mind, works, and desire. For His sake carefully abstain from sin, do all the good you can, and be not afraid of the adversities of this world.', 'courage,love,holiness,obedience', true),
(20, 'The Letters of John Hus, Letter 5, To the People of Laun, c.1410 (1904)', 'If here we have to suffer for Christ''s sake, there we shall be blessed. It is through a cross and through afflictions that we are tried, like gold in the fire, by the Builder who formed the world out of nothing. Blessed then shall we be, if we persevere in that which is good, even to the end.', 'suffering,perseverance,hope,faith', true);

-- 21: Francis Schaeffer
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(21, 'The God Who Is There (1968)', 'Christianity is not a series of truths in the plural, but rather truth spelled with a capital ''T.''', 'truth,gospel,apologetics,faith', false),
(21, 'How Should We Then Live? (1976)', 'Tell me what the world is saying today, and I''ll tell you what the church will be saying in seven years.', 'truth,courage,wisdom,culture', false),
(21, 'The God Who Is There (1968)', 'Every man has built a roof over his head to shield himself at the point of tension.', 'truth,philosophy,humanity,apologetics', true),
(21, 'He Is There and He Is Not Silent (1972)', 'God has spoken. He is not silent. The Bible is the written Word of God.', 'scripture,truth,revelation,word_of_god', false),
(21, 'The Mark of the Christian (1970)', 'Upon his authority he gives the world the right to judge whether you and I are born-again Christians on the basis of our observable love toward all Christians.', 'love,community,truth,witness', true),
(21, 'True Spirituality (1971)', 'Every Christian is to be a saint in the deepest sense; that is, a holy person.', 'holiness,calling,discipleship,truth', false),
(21, 'The Church Before the Watching World (1971)', 'We must be careful that we do not use the word ''love'' as the excuse for lack of clear thinking and strong action.', 'love,truth,wisdom,courage', false),
(21, 'How Should We Then Live? (1976)', 'There is a flow to history and culture. This flow is rooted and has its wellspring in the thoughts of people.', 'truth,wisdom,culture,calling', true);

-- 22: Martyn Lloyd-Jones
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(22, 'Preaching and Preachers (1971)', 'The primary task of the Church is not to educate man, is not to heal him physically or psychologically, it is not to make him happy… Her primary purpose is not any of these; it is rather to put man into the right relationship with God, to reconcile man to God.', 'gospel,salvation,calling,truth', true),
(22, 'Spiritual Depression: Its Causes and Cures (1965)', 'The main art in the matter of spiritual living is to know how to handle yourself.', 'wisdom,holiness,self-awareness,discipleship', true),
(22, 'Studies in the Sermon on the Mount, Vol. 1 (1959)', 'Seek ye first the kingdom of God, and then all the other things will be added. The tragedy is that we do not believe it.', 'faith,obedience,truth,discipleship', false),
(22, 'Preaching and Preachers (1971)', 'What is the chief end of preaching? I like to think it is this: it is to give men and women a sense of God and his presence.', 'worship,preaching,truth,calling', true),
(22, 'Spiritual Depression: Its Causes and Cures (1965)', 'Have you realized that most of your unhappiness in life is due to the fact that you are listening to yourself instead of talking to yourself?', 'faith,hope,wisdom,suffering', true),
(22, 'Romans: An Exposition, Vol. 1 (1985)', 'Faith is not a feeling. Faith is an act of the will responding to the truth revealed in the Word of God.', 'faith,truth,obedience,scripture', false),
(22, 'God''s Way of Reconciliation: Studies in Ephesians 2 (1972)', 'The glory of the gospel is that when the church is absolutely different from the world, she invariably attracts it.', 'holiness,witness,gospel,truth', false),
(22, 'Preaching and Preachers (1971)', 'The business of preaching is not to entertain but to lead people to salvation, to instruct them in the ways of God.', 'truth,calling,gospel,preaching', false);

-- 23: Watchman Nee
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(23, 'The Normal Christian Life (1957)', 'The Lord does not ask us to give up the things of earth, but to exchange them for better things.', 'discipleship,faith,surrender,hope', false),
(23, 'The Normal Christian Life (1957)', 'God''s aim is not to make us something; it is to express himself through what he has made us.', 'purpose,calling,sovereignty,identity', false),
(23, 'Sit, Walk, Stand (1957)', 'God never builds on our ruins.', 'grace,sovereignty,hope,renewal', false),
(23, 'The Normal Christian Life (1957)', 'The Blood can wash away my sins, but it cannot wash away my ''old man''. It needs the Cross to crucify me. The Blood deals with the sins, but the Cross must deal with the sinner.', 'cross,holiness,grace,transformation', true),
(23, 'Changed into His Likeness (1967)', 'True service comes out of what God has done in us, not what we do for God.', 'service,humility,grace,calling', false),
(23, 'The Spiritual Man (1928)', 'God does not fill a vessel that is already full of self.', 'humility,surrender,grace,holiness', false),
(23, 'Sit, Walk, Stand (1957)', 'There is a place where we may rest in the Lord. It is not a place of inactivity but of quiet trust.', 'prayer,trust,peace,faith', false),
(23, 'The Release of the Spirit (1965)', 'The way of service lies in brokenness, in accepting the discipline of the Holy Spirit. The measure of your service is determined by the degree of discipline and brokenness.', 'humility,service,suffering,grace', true);

-- 24: François Fénelon
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(24, 'Christian Perfection (published 1947, written circa 1685)', 'Pure love is patient, kind, gentle. It does not act rashly or selfishly.', 'love,patience,holiness,compassion', false),
(24, 'The Spiritual Letters of Fénelon, Letters to Men, Letter 88', 'Tell Him all that is in your heart, as one unloads one''s heart to a dear friend of all that gives it pain or pleasure.', 'prayer,trust,love,devotion', true),
(24, 'The Spiritual Letters of Fénelon (compiled 1718)', 'Abandon yourself to God, for nothing is impossible with him.', 'faith,surrender,sovereignty,trust', false),
(24, 'Christian Perfection (written circa 1685)', 'God never works in us to please us; He works to accomplish his own purposes and to make us what He wants us to be.', 'sovereignty,holiness,obedience,transformation', false),
(24, 'The Spiritual Letters of Fénelon (compiled 1718)', 'Self-love is the enemy of the love of God; we cannot have both.', 'love,humility,holiness,surrender', false),
(24, 'On Pure Love (written circa 1697)', 'Pure love seeks God alone, without any seeking of self.', 'love,worship,holiness,surrender', false),
(24, 'The Spiritual Letters of Fénelon (compiled 1718)', 'Bear patiently all the troubles and vexations of life, great and small; never be disturbed at them, but receive them as from the hand of God.', 'suffering,trust,patience,sovereignty', false),
(24, 'Christian Perfection (written circa 1685)', 'Resign everything to God and ask him to do with you exactly as he pleases.', 'surrender,obedience,trust,faith', false),
(24, 'The Spiritual Letters of Fénelon, Letters to Men, Letter 4', 'Oh, how compassionate and comforting God is to those who go to Him with hearty confidence in their troubles! Men are hard, critical, harsh, never more than half-tolerant; but God bears with all, is pitiful to all. His Goodness, His Patience and Indulgence, are boundless.', 'compassion,trust,grace,patience', true);

-- 25: Madame Guyon
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(25, 'A Short and Easy Method of Prayer (1685)', 'Come to God with a pure heart and in humility; lay before him all your weakness, your misery, and your sin.', 'prayer,humility,repentance,devotion', false),
(25, 'A Short and Easy Method of Prayer (1685)', 'God meets us where we are; it is enough to open our hearts to him.', 'prayer,grace,love,devotion', false),
(25, 'Spiritual Torrents (written circa 1682)', 'The soul that is truly given up to God fears nothing because it knows that nothing can touch it without his permission.', 'trust,sovereignty,peace,faith', false),
(25, 'Autobiography of Madame Guyon (1720)', 'I have endured the loss of all things, and counted them but dung, that I might win Christ.', 'surrender,suffering,faith,love', false),
(25, 'A Short and Easy Method of Prayer (1685)', 'It matters little what form of prayer we adopt, so long as the spirit of prayer is present.', 'prayer,simplicity,devotion,humility', false),
(25, 'Autobiography of Madame Guyon (1720)', 'There seemed, indeed, to be no will left in me but thine only. My own disappeared, and no desires, tendencies or inclinations were left, but to the one sole object of whatever was most pleasing to thee…', 'surrender,obedience,love,holiness', true),
(25, 'Spiritual Torrents (written circa 1682)', 'Nothing less than God himself can satisfy the heart.', 'worship,love,devotion,longing', false);

-- 26: Thomas à Kempis
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(26, 'The Imitation of Christ, Book I, Chapter 1 (circa 1418)', 'What doth it profit thee to enter into deep discussion concerning the Holy Trinity, if thou lack humility, and be thus displeasing to the Trinity?', 'humility,wisdom,theology,truth', true),
(26, 'The Imitation of Christ, Book I, Chapter 2 (circa 1418)', 'Better of a surety is a lowly peasant who serveth God, than a proud philosopher who watcheth the stars and neglecteth the knowledge of himself.', 'humility,wisdom,self-knowledge,service', true),
(26, 'The Imitation of Christ, Book I, Chapter 2 (circa 1418)', 'Many words satisfy not the soul, but a good life refresheth the mind, and a pure conscience giveth great confidence towards God.', 'truth,holiness,discipleship,simplicity', true),
(26, 'The Imitation of Christ, Book II, Chapter 1 (circa 1418)', 'The kingdom of God is within you, saith the Lord. Turn thee with all thine heart to the Lord and forsake this miserable world, and thou shalt find rest unto thy soul.', 'repentance,surrender,obedience,heart', true),
(26, 'The Imitation of Christ, Book I, Chapter 1 (circa 1418)', 'It is vanity to desire a long life, and to have little care for a good life.', 'wisdom,holiness,purpose,eternity', true),
(26, 'The Imitation of Christ, Book III, Chapter 5 (circa 1418)', 'What doth it profit thee to enter into deep discussion concerning the Holy Trinity, if thou lack humility, and be thus displeasing to the Trinity?', 'humility,worship,truth,theology', false),
(26, 'The Imitation of Christ, Book I, Chapter 6 (circa 1418)', 'For true peace of heart is to be found in resisting passion, not in yielding to it.', 'holiness,peace,self-denial,wisdom', true),
(26, 'The Imitation of Christ, Book I, Chapter 2 (circa 1418)', 'Rest from inordinate desire of knowledge, for therein is found much distraction and deceit.', 'humility,simplicity,wisdom,peace', true),
(26, 'The Imitation of Christ, Book I, Chapter 20 (circa 1418)', 'Better of a surety is a humble rustic who serveth God, than a proud philosopher who watcheth the stars.', 'humility,service,wisdom,simplicity', false),
(26, 'The Imitation of Christ, Book IV, Chapter 1 (circa 1418)', 'Come unto me, all ye that labour and are heavy laden, and I will refresh you, saith the Lord.', 'comfort,rest,grace,love', false);

-- 27: Brother Lawrence
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(27, 'The Practice of the Presence of God, Conversation 1 (published 1692)', 'We should establish ourselves in a sense of God''s presence, by continually conversing with Him.', 'prayer,devotion,presence,worship', true),
(27, 'The Practice of the Presence of God, Letter 5 (published 1692)', 'Were I a preacher, I should, above all other things, preach the practice of the presence of God.', 'prayer,devotion,calling,worship', true),
(27, 'The Practice of the Presence of God, Letter 4 (published 1692)', 'God has infinite treasure to bestow, and we take up with a little sensible devotion, which passes in a moment. Blind as we are, we hinder God.', 'grace,worship,trust,transformation', true),
(27, 'The Practice of the Presence of God, Letter 2 (published 1692)', 'I make it my business only to persevere in His holy presence, wherein I keep myself by a simple attention, and a general fond regard to God.', 'prayer,devotion,perseverance,presence', true),
(27, 'The Practice of the Presence of God, Letter 9 (published 1692)', 'In the way of God, thoughts count for little; love is everything.', 'love,devotion,prayer,simplicity', false),
(27, 'The Practice of the Presence of God, Letter 6 (published 1692)', 'I cannot imagine how religious persons can live satisfied without the practice of the presence of God.', 'devotion,prayer,holiness,worship', true),
(27, 'The Practice of the Presence of God, Letter 7 (published 1692)', 'He requires no great matters of us; a little remembrance of Him from time to time; a little adoration.', 'prayer,simplicity,devotion,grace', true),
(27, 'The Practice of the Presence of God, Letter 8 (published 1692)', 'I do not advise you to use multiplicity of words in prayer: many words and long discourses being often the occasions of wandering.', 'prayer,simplicity,devotion,humility', true);

-- 28: Julian of Norwich
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(28, 'Revelations of Divine Love, Chapter 27 (circa 1393)', 'All shall be well, and all shall be well, and all manner of thing shall be well.', 'hope,sovereignty,trust,love', true),
(28, 'Revelations of Divine Love, Chapter 68 (circa 1393)', 'He said not: Thou shalt not be tempested, thou shalt not be travailed, thou shalt not be afflicted; but He said: Thou shalt not be overcome.', 'hope,suffering,sovereignty,perseverance', true),
(28, 'Revelations of Divine Love, Chapter 19 (circa 1393)', 'I would have looked up from the Cross, but I durst not. For I wist well that while I beheld in the Cross I was surely-safe…', 'cross,faith,trust,salvation', true),
(28, 'Revelations of Divine Love, Chapter 77 (circa 1393)', 'Our courteous Lord willeth that we should be as homely with Him as heart may think or soul may desire.', 'love,prayer,intimacy,grace', true),
(28, 'Revelations of Divine Love, Chapter 41 (circa 1393)', 'I am Ground of thy beseeching: first it is my will that thou have it; and after, I make thee to will it; and after, I make thee to beseech it and thou beseechest it.', 'prayer,sovereignty,grace,love', true),
(28, 'Revelations of Divine Love, Chapter 86 (circa 1393)', 'Wouldst thou learn thy Lord''s meaning in this thing? Learn it well: Love was His meaning. Who shewed it thee? Love. What shewed He thee? Love. Wherefore shewed it He? For Love.', 'love,revelation,theology,joy', true),
(28, 'Revelations of Divine Love, Chapter 5 (circa 1393)', 'God, of Thy Goodness, give me Thyself: for Thou art enough to me, and I may nothing ask that is less that may be full worship to Thee.', 'prayer,love,worship,longing', true),
(28, 'Revelations of Divine Love, Chapter 68 (circa 1393)', 'Wouldst thou learn thy Lord''s meaning in this thing? Learn it well: Love was His meaning.', 'love,truth,revelation,theology', false);

-- 29: John of the Cross
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(29, 'The Ascent of Mount Carmel, Book I, Chapter 11 (circa 1579)', 'This is the state of a soul with particular attachments: it never can attain to the liberty of the divine union, whatever virtues it may possess.', 'surrender,holiness,love,freedom', true),
(29, 'The Ascent of Mount Carmel, Book I, Chapter 13 (circa 1579)', 'That thou mayest have pleasure in everything, seek pleasure in nothing. … That thou mayest possess all things, seek to possess nothing.', 'surrender,humility,wisdom,holiness', true),
(29, 'Sayings of Light and Love (circa 1585)', 'The soul that walks in love neither tires others nor grows tired.', 'love,service,holiness,perseverance', true),
(29, 'Spiritual Canticle, Stanza 1 (circa 1578)', 'Where hast Thou hidden Thyself, And abandoned me in my groaning, O my Beloved?', 'longing,prayer,love,seeking', true),
(29, 'The Dark Night of the Soul, Book II, Chapter 5 (circa 1578)', 'The endurance of darkness is preparation for great light.', 'suffering,hope,transformation,faith', false),
(29, 'Sayings of Light and Love (circa 1585)', 'Seek by reading and you will find by meditating; cry in prayer and the door will be opened in contemplation.', 'prayer,scripture,wisdom,devotion', true),
(29, 'Sayings of Light and Love (circa 1585)', 'Never give up prayer, and should you find dryness and difficulty, persevere in it for this very reason.', 'prayer,perseverance,faith,devotion', false),
(29, 'Sayings of Light and Love (circa 1585)', 'The Father uttered one Word; that Word is His Son: and He utters Him for ever in everlasting silence, and in silence the soul has to hear It.', 'prayer,contemplation,word_of_god,worship', true);

-- 30: Teresa of Ávila
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(30, 'Interior Castle, Mansion 1, Chapter 1 (1577)', 'It is no small misfortune and disgrace that, through our own fault, we neither understand our nature nor our origin.', 'self-knowledge,humility,repentance,truth', true),
(30, 'Interior Castle, Mansion 4, Chapter 1 (1577)', '…it is not so essential to think much as to love much: therefore you must practise whatever most excites you to this.', 'love,prayer,devotion,simplicity', true),
(30, 'The Life of Teresa of Jesus, Chapter 8 (1565)', '…mental prayer is nothing else, in my opinion, but being on terms of friendship with God, frequently conversing in secret with Him Who, we know, loves us.', 'prayer,love,intimacy,devotion', true),
(30, 'Poems, Nada te turbe', 'Let nothing disturb thee, Nothing affright thee; All things are passing; God never changeth; Patient endurance Attaineth to all things; Who God possesseth In nothing is wanting; Alone God sufficeth.', 'trust,sovereignty,peace,hope', true),
(30, 'Interior Castle, Mansion 7, Chapter 4 (1577)', 'God alone suffices.', 'worship,trust,love,surrender', false),
(30, 'The Life of Teresa of Jesus, Chapter 8 (1565)', 'I was more concerned with my honour than with God''s honour.', 'repentance,humility,truth,self-knowledge', false),
(30, 'Interior Castle, Mansion 5, Chapter 2 (1577)', 'Oh, how sweet is this peace — the peace of God which surpasses all understanding.', 'peace,trust,joy,presence', false),
(30, 'The Foundations, Chapter 5 (1576)', '…the good of the soul does not consist in its thinking much, but in its loving much.', 'love,prayer,simplicity,devotion', true),
(30, 'The Life of Teresa of Jesus, Chapter 8 (1565)', 'Prayer is the door to those great graces which our Lord bestowed upon me.', 'prayer,grace,devotion,faith', true);

-- 31: Bernard of Clairvaux
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(31, 'On Loving God, Chapter 1 (circa 1135)', 'The measure of love is to love without measure.', 'love,holiness,surrender,worship', false),
(31, 'On Loving God, Chapter 1 (circa 1135)', 'You want me to tell you why God is to be loved and how much. I answer, the reason for loving God is God Himself; and the measure of love due to Him is immeasurable love.', 'love,worship,theology,truth', true),
(31, 'Sermons on the Song of Songs, Sermon 84 (circa 1153)', 'You would not seek Him at all, O soul, nor love Him at all, if you had not been first sought and first loved.', 'grace,seeking,love,salvation', true),
(31, 'Sermons on the Song of Songs, Sermon 1 (circa 1135)', 'True order of living is to devote ourselves first to those things which belong to eternity, then to those which relate to time.', 'wisdom,purpose,eternity,discipleship', false),
(31, 'On Consideration, Book II, Chapter 6 (circa 1150)', 'Learn the lesson that, if you are to do the work of a prophet, what you want is not a sceptre, but a hoe.', 'humility,service,calling,truth', true),
(31, 'On Grace and Free Choice, Chapter 1 (circa 1128)', 'Take away free will and there remaineth nothing to be saved; take away grace and there is no means whereby it can be saved.', 'grace,salvation,theology,truth', true),
(31, 'Sermons on the Song of Songs, Sermon 18 (circa 1136)', 'If, then, you are wise, you will show yourself rather as a reservoir than as a canal.', 'wisdom,service,humility,stewardship', true),
(31, 'Sermons on the Song of Songs, Sermon 20 (circa 1140)', 'What we love we shall grow to resemble.', 'love,transformation,holiness,worship', false);

-- 32: Francis of Assisi
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(32, 'The Canticle of the Sun (1224)', 'Praise be to Thee, my Lord, with all Thy creatures, Especially to my worshipful brother sun…', 'worship,creation,gratitude,joy', true),
(32, 'The Earlier Rule (Regula non bullata), Chapter 17 (1221)', 'Let all the brothers preach by their works.', 'service,integrity,calling,witness', true),
(32, 'The Admonitions, Admonition 27 (circa 1220)', 'Where there is charity and wisdom there is neither fear nor ignorance.', 'love,wisdom,faith,peace', true),
(32, 'The Admonitions, Admonition 25 (circa 1220)', 'Blessed is that brother who would love his brother as much when he is ill and not able to assist him as he loves him when he is well and able to assist him.', 'love,service,compassion,faithfulness', true),
(32, 'The Canticle of the Sun (1224)', 'Praised be my Lord for our sister, the bodily death, From the which no living man can flee.', 'hope,eternity,peace,surrender', true),
(32, 'The Earlier Rule (Regula non bullata), Chapter 17 (1221)', 'And let us refer all good to the Lord God most High and Supreme; let us acknowledge that all good belongs to Him…', 'humility,gratitude,worship,truth', true),
(32, 'The Testament of St. Francis (1226)', 'And when the Lord gave me some brothers, no one showed me what I ought to do, but the Most High Himself revealed to me that I should live according to the form of the holy Gospel.', 'calling,obedience,sovereignty,scripture', true);

-- 33: Hildegard of Bingen
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(33, 'Scivias, Book I, Vision 1 (1141–1151)', 'I heard a voice speaking to me: The person who has the fear of God will hold fast to the commandments of God.', 'obedience,wisdom,truth,faith', false),
(33, 'Scivias, Book II, Vision 1 (1141–1151)', 'The Word is living, being, spirit, all verdant greening, all creativity. This Word manifests itself in every creature.', 'creation,word_of_god,beauty,worship', false),
(33, 'Letters of Hildegard of Bingen (compiled 12th century)', 'A person who does a good work for God should not seek praise or reward for it.', 'humility,service,calling,obedience', false),
(33, 'Liber Vitae Meritorum (1158–1163)', 'Holy persons draw to themselves all that is earthly.', 'holiness,transformation,service,truth', false),
(33, 'Scivias, Book III, Vision 13 (1141–1151)', 'Man does not live on bread alone, but on every word that comes from the mouth of God.', 'scripture,faith,word_of_god,trust', false),
(33, 'Letters of Hildegard of Bingen (compiled 12th century)', 'Glance at the sun. See the moon and the stars. Gaze at the beauty of earth''s greenings. Now, think.', 'creation,beauty,gratitude,worship', false);

-- 34: Jean-Pierre de Caussade
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(34, 'Abandonment to Divine Providence, Book I, Chapter 1 (published 1861, written circa 1720s)', 'The present moment always reveals the presence of God.', 'presence,trust,sovereignty,peace', false),
(34, 'Abandonment to Divine Providence, Book II, Chapter 4 (published 1861)', '…the words God addresses you each moment—words which are not conveyed to you by means of ink and paper, but by what you have to do and suffer from moment to moment…', 'sovereignty,trust,obedience,presence', true),
(34, 'Abandonment to Divine Providence, Book II, Chapter 1 (published 1861)', 'Faith gives to the whole earth a heavenly aspect; faith transports, enraptures the heart, and raises it above the things of this earth to converse with the blessed.', 'faith,hope,joy,trust', true),
(34, 'Abandonment to Divine Providence, Book II, Chapter 3 (published 1861)', 'The present moment is always filled with infinite treasures: it contains more than you are capable of receiving.', 'trust,sovereignty,gratitude,presence', true),
(34, 'Abandonment to Divine Providence, Book II, Chapter 4 (published 1861)', 'Let us therefore look upon everything that limits us as a gift from God.', 'trust,suffering,surrender,gratitude', false),
(34, 'Abandonment to Divine Providence, Book II, Chapter 2 (published 1861)', 'The soul that abandons itself to God has nothing left to fear.', 'trust,peace,faith,surrender', false),
(34, 'Abandonment to Divine Providence, Book I, Chapter 4 (published 1861)', 'Every moment comes to us pregnant with a command from God, only to pass on and plunge into eternity, there to remain forever what we have made it.', 'obedience,stewardship,eternity,truth', false);

-- 35: Richard Rolle
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(35, 'The Fire of Love (Incendium Amoris, circa 1343)', 'I cannot rest unless I burn with the fire of Thy love.', 'love,devotion,longing,worship', false),
(35, 'The Fire of Love, Book I, Chapter 17 (circa 1343)', 'What is love but the transforming of desire into the thing loved?', 'love,transformation,holiness,worship', true),
(35, 'Mending of Life (Emendatio Vitae, circa 1340)', 'Begin now to reform your life; have sorrow for your sins past.', 'repentance,holiness,obedience,transformation', false),
(35, 'The Fire of Love (circa 1343)', 'I sit and sing of the sweet love of my Saviour, the love that fills my soul and sets it ablaze.', 'joy,worship,love,devotion', false),
(35, 'Mending of Life (circa 1340)', 'Perfect love is stronger than death; it fears no peril; it will pass through fire and water if needs be.', 'love,perseverance,courage,faith', false);

-- 36: Augustine of Hippo
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(36, 'Confessions, Book I, Chapter 1 (397 AD)', 'Thou madest us for Thyself, and our heart is restless, until it repose in Thee.', 'longing,worship,love,truth', true),
(36, 'Confessions, Book X, Chapter 27 (397 AD)', 'Too late loved I Thee, O Thou Beauty of ancient days, yet ever new! too late I loved Thee!', 'love,repentance,conversion,longing', true),
(36, 'The City of God, Book XIV, Chapter 28 (426 AD)', 'Two cities have been formed by two loves: the earthly by the love of self, even to the contempt of God; the heavenly by the love of God, even to the contempt of self.', 'love,truth,justice,eternity', true),
(36, 'Sermons, Sermon 169 (circa 410 AD)', 'Our heart is restless until it rest in Thee.', 'worship,longing,peace,love', false),
(36, 'Confessions, Book VIII, Chapter 7 (397 AD)', 'Give me chastity and continency, only not yet.', 'repentance,honesty,humanity,grace', true),
(36, 'On Christian Doctrine, Book I, Chapter 36 (397 AD)', 'Whoever thinks that he understands the Holy Scriptures, or any part of them, but puts such an interpretation upon them as does not tend to build up this twofold love of God and our neighbour, does not yet understand them as he ought.', 'love,scripture,wisdom,truth', true),
(36, 'Tractates on the Gospel of John, Tractate 7 (circa 406 AD)', 'Our heart is formed for Thee, O Lord, and it is restless until it finds rest in Thee.', 'worship,love,longing,peace', false),
(36, 'The City of God, Book XIX, Chapter 13 (426 AD)', 'Thou awakest us to delight in Thy praise; for Thou madest us for Thyself.', 'worship,gratitude,creation,love', false),
(36, 'On Free Choice of the Will, Book I (395 AD)', 'For it is when a man sins that he is least himself, when he is most a slave to himself.', 'sin,freedom,truth,holiness', false),
(36, 'Enchiridion, Chapter 117 (421 AD)', 'Hope has two beautiful daughters; their names are Anger and Courage. Anger at the way things are, and Courage to see that they do not remain as they are.', 'hope,justice,moral_courage,transformation', false);

-- 37: Athanasius
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(37, 'On the Incarnation, Chapter 54 (circa 318 AD)', 'For He was made man that we might be made God.', 'incarnation,salvation,grace,theology', true),
(37, 'On the Incarnation, Chapter 20 (circa 318 AD)', 'And so it was that two marvels came to pass at once, that the death of all was accomplished in the Lord''s body, and that death and corruption were wholly done away by reason of the Word that was united with it.', 'salvation,cross,atonement,hope', true),
(37, 'Defence of the Nicene Council (circa 352 AD)', 'Athanasius contra mundum — Athanasius against the world.', 'courage,truth,moral_courage,faith', false),
(37, 'Letters to Serapion (circa 359 AD)', 'The Spirit is one, holy, and given to the saints alone.', 'holy_spirit,holiness,truth,theology', false),
(37, 'On the Incarnation, Chapter 8 (circa 318 AD)', 'He, indeed, assumed humanity that we might become God.', 'incarnation,grace,salvation,theology', false),
(37, 'Festal Letter 39 (367 AD)', 'Let no man add to these, neither let him take ought from these.', 'scripture,truth,authority,canon', true),
(37, 'History of the Arians, Chapter 33 (circa 357 AD)', 'We must not be surprised when some of the powerful oppress the poor; for it is their nature.', 'justice,truth,moral_courage,suffering', false),
(37, 'On the Incarnation, Chapter 20 (circa 318 AD)', 'He next offered up His sacrifice also on behalf of all … to show Himself more powerful even than death, displaying His own body incorruptible, as first-fruits of the resurrection of all.', 'hope,resurrection,faith,eternity', true);

-- 38: John Chrysostom
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(38, 'Homilies on Matthew, Homily 50 (circa 390 AD)', 'No one can harm the man who does not injure himself.', 'truth,wisdom,courage,peace', false),
(38, 'Homilies on Matthew, Homily 77 (circa 390 AD)', 'If you cannot find Christ in the beggar at the church door, you will not find him in the chalice.', 'love,service,justice,compassion', false),
(38, 'On the Priesthood, Book VI (circa 386 AD)', 'The road to hell is paved with the bones of priests and monks, and the skulls of bishops are the lamp posts that light the path.', 'truth,repentance,warning,holiness', false),
(38, 'Homilies on the Epistle to the Hebrews, Homily 34 (circa 403 AD)', 'It is not possible for one who prays earnestly not to be saved.', 'prayer,salvation,faith,hope', false),
(38, 'Homilies on First Corinthians, Homily 10 (circa 392 AD)', 'For what is it to be a Christian? To have a new life; to live not for oneself but for God.', 'faith,discipleship,love,calling', false),
(38, 'On Wealth and Poverty (circa 390 AD)', 'Not to enable the poor to share in our goods is to steal from them and deprive them of life. The goods we possess are not ours, but theirs.', 'justice,service,love,compassion', true),
(38, 'Homilies on First Corinthians, Homily 32 (circa 392 AD)', 'Let us consider, if it were every where in abundance, how great benefits would ensue: how there were no need then of laws, or tribunals or punishments … since if all loved and were beloved, no man would injure another.', 'love,grace,truth,transformation', true),
(38, 'Homilies on Matthew, Homily 50 (circa 390 AD)', 'Would you do honor to Christ''s body? Neglect Him not when naked; do not while here you honor Him with silken garments, neglect Him perishing without of cold and nakedness.', 'service,love,justice,compassion', true);

-- 39: Origen
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(39, 'On First Principles, Book II, Chapter 6 (circa 220 AD)', 'For the soul is not moved by external goods, but by the internal good which is found in virtue.', 'holiness,truth,wisdom,character', false),
(39, 'Contra Celsum, Book I, Chapter 9 (circa 248 AD)', 'We are not pretending when we say that we honour Christ as God; for we honour him who is truly God.', 'worship,truth,faith,theology', false),
(39, 'On Prayer, Chapter 8 (circa 233 AD)', 'That benefit accrues to him who prays rightly … follows, I consider, in many ways: It is, first of all, surely in every sense a spiritual advantage to him who is intent upon prayer, in the very composure of prayer to present himself to God and in His presence to speak to Him.', 'prayer,faith,devotion,holiness', true),
(39, 'Homilies on Genesis, Homily 1 (circa 240 AD)', 'What is read in the church is not written only for those who were then alive, but for all the generations of believers.', 'scripture,truth,community,faith', false),
(39, 'Commentary on John, Book I (circa 230 AD)', 'The Word of God takes up residence in the soul that has been made clean.', 'word_of_god,holiness,transformation,grace', false),
(39, 'Contra Celsum, Book VII, Chapter 44 (circa 248 AD)', 'We shall be judged, not by what we profess, but by what we do.', 'obedience,truth,integrity,judgment', false);

-- 40: Polycarp
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(40, 'Letter to the Philippians, Chapter 2 (circa 110 AD)', 'Wherefore, girding up your loins, serve the Lord in fear and truth, as those who have forsaken the vain, empty talk and error of the multitude.', 'service,truth,obedience,faith', true),
(40, 'Letter to the Philippians, Chapter 3 (circa 110 AD)', 'For neither I, nor any other such one, can come up to the wisdom of the blessed and glorified Paul.', 'humility,wisdom,learning,discipleship', true),
(40, 'Martyrdom of Polycarp, Chapter 9 (155 AD)', 'Eighty and six years have I served Him, and He never did me any injury: how then can I blaspheme my King and my Saviour?', 'faith,faithfulness,courage,martyrdom', true),
(40, 'Letter to the Philippians, Chapter 12 (circa 110 AD)', 'Pray for all the saints. Pray also for kings, and potentates, and princes, and for those that persecute and hate you, and for the enemies of the cross, that your fruit may be manifest to all.', 'prayer,love,mission,hope', true);

-- 41: Ignatius of Antioch
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(41, 'Letter to the Romans, Chapter 4 (circa 110 AD)', 'I am the wheat of God, and let me be ground by the teeth of the wild beasts, that I may be found the pure bread of Christ.', 'martyrdom,sacrifice,faith,hope', true),
(41, 'Letter to the Ephesians, Chapter 15 (circa 110 AD)', 'It is better for a man to be silent and be [a Christian], than to talk and not to be one.', 'truth,integrity,wisdom,humility', true),
(41, 'Letter to the Romans, Chapter 7 (circa 110 AD)', 'My Love has been crucified, and there is no fire in me desiring to be fed; but there is within me a water that liveth and speaketh.', 'love,cross,faith,surrender', true),
(41, 'Letter to the Magnesians, Chapter 13 (circa 110 AD)', 'Study, therefore, to be established in the doctrines of the Lord and the apostles.', 'scripture,faith,discipleship,obedience', true),
(41, 'Letter to the Smyrnaeans, Chapter 6 (circa 110 AD)', 'Let no man deceive himself. Both the things which are in heaven, and the glorious angels, and rulers, both visible and invisible, if they believe not in the blood of Christ, shall, in consequence, incur condemnation.', 'truth,salvation,faith,warning', true),
(41, 'Letter to the Ephesians, Chapter 7 (circa 110 AD)', 'There is one Physician who is possessed both of flesh and spirit; both made and not made; God existing in flesh.', 'incarnation,theology,truth,faith', true),
(41, 'Letter to Polycarp, Chapter 6 (circa 110 AD)', 'Let your works be the charge assigned to you, that you may receive a worthy recompense. Be long-suffering, therefore, with one another, in meekness, as God is towards you.', 'service,love,courage,humility', true);

-- 42: Irenaeus
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(42, 'Against Heresies, Book IV, Chapter 20 (circa 180 AD)', 'For the glory of God is a living man; and the life of man consists in beholding God.', 'worship,joy,creation,hope', true),
(42, 'Against Heresies, Book III, Chapter 18 (circa 180 AD)', 'He became what we are that He might bring us to be even what He is.', 'incarnation,salvation,grace,theology', false),
(42, 'Against Heresies, Book IV, Chapter 5 (circa 180 AD)', 'God, therefore, is one and the same … who made the things of time for man, so that coming to maturity in them, he may produce the fruit of immortality; and who, through His kindness, also bestows [upon him] eternal things.', 'sovereignty,creation,eternity,truth', true),
(42, 'Against Heresies, Book I, Preface (circa 180 AD)', 'Error, indeed, is never set forth in its naked deformity, lest, being thus exposed, it should at once be detected.', 'truth,wisdom,discernment,warning', true),
(42, 'Proof of the Apostolic Preaching, Chapter 6 (circa 190 AD)', 'The business of the Christian is nothing else but to be ever preparing for death.', 'eternity,holiness,obedience,wisdom', false),
(42, 'Against Heresies, Book V, Chapter 1 (circa 180 AD)', 'Since the Lord thus has redeemed us through His own blood, giving His soul for our souls, and His flesh for our flesh …', 'atonement,love,salvation,cross', true),
(42, 'Against Heresies, Book V, Preface (circa 180 AD)', 'The Word of God, our Lord Jesus Christ, who did, through His transcendent love, become what we are, that He might bring us to be even what He is Himself.', 'love,incarnation,salvation,grace', true);

-- 43: Clement of Alexandria
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(43, 'Stromateis, Book II, Chapter 4 (circa 198 AD)', 'We call that person wise who does everything with reason, and who knows how to act appropriately in all circumstances.', 'wisdom,truth,holiness,discernment', false),
(43, 'The Instructor (Paedagogus), Book I, Chapter 6 (circa 197 AD)', 'The Word of God, having assumed humanity for the salvation of men, is concerned with the whole creation.', 'incarnation,salvation,love,word_of_god', false),
(43, 'Who Is the Rich Man That Shall Be Saved? Chapter 26 (circa 200 AD)', 'Love, the divine gift, the perfect good, the accomplishment of all virtues, is the crown of all.', 'love,grace,holiness,truth', false),
(43, 'Stromateis, Book I, Chapter 5 (circa 198 AD)', 'For philosophy is the study of wisdom, and wisdom is the knowledge of things divine and human; and their causes.', 'wisdom,truth,theology,discernment', true),
(43, 'Exhortation to the Greeks, Chapter 1 (circa 195 AD)', 'The Word of God became man, that you may learn from man how man may become God.', 'incarnation,salvation,theology,hope', true);

-- 44: Cyprian of Carthage
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(44, 'On the Unity of the Church, Chapter 6 (251 AD)', 'He can no longer have God for his Father, who has not the Church for his mother.', 'church,community,faith,truth', true),
(44, 'On the Lord''s Prayer, Chapter 8 (252 AD)', 'Our prayer is public and common; and when we pray, we pray not for one, but for the whole people, because we the whole people are one.', 'prayer,community,unity,love', true),
(44, 'On Mortality, Chapter 26 (252 AD)', 'Let us greet the day which assigns each of us to his own home, which snatches us hence, and sets us free from the snares of the world, and restores us to paradise and the kingdom.', 'hope,eternity,faith,peace', true),
(44, 'On Mortality, Chapter 20 (252 AD)', 'Our brethren who are freed from this world by the Lord''s summons are not to be lamented, since we know that they are not lost, but sent before.', 'hope,eternity,comfort,faith', true),
(44, 'On the Unity of the Church, Chapter 14 (251 AD)', 'He cannot be a martyr who is not in the Church.', 'church,truth,faith,obedience', true),
(44, 'On Works and Almsgiving, Chapter 1 (254 AD)', 'This fire of persecution is a kind of purifying oven: it tests each of us and separates us according to our merits.', 'suffering,holiness,perseverance,truth', false);

-- 45: Basil the Great
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(45, 'Homilies on the Hexameron, Homily 5 (circa 378 AD)', 'I want creation to penetrate you with so much admiration that everywhere, wherever you may be, the least plant may bring to you the clear remembrance of the Creator.', 'creation,worship,gratitude,beauty', true),
(45, 'Letters, Letter 2 (circa 360 AD)', 'The one who walks with God always has sufficient company.', 'prayer,trust,presence,peace', false),
(45, 'On the Holy Spirit, Chapter 9 (375 AD)', 'The Holy Spirit is not subordinate in dignity; He is, rather, the third Person of the Trinity, equal in honour.', 'holy_spirit,theology,truth,worship', false),
(45, 'Moral Rules, Rule 2 (circa 360 AD)', 'A tree is known by its fruit; a man by his deeds. A good deed is never lost; he who sows courtesy reaps friendship.', 'integrity,service,love,wisdom', false),
(45, 'On Wealth and Poverty (circa 370 AD)', 'The bread you do not use is the bread of the hungry; the garment hanging in your wardrobe is the garment of him who is naked.', 'justice,service,compassion,love', true),
(45, 'Letters, Letter 150 (circa 374 AD)', 'Our works are as a ladder by which we ascend to God.', 'holiness,service,obedience,calling', false),
(45, 'Letters, Letter 1 (circa 360 AD)', 'He who has health has hope; and he who has hope has everything.', 'hope,faith,trust,gratitude', false);

-- 46: Gregory of Nyssa
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(46, 'The Life of Moses, Book I (circa 390 AD)', 'The perfection of human nature consists perhaps in its very growth in goodness.', 'holiness,growth,transformation,truth', true),
(46, 'The Life of Moses, Book II (circa 390 AD)', 'Every concept which comes from some comprehensible image by an approximate understanding and by guessing at the divine nature constitutes an idol of God and does not proclaim God.', 'worship,theology,humility,truth', true),
(46, 'On the Soul and the Resurrection (circa 380 AD)', 'If, then, the soul is purified of every vice, it will most certainly be in the sphere of Beauty. The Deity is in very substance Beautiful; and to the Deity the soul will in its state of purity have affinity.', 'holiness,transformation,hope,grace', true),
(46, 'Homilies on Ecclesiastes, Homily 8 (circa 380 AD)', 'What does it profit a man to have a body adorned with gold if his soul is squalid?', 'holiness,truth,wisdom,repentance', false),
(46, 'The Life of Moses, Part I (circa 390 AD)', 'The one who is going to associate intimately with God must go beyond all that is visible and (lifting up his own mind, as to a mountaintop, to the invisible and incomprehensible) believe that the divine is there where the understanding does not reach.', 'prayer,worship,holiness,contemplation', true),
(46, 'On the Holy Spirit (circa 380 AD)', 'The Holy Spirit is co-equal with the Father and the Son.', 'holy_spirit,theology,truth,worship', false);

-- 47: Gregory of Nazianzus
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(47, 'Oration 6: On Peace (circa 364 AD)', 'Ours is not the old covenant of works, but one of grace.', 'grace,salvation,theology,truth', false),
(47, 'Theological Orations, Oration 29 (circa 380 AD)', 'To ponder on God is to be on fire; to speak of God is to set others on fire.', 'worship,evangelism,calling,zeal', false),
(47, 'Letter 101 (circa 382 AD)', 'For that which He has not assumed He has not healed; but that which is united to His Godhead is also saved.', 'incarnation,salvation,theology,truth', true),
(47, 'Oration 14: On Love for the Poor (circa 372 AD)', 'Give something, however little, to those in need. For it is not little to one who has nothing.', 'service,compassion,justice,love', false),
(47, 'Oration 16: On His Father''s Silence (circa 375 AD)', 'Nothing is so characteristic of a Christian as peacemaking.', 'love,peace,service,truth', false),
(47, 'Theological Orations, Oration 28 (circa 380 AD)', 'It is difficult to conceive God but to define Him in words is an impossibility … But in my opinion it is impossible to express Him, and yet more impossible to conceive Him.', 'worship,prayer,holiness,truth', true);

-- 48: Ambrose of Milan
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(48, 'On the Duties of the Clergy, Book I, Chapter 1 (386 AD)', 'The glory of God is man fully alive; and the life of man consists in beholding God.', 'worship,creation,hope,joy', false),
(48, 'On Virginity, Book I, Chapter 6 (circa 377 AD)', 'The devil fears not the fast which is prolonged, but the prayer which is fervent.', 'prayer,spiritual_warfare,faith,devotion', false),
(48, 'Letters, Sermon Against Auxentius, Chapter 36 (386 AD)', 'The emperor is within the Church, not above it.', 'truth,justice,authority,courage', true),
(48, 'Hexameron (six days of creation, circa 389 AD)', 'God has ordered all things in measure and number and weight.', 'creation,wisdom,sovereignty,truth', false),
(48, 'On the Faith, Book I, Prologue (378 AD)', 'Faith is the foundation of all good things; without it, it is impossible to please God.', 'faith,truth,obedience,salvation', false),
(48, 'On the Duties of the Clergy, Book I, Chapter 36 (386 AD)', 'The crown of old age is wisdom; the glory of youth is its strength.', 'wisdom,truth,character,calling', false);

-- 49: Jerome
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(49, 'Commentary on Isaiah, Prologue (circa 408 AD)', 'Ignorance of Scripture is ignorance of Christ.', 'scripture,truth,word_of_god,faith', true),
(49, 'Commentary on Ezekiel, Preface (circa 415 AD)', 'The Scriptures are shallow enough for a babe to come and drink without fear of drowning, and deep enough for theologians to swim in without ever touching the bottom.', 'scripture,wisdom,truth,word_of_god', false),
(49, 'Letter 60 to Heliodorus (396 AD)', 'Love the knowledge of Scripture, and you will not love the vices of the flesh.', 'scripture,holiness,wisdom,obedience', false),
(49, 'Commentary on Isaiah (circa 408 AD)', 'I interpreted not according to what I felt, but according to what was written.', 'scripture,truth,integrity,obedience', false),
(49, 'Letter 22 to Eustochium (384 AD)', 'Do not let the women of Jerusalem see you walking in the street; let them not know your face; let them not know your name.', 'holiness,wisdom,purity,obedience', false),
(49, 'Letter 108 on the death of Paula (404 AD)', 'In the Holy Scriptures, every word is pregnant with meaning.', 'scripture,truth,word_of_god,wisdom', false),
(49, 'Letter 22 to Eustochium, Chapter 16 (384 AD)', 'Do not court the company of married ladies or visit the houses of the high-born. Do not look too often on the life which you despised to become a virgin.', 'holiness,wisdom,purity,warning', true);

-- 50: Justin Martyr
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(50, 'First Apology, Chapter 67 (circa 155 AD)', 'And on the day called Sunday, all who live in cities or in the country gather together to one place, and the memoirs of the apostles or the writings of the prophets are read, as long as time permits.', 'church,scripture,community,worship', true),
(50, 'First Apology, Chapter 10 (circa 155 AD)', 'And we have been taught, and are convinced, and do believe, that He accepts those only who imitate the excellences which reside in Him, temperance, and justice, and philanthropy.', 'faith,obedience,holiness,truth', true),
(50, 'Second Apology, Chapter 13 (circa 160 AD)', 'Whatever things were rightly said among all men are the property of us Christians.', 'truth,wisdom,faith,apologetics', true),
(50, 'Dialogue with Trypho, Chapter 8 (circa 155 AD)', 'I found this philosophy alone to be safe and profitable. Thus, and for this reason, I am a philosopher.', 'truth,wisdom,faith,testimony', true),
(50, 'First Apology, Chapter 13 (circa 155 AD)', 'What sober-minded man, then, will not acknowledge that we are not atheists, worshipping as we do the Maker of this universe … whom we praise to the utmost of our power by the exercise of prayer and thanksgiving for all things wherewith we are supplied …', 'worship,truth,faith,theology', true),
(50, 'First Apology, Chapter 16 (circa 155 AD)', 'The teachings of Jesus are not private; they are meant for all mankind.', 'mission,truth,love,evangelism', false);

-- 51: Martin Luther King Jr.
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(51, 'Letter from Birmingham Jail (April 16, 1963)', 'Injustice anywhere is a threat to justice everywhere. We are caught in an inescapable network of mutuality, tied in a single garment of destiny.', 'justice,love,community,truth', true),
(51, 'Strength to Love (1963)', 'Darkness cannot drive out darkness; only light can do that. Hate cannot drive out hate; only love can do that.', 'love,hope,justice,truth', true),
(51, 'Letter from Birmingham Jail (April 16, 1963)', 'One has not only a legal but a moral responsibility to obey just laws. Conversely, one has a moral responsibility to disobey unjust laws.', 'justice,moral_courage,obedience,truth', true),
(51, 'I Have a Dream (August 28, 1963)', 'I have a dream that my four little children will one day live in a nation where they will not be judged by the color of their skin but by the content of their character.', 'hope,justice,equality,love', true),
(51, 'Strength to Love (1963)', 'The church must be reminded that it is not the master or the servant of the state, but rather the conscience of the state.', 'justice,truth,calling,moral_courage', true),
(51, 'Where Do We Go from Here: Chaos or Community? (1967)', 'Power without love is reckless and abusive, and love without power is sentimental and anemic.', 'love,justice,truth,wisdom', true),
(51, 'Strength to Love (1963)', 'We must develop and maintain the capacity to forgive. He who is devoid of the power to forgive is devoid of the power to love.', 'love,forgiveness,grace,compassion', true),
(51, 'Stride Toward Freedom (1958)', 'Human progress is neither automatic nor inevitable. … Every step toward the goal of justice requires sacrifice, suffering, and struggle; the tireless exertions and passionate concern of dedicated individuals.', 'justice,suffering,perseverance,calling', true),
(51, 'Strength to Love (1963)', 'The tough-minded person always examines the facts before he reaches conclusions; in short, he postjudges. The tender-minded person reaches a conclusion before he has examined the first fact; in short, he prejudges and is prejudiced.', 'truth,justice,wisdom,courage', true),
(51, 'I''ve Been to the Mountaintop (April 3, 1968)', 'I just want to do God''s will. And He''s allowed me to go up to the mountain. And I''ve looked over. And I''ve seen the Promised Land.', 'hope,faith,sovereignty,courage', true);

-- 52: William Wilberforce
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(52, 'Cited in Robert Isaac Wilberforce and Samuel Wilberforce, The Life of William Wilberforce, vol. 1 (1838)', 'God Almighty has set before me two great objects, the suppression of the slave trade and the reformation of manners.', 'justice,calling,obedience,moral_courage', true),
(52, 'A Practical View of Christianity (1797)', 'Christianity is not merely a system of ethics, but a supernatural religion. We must have a change of heart.', 'faith,transformation,gospel,truth', false),
(52, 'Speech in the House of Commons (May 12, 1789)', 'Having heard all of this you may choose to look the other way but you can never again say that you did not know.', 'justice,moral_courage,truth,responsibility', false),
(52, 'Letter to William Hey (1801)', 'The objects of the present life fill the human eye with a false magnifying glass; it is only the eye of faith that sees the eternal world in its true proportions.', 'faith,eternity,truth,wisdom', false),
(52, 'Cited in Robert Isaac Wilberforce and Samuel Wilberforce, The Life of William Wilberforce, vol. 4 (1838)', 'If to be feelingly alive to the sufferings of my fellow-creatures and to be warmed with the desire of relieving their distresses, is to be a fanatic, I am one of the most incurable fanatics ever permitted to be at large.', 'compassion,justice,service,love', true),
(52, 'Cited in Cobbett''s Parliamentary History of England, vol. 29 (1817)', 'Never, never will we desist till we have wiped away this scandal from the Christian name, released ourselves from the load of guilt, under which we at present labour, and extinguished every trace of this bloody traffic…', 'justice,perseverance,moral_courage,calling', true),
(52, 'A Practical View of Christianity, Chapter 4, Section 2 (1797)', 'This state of mind contributes … to rectify the illusions of vision … and to remove backward, and reduce to their true comparative dimensions, the objects of the present life, which are apt to fill the human eye, assuming a false magnitude from their vicinity.', 'faith,wisdom,eternity,truth', true),
(52, 'Real Christianity (A Practical View, popular edition title)', 'Is it not the great end of religion, and, in particular, the glory of Christianity, to extinguish the malignant passions?', 'holiness,love,truth,transformation', false),
(52, 'A Practical View of Christianity, Chapter 4, Section 1 (1797)', 'This is the Christian love of God! A love compounded of admiration, of preference, of hope, of trust, of joy; chastised by reverential awe, and wakeful with continual gratitude.', 'love,worship,gratitude,trust', true),
(52, 'A Practical View of Christianity, Chapter 7 (1797)', 'Ever look to him for help: your only safety consists in a deep and abiding sense of your own weakness, and in a firm reliance on his strength.', 'humility,trust,faith,perseverance', true);

-- 53: Harriet Tubman
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(53, 'Cited in Kate Clifford Larson, Bound for the Promised Land (2004)', 'I never ran my train off the track, and I never lost a passenger.', 'faithfulness,courage,service,perseverance', false),
(53, 'Cited in Kate Clifford Larson, Bound for the Promised Land (2004)', 'I was the conductor of the Underground Railroad for eight years, and I can say what most conductors can''t say — I never ran my train off the track.', 'faithfulness,justice,service,courage', false),
(53, 'Cited in Sarah Bradford, Scenes in the Life of Harriet Tubman (1869)', 'No one will take me back alive; I shall fight for my liberty, and when de time has come for me to go, de Lord will let dem kill me.', 'justice,faith,courage,sovereignty', true),
(53, 'Cited in Sarah Bradford, Scenes in the Life of Harriet Tubman (1869)', 'I never met with any person, of any color, who had more confidence in the voice of God.', 'faith,trust,obedience,sovereignty', false),
(53, 'Cited in Ednah Dow Cheney, "Moses," The Freedmen''s Record (March 1865)', 'I prayed to God to make me strong and able to fight and that''s what I''ve allers prayed for ever since.', 'prayer,courage,faith,justice', true);

-- 54: Sojourner Truth
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(54, 'Ain''t I a Woman? Women''s Rights Convention, Akron, Ohio (1851)', 'And ar''n''t I a woman? Look at me! Look at my arm! I have plowed, and planted, and gathered into barns, and no man could head me—and ar''n''t I a woman?', 'justice,equality,moral_courage,truth', true),
(54, 'Narrative of Sojourner Truth, Book of Life (1884 edition)', 'I feel safe even in the midst of my enemies; for the truth is powerful and will prevail.', 'truth,faith,courage,hope', true),
(54, 'Narrative of Sojourner Truth (1850)', 'God will not make me suffer any more than I can bear.', 'trust,sovereignty,suffering,faith', false),
(54, 'Speech at the American Equal Rights Association (1867)', 'If women want any rights more than they''s got, why don''t they just take them, and not be talking about it?', 'justice,courage,moral_courage,calling', false),
(54, 'Cited in Olive Gilbert, Narrative of Sojourner Truth (1850)', 'I am not going to die, I''m going home like a shooting star.', 'hope,eternity,faith,joy', false),
(54, 'Narrative of Sojourner Truth, Book of Life (1884 edition)', 'When I left the house of bondage, I left everything behind. I wa''n''t goin'' to keep nothin'' of Egypt on me, an'' so I went to the Lord an'' asked him to give me a new name.', 'faith,transformation,freedom,calling', true);

-- 55: Desmond Tutu
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(55, 'No Future Without Forgiveness (1999)', '…when someone cannot be forgiven there is no future.', 'forgiveness,hope,love,reconciliation', true),
(55, 'God Has a Dream (2004)', 'My humanity is caught up and inextricably bound up in yours. I am human because I belong.', 'love,community,justice,truth', true),
(55, 'Cited in publisher Q&A on God Has a Dream (2004)', 'God''s dream is that you and I and all of us will realize that we are family, that we are made for togetherness, for goodness, and for compassion.', 'love,hope,community,justice', false),
(55, 'Cited in Tutu: The Authorised Portrait (2011)', 'Do your little bit of good where you are; it''s those little bits of good put together that overwhelm the world.', 'service,love,hope,calling', true),
(55, 'Cited in Robert McAfee Brown, Unexpected News: Reading the Bible with Third World Eyes (1984)', 'If you are neutral in situations of injustice, you have chosen the side of the oppressor.', 'justice,moral_courage,truth,calling', true),
(55, 'No Future Without Forgiveness (1999)', 'Forgiving is not forgetting; it''s actually remembering — remembering and not using your right to hit back.', 'forgiveness,love,grace,wisdom', false),
(55, 'Crying in the Wilderness: The Struggle for Justice in South Africa (1982)', 'I am not interested in picking up crumbs of compassion thrown from the table of someone who considers himself my master.', 'justice,dignity,truth,moral_courage', false),
(55, 'Made for Goodness (2010)', 'We are made for goodness by God, who is goodness itself. We are made like God. We are made for goodness.', 'love,joy,community,hope', true);

-- 56: Frederick Douglass
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(56, 'Narrative of the Life of Frederick Douglass (1845)', 'I would unite with anybody to do right and with nobody to do wrong.', 'justice,truth,moral_courage,integrity', false),
(56, 'What to the Slave Is the Fourth of July? (July 5, 1852)', 'This Fourth of July is yours, not mine. You may rejoice, I must mourn.', 'justice,truth,moral_courage,suffering', true),
(56, 'The Significance of Emancipation in the West Indies, address at Canandaigua, N.Y. (August 3, 1857)', 'If there is no struggle there is no progress.', 'perseverance,justice,truth,courage', true),
(56, 'Narrative of the Life of Frederick Douglass (1845)', 'I prayed for freedom for twenty years, but received no answer until I prayed with my legs.', 'faith,justice,action,courage', false),
(56, 'The Significance of Emancipation in the West Indies, address at Canandaigua, N.Y. (August 3, 1857)', 'The limits of tyrants are prescribed by the endurance of those whom they oppress.', 'justice,courage,truth,perseverance', true),
(56, 'My Bondage and My Freedom, Chapter 10 (1855)', '"Very well," thought I; "knowledge unfits a child to be a slave." I instinctively assented to the proposition; and from that moment I understood the direct pathway from slavery to freedom.', 'truth,justice,wisdom,freedom', true),
(56, 'Life and Times of Frederick Douglass, revised edition (1892)', 'No man can put a chain about the ankle of his fellow-man, without at last finding the other end of it about his own neck.', 'justice,truth,wisdom,moral_courage', true),
(56, 'Narrative of the Life of Frederick Douglass (1845)', 'Once you learn to read, you will be forever free.', 'truth,freedom,wisdom,hope', false);

-- 57: Abraham Lincoln
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(57, 'Second Inaugural Address (March 4, 1865)', 'With malice toward none, with charity for all, with firmness in the right as God gives us to see the right, let us strive on to finish the work we are in.', 'love,justice,humility,perseverance', true),
(57, 'Gettysburg Address (November 19, 1863)', 'We here highly resolve that these dead shall not have died in vain — that this nation, under God, shall have a new birth of freedom.', 'hope,justice,sacrifice,faith', true),
(57, 'Second Inaugural Address (March 4, 1865)', 'Both read the same Bible and pray to the same God, and each invokes His aid against the other.', 'prayer,truth,humility,justice', true),
(57, 'Letter to Joshua Speed (August 24, 1855)', 'I am not a master, I am nothing. But I do believe in God; and though I feel that I am not master of circumstances, I still believe.', 'faith,humility,trust,sovereignty', false),
(57, 'Speech to the Young Men''s Lyceum (January 27, 1838)', 'Let every American, every lover of liberty, every well-wisher to his posterity swear by the blood of the Revolution never to violate in the least particular the laws of the country, and never to tolerate their violation by others.', 'justice,obedience,truth,calling', true),
(57, 'Meditation on the Divine Will (September 2, 1862)', 'In the present civil war it is quite possible that God''s purpose is something different from the purpose of either party…', 'sovereignty,humility,truth,wisdom', true),
(57, 'Proclamation of a National Fast Day (March 30, 1863)', 'We have forgotten God. We have forgotten the gracious hand which preserved us in peace, and multiplied and enriched and strengthened us.', 'repentance,humility,truth,prayer', true),
(57, 'Letter to Eliza Gurney (September 4, 1864)', 'We hoped for a happy termination of this terrible war long before this; but God knows best, and has ruled otherwise.', 'trust,sovereignty,suffering,faith', true);

-- 58: Corrie ten Boom
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(58, 'The Hiding Place (1971)', 'There is no pit so deep that He is not deeper still.', 'hope,suffering,sovereignty,trust', true),
(58, 'Clippings from My Notebook (1982)', 'Worry does not empty tomorrow of its sorrow, it empties today of its strength.', 'trust,faith,peace,wisdom', true),
(58, 'Tramp for the Lord (1974)', 'If God sends us on stony paths, he provides strong shoes.', 'trust,sovereignty,suffering,hope', false),
(58, 'The Hiding Place (1971)', 'I must trust in the Love, not the feeling of love.', 'faith,trust,love,obedience', false),
(58, 'Clippings from My Notebook (1982)', 'I have learned in my years on earth to hold everything loosely because when I hold things tightly, God has to pry my fingers away. And that hurts.', 'surrender,faith,trust,wisdom', true),
(58, 'Tramp for the Lord (1974)', 'Forgiveness is an act of the will, and the will can function regardless of the temperature of the heart.', 'forgiveness,obedience,love,grace', true),
(58, 'The Hiding Place, Foreword (1971)', 'Every experience God gives us, every person He puts in our lives is the perfect preparation for the future that only He can see.', 'sovereignty,trust,hope,purpose', true),
(58, 'Each New Day (1977)', 'Never be afraid to trust an unknown future to a known God.', 'trust,faith,hope,sovereignty', true),
(58, 'Clippings from My Notebook (1982)', 'Is prayer your steering wheel or your spare tire?', 'prayer,faith,devotion,truth', true),
(58, 'Tramp for the Lord (1974)', 'You can never learn that Christ is all you need, until Christ is all you have.', 'faith,trust,suffering,grace', false);

-- 59: Eric Liddell
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(59, 'Cited in Sally Magnusson, The Flying Scotsman (1981)', 'I believe God made me for a purpose, but He also made me fast. And when I run I feel His pleasure.', 'calling,joy,worship,purpose', false),
(59, 'The Disciplines of the Christian Life (1985, posthumous)', 'Absolute surrender to the will of God is the key to Christian joy.', 'surrender,joy,obedience,faith', false),
(59, 'The Disciplines of the Christian Life (1985, posthumous)', 'In the dust of defeat as well as the laurels of victory there is a glory to be found if one has done his best.', 'perseverance,faithfulness,calling,integrity', false),
(59, 'The Disciplines of the Christian Life (1985, posthumous)', 'Have patience with God. He never hurries, but He is always on time.', 'trust,patience,sovereignty,faith', false),
(59, 'Cited in Julian Wilson, Complete Surrender: A Biography of Eric Liddell (1996)', 'Christ for the world, for the world needs Christ.', 'mission,love,hope,calling', true);

-- 60: Lord Shaftesbury (Anthony Ashley Cooper, 7th Earl)
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(60, 'Cited in Edwin Hodder, The Life and Work of the Seventh Earl of Shaftesbury (1886)', 'I cannot bear to leave the world with all the misery in it.', 'service,compassion,justice,calling', true),
(60, 'Address to Parliament (circa 1843)', 'The principle of the Ten Hours Bill is the right of the operatives to rest.', 'justice,service,calling,love', false),
(60, 'Diary entry (cited in Edwin Hodder, The Life of the Earl of Shaftesbury, 1886)', 'Nothing is great and lasting but what is done for eternity.', 'eternity,calling,service,wisdom', false),
(60, 'Speech in Parliament (1842)', 'What can I do, I ask myself, for the temporal and eternal welfare of these miserable beings?', 'service,justice,compassion,calling', false),
(60, 'Diary entry (cited in Edwin Hodder, The Life of the Earl of Shaftesbury, 1886)', 'The Bible is not the book of the Church: it is the book of the people.', 'scripture,truth,equality,word_of_god', false),
(60, 'Speeches of the Earl of Shaftesbury, Legislation for the Labouring Classes, Manchester, 26 October 1844 (1868)', 'God has given me leisure and freedom from the necessity of daily toil; and I willingly therefore devote them to the service of those people who have neither the one nor the other of these responsible gifts.', 'service,calling,justice,compassion', true),
(60, 'Speeches of the Earl of Shaftesbury, Young Men''s Christian Association, Manchester, 25 March 1856 (1868)', 'I know no sentiment that so tends to exalt, and at the same time so tends to humble the human heart, as does the sense of direct and immediate responsibility to Almighty God.', 'responsibility,humility,faith,calling', true),
(60, 'Speeches of the Earl of Shaftesbury, Sanitary Legislation, Social Science Congress, 1858 (1868)', 'But when people say we should think more of the soul and less of the body, my answer is, that the same God who made the soul made the body also.', 'compassion,justice,service,creation', true),
(60, 'Speeches of the Earl of Shaftesbury, Religious Service in Theatres, House of Lords, 24 February 1860 (1868)', 'These efforts, nevertheless, must be made; no good may be left unattempted; against hope we must believe in hope, and endeavour, though the mass be inaccessible, to rescue individuals.', 'hope,perseverance,service,compassion', true);

-- 61: Charles Finney
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(61, 'Lectures on Revivals of Religion, Lecture 1 (1835)', 'It is not a miracle, or dependent on a miracle, in any sense. It is a purely philosophical result of the right use of the constituted means—as much so as any other effect produced by the application of means.', 'faith,calling,obedience,mission', true),
(61, 'Lectures on Revivals of Religion (1835)', 'If sinners will be damned, at least let them leap to hell over our bodies.', 'evangelism,love,courage,mission', false),
(61, 'Lectures on Systematic Theology (1846)', 'Sanctification, then, is nothing more nor less than entire obedience, for the time being, to the moral law.', 'holiness,obedience,transformation,truth', true),
(61, 'Memoirs of Charles G. Finney (1876)', 'The moment I was converted, I felt it my duty to go immediately and tell the people around me what I had found.', 'conversion,evangelism,calling,boldness', false),
(61, 'Lectures on Revivals of Religion (1835)', 'Christians are more to blame for not being revived than sinners are for not being converted.', 'repentance,truth,calling,holiness', true),
(61, 'Lectures on Systematic Theology (1846)', 'True conversion implies the yielding of the will to the authority and benevolence of God.', 'repentance,obedience,surrender,faith', false),
(61, 'Memoirs of Charles G. Finney (1876)', 'I had read, of course, that God was everywhere present; but I had not understood it in any practical sense.', 'faith,presence,transformation,truth', false);

-- 62: John Newton
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(62, 'Amazing Grace, hymn (1779)', 'Amazing grace! How sweet the sound that saved a wretch like me! I once was lost, but now am found; was blind, but now I see.', 'grace,salvation,conversion,gratitude', true),
(62, 'Letters of John Newton (compiled 1960)', 'Not what I ought to be, not what I might be, not what I wish or hope to be, and not what I once was, I think I can truly say with the apostle, ''By the grace of God I am what I am.''', 'transformation,grace,hope,humility', true),
(62, 'Cited in Richard Cecil, Memoirs of the Rev. John Newton (1808)', 'If two angels came down from heaven to execute a divine command, and one was appointed to conduct an empire, and the other to sweep a street in it, they would feel no inclination to choose employments.', 'service,humility,calling,obedience', true),
(62, 'Olney Hymns, Book 1, Hymn 31 (1779)', 'Thou art coming to a King; large petitions with thee bring; for His grace and power are such, none can ever ask too much.', 'prayer,grace,faith,hope', true),
(62, 'Cited in Josiah Bull, John Newton of Olney and St. Mary Woolnoth (1868)', 'My memory is nearly gone, but I remember two things: that I am a great sinner and that Christ is a great Saviour.', 'grace,humility,salvation,love', true),
(62, 'Olney Hymns, Preface (1779)', 'The Christian life is not merely a changed life but an exchanged life.', 'transformation,grace,faith,discipleship', false),
(62, 'Cited in Richard Cecil, Memoirs of the Rev. John Newton (1808)', 'I endeavour to walk through the world as a physician goes through Bedlam: the patients make a noise, pester him with impertinence, and hinder him in his business; but he does the best he can, and so gets through.', 'service,love,compassion,wisdom', true),
(62, 'Letter to William Cowper (circa 1780)', 'Solid lasting peace is not obtained by reasoning, but by trusting in the Lord.', 'trust,peace,faith,prayer', false),
(62, 'Cardiphonia, Letter II to Mrs. G— (1781)', 'After all, I know the Lord keeps the key of comfort in His own hands, yet He has commanded us to attempt comforting one another.', 'comfort,encouragement,love,community', true);

-- 63: Olaudah Equiano
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(63, 'The Interesting Narrative of the Life of Olaudah Equiano (1789)', 'I have been all my life a wanderer; but I trust my God will be with me, wherever I go.', 'faith,trust,sovereignty,hope', false),
(63, 'The Interesting Narrative of the Life of Olaudah Equiano (1789)', 'I regarded myself as no longer a slave in any respect whatever; for I was purchased by Christ.', 'salvation,freedom,grace,faith', false),
(63, 'The Interesting Narrative of the Life of Olaudah Equiano, Chapter 7 (1789)', 'Before night, I who had been a slave in the morning, trembling at the will of another, was become my own master, and completely free. I thought this was the happiest day I had ever experienced.', 'joy,freedom,salvation,gratitude', true),
(63, 'The Interesting Narrative of the Life of Olaudah Equiano, Chapter 1 (1789)', 'Does not slavery itself depress the mind, and extinguish all its fire and every noble sentiment?', 'justice,truth,dignity,moral_courage', true),
(63, 'The Interesting Narrative of the Life of Olaudah Equiano, Chapter 2 (1789)', 'O, ye nominal Christians! might not an African ask you, learned you this from your God, who says unto you, Do unto all men as you would men should do unto you?', 'truth,obedience,integrity,calling', true);

-- 64: Howard Thurman
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(64, 'Jesus and the Disinherited (1949)', 'Don''t ask what the world needs. Ask what makes you come alive, and go do it. Because what the world needs is people who have come alive.', 'calling,joy,purpose,service', false),
(64, 'Jesus and the Disinherited (1949)', 'The masses of men live with their backs constantly against the wall. They are the poor, the disinherited, the dispossessed. What does our religion say to them?', 'justice,love,gospel,compassion', true),
(64, 'Jesus and the Disinherited (1949)', 'Fear is one of the persistent hounds of hell that dog the footsteps of the poor, the dispossessed, the disinherited. There is nothing new or recent about fear…', 'justice,truth,suffering,hope', true),
(64, 'Meditations of the Heart (1953)', 'The first step toward the love of God is the love of one''s own soul.', 'love,self-knowledge,truth,faith', false),
(64, 'With Head and Heart: The Autobiography of Howard Thurman (1979)', 'I looked upon myself as belonging to a great communion of saints; all who had suffered and lived in the faith.', 'community,faith,perseverance,hope', false),
(64, 'Deep is the Hunger (1951)', 'A man must have some private place, some solitary ground where he can be restored.', 'prayer,peace,wisdom,rest', false),
(64, 'The Luminous Darkness (1965)', 'The cross is the symbol of the redemption of the world through suffering.', 'cross,suffering,redemption,hope', false);

-- 65: John Perkins
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(65, 'Let Justice Roll Down (1976)', 'The most fundamental need of the poor is not money, but love.', 'love,justice,service,truth', false),
(65, 'Let Justice Roll Down (1976)', 'Racism made me hate and despair. Jesus Christ made me believe that reconciliation is possible.', 'love,reconciliation,faith,hope', false),
(65, 'With Justice for All (1982)', 'Reconciliation is the whole message of the Bible.', 'reconciliation,love,gospel,truth', false),
(65, 'Dream with Me (2017)', 'Love will always be our final fight.', 'love,justice,perseverance,calling', true),
(65, 'Let Justice Roll Down (1976)', 'God''s call is always to incarnation — to move into the neighborhood.', 'service,love,calling,justice', false),
(65, 'With Justice for All (1982)', 'Community development is most effective when it is based on a biblical understanding of what it means to be human.', 'justice,service,truth,love', false),
(65, 'Dream with Me (2017)', 'The gospel we preach must be a gospel that transforms individuals and communities.', 'gospel,transformation,justice,love', false);

-- 66: Isaac Newton
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(66, 'The Principia: Mathematical Principles of Natural Philosophy, General Scholium (1713)', 'This most beautiful system of the sun, planets, and comets, could only proceed from the counsel and dominion of an intelligent and powerful Being.', 'creation,sovereignty,wisdom,worship', true),
(66, 'Letter to Richard Bentley (December 10, 1692)', 'When I wrote my treatise about our System I had an eye upon such principles as might work with considering men for the belief of a Deity.', 'faith,creation,wisdom,truth', true),
(66, 'Observations upon the Prophecies of Daniel (published 1733)', 'I have a fundamental belief in the Bible as the Word of God, written by those who were inspired.', 'scripture,faith,truth,word_of_god', false),
(66, 'Letter to Robert Hooke (February 5, 1676)', 'If I have seen further it is by standing on the shoulders of giants.', 'humility,wisdom,learning,truth', true),
(66, 'Cited in David Brewster, Memoirs of the Life, Writings and Discoveries of Sir Isaac Newton (1855)', 'I do not know what I may appear to the world; but to myself I seem to have been only like a boy playing on the sea-shore, and diverting myself in now and then finding a smoother pebble or a prettier shell than ordinary, whilst the great ocean of truth lay all undiscovered before me.', 'humility,wisdom,wonder,truth', true),
(66, 'The Principia: Mathematical Principles of Natural Philosophy, General Scholium (1713)', 'He governs all things, and knows all things that are or can be done.', 'sovereignty,wisdom,trust,faith', true);

-- 67: Blaise Pascal
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(67, 'Pensées, Fragment 148 (Lafuma), published 1670', 'There was once in man a true happiness of which there now remain to him only the mark and empty trace… But these are all inadequate, because the infinite abyss can only be filled by an infinite and immutable object, that is to say, only by God Himself.', 'worship,longing,truth,love', true),
(67, 'Pensées, Fragment 423 (Lafuma)', 'The heart has its reasons, which reason does not know.', 'faith,wisdom,love,truth', true),
(67, 'Pensées, Fragment 136 (Lafuma)', 'All the unhappiness of men arises from one single fact, that they cannot stay quietly in their own chamber.', 'prayer,wisdom,truth,self-knowledge', true),
(67, 'Pensées, Fragment 418 (Lafuma)', 'If you gain, you gain all; if you lose, you lose nothing. Wager, then, without hesitation that He is.', 'faith,wisdom,eternity,truth', true),
(67, 'Memorial, written November 23, 1654', 'God of Abraham, God of Isaac, God of Jacob, not of the philosophers and scholars. Certitude, certitude, feeling, joy, peace.', 'faith,joy,peace,conversion', true),
(67, 'Pensées, Fragment 149 (Lafuma)', 'There is enough light for those who only desire to see, and enough obscurity for those who have a contrary disposition.', 'faith,truth,wisdom,obedience', true),
(67, 'Pensées, Fragment 200 (Lafuma)', 'Man is but a reed, the most feeble thing in nature; but he is a thinking reed.', 'humility,truth,wisdom,humanity', true),
(67, 'Pensées, Fragment 671 (Lafuma)', 'Do you wish people to believe good of you? Don''t speak.', 'humility,wisdom,truth,integrity', true),
(67, 'Pensées, Fragment 212 (Lafuma)', 'Jesus Christ is a God whom we approach without pride, and before whom we humble ourselves without despair.', 'grace,love,humility,salvation', true),
(67, 'Pensées, Fragment 192 (Lafuma)', 'The knowledge of God without that of man''s misery causes pride. The knowledge of man''s misery without that of God causes despair.', 'truth,humility,grace,wisdom', true);

-- 68: C.S. Lewis
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(68, 'Mere Christianity, Book II, Chapter 3 (1952)', 'God cannot give us a happiness and peace apart from Himself, because it is not there. There is no such thing.', 'worship,love,joy,truth', true),
(68, 'The Weight of Glory (1941)', 'Indeed, if we consider the unblushing promises of reward and the staggering nature of the rewards promised in the Gospels, it would seem that Our Lord finds our desires, not too strong, but too weak.', 'hope,joy,eternity,desire', true),
(68, 'God in the Dock (1970)', 'Christianity is a statement which, if false, is of no importance, and, if true, of infinite importance. The one thing it cannot be is moderately important.', 'truth,faith,wisdom,apologetics', true),
(68, 'The Problem of Pain, Chapter 6 (1940)', 'God whispers to us in our pleasures, speaks in our conscience, but shouts in our pains: it is His megaphone to rouse a deaf world.', 'suffering,sovereignty,truth,transformation', true),
(68, 'The Four Loves, Chapter 6 (1960)', 'To love at all is to be vulnerable. Love anything, and your heart will certainly be wrung and possibly be broken.', 'love,suffering,courage,truth', true),
(68, 'Surprised by Joy, Chapter 14 (1955)', 'I gave in, and admitted that God was God, and knelt and prayed: perhaps, that night, the most dejected and reluctant convert in all England.', 'conversion,faith,humility,truth', true),
(68, 'The Screwtape Letters, Letter 29 (1942)', 'Courage is not simply one of the virtues, but the form of every virtue at the testing point.', 'courage,holiness,wisdom,truth', true),
(68, 'A Grief Observed (1961)', 'Part of every misery is, so to speak, the misery''s shadow or reflection: the fact that you don''t merely suffer but have to keep on thinking about the fact that you suffer.', 'suffering,truth,wisdom,hope', true),
(68, 'Mere Christianity, Book III, Chapter 10 (1952)', 'If I find in myself a desire which no experience in this world can satisfy, the most probable explanation is that I was made for another world.', 'hope,eternity,longing,faith', true),
(68, 'The Weight of Glory (1941)', 'Next to the Blessed Sacrament itself, your neighbour is the holiest object presented to your senses.', 'love,service,worship,truth', true);

-- 69: G.K. Chesterton
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(69, 'What''s Wrong with the World, Part I, Chapter 5 (1910)', 'The Christian ideal has not been tried and found wanting. It has been found difficult; and left untried.', 'truth,faith,wisdom,apologetics', true),
(69, 'Autobiography (1936)', 'The object of opening the mind, as of opening the mouth, is to shut it again on something solid.', 'truth,wisdom,faith,discernment', true),
(69, 'Orthodoxy, Chapter 9 (1908)', 'It is not merely that God has arbitrarily made us such that we find joy in certain things and not in others. It is that we were made for joy, and joy is our deepest need.', 'joy,truth,purpose,love', false),
(69, 'The Defendant, A Defence of Heraldry (1901)', 'There is a road from the eye to the heart that does not go through the intellect.', 'love,wisdom,beauty,faith', true),
(69, 'What''s Wrong with the World (1910)', 'The reformer is always right about what''s wrong. He is generally wrong about what is right.', 'wisdom,truth,humility,justice', false),
(69, 'Orthodoxy, Chapter 4 (1908)', 'If I am wrong, I lose nothing by being a Christian; if I am right, I gain everything.', 'faith,wisdom,truth,hope', false),
(69, 'Saint Thomas Aquinas (1933)', 'The riddles of God are more satisfying than the solutions of man.', 'wisdom,sovereignty,truth,wonder', false),
(69, 'Heretics, Chapter 1 (1905)', 'There are two ways to get enough: one is to accumulate more and more. The other is to desire less.', 'humility,simplicity,wisdom,contentment', false),
(69, 'Orthodoxy, Chapter 7 (1908)', 'Angels can fly because they can take themselves lightly.', 'joy,humility,wisdom,grace', true),
(69, 'The Everlasting Man, Part II, Chapter 6 (1925)', 'Christianity has died many times and risen again; for it had a god who knew the way out of the grave.', 'hope,resurrection,faith,truth', true);

-- 70: Francis Bacon
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(70, 'Essays, Of Atheism (1612)', 'A little philosophy inclineth man''s mind to atheism, but depth in philosophy bringeth men''s minds about to religion.', 'faith,wisdom,truth,apologetics', true),
(70, 'The Advancement of Learning, Book I (1605)', 'God hath framed the mind of man as a mirror or glass, capable of the image of the universal world, and joyful to receive the impression thereof.', 'creation,wisdom,truth,wonder', true),
(70, 'Essays, Of Truth (1625)', 'What is truth? said jesting Pilate, and would not stay for an answer.', 'truth,wisdom,justice,discernment', true),
(70, 'Novum Organum, Aphorism 89 (1620)', 'A little science estranges a man from God; a lot of science brings him back.', 'faith,wisdom,creation,truth', false),
(70, 'Essays, Of Beauty (1625)', 'Virtue is like a rich stone, best plain set.', 'holiness,simplicity,wisdom,truth', true);

-- 71: Galileo Galilei
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(71, 'Letter to the Grand Duchess Christina (1615)', 'I would say here something that was heard from an ecclesiastic of the most eminent degree: "That the intention of the Holy Ghost is to teach us how one goes to heaven, not how heaven goes."', 'truth,scripture,wisdom,faith', true),
(71, 'Letter to the Grand Duchess Christina (1615)', 'This granted, and it being true that two truths cannot contradict one another, it is the function of expositors to seek out the true senses of scriptural texts.', 'truth,wisdom,creation,faith', true),
(71, 'The Assayer (Il Saggiatore, 1623)', 'Philosophy is written in this grand book, the universe, which stands continually open to our gaze… It is written in the language of mathematics.', 'creation,wisdom,beauty,truth', true),
(71, 'Letter to the Grand Duchess Christina (1615)', 'The holy Bible and the phenomena of nature proceed alike from the divine Word, the former as the dictate of the Holy Ghost and the latter as the observant executrix of God''s commands.', 'scripture,creation,truth,faith', true);

-- 72: Johannes Kepler
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(72, 'Mysterium Cosmographicum (1596)', 'I was merely thinking God''s thoughts after him. Since we astronomers are priests of the highest God in regard to the book of nature, it befits us to be thoughtful.', 'worship,creation,wisdom,calling', false),
(72, 'Harmonices Mundi (1619)', 'The chief aim of all investigations of the external world should be to discover the rational order and harmony which has been imposed on it by God.', 'creation,wisdom,worship,truth', false),
(72, 'Epitome of Copernican Astronomy (1618)', 'I give thanks to God, who is the author of my understanding; let Him deal with me according to His mercy.', 'gratitude,humility,worship,grace', false),
(72, 'Cited in Max Caspar, Kepler (1959)', 'Since we astronomers are priests of the highest God in regard to the book of nature, it befits us to be thoughtful, not of the glory of our minds, but rather, above all else, of the glory of God.', 'worship,calling,humility,truth', true),
(72, 'Harmonices Mundi, Book V, Chapter 9 (1619)', 'I give thanks to Thee, O Lord Creator, Who hast delighted me with Thy makings and in the works of Thy hands have I exulted.', 'gratitude,worship,creation,joy', true),
(72, 'Mysterium Cosmographicum, Preface (1596)', 'I believe it was by divine ordinance that I obtained by chance that which previously I could not reach by any pains.', 'calling,sovereignty,worship,truth', true);

-- 73: George Washington Carver
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(73, 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)', 'I go to the laboratory early every morning. I never know what I am going to find out. I get down on my knees and pray, and then I go to work.', 'prayer,calling,devotion,service', false),
(73, 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)', 'I love to think of nature as an unlimited broadcasting station, through which God speaks to us every hour.', 'creation,prayer,worship,trust', false),
(73, 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)', 'When you do the common things in life in an uncommon way, you will command the attention of the world.', 'service,calling,faithfulness,integrity', false),
(73, 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)', 'Fear of something is at the root of hate for others, and hate within will eventually destroy the hater.', 'love,wisdom,truth,justice', false),
(73, 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)', 'Reading about nature is fine, but if a person walks in the woods and listens carefully, he can learn more than what is in books.', 'wisdom,creation,truth,wonder', false),
(73, 'Cited in Gary Kremer, George Washington Carver: In His Own Words (1987)', 'It is simply service that measures success.', 'service,humility,calling,truth', false),
(73, 'Cited in Rackham Holt, George Washington Carver: An American Biography (1943)', 'I never have to grope for methods; the method is revealed at the moment I am inspired to create something new.', 'faith,calling,creativity,trust', true);

-- 74: Gregor Mendel
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(74, 'Cited in Hugo Iltis, Life of Mendel (1932)', 'My scientific studies have afforded me great gratification; and I am convinced that it will not be long before the whole world acknowledges the results of my work.', 'faith,perseverance,truth,hope', true),
(74, 'Experiments on Plant Hybridization (1866)', 'It requires indeed some courage to undertake a labor of such far-reaching extent; this appears, however, to be the only right way by which we can finally reach the solution of a question.', 'courage,calling,perseverance,truth', true),
(74, 'Cited in Hugo Iltis, Life of Mendel (1932)', 'My time will come.', 'perseverance,faith,hope,trust', true);

-- 75: Francis Collins
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(75, 'The Language of God (2006)', 'Science is not threatened by God; it is enhanced. God is most certainly not threatened by science; He made it all possible.', 'faith,creation,truth,wisdom', true),
(75, 'The Language of God (2006)', 'The God of the Bible is also the God of the genome. He can be worshiped in the cathedral or in the laboratory.', 'worship,creation,faith,truth', true),
(75, 'The Language of God (2006)', 'Atheism, I came to see, required just as much of a leap of faith as religion did.', 'faith,truth,wisdom,apologetics', false),
(75, 'The Language of God (2006)', 'Faith in God and science can be fully compatible.', 'faith,truth,creation,wisdom', false),
(75, 'The Language of God (2006)', 'The human genome is a record of God''s creation, written in the language of life.', 'creation,worship,truth,wonder', false),
(75, 'The Language of God (2006)', 'When I hear the Hallelujah chorus, I am moved to tears. But this is not proof of God; it is evidence of transcendence.', 'worship,beauty,truth,faith', false),
(75, 'BioLogos Forum (2009)', 'Evolution is the means by which God created us. This is not a position of compromise; it is a position of integration.', 'creation,faith,truth,wisdom', false),
(75, 'Is There a God and Does He Care About Me? The Testimony of BioLogos Founder Francis Collins, BioLogos (2019)', 'I realized I’d really neglected the most important question that any of us ever asks: Is there a God, and does that God care about me?', 'faith,truth,seeking,conviction', true);

-- 76: Alexis de Tocqueville
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(76, 'Democracy in America, Vol. 1, Chapter 17 (1835)', 'Despotism may govern without faith, but liberty cannot. Religion is much more necessary in the republic… than in the monarchy which they attack.', 'faith,justice,truth,freedom', true),
(76, 'Democracy in America, Vol. 1, Chapter 17 (1835)', 'There is no country in the whole world in which the Christian religion retains a greater influence over the souls of men than in America.', 'faith,truth,community,calling', true),
(76, 'Democracy in America, Vol. 2, Part 4, Chapter 8 (1840)', 'The health of a democratic society may be measured by the quality of functions performed by private citizens.', 'service,justice,community,calling', false),
(76, 'Democracy in America, Vol. 1, Chapter 17 (1835)', 'I do not know whether all the Americans have a sincere faith in their religion, for who can search the human heart? but I am certain that they hold it to be indispensable to the maintenance of republican institutions.', 'faith,truth,justice,wisdom', true),
(76, 'Recollections, Part II, Chapter 2 (published 1893, written 1850)', 'Even that is a great thing in politics, where a community of hatred is almost always the foundation of friendships.', 'wisdom,truth,love,justice', true);

-- 77: Søren Kierkegaard
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(77, 'Journals (1842)', 'The most common form of despair is not being who you are.', 'truth,self-knowledge,faith,identity', false),
(77, 'Either/Or, Part II (1843)', 'To dare is to lose one''s footing momentarily. Not to dare is to lose oneself.', 'faith,courage,truth,obedience', false),
(77, 'Concluding Unscientific Postscript (1846)', 'An objective uncertainty held fast in an appropriation-process of the most passionate inwardness is the truth, the highest truth attainable for an existing individual… But the above definition of truth is an equivalent expression for faith.', 'faith,truth,courage,obedience', true),
(77, 'Works of Love (1847)', 'To help someone to become himself free, independent, his own, to help him to stand alone: that is the greatest benefit one man can confer upon another.', 'love,service,truth,calling', true),
(77, 'Purity of Heart Is to Will One Thing (1847)', 'Purity of heart is to will one thing.', 'holiness,obedience,truth,heart', true),
(77, 'The Sickness unto Death (1849)', 'God creates everything out of nothing. And everything which God is to use, he first reduces to nothing.', 'grace,sovereignty,humility,transformation', false),
(77, 'Journals (1847)', 'The Bible is very easy to understand. But we Christians are a bunch of scheming swindlers. We pretend to be unable to understand it because we know very well that the minute we understand, we are obligated to act accordingly.', 'scripture,truth,obedience,repentance', false),
(77, 'Journals (1843)', 'It is perfectly true, as the philosophers say, that life must be understood backwards. But they forget the other proposition, that it must be lived forwards.', 'wisdom,faith,trust,truth', true);

-- 78: William James
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(78, 'The Varieties of Religious Experience (1902)', 'The greatest revolution of our generation is the discovery that human beings, by changing the inner attitudes of their minds, can change the outer aspects of their lives.', 'transformation,faith,truth,wisdom', false),
(78, 'The Will to Believe and Other Essays, The Moral Philosopher and the Moral Life (1897)', 'The nobler thing tastes better, and that is all that we can say.', 'calling,perseverance,faith,service', true),
(78, 'The Will to Believe and Other Essays (1897)', 'Act as if what you do makes a difference. It does.', 'faith,calling,obedience,truth', false),
(78, 'The Varieties of Religious Experience (1902)', 'Religion is the feelings, acts, and experiences of individual men in their solitude, so far as they apprehend themselves to stand in relation to whatever they may consider the divine.', 'faith,prayer,devotion,truth', true);

-- 79: Dorothy Sayers
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(79, 'Creed or Chaos?, The Dogma Is the Drama (1949)', 'Surely it is not the business of the Church to adapt Christ to men, but to adapt men to Christ.', 'truth,gospel,calling,transformation', true),
(79, 'Creed or Chaos? (1949)', 'The people who hanged Christ never, to do them justice, accused Him of being a bore; on the contrary, they thought Him too dynamic to be safe.', 'truth,justice,moral_courage,faith', true),
(79, 'Are Women Human? (1938)', 'What is repugnant to every human being is to be reckoned always as a member of a class and not as an individual person.', 'dignity,justice,truth,love', true),
(79, 'Creed or Chaos?, The Greatest Drama Ever Staged (1949)', 'The dogma is the drama.', 'truth,faith,gospel,joy', true),
(79, 'Letters to a Diminished Church (2004, compiled)', 'If we insist on calling a trivial Jesus Lord, we shall find that we are not following the Lamb; we are following a lapdog.', 'truth,discipleship,calling,faith', false),
(79, 'Creed or Chaos? (1949)', 'Cowardice is the sin most immediately responsible for human misery.', 'courage,truth,moral_courage,justice', false),
(79, 'The Mind of the Maker (1941)', 'A life of ease is not the destiny of man. A life of service is.', 'service,calling,truth,humility', false),
(79, 'Begin Here (1940)', 'Love is not enough. In the end you have to have something to say to people — something true.', 'truth,love,wisdom,calling', false);

-- 80: John Lennox
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(80, 'God''s Undertaker: Has Science Buried God? (2007)', 'Statements about the existence of God are not statements that science can adjudicate.', 'faith,truth,wisdom,apologetics', false),
(80, 'God and Stephen Hawking (2011)', 'If we are simply products of evolution, our belief in God becomes just another product of evolution — not necessarily true.', 'faith,truth,wisdom,apologetics', false),
(80, 'Gunning for God (2011)', 'The New Atheism is not a serious intellectual movement; it is simply an expression of emotion and prejudice.', 'truth,wisdom,courage,apologetics', false),
(80, 'God''s Undertaker: Has Science Buried God? (2007)', 'Science and faith are not in conflict. They are two ways of knowing the same universe.', 'faith,truth,creation,wisdom', false),
(80, 'Against the Flow (2015)', 'Daniel''s life shows that it is possible to maintain integrity in a corrupt environment.', 'integrity,courage,truth,obedience', false),
(80, 'Against the Flow (2015)', 'The resurrection of Jesus is the best-attested fact in ancient history.', 'hope,truth,faith,resurrection', false),
(80, 'God and Stephen Hawking (2011)', 'A new age of information does not make God obsolete; it reminds us that information is not self-creating.', 'truth,creation,wisdom,faith', false);

-- 81: Mother Teresa
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(81, 'A Simple Path (1995)', 'Try not to judge people. If you judge others then you are not giving love.', 'love,compassion,service,wisdom', true),
(81, 'In the Heart of the World (1997)', 'Not all of us can do great things. But we can do small things with great love.', 'service,love,humility,calling', false),
(81, 'No Greater Love (1997)', 'We will never know how much good just a simple smile can do.', 'love,joy,service,compassion', true),
(81, 'Cited in Malcolm Muggeridge, Something Beautiful for God (1971)', 'I see God in every human being. When I wash the leper''s wounds I feel I am nursing the Lord himself.', 'service,love,worship,compassion', false),
(81, 'A Simple Path (1995)', 'If we have no peace, it is because we have forgotten that we belong to each other.', 'peace,love,community,truth', false),
(81, 'In the Heart of the World (1997)', 'Loneliness and the feeling of being unwanted is the most terrible poverty.', 'love,compassion,service,truth', false),
(81, 'A Simple Path (1995)', 'The fruit of silence is prayer. The fruit of prayer is faith. The fruit of faith is love. The fruit of love is service. The fruit of service is peace.', 'prayer,faith,love,service', true),
(81, 'No Greater Love (1997)', 'I''m just a little pencil in His hand.', 'calling,humility,love,service', true),
(81, 'A Simple Path (1995)', 'We need to find God, and he cannot be found in noise and restlessness. God is the friend of silence.', 'prayer,peace,presence,devotion', false),
(81, 'A Simple Path (1995)', 'The greatest disease in the West today is not TB or leprosy; it is being unwanted, unloved, and uncared for.', 'love,service,compassion,justice', true);

-- 82: Hudson Taylor
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(82, 'Union and Communion (1894)', 'All God''s giants have been weak men who did great things for God because they reckoned on His being with them.', 'faith,humility,calling,sovereignty', false),
(82, 'Cited in Howard Taylor and Geraldine Taylor, Hudson Taylor''s Spiritual Secret (1932)', 'The secret of a man''s life is what he does with his waiting.', 'faith,patience,trust,calling', false),
(82, 'Retrospect (1894)', 'I have found that there are three stages in every great work of God: first, it is impossible, then it is difficult, then it is done.', 'faith,hope,perseverance,sovereignty', false),
(82, 'Cited in Howard Taylor and Geraldine Taylor, Hudson Taylor''s Spiritual Secret (1932)', 'God chose me because I was weak enough. God does not do His great works by large committees. He trains somebody to be quiet enough, and little enough, and then He uses him.', 'humility,calling,grace,sovereignty', false),
(82, 'China''s Spiritual Need and Claims (1865)', 'If I had a thousand pounds, China should have it. If I had a thousand lives, China should have them.', 'mission,sacrifice,love,calling', false),
(82, 'Retrospect (1894)', 'Since the day I left England I have not for one single moment ceased to be a missionary.', 'faithfulness,calling,perseverance,mission', false),
(82, 'Cited in Howard Taylor and Geraldine Taylor, Hudson Taylor''s Spiritual Secret (1932)', 'The prayer power has never been tried to its full capacity. If we want to see mighty wonders of divine grace and power wrought in the place of weakness, failure and disappointment, let us answer God''s standing challenge.', 'prayer,faith,sovereignty,mission', false),
(82, 'Retrospect (1894)', 'Never mind how great the difficulties are; God has promised, and God will do it.', 'faith,trust,sovereignty,perseverance', false);

-- 83: Amy Carmichael
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(83, 'Toward Jerusalem (1936)', 'You can give without loving, but you cannot love without giving.', 'love,service,giving,truth', false),
(83, 'If (1938)', 'If I am not willing to be the bond-servant of all, giving up my preferences, then I know nothing of Calvary love.', 'love,service,sacrifice,humility', false),
(83, 'If (1938)', 'If I take offence easily, if I am content to continue in a cool unfriendliness, though friendship be possible, then I know nothing of Calvary love.', 'love,forgiveness,humility,grace', true),
(83, 'Rose from Brier (1933)', 'God holds us to our own trust. He never lets us go.', 'trust,sovereignty,faithfulness,hope', false),
(83, 'Gold Cord (1932)', 'We are not workers for God who have our times with him, but sons and daughters of God who have our times with the world.', 'calling,prayer,identity,service', false),
(83, 'Cited in Frank Houghton, Amy Carmichael of Dohnavur (1953)', 'It is a life of joy. There is nothing dreary and doubtful about it. It is meant to be continually joyful.', 'joy,faith,truth,hope', true),
(83, 'Edges of His Ways (1955)', 'Good works do not make a good man, but a good man does good works.', 'faith,service,truth,integrity', false),
(83, 'If (1938)', 'If the praise of man elates me and his blame depresses me; if I cannot rest under misunderstanding without defending myself … then I know nothing of Calvary love.', 'humility,love,obedience,wisdom', true),
(83, 'If (1938)', 'A cup brimful of sweet water cannot spill even one drop of bitter water however suddenly jolted.', 'love,holiness,character,wisdom', true);

-- 84: David Livingstone
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(84, 'Dr. Livingstone''s Cambridge Lectures, Lecture I, Senate House (December 4, 1857)', 'I beg to direct your attention to Africa. I know that in a few years I shall be cut off in that country, which is now open. Do not let it be shut again!', 'mission,calling,courage,service', true),
(84, 'Cited in William Garden Blaikie, The Personal Life of David Livingstone (1880)', 'I will place no value on anything I have or may possess, except in relation to the kingdom of Christ.', 'surrender,calling,faith,obedience', true),
(84, 'Letter to his father (1838)', 'Nowhere have I ever appeared as a mere man of science, but as a Christian.', 'calling,integrity,truth,mission', false),
(84, 'Cited in William Garden Blaikie, The Personal Life of David Livingstone (1880)', '…to go anywhere, provided it be forward.', 'faith,courage,calling,obedience', true),
(84, 'Cambridge address, December 4, 1857', 'Sending the gospel to the heathen must include the highest and best thing that England has yet to give.', 'mission,service,love,calling', false),
(84, 'Cited in William Garden Blaikie, The Personal Life of David Livingstone (1880)', 'Fear God and work hard.', 'obedience,faith,diligence,truth', true),
(84, 'Missionary Travels (1857)', 'All that I am I owe to Jesus Christ, revealed to me in His divine Book.', 'gratitude,scripture,faith,love', false),
(84, 'Missionary Travels and Researches in South Africa, Introduction (1857)', 'The perfect freeness with which the pardon of all our guilt is offered in God’s book drew forth feelings of affectionate love to Him who bought us with his blood, and a sense of deep obligation to Him for his mercy has influenced, in some small measure, my conduct ever since.', 'grace,forgiveness,gratitude,love', true),
(84, 'Missionary Travels and Researches in South Africa, Chapter 32 (1857)', 'I have not mentioned half the favors bestowed, but I may just add that no one has cause for more abundant gratitude to his fellow-men and to his Maker than I have; and may God grant that the effect on my mind be such that I may be more humbly devoted to the service of the Author of all our mercies!', 'gratitude,humility,service,devotion', true);

-- 85: Jim Elliot
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(85, 'The Journals of Jim Elliot (October 28, 1949)', 'He is no fool who gives what he cannot keep to gain that which he cannot lose.', 'sacrifice,eternity,faith,wisdom', true),
(85, 'The Journals of Jim Elliot (1948)', 'Wherever you are, be all there.', 'obedience,calling,faith,presence', true),
(85, 'Cited in Elisabeth Elliot, Shadow of the Almighty (1958)', 'God, I pray Thee, light these idle sticks of my life and may I burn for Thee.', 'prayer,calling,surrender,zeal', true),
(85, 'The Journals of Jim Elliot (1952)', 'I seek not a long life but a full one like Yours, Lord Jesus.', 'calling,faith,sacrifice,obedience', true),
(85, 'The Journals of Jim Elliot (1950)', 'The thinnest place between time and eternity is the place of prayer.', 'prayer,eternity,faith,devotion', false),
(85, 'The Journals of Jim Elliot (1948)', 'Make my life a prayer unto You — I want to do what You want me to do.', 'prayer,obedience,surrender,calling', false),
(85, 'The Journals of Jim Elliot (1951)', 'Am I ignitible? God deliver me from the dread asbestos of ''other things.''', 'zeal,obedience,surrender,calling', true),
(85, 'The Journals of Jim Elliot (1978)', 'When it comes time to die, make sure that all you have to do is die!', 'holiness,wisdom,eternity,obedience', true);

-- 86: William Booth
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(86, 'In Darkest England and the Way Out (1890)', 'Not until I saw the misery of the poor in London did I understand the claims of Christ upon me.', 'service,compassion,calling,justice', false),
(86, 'Cited in Hulda Friederichs, The Life of General Booth (1912)', 'Go for souls, and go for the worst.', 'evangelism,service,love,calling', false),
(86, 'The Founder Speaks Again (1960, compiled)', 'I consider that the chief dangers which confront the coming century will be religion without the Holy Ghost, Christianity without Christ, forgiveness without repentance.', 'truth,warning,holiness,faith', false),
(86, 'I''ll Fight, Royal Albert Hall address (May 9, 1912)', 'While women weep, as they do now, I''ll fight; while little children go hungry, as they do now, I''ll fight.', 'justice,service,love,perseverance', true),
(86, 'The Founder Speaks Again (1960, compiled)', 'Love is the secret of the Cross.', 'love,cross,salvation,truth', false),
(86, 'The Founder Speaks Again (1960, compiled)', 'A man who is doing something, even if he is wrong, is better than a man who does nothing.', 'service,calling,faith,diligence', false),
(86, 'In Darkest England and the Way Out, Part 1, ch. 5 ''On the Verge of the Abyss'' (1890)', 'What is the use of preaching the Gospel to men whose whole attention is concentrated upon a mad, desperate struggle to keep themselves alive?', 'service,justice,compassion,truth', true);

-- 87: Lottie Moon
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(87, 'Letter published in the Foreign Mission Journal (December 1887)', 'I pray that no missionary will ever be as lonely as I have been. And yet the loneliness has been the means of forcing me to lean on God.', 'prayer,suffering,trust,sovereignty', false),
(87, 'Letter from China (1873)', 'I have a firm conviction that I am called to the work of a foreign missionary.', 'calling,faith,obedience,mission', false),
(87, 'Letter published in the Foreign Mission Journal (1887)', 'I would I could have my life to live over again, and give it all to mission work.', 'calling,sacrifice,love,mission', false),
(87, 'Letter from China (1880)', 'Is it not possible that women may do something for the evangelization of women?', 'calling,courage,justice,mission', false),
(87, 'Letter published in the Foreign Mission Journal (1887)', 'I do hope that no one will try to lessen the spirit of self-sacrifice. That is what distinguishes true Christianity.', 'sacrifice,love,calling,truth', false),
(87, 'Letter from China (1875)', 'God give us men and women, men and women filled with the spirit of God.', 'prayer,calling,holiness,mission', false),
(87, 'Cited in Una Roberts Lawrence, Lottie Moon (1927)', 'As you wind your way from village to village you feel it is no idle fancy that the Master walks beside you.', 'presence,mission,faith,comfort', true),
(87, 'Cited in Una Roberts Lawrence, Lottie Moon (1927)', 'I am more and more impressed by the belief that to win these people to God, we must first win them to ourselves. We need to go out and live among them, manifesting the gentle and loving spirit of our Lord.', 'love,mission,gentleness,evangelism', true),
(87, 'Cited in Una Roberts Lawrence, Lottie Moon (1927)', 'The prayer in former times was that God would open China. God has answered that prayer, and now who goes forth to possess the land?', 'prayer,mission,calling,obedience', true),
(87, 'Cited in Una Roberts Lawrence, Lottie Moon (1927)', 'The world is the field, and woman’s work for Christ is wherever there is a home to be reformed, or a soul to be redeemed.', 'calling,service,mission,women', false),
(87, 'Cited in Una Roberts Lawrence, Lottie Moon (1927)', 'If the joy of the Lord be their strength, the blessedness of the work will more than compensate for its hardships. Let them come rejoicing to sacrifice for that Lord and Master who so freely gave himself for them.', 'joy,sacrifice,mission,strength', true),
(87, 'Cited in Una Roberts Lawrence, Lottie Moon (1927)', 'I wish our people could realize that our Lord’s last command is as binding as is the command to baptize.', 'obedience,mission,calling,conviction', true);

-- 88: Adoniram Judson
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(88, 'Cited in Francis Wayland, A Memoir of the Life and Labors of the Rev. Adoniram Judson (1853)', 'Had it not been for the consolations of religion, and an assured conviction that every additional trial was ordered by infinite love and mercy, I must have sunk under my accumulated sufferings.', 'suffering,trust,love,sovereignty', true),
(88, 'Cited in Francis Wayland, A Memoir of the Life and Labors of the Rev. Adoniram Judson (1853)', 'God is God. Because he is God, He is worthy of my trust and obedience.', 'faith,obedience,trust,worship', false),
(88, 'Cited in Francis Wayland, A Memoir of the Life and Labors of the Rev. Adoniram Judson (1853)', 'I am not tired of my work, neither am I tired of the world; yet when Christ calls me home, I shall go with the gladness of a boy bounding away from his school.', 'hope,joy,eternity,faith', true),
(88, 'Letter to his family (1831)', 'Come over to Macedonia and help us. The call is still ringing.', 'mission,calling,evangelism,obedience', false),
(88, 'Cited in Edward Judson, The Life of Adoniram Judson (1883)', 'I have now to ask whether you can consent to part with your daughter early next spring, to see her no more in this world?', 'sacrifice,calling,love,mission', true),
(88, 'Cited in Francis Wayland, A Memoir of the Life and Labors of the Rev. Adoniram Judson (1853)', 'Beware of the world, its cares, its friendships, its pleasures.', 'holiness,wisdom,obedience,warning', false),
(88, 'Cited in Francis Wayland, A Memoir of the Life and Labors of the Rev. Adoniram Judson (1853)', 'But I hope I can say with truth that I love Christ above all; and I am striving, in the strength of my weak faith, to gird up my mind to face and welcome all his appointments.', 'surrender,love,faith,trust', true);

-- 89: Mary Slessor
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(89, 'Cited in W.P. Livingstone, Mary Slessor of Calabar (1915)', 'God and one is always a majority.', 'faith,courage,sovereignty,truth', false),
(89, 'Cited in W.P. Livingstone, Mary Slessor of Calabar (1915)', 'Lord, the task is impossible for me but not for Thee. Lead the way and I will follow.', 'prayer,obedience,trust,calling', false),
(89, 'Cited in W.P. Livingstone, Mary Slessor of Calabar (1915)', 'Christ is very near to me, and His presence makes all things lovely.', 'love,joy,presence,faith', false),
(89, 'Cited in W.P. Livingstone, Mary Slessor of Calabar (1915)', 'The only way I can explain it is on the ground that I have been prayed for more than most. Pray on, dear one—the power lies that way.', 'prayer,mission,perseverance,calling', true);

-- 90: Count Zinzendorf
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(90, 'Cited in A.J. Lewis, Zinzendorf the Ecumenical Pioneer (1962)', 'I have but one passion; ''tis He, ''tis only He.', 'love,worship,surrender,devotion', true),
(90, 'Cited in A.J. Lewis, Zinzendorf the Ecumenical Pioneer (1962)', 'Preach the Gospel, die, and be forgotten.', 'service,humility,calling,sacrifice', false),
(90, 'Cited in A.J. Lewis, Zinzendorf the Ecumenical Pioneer (1962)', 'Until we have prayed through together, we cannot march out together.', 'prayer,community,unity,calling', false),
(90, 'Cited in A.J. Lewis, Zinzendorf the Ecumenical Pioneer (1962)', 'The Saviour is always with me and this is enough.', 'trust,peace,faith,love', false),
(90, 'Maxims (collected early 18th century)', 'I have but one passion: it is Christ, Christ alone.', 'love,worship,obedience,calling', false);

-- 91: George Müller
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(91, 'Autobiography of George Müller (1861)', 'The beginning of anxiety is the end of faith, and the beginning of true faith is the end of anxiety.', 'faith,trust,peace,prayer', false),
(91, 'Autobiography of George Müller (1861)', 'The only way to learn strong faith is to endure great trials.', 'faith,suffering,perseverance,trust', false),
(91, 'Autobiography of George Müller (1861)', 'Prayer is not overcoming God''s reluctance; it is laying hold of His willingness.', 'prayer,faith,sovereignty,trust', false),
(91, 'Autobiography of George Müller (1905)', 'I saw more clearly than ever, that the first great and primary business to which I ought to attend every day was, to have my soul happy in the Lord.', 'joy,prayer,devotion,faith', true),
(91, 'A Narrative of Some of the Lord''s Dealings with George Müller (1837)', '…the first and primary object of the work was, (and still is:) that God might be magnified by the fact, that the orphans under my care are provided, with all they need, only by prayer and faith, without any one being asked by me or my fellow-labourers, whereby it may be seen, that God is faithful still, and hears prayer still.', 'service,humility,calling,faith', true),
(91, 'Answers to Prayer (1895)', 'I never remember, in all my Christian course, a period now (in March, 1895) of sixty-nine years and four months, that I ever sincerely and patiently sought to know the will of God by the teaching of the Holy Ghost, through the instrumentality of the Word of God, but I have been always directed rightly.', 'faith,prayer,obedience,trust', true),
(91, 'Autobiography of George Müller (1861)', 'Trust in God and do the next thing.', 'trust,obedience,faith,wisdom', false),
(91, 'Autobiography of George Müller (1905)', 'It is not enough to begin to pray, nor to pray aright; nor is it enough to continue for a time to pray; but we must patiently, believingly continue in prayer, until we obtain an answer.', 'prayer,perseverance,faith,trust', true),
(91, 'A Narrative of Some of the Lord''s Dealings with George Müller (1837)', 'God delights to increase the faith of His children.', 'faith,sovereignty,trust,grace', false);

-- 92: Gladys Aylward
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(92, 'Cited in Phyllis Thompson, A London Sparrow (1971)', 'I wasn’t God’s first choice for what I’ve done for China. There was somebody else … I don’t know who it was — God’s first choice. It must have been a man — a wonderful man. A well-educated man. I don’t know what happened. Perhaps he died. Perhaps he wasn’t willing … And God looked down … and saw Gladys Aylward.', 'calling,humility,sovereignty,grace', true),
(92, 'Cited in Phyllis Thompson, A London Sparrow (1971)', 'I just did the next thing.', 'obedience,faith,calling,simplicity', false),
(92, 'Cited in Alan Burgess, The Small Woman (1957)', 'I am not a great person. But I believe God is a great God who uses small people for great purposes.', 'humility,calling,sovereignty,faith', false),
(92, 'Cited in Phyllis Thompson, A London Sparrow (1971)', 'Never give up. Never, never, never.', 'perseverance,faith,courage,hope', false);

-- 93: C.T. Studd
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(93, 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)', 'If Jesus Christ be God and died for me, then no sacrifice can be too great for me to make for Him.', 'sacrifice,love,faith,calling', true),
(93, 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)', 'Some wish to live within the sound of Church or Chapel bell, I want to run a Rescue Shop within a yard of hell.', 'mission,courage,love,calling', true),
(93, 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)', 'Only one life, twill soon be past, only what''s done for Christ will last.', 'calling,eternity,sacrifice,wisdom', false),
(93, 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)', 'Not to do what seems impossible, but to do what God commands, is our business.', 'obedience,faith,calling,courage', false),
(93, 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)', 'How could I spend the best years of my life in living for the honours of this world, when thousands of souls are perishing every day?', 'sacrifice,calling,mission,love', true),
(93, 'Cited in Norman Grubb, C.T. Studd: Cricketer and Pioneer (1933)', 'If God calls you to be a missionary, don''t stoop to be a king.', 'calling,humility,service,truth', false);

-- 94: Jonathan Goforth
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(94, 'By My Spirit (1929)', 'The sin of silence has rested upon the Church long enough.', 'truth,moral_courage,calling,evangelism', false),
(94, 'By My Spirit (1929)', 'God is sovereign. He can have revival when and where He chooses.', 'sovereignty,faith,hope,prayer', false),
(94, 'Cited in Rosalind Goforth, Goforth of China (1937)', 'It is possible to be so active in the service of Christ as to forget to love Him.', 'love,warning,devotion,truth', false),
(94, 'By My Spirit (1929)', 'A revival is not a miracle worked from outside; it is a result of faithful prayer, faithful preaching, and faithful obedience.', 'prayer,faith,obedience,mission', false),
(94, 'Cited in Rosalind Goforth, Goforth of China (1937)', 'We must open the door of our hearts wider and wider to Christ if we want his life to flow more freely through us.', 'faith,surrender,love,calling', false);

-- 95: Elisabeth Elliot
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(95, 'Through Gates of Splendor (1957)', 'The will of God is always a bigger thing than we bargain for.', 'sovereignty,obedience,trust,faith', false),
(95, 'A Slow and Certain Light (1973)', 'In whatever I do, I want to say yes to God.', 'obedience,surrender,faith,calling', false),
(95, 'Let Me Be a Woman (1976)', 'The fact that I am a woman does not make me a different kind of Christian, but the fact that I am a Christian does make me a different kind of woman.', 'faith,identity,truth,calling', true),
(95, 'Keep a Quiet Heart (1995)', 'The secret is Christ in me, not me in a different set of circumstances.', 'faith,sovereignty,trust,hope', true),
(95, 'These Strange Ashes (1998)', 'Of one thing I am perfectly sure: God’s story never ends with “ashes.”', 'hope,sovereignty,trust,suffering', true),
(95, 'A Slow and Certain Light (1973)', 'In order to learn what it means to love, we must first learn what it means to give.', 'love,service,sacrifice,truth', false),
(95, 'Keep a Quiet Heart (1995)', 'Do not demand from others the things that only God can give.', 'trust,wisdom,love,truth', false),
(95, 'Let Me Be a Woman (1976)', 'The strength of a woman''s calling lies not in what she demands, but in what she willingly lays down.', 'service,humility,calling,love', false);

-- 96: Frank Laubach
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(96, 'Letters by a Modern Mystic (1937)', 'Can we have that contact with God all the time? All the time awake, fall asleep in His arms, and awaken in His presence, can we attain that?', 'prayer,devotion,presence,calling', true),
(96, 'Letters by a Modern Mystic (1937)', 'Oh, this thing of keeping in constant touch with God, of making him the object of my thought and the companion of my conversations, is the most amazing thing I ever ran across.', 'prayer,presence,joy,devotion', true),
(96, 'Letters by a Modern Mystic (1937)', 'I choose to look at people through God, using God as my glasses, colored with his love for them.', 'love,compassion,service,prayer', true),
(96, 'Prayer: The Mightiest Force in the World (1946)', 'Prayer is as mighty today as it ever was, if we will give it a chance.', 'prayer,faith,trust,hope', false),
(96, 'Cited in Marjorie Medary, Champion of the Silent Billion (1954)', 'The trouble with nearly everybody who prays is that he says ''Amen'' and runs away before God has a chance to reply.', 'prayer,wisdom,trust,devotion', true),
(96, 'Letters by a Modern Mystic (1937)', 'Open your heart and ask God to fill you minute by minute with His thoughts.', 'prayer,surrender,devotion,presence', false);

-- 97: Samuel Zwemer
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(97, 'The Unoccupied Mission Fields of Africa and Asia (1911)', 'The chief sin of the church today is the sin of prayerlessness.', 'prayer,truth,calling,repentance', false),
(97, 'Islam: A Challenge to Faith (1907)', 'Nothing is impossible to God. The evangelization of the Muslim world is not beyond the reach of the gospel.', 'faith,hope,mission,sovereignty', false),
(97, 'Cited in J. Christy Wilson, Apostle to Islam (1952)', 'The greatest need of the Muslim world is Christ.', 'mission,love,truth,calling', false),
(97, 'The Unoccupied Mission Fields of Africa and Asia (1911)', 'Why should I seek comfort when the Master went ahead of me in agony?', 'sacrifice,suffering,calling,obedience', false),
(97, 'Cited in J. Christy Wilson, Apostle to Islam (1952)', 'A man who is not praying is not preparing.', 'prayer,wisdom,calling,obedience', false);

-- 98: Nate Saint
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(98, 'Journal of Nate Saint (cited in Elisabeth Elliot, Through Gates of Splendor, 1957)', 'People who do not know the Lord ask why in the world we waste our lives as missionaries. They forget that they too are expending their lives, and that the difference is that we are driven by a higher purpose.', 'calling,sacrifice,mission,truth', false),
(98, 'Letter to his son (January 1956)', 'As we weigh the future and seek the will of God, does it seem right that we should go on considering our own safety when a small group of men have not heard of the risen Christ?', 'mission,sacrifice,calling,obedience', false),
(98, 'Cited in Elisabeth Elliot, Through Gates of Splendor (1957)', 'If God would grant us the vision, the word sacrifice would disappear from our lips and thoughts…', 'calling,love,sacrifice,faith', true),
(98, 'Journal of Nate Saint (1955)', 'We are not looking for a thrill or an adventure; we are looking to obey Christ.', 'obedience,calling,mission,faith', false),
(98, 'Letter to the Christian Airmen''s Missionary Fellowship (1945)', 'Til tonight I couldn’t see just why the Lord would want me because I have not had fitting training for the work so far but the Lord has given me an irresistible desire to take the Gospel to those who have never heard of His love.', 'calling,mission,surrender,love', true),
(98, 'Letter to the Christian Airmen''s Missionary Fellowship (1945)', 'Last New Year’s eve in a watch-nite service I said “yes” to God on the missionary challenge.', 'calling,obedience,surrender,mission', true);

-- 99: John G. Paton
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(99, 'John G. Paton: An Autobiography (1889)', 'We prayed as one can only pray when in the jaws of death and on the brink of Eternity. We felt that God was near, and omnipotent to do what seemed best in His sight.', 'faith,presence,trust,mission', true),
(99, 'John G. Paton: An Autobiography (1889)', 'God is stronger than these cannibals, and in Him I trust.', 'faith,courage,sovereignty,trust', false),
(99, 'John G. Paton: An Autobiography (1889)', 'If it is not doing His will to go to the heathen, then there is no meaning in the Bible.', 'mission,obedience,scripture,calling', false),
(99, 'John G. Paton: An Autobiography (1889)', 'In Jesus I felt invulnerable and immortal, so long as I was doing His work.', 'faith,sovereignty,courage,calling', true),
(99, 'John G. Paton: An Autobiography (1889)', 'Without that abiding consciousness of the presence and power of my dear Lord and Saviour, nothing else in all the world could have preserved me from losing my reason and perishing miserably.', 'trust,suffering,faith,presence', true);

-- 100: Andrew Murray
INSERT INTO quotes (figure_id, source, text, themes, verified) VALUES
(100, 'With Christ in the School of Prayer (1885)', 'The man who mobilizes the Christian church to pray will make the greatest contribution to world evangelization in history.', 'prayer,mission,calling,faith', false),
(100, 'Abide in Christ (1882)', 'God is ready to assume full responsibility for the life totally yielded to Him.', 'surrender,trust,sovereignty,faith', false),
(100, 'Humility (1895)', 'Humility is not thinking meanly of oneself; it is simply not thinking of oneself at all.', 'humility,love,service,truth', false),
(100, 'With Christ in the School of Prayer (1885)', 'Prayer is not monologue, but dialogue; God''s voice in response to mine is its most essential part.', 'prayer,trust,sovereignty,devotion', true),
(100, 'The Ministry of Intercession (1897)', 'Time spent in prayer will yield more than that given to work. Prayer alone gives work its worth and its success.', 'prayer,faith,wisdom,calling', true),
(100, 'The Spirit of Christ (1888)', 'The Holy Spirit is the Spirit of prayer. He is the One who makes intercession in us and through us.', 'prayer,holy_spirit,faith,calling', false),
(100, 'Waiting on God (1896)', 'God is patient. He is never in a hurry. To wait on God is to be in the school of patience.', 'patience,trust,sovereignty,wisdom', false),
(100, 'Humility (1895)', 'Just as water ever seeks and fills the lowest place, so the moment God finds the creature abased and empty, His glory and power flow in to exalt and to bless.', 'humility,grace,sovereignty,transformation', true),
(100, 'Abide in Christ (1882)', 'In the life of faith everything depends upon the certainty that God is wholly and always on our side.', 'faith,trust,sovereignty,hope', false),
(100, 'The Ministry of Intercession (1897)', 'Christ has called every one of us to intercession. This is not an option; it is our holy calling.', 'prayer,calling,obedience,love', false);

-- Reset Postgres sequence after explicit ID inserts
SELECT setval('quotes_id_seq', (SELECT MAX(id) FROM quotes));
