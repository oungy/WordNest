package io.github.oungy.wordnest.data.local.study

internal object SampleCatalog {
    private val packContents = linkedMapOf(
        "foundations" to listOf(
            "notice-verb", "steady-adjective", "effort-noun", "choose-verb", "book-noun",
            "quiet-adjective", "supply-noun", "spare-adjective", "carry-verb", "enough-adverb",
        ),
        "everyday" to listOf(
            "notice-verb", "route-noun", "receipt-noun", "quiet-adjective", "book-verb",
            "aisle-noun", "nearby-adjective", "spare-adjective", "carry-verb", "fresh-adjective",
        ),
    )
    val packIds: Set<String> = packContents.keys

    suspend fun insertInto(dao: StudyDao) {
        dao.insertWords(words)
        dao.insertPacks(packIds.mapIndexed { position, id -> StudyPack(id, position, contentVersion = 1) })
        dao.insertPackWords(packContents.flatMap { (packId, ids) ->
            ids.mapIndexed { position, id -> StudyPackWord(packId, id, position) }
        })
    }

    private val words = listOf(
        StudyWord("notice-verb", "notice", "v.", "to become aware of something", "注意到，察觉",
            "Did you notice the sign by the door?", "你注意到门边的告示了吗？"),
        StudyWord("steady-adjective", "steady", "adj.", "regular and unlikely to change suddenly", "稳定的，平稳的",
            "Try to keep a steady pace.", "尽量保持匀速。"),
        StudyWord("effort-noun", "effort", "n.", "energy used to do something difficult", "努力，费力",
            "It took some effort to move the table.", "搬那张桌子费了些力。"),
        StudyWord("choose-verb", "choose", "v.", "to decide which thing you want", "选择",
            "You can choose a seat by the window.", "你可以选一个靠窗的座位。"),
        StudyWord("book-noun", "book", "n.", "a written work you can read", "书，书籍",
            "I left my book on the train.", "我把书落在火车上了。"),
        StudyWord("quiet-adjective", "quiet", "adj.", "making very little sound", "安静的",
            "We found a quiet corner to talk.", "我们找了个安静的角落聊天。"),
        StudyWord("supply-noun", "supply", "n.", "an amount of something available to use", "储备，供应量",
            "We keep a small supply of batteries.", "我们备了一些电池。"),
        StudyWord("spare-adjective", "spare", "adj.", "extra and available if needed", "备用的，多余的",
            "There is a spare key in the drawer.", "抽屉里有一把备用钥匙。"),
        StudyWord("carry-verb", "carry", "v.", "to hold and move something from place to place", "携带，搬运",
            "Can you carry this bag for me?", "你能帮我拿一下这个包吗？"),
        StudyWord("enough-adverb", "enough", "adv.", "to the degree that is needed", "足够地",
            "The coat is warm enough for this weather.", "这件外套应付这种天气够暖和。"),
        StudyWord("route-noun", "route", "n.", "a way from one place to another", "路线，路径",
            "This bus takes a different route on Sundays.", "这辆公交车周日走另一条路线。"),
        StudyWord("receipt-noun", "receipt", "n.", "a record showing that you paid for something", "收据，小票",
            "Keep the receipt in case you need to return it.", "留着小票，以备退货。"),
        StudyWord("book-verb", "book", "v.", "to arrange to use something at a future time", "预订",
            "Let’s book a table for Friday evening.", "我们订周五晚上的座位吧。"),
        StudyWord("aisle-noun", "aisle", "n.", "a passage between rows of seats or shelves", "过道，走道",
            "The tea is in the next aisle.", "茶叶在下一排货架。"),
        StudyWord("nearby-adjective", "nearby", "adj.", "a short distance away", "附近的",
            "We bought bread from a nearby bakery.", "我们在附近一家面包店买了面包。"),
        StudyWord("fresh-adjective", "fresh", "adj.", "recently made, picked, or prepared", "新鲜的",
            "They serve fresh fruit with breakfast.", "他们早餐供应新鲜水果。"),
    )
}
