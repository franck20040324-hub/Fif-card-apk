package com.example.data.local

import com.example.model.AvatarGender
import com.example.model.PreferredFoot

object FifProSeedData {

    private fun p(
        name: String,
        nickname: String,
        nationality: String,
        club: String,
        league: String,
        mainPos: String,
        extraPos: String,
        gender: AvatarGender,
        foot: PreferredFoot,
        pac: Int,
        sho: Int,
        pas: Int,
        dri: Int,
        def: Int,
        phy: Int,
        hea: Int,
        cro: Int,
        tac: Int,
        sta: Int,
        gk: Int,
        pGk: Int,
        pDef: Int,
        pMid: Int,
        pAtt: Int
    ): PlayerEntity {
        val base = PlayerEntity(
            name = name,
            nickname = nickname,
            nationality = nationality,
            flagEmoji = "",
            club = club,
            league = league,
            mainPosition = mainPos,
            secondaryPositions = extraPos,
            avatarGender = gender.name,
            customImageUri = null,
            customClubLogoUri = null,
            preferredFoot = foot.code,
            pace = pac,
            shooting = sho,
            passing = pas,
            dribbling = dri,
            defending = def,
            physical = phy,
            heading = hea,
            crossing = cro,
            tackling = tac,
            stamina = sta,
            gkStat = gk,
            powerGk = pGk,
            powerDef = pDef,
            powerMid = pMid,
            powerAtt = pAtt,
            isBuiltIn = true,
            defaultSnapshot = ""
        )
        return base.copy(defaultSnapshot = base.packDefaults())
    }

    fun initialPlayers(): List<PlayerEntity> = listOf(
        // Goalkeepers (Male & Female)
        p("Thibaut Courtois", "The Wall", "Belgium", "Real Madrid", "La Liga", "GK", "", AvatarGender.MALE, PreferredFoot.LEFT, 48, 35, 74, 46, 48, 86, 55, 40, 42, 62, 90, 90, 52, 49, 38),
        p("Alisson Becker", "Ali", "Brazil", "Liverpool", "Premier League", "GK", "", AvatarGender.MALE, PreferredFoot.RIGHT, 52, 34, 82, 54, 49, 84, 52, 48, 45, 64, 89, 89, 54, 55, 39),
        p("Gianluigi Donnarumma", "Gigio", "Italy", "PSG", "Ligue 1", "GK", "", AvatarGender.MALE, PreferredFoot.RIGHT, 50, 32, 72, 48, 45, 85, 53, 38, 40, 60, 89, 89, 50, 48, 36),
        p("Jan Oblak", "The Lock", "Slovenia", "Atlético Madrid", "La Liga", "GK", "", AvatarGender.MALE, PreferredFoot.RIGHT, 47, 31, 70, 46, 47, 84, 50, 38, 41, 61, 88, 88, 50, 47, 35),
        p("Manuel Neuer", "Sweeper", "Germany", "Bayern Munich", "Bundesliga", "GK", "", AvatarGender.MALE, PreferredFoot.RIGHT, 54, 38, 88, 58, 52, 83, 54, 55, 50, 63, 87, 87, 56, 58, 40),
        p("Christiane Endler", "Tiane", "France", "Lyon Féminin", "Première Ligue", "GK", "", AvatarGender.FEMALE, PreferredFoot.RIGHT, 50, 34, 78, 52, 48, 82, 51, 42, 44, 62, 88, 88, 52, 51, 38),
        p("Cata Coll", "Cata", "Spain", "Barcelona Femení", "Liga F", "GK", "", AvatarGender.FEMALE, PreferredFoot.RIGHT, 52, 33, 80, 54, 47, 78, 49, 44, 43, 62, 85, 85, 51, 52, 37),

        // Defenders
        p("Virgil van Dijk", "VVD", "Netherlands", "Liverpool", "Premier League", "LCB", "CB,RCB", AvatarGender.MALE, PreferredFoot.RIGHT, 78, 60, 74, 72, 90, 89, 91, 64, 92, 82, 25, 48, 90, 76, 63),
        p("William Saliba", "Wilo", "France", "Arsenal", "Premier League", "RCB", "CB,LCB", AvatarGender.MALE, PreferredFoot.RIGHT, 82, 48, 74, 74, 88, 85, 85, 62, 90, 84, 22, 46, 88, 74, 58),
        p("Rúben Dias", "The Rock", "Portugal", "Manchester City", "Premier League", "CB", "LCB,RCB", AvatarGender.MALE, PreferredFoot.RIGHT, 72, 52, 73, 70, 89, 87, 87, 60, 90, 83, 22, 46, 88, 73, 56),
        p("Antonio Rüdiger", "El Loco", "Germany", "Real Madrid", "La Liga", "LCB", "CB,RCB", AvatarGender.MALE, PreferredFoot.RIGHT, 82, 54, 71, 70, 87, 88, 88, 60, 89, 85, 22, 46, 88, 72, 58),
        p("Achraf Hakimi", "Flash", "Morocco", "PSG", "Ligue 1", "RB", "RWB,RM", AvatarGender.MALE, PreferredFoot.RIGHT, 92, 75, 80, 82, 78, 79, 71, 86, 80, 92, 21, 43, 85, 82, 79),
        p("Theo Hernández", "Theo", "France", "AC Milan", "Serie A", "LB", "LWB,LM", AvatarGender.MALE, PreferredFoot.LEFT, 93, 76, 78, 83, 81, 86, 76, 85, 82, 90, 21, 44, 86, 81, 78),
        p("Trent Alexander-Arnold", "TAA", "England", "Liverpool", "Premier League", "RB", "RWB,CM", AvatarGender.MALE, PreferredFoot.RIGHT, 77, 72, 90, 81, 78, 74, 66, 94, 78, 86, 20, 42, 85, 87, 77),
        p("Alessandro Bastoni", "Basto", "Italy", "Inter Milan", "Serie A", "LCB", "CB,LB", AvatarGender.MALE, PreferredFoot.LEFT, 75, 53, 78, 75, 88, 83, 85, 72, 89, 83, 22, 45, 88, 76, 59),
        p("Jeremie Frimpong", "Jet", "Netherlands", "Bayer Leverkusen", "Bundesliga", "RWB", "RB,RM", AvatarGender.MALE, PreferredFoot.RIGHT, 94, 73, 76, 86, 74, 72, 62, 84, 75, 91, 20, 40, 84, 82, 79),
        p("Josko Gvardiol", "Josko", "Croatia", "Manchester City", "Premier League", "LB", "LCB,CB", AvatarGender.MALE, PreferredFoot.LEFT, 81, 64, 77, 78, 85, 84, 83, 76, 86, 85, 21, 44, 86, 77, 65),
        p("Dani Carvajal", "Carva", "Spain", "Real Madrid", "La Liga", "RB", "RWB", AvatarGender.MALE, PreferredFoot.RIGHT, 81, 61, 79, 80, 84, 81, 75, 84, 85, 88, 21, 43, 86, 78, 65),
        p("Mapi León", "Mapi", "Spain", "Barcelona Femení", "Liga F", "LCB", "CB,LB", AvatarGender.FEMALE, PreferredFoot.LEFT, 78, 62, 83, 80, 89, 80, 82, 78, 90, 85, 21, 44, 89, 81, 64),
        p("Wendie Renard", "Captain", "France", "Lyon Féminin", "Première Ligue", "CB", "LCB,RCB", AvatarGender.FEMALE, PreferredFoot.RIGHT, 69, 66, 76, 73, 90, 88, 92, 66, 89, 80, 22, 46, 88, 75, 66),
        p("Millie Bright", "Bright", "England", "Chelsea Women", "WSL", "RCB", "CB", AvatarGender.FEMALE, PreferredFoot.RIGHT, 74, 58, 75, 72, 87, 86, 87, 63, 88, 82, 21, 45, 86, 73, 60),

        // Midfielders
        p("Rodri", "El Jefe", "Spain", "Manchester City", "Premier League", "CDM", "CM,CB", AvatarGender.MALE, PreferredFoot.RIGHT, 68, 80, 87, 83, 87, 86, 84, 79, 89, 91, 22, 46, 87, 91, 79),
        p("Jude Bellingham", "Hey Jude", "England", "Real Madrid", "La Liga", "CAM", "CM,LCM", AvatarGender.MALE, PreferredFoot.RIGHT, 81, 87, 84, 89, 78, 85, 84, 78, 80, 93, 22, 44, 80, 90, 88),
        p("Kevin De Bruyne", "KDB", "Belgium", "Manchester City", "Premier League", "CAM", "CM,RCM", AvatarGender.MALE, PreferredFoot.BOTH, 69, 87, 94, 86, 65, 77, 68, 95, 67, 81, 21, 43, 72, 90, 86),
        p("Federico Valverde", "El Halcón", "Uruguay", "Real Madrid", "La Liga", "RCM", "CM,RM", AvatarGender.MALE, PreferredFoot.RIGHT, 88, 84, 85, 84, 82, 84, 74, 83, 84, 95, 21, 44, 83, 89, 84),
        p("Florian Wirtz", "Flo", "Germany", "Bayer Leverkusen", "Bundesliga", "CAM", "LW,CM", AvatarGender.MALE, PreferredFoot.RIGHT, 82, 80, 87, 90, 56, 68, 63, 84, 58, 86, 20, 39, 64, 89, 86),
        p("Martin Ødegaard", "Cap", "Norway", "Arsenal", "Premier League", "CAM", "RCM,CM", AvatarGender.MALE, PreferredFoot.LEFT, 77, 81, 89, 89, 65, 69, 64, 88, 66, 90, 20, 40, 70, 89, 84),
        p("Jamal Musiala", "Bambi", "Germany", "Bayern Munich", "Bundesliga", "CAM", "LM,LW", AvatarGender.MALE, PreferredFoot.RIGHT, 85, 79, 81, 93, 62, 68, 64, 77, 64, 84, 20, 39, 67, 88, 86),
        p("Pedri", "El Mago", "Spain", "FC Barcelona", "La Liga", "LCM", "CM,CAM", AvatarGender.MALE, PreferredFoot.RIGHT, 78, 72, 87, 90, 72, 71, 61, 80, 74, 89, 20, 40, 74, 88, 80),
        p("Declan Rice", "Deccy", "England", "Arsenal", "Premier League", "CDM", "CM,CB", AvatarGender.MALE, PreferredFoot.RIGHT, 75, 74, 83, 81, 86, 86, 82, 76, 88, 93, 22, 45, 86, 87, 76),
        p("Nicolò Barella", "Barella", "Italy", "Inter Milan", "Serie A", "RCM", "CM,CDM", AvatarGender.MALE, PreferredFoot.RIGHT, 81, 78, 84, 86, 81, 78, 71, 82, 84, 92, 21, 42, 81, 87, 80),
        p("Vitinha", "Viti", "Portugal", "PSG", "Ligue 1", "CM", "LCM,CDM", AvatarGender.MALE, PreferredFoot.RIGHT, 76, 76, 87, 89, 76, 70, 62, 81, 77, 89, 20, 40, 76, 87, 80),
        p("Aitana Bonmatí", "Aitana", "Spain", "Barcelona Femení", "Liga F", "RCM", "CAM,CM", AvatarGender.FEMALE, PreferredFoot.BOTH, 81, 86, 91, 92, 78, 74, 70, 86, 79, 91, 20, 41, 77, 91, 87),
        p("Alexia Putellas", "La Reina", "Spain", "Barcelona Femení", "Liga F", "LCM", "CAM,LW", AvatarGender.FEMALE, PreferredFoot.LEFT, 79, 88, 90, 90, 72, 76, 74, 87, 73, 86, 20, 41, 74, 90, 87),
        p("Lena Oberdorf", "Obi", "Germany", "Bayern Frauen", "Frauen-Bundesliga", "CDM", "CB,CM", AvatarGender.FEMALE, PreferredFoot.RIGHT, 76, 73, 82, 80, 88, 86, 83, 74, 90, 90, 21, 44, 87, 87, 75),

        // Attackers
        p("Kylian Mbappé", "Kyky", "France", "Real Madrid", "La Liga", "ST", "LW,CF", AvatarGender.MALE, PreferredFoot.RIGHT, 97, 90, 81, 92, 38, 78, 76, 82, 36, 88, 20, 38, 52, 83, 92),
        p("Erling Haaland", "Cyborg", "Norway", "Manchester City", "Premier League", "ST", "CF", AvatarGender.MALE, PreferredFoot.LEFT, 89, 93, 67, 81, 45, 88, 89, 64, 42, 84, 22, 40, 56, 74, 92),
        p("Vinícius Júnior", "Vini", "Brazil", "Real Madrid", "La Liga", "LW", "LF,ST", AvatarGender.MALE, PreferredFoot.RIGHT, 95, 84, 81, 94, 32, 69, 64, 84, 34, 87, 19, 36, 48, 84, 91),
        p("Mohamed Salah", "Mo", "Egypt", "Liverpool", "Premier League", "RW", "RF,ST", AvatarGender.MALE, PreferredFoot.LEFT, 89, 88, 83, 88, 45, 75, 71, 85, 44, 88, 20, 38, 54, 85, 90),
        p("Harry Kane", "Hurrikane", "England", "Bayern Munich", "Bundesliga", "ST", "CF,CAM", AvatarGender.MALE, PreferredFoot.BOTH, 67, 93, 84, 83, 49, 83, 88, 80, 46, 83, 21, 40, 58, 82, 91),
        p("Lamine Yamal", "Lamine", "Spain", "FC Barcelona", "La Liga", "RW", "RM,LW", AvatarGender.MALE, PreferredFoot.LEFT, 85, 78, 83, 91, 35, 58, 56, 86, 36, 82, 19, 36, 46, 84, 86),
        p("Bukayo Saka", "Starboy", "England", "Arsenal", "Premier League", "RW", "RM,LW", AvatarGender.MALE, PreferredFoot.LEFT, 86, 83, 84, 89, 60, 73, 66, 87, 62, 89, 20, 39, 65, 85, 88),
        p("Lautaro Martínez", "El Toro", "Argentina", "Inter Milan", "Serie A", "ST", "CF", AvatarGender.MALE, PreferredFoot.RIGHT, 83, 88, 76, 86, 52, 84, 86, 72, 51, 87, 21, 40, 60, 79, 89),
        p("Robert Lewandowski", "Lewy", "Poland", "FC Barcelona", "La Liga", "ST", "CF", AvatarGender.MALE, PreferredFoot.RIGHT, 75, 89, 79, 86, 44, 83, 90, 74, 42, 79, 21, 40, 55, 79, 89),
        p("Antoine Griezmann", "Grizou", "France", "Atlético Madrid", "La Liga", "CF", "ST,CAM", AvatarGender.MALE, PreferredFoot.LEFT, 79, 88, 87, 88, 59, 75, 79, 86, 58, 87, 21, 41, 64, 87, 88),
        p("Rafael Leão", "Rafa", "Portugal", "AC Milan", "Serie A", "LW", "LM,ST", AvatarGender.MALE, PreferredFoot.RIGHT, 93, 80, 79, 88, 31, 79, 74, 82, 33, 83, 20, 38, 48, 81, 87),
        p("Son Heung-min", "Sonny", "South Korea", "Tottenham", "Premier League", "LW", "ST,LM", AvatarGender.MALE, PreferredFoot.BOTH, 87, 89, 80, 84, 42, 72, 74, 82, 41, 86, 20, 38, 53, 81, 88),
        p("Ousmane Dembélé", "Dembouz", "France", "PSG", "Ligue 1", "RW", "LW,RM", AvatarGender.MALE, PreferredFoot.BOTH, 92, 77, 82, 90, 37, 59, 58, 86, 36, 80, 19, 36, 48, 83, 86),
        p("Brahim Díaz", "Brahim", "Morocco", "Real Madrid", "La Liga", "CAM", "RW,LW", AvatarGender.MALE, PreferredFoot.BOTH, 83, 77, 80, 88, 42, 61, 59, 79, 43, 82, 19, 36, 51, 83, 83),
        p("Khvicha Kvaratskhelia", "Kvara", "Georgia", "PSG", "Ligue 1", "LW", "LM,RW", AvatarGender.MALE, PreferredFoot.BOTH, 86, 81, 81, 89, 41, 72, 68, 83, 42, 85, 20, 38, 52, 83, 86),
        p("Victor Osimhen", "Osi", "Nigeria", "Napoli", "Serie A", "ST", "CF", AvatarGender.MALE, PreferredFoot.RIGHT, 90, 85, 68, 81, 42, 84, 90, 66, 40, 86, 21, 40, 54, 73, 87),
        p("Sam Kerr", "Sammy", "Australia", "Chelsea Women", "WSL", "ST", "CF", AvatarGender.FEMALE, PreferredFoot.RIGHT, 86, 89, 76, 87, 44, 80, 89, 74, 42, 85, 20, 39, 55, 79, 90),
        p("Caroline Graham Hansen", "CGH", "Norway", "Barcelona Femení", "Liga F", "RW", "RM,LW", AvatarGender.FEMALE, PreferredFoot.RIGHT, 87, 85, 88, 91, 48, 72, 68, 91, 47, 86, 20, 39, 57, 87, 90),
        p("Ada Hegerberg", "Ada", "Norway", "Lyon Féminin", "Première Ligue", "ST", "CF", AvatarGender.FEMALE, PreferredFoot.RIGHT, 81, 89, 75, 83, 42, 82, 89, 71, 40, 82, 20, 39, 53, 76, 89),
        p("Lauren James", "LJ", "England", "Chelsea Women", "WSL", "RW", "CAM,ST", AvatarGender.FEMALE, PreferredFoot.RIGHT, 86, 84, 83, 90, 44, 78, 70, 83, 44, 82, 20, 39, 54, 85, 87),
        p("Alessia Russo", "Lessi", "England", "Arsenal Women", "WSL", "ST", "CF", AvatarGender.FEMALE, PreferredFoot.RIGHT, 82, 84, 76, 83, 45, 80, 85, 73, 44, 86, 20, 39, 55, 78, 86)
    )
}
