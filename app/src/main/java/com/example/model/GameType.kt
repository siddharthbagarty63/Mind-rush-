package com.example.model

enum class GameType(
    val title: String,
    val category: String,
    val description: String,
    val difficultyLabel: String
) {
    MEMORY_MATRIX(
        title = "Memory Matrix",
        category = "Visual Memory",
        description = "Memorize and recall glowing tile patterns across shifting grids.",
        difficultyLabel = "Scaling 3×3 to 6×6"
    ),
    BRAIN_SPEED(
        title = "Brain Speed",
        category = "Reaction Time",
        description = "Test reaction speed down to the millisecond. Zero false starts.",
        difficultyLabel = "Microsecond Precision"
    ),
    COLOR_CHAOS(
        title = "Color Chaos",
        category = "Cognitive Control",
        description = "Decode the Stroop effect under pressure. Obey shifting match rules.",
        difficultyLabel = "Timed Challenge"
    ),
    NUMBER_NINJA(
        title = "Number Ninja",
        category = "Mental Arithmetic",
        description = "Solve arithmetic blitz and number pattern sequences rapidly.",
        difficultyLabel = "Multi-Stage Math"
    )
}
