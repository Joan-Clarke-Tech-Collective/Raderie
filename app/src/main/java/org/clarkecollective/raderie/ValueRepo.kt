package org.clarkecollective.raderie

import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.models.HumanValue

class ValueRepo {

  fun freshDeckObject(): ArrayList<HumanValue> { val rv = arrayListOf<HumanValue>()
    rv.addAll(hvObjectList)
    return rv
  }

  val hvObjectList = arrayListOf (
    HumanValue(0, "acceptance", 0),
    HumanValue(1, "accomplishment", 1),
    HumanValue(2, "accountability", 1),
    HumanValue(3, "accuracy", 8),
    HumanValue(4, "achievement", 2),
    HumanValue(5, "adaptability", 1),
    HumanValue(6, "alertness", 5),
    HumanValue(7, "altruism", 2),
    HumanValue(8, "Ambition", 1),
    HumanValue(9, "Amusement", 9),
    HumanValue(10, "Assertiveness", 0),
    HumanValue(11, "Attentiveness", 2),
    HumanValue(12, "Awareness", 5),
    HumanValue(13, "Balance", 2),
    HumanValue(14, "Beauty", 1),
    HumanValue(15, "Boldness", 6),
    HumanValue(16, "Bravery", 0),
    HumanValue(17, "Brilliance", 7),
    HumanValue(18, "Calm", 1),
    HumanValue(19, "Candor", 0),
    HumanValue(20, "Capability", 0),
    HumanValue(21, "Carefulness", 1),
    HumanValue(22, "Certainty", 2),
    HumanValue(23, "Challenge", 1),
    HumanValue(24, "Charisma", 0),
    HumanValue(25, "Charity", 1),
    HumanValue(26, "Cheerfulness", 0),
    HumanValue(27, "Clarity", 2),
    HumanValue(28, "Cleanliness", 3),
    HumanValue(29, "Clear-headedness", 1),
    HumanValue(30, "Cleverness", 1),
    HumanValue(31, "Comfort", 2),
    HumanValue(32, "Commitment", 1 ),
    HumanValue(33, "Communication", 2),
    HumanValue(34, "Community", 0),
    HumanValue(35, "Compassion", 0),
    HumanValue(36, "Competence", 0),
    HumanValue(37, "Composure", 3),
    HumanValue(38, "Concentration", 5),
    HumanValue(39, "Confidence", 2),
    HumanValue(40, "Conformity", 0),
    HumanValue(41, "Conscientiousness"),
    HumanValue(42, "Connection"),
    HumanValue(43, "Consciousness"),
    HumanValue(44, "Consideration"),
    HumanValue(45, "Consistency"),
    HumanValue(46, "Contentment"),
    HumanValue(47, "Contribution"),
    HumanValue(48, "Control"),
    HumanValue(49, "Conviction"),
    HumanValue(50, "Cooperation"),
    HumanValue(51, "Courage"),
    HumanValue(52, "Courtesy"),
    HumanValue(53, "Creativity"),
    HumanValue(54, "Credibility"),
    HumanValue(55, "Curiosity"),
    HumanValue(56, "Decisiveness"),
    HumanValue(57, "Decorum"),
    HumanValue(58, "Dedication"),
    HumanValue(59, "Dependability"),
    HumanValue(60, "Determination"),
    HumanValue(61, "Development"),
    HumanValue(62, "Devotion"),
    HumanValue(63, "Dignity"),
    HumanValue(64, "Discipline"),
    HumanValue(65, "Discovery"),
    HumanValue(66, "Dreams"),
    HumanValue(67, "Drive"),
    HumanValue(68, "Duty"),
    HumanValue(69, "earnestness"),
    HumanValue(70, "effectiveness"),
    HumanValue(71, "efficiency"),
    HumanValue(72, "elegance"),
    HumanValue(73, "empathy"),
    HumanValue(74, "endurance"),
    HumanValue(75, "energy"),
    HumanValue(76, "enjoyment"),
    HumanValue(77, "enthusiasm", 2),
    HumanValue(78, "equity"),
    HumanValue(79, "ethics"),
    HumanValue(80, "excellence"),
    HumanValue(81, "excitement"),
    HumanValue(82, "experience"),
    HumanValue(83, "exercise"),
    HumanValue(84, "exploration"),
    HumanValue(85, "expressiveness"),
    HumanValue(86, "faith"),
    HumanValue(87, "fairness"),
    HumanValue(88, "fame"),
    HumanValue(89, "favors"),
    HumanValue(90, "fearlessness"),
    HumanValue(91, "feelings"),
    HumanValue(92, "ferocity"),
    HumanValue(93, "fidelity"),
    HumanValue(94, "flexibility"),
    HumanValue(95, "focus"),
    HumanValue(96, "foresight"),
    HumanValue(97, "forgiveness"),
    HumanValue(98, "fortitude"),
    HumanValue(99, "freedom"),
    HumanValue(100, "friendship"),
    HumanValue(101, "generosity"),
    HumanValue(102, "genius"),
    HumanValue(103, "giving"),
    HumanValue(104, "goodness"),
    HumanValue(105, "grace"),
    HumanValue(106, "gratitude", 0),
    HumanValue(107, "greatness"),
    HumanValue(108, "growth"),
    HumanValue(109, "guidance"),
    HumanValue(110, "happiness"),
    HumanValue(111, "hard_work"),
    HumanValue(112, "harmony"),
    HumanValue(113, "health"),
    HumanValue(114, "helping friends", 0),
    HumanValue(115, "helping_strangers", 0),
    HumanValue(116, "honesty"),
    HumanValue(117, "honor"),
    HumanValue(118, "hope"),
    HumanValue(119, "hospitality"),
    HumanValue(120, "humility"),
    HumanValue(121, "imagination"),
    HumanValue(122, "improvement"),
    HumanValue(123, "independence", 0),
    HumanValue(124, "individuality"),
    HumanValue(125, "industriousness"),
    HumanValue(126, "influence"),
    HumanValue(127, "innovation"),
    HumanValue(128, "inquisitiveness"),
    HumanValue(129, "insight"),
    HumanValue(130, "inspiration"),
    HumanValue(131, "integrity"),
    HumanValue(132, "intelligence"),
    HumanValue(133, "intensity"),
    HumanValue(134, "intimacy"),
    HumanValue(135, "intuition", 4),
    HumanValue(136, "inventiveness"),
    HumanValue(137, "joy", 1),
    HumanValue(138, "justice"),
    HumanValue(139, "kindness"),
    HumanValue(140, "knowledge"),
    HumanValue(141, "lawfulness", 1),
    HumanValue(142, "leadership"),
    HumanValue(143, "learning"),
    HumanValue(144, "liberty"),
    HumanValue(145, "logic"),
    HumanValue(146, "love"),
    HumanValue(147, "loyalty", 0),
    HumanValue(148, "mastery"),
    HumanValue(149, "maturity", 0),
    HumanValue(150, "meaning"),
    HumanValue(151, "moderation"),
    HumanValue(152, "motivation"),
    HumanValue(153, "openness"),
    HumanValue(154, "optimism"),
    HumanValue(155, "order"),
    HumanValue(156, "organization"),
    HumanValue(157, "originality"),
    HumanValue(158, "passion"),
    HumanValue(159, "patience"),
    HumanValue(160, "peace"),
    HumanValue(161, "performance"),
    HumanValue(162, "persistence"),
    HumanValue(163, "playfulness"),
    HumanValue(164, "poise", 8),
    HumanValue(165, "potential"),
    HumanValue(166, "power", 4),
    HumanValue(167, "presence"),
    HumanValue(168, "productivity", 1),
    HumanValue(169, "professionalism"),
    HumanValue(170, "prosperity"),
    HumanValue(171, "punctuality", 0),
    HumanValue(172, "purpose"),
    HumanValue(173, "rationality", 0),
    HumanValue(174, "realism"),
    HumanValue(175, "reason"),
    HumanValue(176, "recognition"),
    HumanValue(177, "recreation"),
    HumanValue(178, "refinement"),
    HumanValue(179, "reflection"),
    HumanValue(180, "reliability"),
    HumanValue(181, "resilience"),
    HumanValue(182, "resolution"),
    HumanValue(183, "resourcefulness"),
    HumanValue(184, "respect"),
    HumanValue(185, "responsibility"),
    HumanValue(186, "responsiveness"),
    HumanValue(187, "rest"),
    HumanValue(188, "restraint"),
    HumanValue(189, "reverence"),
    HumanValue(190, "rigor"),
    HumanValue(191, "risk_taking"),
    HumanValue(192, "satisfaction"),
    HumanValue(193, "security"),
    HumanValue(194, "self reliance", 0),
    HumanValue(195, "self awareness"),
    HumanValue(196, "self control"),
    HumanValue(197, "selflessness"),
    HumanValue(198, "sensitivity"),
    HumanValue(199, "serenity"),
    HumanValue(200, "service"),
    HumanValue(201, "sharing"),
    HumanValue(202, "silence"),
    HumanValue(203, "simplicity"),
    HumanValue(204, "sincerity", 0),
    HumanValue(205, "skillfulness"),
    HumanValue(206, "solitude"),
    HumanValue(207, "spirituality", 0),
    HumanValue(208, "spontaneity"),
    HumanValue(209, "stability"),
    HumanValue(210, "status"),
    HumanValue(211, "stewardship"),
    HumanValue(212, "strength", 1),
    HumanValue(213, "structure"),
    HumanValue(214, "success"),
    HumanValue(215, "support"),
    HumanValue(216, "surprise"),
    HumanValue(217, "sustainability"),
    HumanValue(218, "sympathy"),
    HumanValue(219, "talent"),
    HumanValue(220, "teamwork"),
    HumanValue(221, "temperance"),
    HumanValue(222, "thankfulness"),
    HumanValue(223, "thoroughness"),
    HumanValue(224, "thoughtfulness"),
    HumanValue(225, "thrift", 0),
    HumanValue(226, "timelessness"),
    HumanValue(227, "tolerance"),
    HumanValue(228, "toughness"),
    HumanValue(229, "tradition", 1),
    HumanValue(230, "tranquility"),
    HumanValue(231, "transparency"),
    HumanValue(232, "trust"),
    HumanValue(233, "trustworthiness"),
    HumanValue(234, "truth"),
    HumanValue(235, "understanding"),
    HumanValue(236, "uniqueness", 3),
    HumanValue(237, "unity", 0),
    HumanValue(238, "valor"),
    HumanValue(239, "victory", 4),
    HumanValue(240, "vigor"),
    HumanValue(241, "vision", 1),
    HumanValue(242, "vitality", 4),
    HumanValue(243, "wealth", 2),
    HumanValue(244, "welcoming", 9),
    HumanValue(245, "wholeness", 8),
    HumanValue(246, "wisdom", 1),
    HumanValue(247, "wonder", 1),
    HumanValue(248, "worthiness", 9),
    HumanValue(249, "zeal", 8),
    )

  fun drawTwo(valueList: List<HumanValue>): List<HumanValue> {
      val splitList = valueList.groupBy { it.priority }
      val average = splitList.values.map { list -> list.map { value -> value.gamesPlayed }.average() }.average()
      val topHalf = valueList.filter { it.gamesPlayed <= average }.sortedBy { it.priority }.chunked(2).flatten()
      Logger.d(topHalf)
      return topHalf.shuffled().take(2)
  }

  fun oldDrawTwo(valueList: List<HumanValue>): List<HumanValue> {
    val resultList = mutableListOf<HumanValue>()
    val splitList = valueList.sortedBy { it.gamesPlayed }.chunked(valueList.size / 3)
    resultList.add(splitList[1].random())
    val coinFlip = listOf(0, 1).random()
    if (coinFlip == 0) {
      resultList.add(splitList[0].random())
    } else {
      resultList.add((splitList[2] + splitList.last()).random())
    }
    return resultList
  }
}