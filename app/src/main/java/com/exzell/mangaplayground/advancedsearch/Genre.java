package com.exzell.mangaplayground.advancedsearch;

public enum Genre {

    ACTION("action", "Action"),
    ADAPTATION("adaptation", "Adaptation"),
    ADULT("adult", "Adult"),
    ADVENTURE("adventure", "Adventure"),
    ALIENS("aliens", "Aliens"),
    ANIMALS("animals", "Animals"),
    ANTHOLOGY("anthology", "Anthology"),
    AWARD_WINNING("award winning", "Award Winning"),
    COMEDY("comedy", "Comedy"),
    COOKING("cooking", "Cooking"),
    CRIME("crime", "Crime"),
    CROSS_DRESS("crossdressing", "Cross Dressing"),
    DELINQUENTS("delinquents", "Delinquents"),
    DEMONS("demons", "Demons"),
    DOUJINSHI("doujinshi", "Doujinshi"),
    DRAMA("drama", "Drama"),
    ECCHI("ecchi", "Ecchi"),
    FANTASY("fantasy", "Fantasy"),
    FAN_COLOUR("fan-colored", "Fan Colored"),
    FOOD("food", "Food"),
    FOUR_KOMA("4-koma", "4 Koma"),
    FULL_COLOR("full-color", "Full Color"),
    GAME("game", "Game"),
    GENDER_BEND("gender-bender", "Gender Bender"),
    GENDER_SWAP("genderswap", "Gender Swap"),
    GHOSTS("ghosts", "Ghosts"),
    GORE("gore", "Gore"),
    GOSSIP("gossip", "Gossip"),
    GYARU("gyaru", "Gyaru"),
    HAREM("harem", "Harem"),
    HISTORICAL("historical", "Historical"),
    HORROR("horror", "Horror"),
    INCEST("incest", "Incest"),
    ISEKAI("isekai", "Isekai"),
    JOSEI("josei", "Josei"),
    KIDS("kids", "Kids"),
    LOLI("loli", "Loli"),
    LOLICON("lolicon", "Lolicon"),
    LONGSTRIP("long-strip", "Long Strip"),
    MAFIA("mafia", "Mafia"),
    MAGIC("magic", "Magic"),
    MAGICAL_GIRLS("magical-girls", "Magical Girls"),
    MANHWA("manhwa", "Manhwa"),
    MATRIAL_ARTIST("martial-artist", "Martial Artist"),
    MATURE("mature", "Mature"),
    MECHA("mecha", "Mecha"),
    MEDICAL("medical", "Medical"),
    MILITARY("military", "Military"),
    MONSTERS("monsters", "Monsters"),
    MONSTER_GIRLS("monster-girls", "Monster Girls"),
    MUSIC("music", "Music"),
    MYSTERY("mystery", "Mystery"),
    NINJA("ninja", "Ninja"),
    OFFICE_WORKER("office-workers", "Office Workers"),
    OFFICIAL_COLORED("official-colored", "Official Colored"),
    ONE_SHOT("one-shot", "One Shot"),
    PARODY("parody", "Parody"),
    PHILOSOPHICAL("philosophical", "Philosophical"),
    POLICE("police", "Police"),
    POST_APOCALYPTIC("post-apocalyptic", "Post Apocalyptic"),
    PSYCHOLOGICAL("psychological", "Psychological"),
    REINCARNATION("reincarnation", "Reincarnation"),
    REVERSE_HAREM("reverse_harem", "Reverse Harem"),
    ROMANCE("romance", "Romance"),
    SCHOOL_LIFE("school-life", "School Life"),
    SCIFI("sci-fi", "Sci Fi"),
    SEINEN("seinen", "Seinen"),
    SHONEN("shounen", "Shounen"),
    SHONEN_AI("shounen-ai", "Shounen Ai"),
    SHOTA("shota", "Shota"),
    SHOTACON("shotacon", "Shotacon"),
    SHOUJO("shoujo", "Shoujo"),
    SHOUJO_AI("shoujo-ai", "Shoujo Ai"),
    SLICE_OF_LIFE("slice-of-life", "Slice of Life"),
    SMUT("smut", "Smut"),
    SPACE("space", "Space"),
    SPORT("sports", "Sports"),
    SUPERNATURAL("supernatural", "Supernatural"),
    SUPER_HERO("superhero", "Super Hero"),
    SUPER_POWER("super-power", "Super Power"),
    SURVIVAL("survival", "Survival"),
    SUSPENSE("suspense", "Suspense"),
    THRILLER("thriller", "Thriller"),
    TIME_TRAVEL("tine-travel", "Time Travel"),
    TOOMICS("toomics", "Toomics"),
    TRADITIONAL_GAME("traditional-games", "Traditional Games"),
    TRAGEDY("tragedy", "Tragedy"),
    USER_CREATED("user-created", "User Created"),
    VAMPIRE("vampire", "Vampire"),
    VAMPIRES("vampires", "Vampires"),
    VIDEO_GAMES("video-games", "Video Games"),
    VILLAINESS("villainess", "Villainess"),
    VIRTUAL_REALITY("virtual-reality", "Virtual Reality"),
    WEB_COMIC("web-comic", "Web Comic"),
    WEB_TOON("web-toon", "WebToon"),
    WUXIA("wuxia", "Wuxia"),
    YAOI("yaoi", "Yaoi"),
    YURI("yuri", "Yuri"),
    ZOMBIES("zombies", "Zombies");


    Genre(String name, String dispName) {

        this.name = name;
        this.dispName = dispName;
        this.link = "genre/" + name;
    }

    public String name;
    public String dispName;
    public String link;
}
