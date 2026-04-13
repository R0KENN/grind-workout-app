package com.example.dumbbellworkout

import java.util.Calendar

val SCHEDULE = mapOf(
    0 to "upper_a",
    1 to "lower_a",
    2 to "rest",
    3 to "upper_b",
    4 to "lower_b",
    5 to "rest",
    6 to "rest"
)

val DAY_NAMES = listOf(
    "Понедельник", "Вторник", "Среда",
    "Четверг", "Пятница", "Суббота", "Воскресенье"
)

val ALL_WORKOUTS = mapOf(

    "upper_a" to Workout(
        id = "upper_a",
        name = "ВЕРХ: PUSH DAY",
        time = "55–65 минут",
        exercises = listOf(
            Exercise(1, "Жим гантелей лёжа на горизонтальной скамье", 4, "8–10", 120, "2 мин", "Грудь (середина)", R.raw.d1_1_bench_press),
            Exercise(2, "Тяга гантелей в наклоне двумя руками", 4, "8–10", 120, "2 мин", "Спина (широчайшие, ромбовидные)", R.raw.d1_2_bent_over_row),
            Exercise(3, "Жим гантелей на наклонной скамье (30–45°)", 3, "10–12", 90, "90 сек", "Грудь (верхняя часть)", R.raw.d1_3_incline_press),
            Exercise(4, "Пуловер с гантелью лёжа поперёк скамьи", 3, "12–15", 90, "90 сек", "Спина (широчайшие) + грудь", R.raw.d1_4_pullover),
            Exercise(5, "Разводка гантелей лёжа на скамье", 3, "12–15", 60, "60 сек", "Грудь (внутренняя часть)", R.raw.d1_5_flyes),
            Exercise(6, "Латеральный подъём гантелей стоя", 3, "12–15", 60, "60 сек", "Плечи (средняя дельта)", R.raw.d1_6_lateral_raise),
            Exercise(7, "Жим гантелей сидя (армейский жим)", 3, "10–12", 90, "90 сек", "Плечи (передняя + средняя дельта)", R.raw.d1_7_shoulder_press),
            Exercise(8, "Сгибание рук с гантелями (бицепс)", 2, "10–12", 60, "60 сек", "Бицепс", R.raw.d1_8_bicep_curl),
            Exercise(9, "Французский жим с гантелью из-за головы", 2, "10–12", 60, "60 сек", "Трицепс", R.raw.d1_9_french_press)
        )
    ),

    "lower_a" to Workout(
        id = "lower_a",
        name = "НИЗ: SQUAD DAY",
        time = "50–60 минут",
        exercises = listOf(
            Exercise(1, "Гоблет-присед", 4, "8–10", 120, "2 мин", "Квадрицепсы, ягодицы", R.raw.d2_1_goblet_squat),
            Exercise(2, "Румынская тяга с гантелями", 4, "10–12", 120, "2 мин", "Задняя поверхность бедра, ягодицы", R.raw.d2_2_romanian_deadlift),
            Exercise(3, "Болгарские сплит-приседания", 3, "10–12 /ногу", 90, "90 сек", "Квадрицепсы, ягодицы", R.raw.d2_3_bulgarian_split),
            Exercise(4, "Сгибание ног лёжа с гантелью", 3, "12–15", 60, "60 сек", "Бицепс бедра", R.raw.d2_4_leg_curl),
            Exercise(5, "Подъём на носки стоя с гантелями", 3, "15–20", 60, "60 сек", "Икры", R.raw.d2_5_calf_raise),
            Exercise(6, "Планка", 3, "40–60 сек", 60, "60 сек", "Кор", R.raw.d2_6_plank),
            Exercise(7, "Сгибание запястий сидя (ладони вверх)", 3, "15–20", 40, "30–45 сек", "Предплечья (сгибатели)", R.raw.d2_7_wrist_curl),
            Exercise(8, "Обратные сгибания запястий (ладони вниз)", 3, "15–20", 40, "30–45 сек", "Предплечья (разгибатели)", R.raw.d2_8_reverse_wrist)
        )
    ),

    "upper_b" to Workout(
        id = "upper_b",
        name = "ВЕРХ: PULL DAY",
        time = "60–65 минут",
        exercises = listOf(
            Exercise(1, "Тяга гантели одной рукой в упоре на скамью", 4, "8–10 /руку", 120, "2 мин", "Спина (широчайшие)", R.raw.d3_1_one_arm_row),
            Exercise(2, "Жим гантелей стоя (overhead press)", 4, "8–10", 120, "2 мин", "Плечи (все три пучка)", R.raw.d3_2_overhead_press),
            Exercise(3, "Тяга гантелей лёжа на наклонной скамье", 3, "10–12", 90, "90 сек", "Спина (ромбовидные, задние дельты)", R.raw.d3_3_prone_row),
            Exercise(4, "Отжимания от пола (широко)", 3, "до отказа", 90, "90 сек", "Грудь + трицепс", R.raw.d3_4_pushup),
            Exercise(5, "Тяга гантелей к подбородку в наклоне", 3, "10–12", 60, "60 сек", "Задняя дельта", R.raw.d3_5_rear_delt_row),
            Exercise(6, "Латеральный подъём с наклоном (lean-away)", 3, "12–15 /руку", 60, "60 сек", "Средняя дельта", R.raw.d3_6_lean_away),
            Exercise(7, "Разведение гантелей лёжа лицом вниз", 3, "12–15", 60, "60 сек", "Задняя дельта + манжета", R.raw.d3_7_reverse_flyes),
            Exercise(8, "Молотковые сгибания (хват молоток)", 2, "10–12", 60, "60 сек", "Бицепс + брахиалис", R.raw.d3_8_hammer_curl),
            Exercise(9, "Жим гантелей лёжа на полу узким хватом", 2, "10–12", 60, "60 сек", "Трицепс + грудь", R.raw.d3_9_floor_press)
        )
    ),

    "lower_b" to Workout(
        id = "lower_b",
        name = "НИЗ: POWER DAY",
        time = "50–60 минут",
        exercises = listOf(
            Exercise(1, "Румынская тяга с гантелями", 4, "8–10", 120, "2 мин", "Задняя цепь + разгибатели спины", R.raw.d4_1_romanian_deadlift),
            Exercise(2, "Обратные выпады с гантелями", 4, "10–12 /ногу", 90, "90 сек", "Квадрицепсы, ягодицы", R.raw.d4_2_reverse_lunge),
            Exercise(3, "Ягодичный мостик с гантелью", 4, "10–12", 90, "90 сек", "Ягодицы", R.raw.d4_3_hip_thrust),
            Exercise(4, "Плие-присед с гантелью", 3, "12–15", 60, "60 сек", "Приводящие, квадрицепсы", R.raw.d4_4_plie_squat),
            Exercise(5, "Подъём на носки сидя с гантелью", 3, "15–20", 60, "60 сек", "Камбаловидная (икры)", R.raw.d4_5_seated_calf),
            Exercise(6, "Подъём ног лёжа", 3, "15–20", 60, "60 сек", "Нижний пресс", R.raw.d4_6_leg_raise),
            Exercise(7, "Зоттман-сгибания (Zottman curls)", 3, "10–12", 60, "60 сек", "Бицепс + предплечья", R.raw.d4_7_zottman_curl),
            Exercise(8, "Прогулка фермера с гантелями", 3, "30–40 сек", 60, "60 сек", "Предплечья, трапеции, кор", R.raw.d4_8_farmer_walk)
        )
    ),

    "rest" to Workout(
        id = "rest",
        name = "ДЕНЬ ОТДЫХА",
        time = "—",
        exercises = emptyList()
    )
)

fun getTodayWorkout(): Workout {
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val index = when (dayOfWeek) {
        Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6; else -> 0
    }
    val key = SCHEDULE[index] ?: "rest"
    return ALL_WORKOUTS[key] ?: ALL_WORKOUTS["rest"]!!
}

fun getTodayDayName(): String {
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val index = when (dayOfWeek) {
        Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6; else -> 0
    }
    return DAY_NAMES[index]
}
