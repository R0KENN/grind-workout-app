package com.example.dumbbellworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dumbbellworkout.*
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

data class HomeUiState(
    val userLevel: UserLevel = UserLevel(1, title = "Новичок", currentXP = 0, xpForNextLevel = 100, totalXP = 0),
    val currentStreak: Int = 0,
    val canRecover: Boolean = false,
    val missedDate: String? = null,
    val missedWorkoutName: String = "",
    val missedWorkoutId: String = "",
    val completedDays: Int = 0,
    val totalDays: Int = 0,
    val weeklyProgress: Float = 0f,
    val advice: String = "",
    val todayQuote: String = "",
    val todayWorkout: Workout = Workout("rest", "Отдых", "", emptyList()),
    val isRestDay: Boolean = true,
    val challenges: List<WeeklyChallenge> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repo = WorkoutRepository(context)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val quotes = listOf(
        "Тяжело в зале — легко по жизни. 💪",
        "Прогресс, а не совершенство.",
        "Дисциплина — мост между целями и достижениями.",
        "Каждый повтор приближает тебя к цели.",
        "Не останавливайся, пока не станешь гордиться собой.",
        "Боль временна, гордость — навсегда. 🔥",
        "Ты сильнее, чем думаешь.",
        "Успех начинается за пределами зоны комфорта.",
        "Сегодняшняя тренировка — завтрашняя сила.",
        "Маленькие шаги ведут к большим результатам.",
        "Тело достигает того, во что верит разум.",
        "Будь сильнее своих отговорок.",
        "Каждый день — новый шанс стать лучше.",
        "Нет коротких путей к месту, которое стоит достижения.",
        "Пот — это жир, который плачет. 💧",
        "Твоё тело может всё. Убеди свой разум.",
        "Результат = постоянство × время.",
        "Один час тренировки — это 4% твоего дня.",
        "Не жди мотивацию. Создавай привычку.",
        "Сильное тело — сильный дух. 🔱",
        "Рекорды созданы, чтобы их бить.",
        "Ты не проиграл, пока не сдался.",
        "Инвестиция в себя — лучшая инвестиция.",
        "Фитнес — не наказание, а награда телу.",
        "Делай сегодня то, за что скажешь спасибо завтра.",
        "Лучшая версия тебя ждёт в зале.",
        "Нет ничего невозможного для того, кто пробует.",
        "Путь в тысячу миль начинается с одного шага.",
        "Железо не лжёт — ты либо поднял, либо нет.",
        "Постоянство бьёт талант.",
        "Чем тяжелее тренировка, тем слаще победа. 🏆",
        "Не считай дни — делай так, чтобы дни считались.",
        "Сила не приходит от побед, а от борьбы.",
        "Будь тем, кем ты хотел бы стать.",
        "Мышцы растут не в зале, а после него.",
        "Секрет успеха? Не пропускай тренировки.",
        "Ты vs ты вчерашний. Побеждай.",
        "Цель без плана — просто мечта.",
        "Поднимай тяжёлое, живи легко. ⚡",
        "Каждая тренировка — это шаг вперёд."
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // Миграция при первом запуске
            repo.migrateFromSharedPrefs(context)

            val todayWorkout = getTodayWorkout()
            val userLevel = LevelManager.getUserLevel(context)
            val streak = StreakManager.getCurrentStreak(context)
            val canRecover = StreakManager.canRecoverStreak(context)
            val missedDate = StreakManager.getMissedTrainingDay(context)
            val completedDays = repo.getCompletedDaysThisWeek()
            val totalDays = SCHEDULE.values.count { it != "rest" }
            val challenges = ChallengeManager.getWeeklyChallenges(context)
            val quote = quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]

            _uiState.value = HomeUiState(
                userLevel = userLevel,
                currentStreak = streak,
                canRecover = canRecover,
                missedDate = missedDate,
                missedWorkoutName = missedDate?.let { StreakManager.getMissedWorkoutName(it) } ?: "",
                missedWorkoutId = missedDate?.let { StreakManager.getMissedWorkoutId(it) } ?: "",
                completedDays = completedDays,
                totalDays = totalDays,
                weeklyProgress = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f,
                advice = SmartAdvice.getAdvice(context),
                todayQuote = quote,
                todayWorkout = todayWorkout,
                isRestDay = todayWorkout.id == "rest",
                challenges = challenges,
                isLoading = false
            )
        }
    }
}
