package de.ywegel.svenska.testdata

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.AR
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.ER
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.N
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.OR
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.R
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.SPECIAL
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.UNCHANGED_EN
import de.ywegel.svenska.data.model.WordGroup.NounSubgroup.UNCHANGED_ETT
import de.ywegel.svenska.data.model.WordGroup.VerbSubgroup.GROUP_1
import de.ywegel.svenska.data.model.WordGroup.VerbSubgroup.GROUP_2A
import de.ywegel.svenska.data.model.WordGroup.VerbSubgroup.GROUP_2B
import de.ywegel.svenska.data.model.WordGroup.VerbSubgroup.GROUP_3
import de.ywegel.svenska.data.model.WordGroup.VerbSubgroup.GROUP_4_SPECIAL

/**
 * Single source of truth for real Swedish words in tests. Typos and made up test patterns stay in the test classes.
 *
 * Endings and forms are written space separated: "-n -or -orna", "pennan pennor pennorna".
 * Words with a prefix or suffix need the [ReferenceWord.text] as written in the source: "känna sig (-er, -de, -t)".
 * Words without abbreviated endings in the source repeat the written out forms as endings.
 */
object ReferenceWords {
    val nouns: List<ReferenceWord> = listOf(
        // group 1: -or
        en("penna", "-n -or -orna", "pennan pennor pennorna", OR),
        en("våg", "-en -or -orna", "vågen vågor vågorna", OR),
        en("ros", "-en -or -orna", "rosen rosor rosorna", OR),
        en("sida", "-n sidor sidorna", "sidan sidor sidorna", OR),
        en("flicka", "-an flickor flickorna", "flickan flickor flickorna", OR),
        en("självkänsla", "-n självkänslor -orna", "självkänslan självkänslor självkänslorna", OR),

        // group 2: -ar
        en("bil", "-en -ar -arna", "bilen bilar bilarna", AR),
        en("vägg", "-en -ar -arna", "väggen väggar väggarna", AR),
        en("topp", "-en -ar -arna", "toppen toppar topparna", AR),
        en("bro", "-n -ar -arna", "bron broar broarna", AR),
        en("by", "-n -ar -arna", "byn byar byarna", AR),
        en("ö", "-n -ar -arna", "ön öar öarna", AR),
        en("å", "-n -ar -arna", "ån åar åarna", AR),
        en("timme", "-n timmar timmarna", "timmen timmar timmarna", AR),
        en("hare", "-n harar hararna", "haren harar hararna", AR),
        en("hurtbulle", "-n bullar bullarna", "hurtbullen hurtbullar hurtbullarna", AR),
        en("cykel", "-n cyklar cyklarna", "cykeln cyklar cyklarna", AR),
        en("titel", "titeln titlar titlarna", "titeln titlar titlarna", AR),
        en("minister", "-n ministrar ministrarna", "ministern ministrar ministrarna", AR),
        en("semester", "semestern semestrar semestrarna", "semestern semestrar semestrarna", AR),
        en("morgon", "morgonen morgnar morgnarna", "morgonen morgnar morgnarna", AR),
        en("parkering", "-en -ar -arna", "parkeringen parkeringar parkeringarna", AR),
        en("mening", "-en -ar -arna", "meningen meningar meningarna", AR),
        en("kompis", "-en -ar -arna", "kompisen kompisar kompisarna", AR),
        en("sjukdom", "-en -ar -arna", "sjukdomen sjukdomar sjukdomarna", AR),
        en("storlek", "-en -ar -arna", "storleken storlekar storlekarna", AR),

        // group 3: -er
        en("maskerad", "-en -er -erna", "maskeraden maskerader maskeraderna", ER),
        en("japan", "-en -er -erna", "japanen japaner japanerna", ER),
        en("demonstrant", "-en -er -erna", "demonstranten demonstranter demonstranterna", ER),
        en("student", "-en -er -erna", "studenten studenter studenterna", ER),
        en("tomat", "-en -er -erna", "tomaten tomater tomaterna", ER),
        en("konferens", "-en -er -erna", "konferensen konferenser konferenserna", ER),
        en("protest", "-en -er -erna", "protesten protester protesterna", ER),
        en("raritet", "-en -er -erna", "rariteten rariteter rariteterna", ER),
        en("omelett", "-en -er -erna", "omeletten omeletter omeletterna", ER),
        en("mobil", "-en -er -erna", "mobilen mobiler mobilerna", ER),
        en("station", "-en -er -erna", "stationen stationer stationerna", ER),
        en("cyklist", "-en -er -erna", "cyklisten cyklister cyklisterna", ER),
        en("biolog", "-en -er -erna", "biologen biologer biologerna", ER),
        en("kromosom", "-en -er -erna", "kromosomen kromosomer kromosomerna", ER),
        en("idiot", "-en -er -erna", "idioten idioter idioterna", ER),
        en("filosof", "-en -er -erna", "filosofen filosofer filosoferna", ER),
        en("frisör", "-en -er -erna", "frisören frisörer frisörerna", ER),
        en("nyhet", "-en -er -erna", "nyheten nyheter nyheterna", ER),
        en("motor", "-n -er -erna", "motorn motorer motorerna", ER),
        en("kunskap", "-en -er -erna", "kunskapen kunskaper kunskaperna", ER),
        en("film", "-en -er -erna", "filmen filmer filmerna", ER),
        en("fest", "-en -er -erna", "festen fester festerna", ER),
        en("rad", "-en -er -erna", "raden rader raderna", ER),
        en("kö", "-n -er -erna", "kön köer köerna", ER),
        en("vy", "-n -er -erna", "vyn vyer vyerna", ER),
        ett("kafé", "-t -er -erna", "kaféet kaféer kaféerna", ER),
        ett("pris", "-et -er -erna", "priset priser priserna", ER),
        ett("bageri", "-t -er -erna", "bageriet bagerier bagerierna", ER),
        ett("museum", "-et museer museerna", "museet museer museerna", ER),
        ett("te", "teet teer teerna", "teet teer teerna", ER),

        // group 4: -r
        en("ko", "-n -r -rna", "kon kor korna", R),
        en("sko", "-n -r -rna", "skon skor skorna", R),
        en("sko", "-n skor skorna", "skon skor skorna", R),
        en("tå", "-n -r -rna", "tån tår tårna", R),
        en("same", "-n -er -erna", "samen samer samerna", R),
        en("serie", "-n -er -erna", "serien serier serierna", R),
        en("aktie", "-n -r -rna", "aktien aktier aktierna", R),
        en("fiende", "-n -r -rna", "fienden fiender fienderna", R),
        en("linje", "-n -r -rna", "linjen linjer linjerna", R),
        en("bakelse", "-n -r -rna", "bakelsen bakelser bakelserna", R),
        en("bibliotekarie", "-n -r -rna", "bibliotekarien bibliotekarier bibliotekarierna", R),

        // group 5: -n
        ett("konto", "-t -n -na", "kontot konton kontona", N),
        ett("meddelande", "-t -n -na", "meddelandet meddelanden meddelandena", N),
        ett("yrke", "-t -n -na", "yrket yrken yrkena", N),
        ett("bi", "-et -n -na", "biet bin bina", N),

        // group 6a: unchanged ett-words
        ett("träd", "-et - -en", "trädet träd träden", UNCHANGED_ETT),
        ett("program", "-met - -men", "programmet program programmen", UNCHANGED_ETT),
        ett("spel", "-et - -en", "spelet spel spelen", UNCHANGED_ETT),
        ett("hus", "-et - -en", "huset hus husen", UNCHANGED_ETT),
        ett("block", "-et - -en", "blocket block blocken", UNCHANGED_ETT),
        ett("uttryck", "-et - -en", "uttrycket uttryck uttrycken", UNCHANGED_ETT),
        ett("kapitel", "kapitlet - kapitlen", "kapitlet kapitel kapitlen", UNCHANGED_ETT),
        ett("plåster", "plåstret plåster plåstren", "plåstret plåster plåstren", UNCHANGED_ETT),
        ett(
            "fenomen", "-et - -en", "fenomenet fenomen fenomenen", UNCHANGED_ETT,
            text = "ett fenomen (-et, -, -en)", prefix = "ett",
        ),

        // group 6b: unchanged en-words
        en("målare", "-n - målarna", "målaren målare målarna", UNCHANGED_EN),
        en("indier", "-n - -na", "indiern indier indierna", UNCHANGED_EN),
        en("elektriker", "-n - -na", "elektrikern elektriker elektrikerna", UNCHANGED_EN),

        // Vowel change in the plural
        en("fot", "-en fötter fötterna", "foten fötter fötterna", SPECIAL),
        en("dotter", "-n döttrar döttrarna", "dottern döttrar döttrarna", SPECIAL),
        en("bonde", "-n bönder bönderna", "bonden bönder bönderna", SPECIAL),
        en("man", "mannen män männen", "mannen män männen", SPECIAL),
        en("brandman", "-nen -män -männen", "brandmannen brandmän brandmännen", SPECIAL),
        en("mus", "musen möss mössen", "musen möss mössen", SPECIAL),
        en("dagbok", "-en dagböcker dagböckerna", "dagboken dagböcker dagböckerna", SPECIAL),
        ett("land", "-et länder länderna", "landet länder länderna", SPECIAL),

        // Without a plural
        en("mat", "-en", "maten", SPECIAL),
        en("hunger", "-n", "hungern", SPECIAL),
        en("trafik", "-en", "trafiken", SPECIAL),
        en("skam", "-men", "skammen", SPECIAL),
        ett("kaffe", "-t", "kaffet", SPECIAL),
        ett("beröm", "-met", "berömmet", SPECIAL),
    )

    val verbs: List<ReferenceWord> = listOf(
        // Group 1: -ar
        verb("svara", "-r -de -t", "svarar svarade svarat", GROUP_1),
        verb("markera", "-r -de -t", "markerar markerade markerat", GROUP_1),
        verb("plugga", "-r -de -t", "pluggar pluggade pluggat", GROUP_1),

        // Group 2a: -er, -de
        verb("bygga", "bygger byggde byggt", "bygger byggde byggt", GROUP_2A),
        verb("köra", "kör körde kört", "kör körde kört", GROUP_2A),
        verb("glömma", "glömmer glömde glömt", "glömmer glömde glömt", GROUP_2A),
        verb(
            "känna", "-er kände känt", "känner kände känt", GROUP_2A,
            text = "känna sig (-er, kände, känt)", suffix = "sig",
        ),

        // Group 2b: -er, -te
        verb("söka", "-er -te -t", "söker sökte sökt", GROUP_2B),
        verb("läsa", "-er -te -t", "läser läste läst", GROUP_2B),
        verb("köpa", "köper köpte köpt", "köper köpte köpt", GROUP_2B),
        verb("byta", "byter bytte bytt", "byter bytte bytt", GROUP_2B),
        verb("åka", "-er åkte åkt", "åker åkte åkt", GROUP_2B, text = "åka (-er, åkte, åkt) till", suffix = "till"),
        verb(
            "gifta", "-er -te -t", "gifter gifte gift", GROUP_2B,
            text = "gifta sig (-er, -te, -t) med någon", suffix = "sig med någon",
        ),

        // Group 3: -r, -dde
        verb("bo", "-r -dde -tt", "bor bodde bott", GROUP_3),
        verb("må", "-r mådde mått", "mår mådde mått", GROUP_3),

        // Group 4: irregular
        verb("göra", "gör gjorde gjort", "gör gjorde gjort", GROUP_4_SPECIAL),
        verb("skriva", "skriver skrev skrivit", "skriver skrev skrivit", GROUP_4_SPECIAL),
        verb("välja", "väljer valde valt", "väljer valde valt", GROUP_4_SPECIAL),
        verb("kunna", "kan kunde kunnat", "kan kunde kunnat", GROUP_4_SPECIAL),
        verb("sätta", "sätter satte satt", "sätter satte satt", GROUP_4_SPECIAL),
        verb("växa", "växer växte vuxit", "växer växte vuxit", GROUP_4_SPECIAL),
        verb("glädja", "glädjer/gläder gladde glatt", "glädjer/gläder gladde glatt", GROUP_4_SPECIAL),
        verb(
            "ha", "-r hade haft", "har hade haft", GROUP_4_SPECIAL,
            text = "ha (-r, hade, haft) ont i", suffix = "ont i",
        ),
        verb("dö", "dör dog dött", "dör dog dött", GROUP_4_SPECIAL, text = "dö ut (dör, dog, dött)", suffix = "ut"),
        verb(
            "säga", "-er sa/sade sagt", "säger sa/sade sagt", GROUP_4_SPECIAL,
            text = "säga till (någon) (-er, sa/sade, sagt)", suffix = "till (någon)",
        ),
        verb(
            "flyga", "-er flög flugit", "flyger flög flugit", GROUP_4_SPECIAL,
            text = "flyga drake (-er, flög, flugit)", suffix = "drake",
        ),
        verb(
            "gå", "-r gick gått", "går gick gått", GROUP_4_SPECIAL,
            text = "gå ut med hunden (-r, gick, gått)", suffix = "ut med hunden",
        ),
    )

    val adjectives: List<ReferenceWord> = listOf(
        adjective("vanlig", "-t -a", "vanligt vanliga"),
        adjective("generell", "-t -a", "generellt generella"),
        adjective("svensk", "-t -a", "svenskt svenska"),

        // Already ending on -t, so only the plural is listed
        adjective("trött", "-a", "trötta"),
        adjective("bäst", "-a", "bästa"),
    )

    val all: List<ReferenceWord> get() = nouns + verbs + adjectives

    private fun en(
        word: String,
        endings: String,
        forms: String,
        subgroup: WordGroup.NounSubgroup,
        text: String? = null,
        prefix: String = "",
        suffix: String = "",
    ) = ReferenceWord(
        word, endings.words(), forms.words(), WordGroup.Noun(subgroup), Gender.Ultra,
        text ?: defaultText(word, endings), prefix, suffix,
    )

    private fun ett(
        word: String,
        endings: String,
        forms: String,
        subgroup: WordGroup.NounSubgroup,
        text: String? = null,
        prefix: String = "",
        suffix: String = "",
    ) = ReferenceWord(
        word, endings.words(), forms.words(), WordGroup.Noun(subgroup), Gender.Neutra,
        text ?: defaultText(word, endings), prefix, suffix,
    )

    private fun verb(
        word: String,
        endings: String,
        forms: String,
        subgroup: WordGroup.VerbSubgroup,
        text: String? = null,
        prefix: String = "",
        suffix: String = "",
    ) = ReferenceWord(
        word, endings.words(), forms.words(), WordGroup.Verb(subgroup), gender = null,
        text ?: defaultText(word, endings), prefix, suffix,
    )

    private fun adjective(
        word: String,
        endings: String,
        forms: String,
        text: String? = null,
        prefix: String = "",
        suffix: String = "",
    ) = ReferenceWord(
        word, endings.words(), forms.words(), WordGroup.Adjective, gender = null,
        text ?: defaultText(word, endings), prefix, suffix,
    )

    private fun String.words() = split(" ")

    // "penna", "-n -or -orna" -> "penna (-n, -or, -orna)"
    private fun defaultText(word: String, endings: String) = "$word (${endings.words().joinToString(", ")})"
}
