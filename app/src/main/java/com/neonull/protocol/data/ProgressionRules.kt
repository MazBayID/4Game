package com.neonull.protocol.data

/**
 * Linear prerequisite rules for the current campaign sequence.
 *
 * A later lesson cannot unlock by completing a higher ID out of order.
 * Completed lessons remain replayable; the first incomplete lesson is the
 * single next unlock. This can later be replaced by an explicit dependency
 * graph without changing callers.
 */
object ProgressionRules {
    fun nextUnlockedModuleId(
        modules: List<ProtocolModule>,
        completedModuleIds: Set<Int>
    ): Int {
        val orderedIds = modules.map { it.id }.distinct().sorted()
        return orderedIds.firstOrNull { it !in completedModuleIds }
            ?: orderedIds.lastOrNull()
            ?: 1
    }

    fun isAccessible(
        moduleId: Int,
        modules: List<ProtocolModule>,
        completedModuleIds: Set<Int>
    ): Boolean {
        return moduleId in completedModuleIds ||
            moduleId == nextUnlockedModuleId(modules, completedModuleIds)
    }

    /**
     * Returns the next campaign module after [currentModuleId] only when the
     * current module is complete and the candidate is accessible.
     */
    fun nextAccessibleModuleIdAfter(
        currentModuleId: Int,
        modules: List<ProtocolModule>,
        completedModuleIds: Set<Int>
    ): Int? {
        if (currentModuleId !in completedModuleIds) return null
        return modules.asSequence()
            .map { it.id }
            .distinct()
            .filter { it > currentModuleId }
            .sorted()
            .firstOrNull { isAccessible(it, modules, completedModuleIds) }
    }
}
