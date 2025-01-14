package dev.kavrin.rsable.di

import dev.kavrin.rsable.presentation.screens.ble_list.ClientBleListViewModel
import dev.kavrin.rsable.presentation.screens.intro.IntroViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::IntroViewModel)
    viewModelOf(::ClientBleListViewModel)
}