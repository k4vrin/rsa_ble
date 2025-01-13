package dev.kavrin.rsable.di

import dev.kavrin.rsable.presentation.intro.IntroViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::IntroViewModel)
}