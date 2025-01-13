package dev.kavrin.rsable.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Intro : Route

    @Serializable
    data object PeripheralGraph : Route

    @Serializable
    data object PeripheralList : Route

    @Serializable
    data object PeripheralDetail : Route
}