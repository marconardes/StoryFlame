package br.com.marconardes.storyflame.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// A simple representation of a screen route. Could be a sealed class for more type safety.
typealias Route = String

interface Navigator {
    val currentRoute: State<Route?>
    fun push(route: Route)
    fun pop()
    fun replace(route: Route) // Clears the stack and adds the new route
    fun navigateBackTo(route: Route) // Pops until the given route is found, or does nothing if not found
}

class CustomNavigator(initialRoute: Route?) : Navigator {
    private var _backStack = mutableListOf<Route>()
    private val _currentRouteInternal = mutableStateOf<Route?>(null)

    override val currentRoute: State<Route?> = _currentRouteInternal

    init {
        if (initialRoute != null) {
            _backStack.add(initialRoute)
            _currentRouteInternal.value = initialRoute
        }
    }

    override fun push(route: Route) {
        _backStack.add(route)
        _currentRouteInternal.value = route
    }

    override fun pop() {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLast()
            _currentRouteInternal.value = _backStack.lastOrNull()
        }
        if (_backStack.isEmpty()) {
             _currentRouteInternal.value = null // Or handle empty stack case as needed
        }
    }

    override fun replace(route: Route) {
        _backStack.clear()
        _backStack.add(route)
        _currentRouteInternal.value = route
    }

    override fun navigateBackTo(route: Route) {
        while (_backStack.isNotEmpty() && _backStack.last() != route) {
            _backStack.removeLast()
        }
        _currentRouteInternal.value = _backStack.lastOrNull()
         if (_backStack.isEmpty()) {
             _currentRouteInternal.value = null
        }
    }

    // Optional: A way to check the back stack, useful for debugging or specific logic
    fun getBackStack(): List<Route> = _backStack.toList()
}
