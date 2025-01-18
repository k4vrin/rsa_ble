package dev.kavrin.rsable.di

import dev.kavrin.rsable.presentation.screens.client.BleClientViewModel
import dev.kavrin.rsable.presentation.screens.client.ble_detail.BleDetailViewModel
import dev.kavrin.rsable.presentation.screens.client.ble_list.BleListViewModel
import dev.kavrin.rsable.presentation.screens.intro.IntroViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::IntroViewModel)
    viewModelOf(::BleListViewModel)
    viewModelOf(::BleDetailViewModel)
    viewModelOf(::BleClientViewModel)
}