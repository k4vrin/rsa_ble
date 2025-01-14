package dev.kavrin.rsable.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Intro : Route

    @Serializable
    data object ClientGraph : Route

    @Serializable
    data object ClientBleList : Route

    @Serializable
    data object ClientBleDetail : Route
}