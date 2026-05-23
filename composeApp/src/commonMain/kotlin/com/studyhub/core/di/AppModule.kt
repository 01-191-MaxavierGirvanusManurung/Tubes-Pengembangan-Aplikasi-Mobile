package com.studyhub.core.di

import com.studyhub.core.network.createHttpClient
import com.studyhub.data.local.DatabaseDriverFactory
import com.studyhub.database.StudyHubDatabase
import com.studyhub.data.local.LocalSubjectDataSource
import com.studyhub.data.local.LocalTaskDataSource
import com.studyhub.data.local.datastore.DataStoreFactory
import com.studyhub.data.local.datastore.UserPreferences
import com.studyhub.data.local.datastore.create
import com.studyhub.data.repository.SubjectRepositoryImpl
import com.studyhub.data.repository.TaskRepositoryImpl
import com.studyhub.domain.repository.SubjectRepository
import com.studyhub.domain.repository.TaskRepository
import com.studyhub.domain.usecase.task.*
import com.studyhub.domain.usecase.subject.*
import com.studyhub.presentation.screens.add_task.AddTaskViewModel
import com.studyhub.presentation.screens.home.HomeViewModel
import com.studyhub.presentation.screens.task.TasksViewModel
import com.studyhub.presentation.screens.task.AddEditTaskViewModel
import com.studyhub.presentation.screens.calendar.CalendarViewModel
import com.studyhub.presentation.screens.profile.ProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.context.startKoin

// ==================== NETWORK MODULE ====================

val networkModule = module {
    single { createHttpClient() }
}

// ==================== DATABASE MODULE ====================

val databaseModule = module {
    single { StudyHubDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { LocalTaskDataSource(get()) }
    single { LocalSubjectDataSource(get()) }
}

// ==================== PREFERENCES MODULE ====================

val preferencesModule = module {
    single { get<DataStoreFactory>().create() }
    single { UserPreferences(get()) }
}

// ==================== REPOSITORY MODULE ====================

val repositoryModule = module {
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<SubjectRepository> { SubjectRepositoryImpl(get()) }
}

// ==================== USE CASE MODULE ====================

val useCaseModule = module {
    factory { AddTaskUseCase(get()) }
    factory { GetAllTasksUseCase(get()) }
    factory { GetActiveTasksUseCase(get()) }
    factory { GetTaskByIdUseCase(get()) }
    factory { GetTasksByDateUseCase(get()) }
    factory { UpdateTaskUseCase(get()) }
    factory { UpdateTaskStatusUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { FilterAndSortTasksUseCase() }
    factory { GetAllSubjectsUseCase(get()) }
    factory { AddSubjectUseCase(get()) }
}

// ==================== VIEWMODEL MODULE ====================

val viewModelModule = module {
    viewModelOf(::AddTaskViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::AddEditTaskViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::ProfileViewModel)
}

// ==================== SHARED MODULES ====================

val sharedModules = listOf(
    networkModule,
    databaseModule,
    preferencesModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

// ==================== INIT FUNCTION ====================

fun initKoin(
    platformModules: List<Module> = emptyList(),
    config: KoinAppDeclaration? = null
) {
    startKoin {
        config?.invoke(this)
        modules(platformModules + sharedModules)
    }
}
