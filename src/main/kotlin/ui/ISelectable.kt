package ui

import kotlinx.coroutines.flow.StateFlow

interface ISelectable {
    val selected: StateFlow<Boolean>
    fun select()
    fun unselect()
}