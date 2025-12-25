package com.nordicwalk.core.data.repository

import com.nordicwalk.core.domain.model.AnalysisSession
import com.nordicwalk.core.domain.model.PostureViolation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing pose analysis sessions and results
 */
@Singleton
class AnalysisRepository @Inject constructor() {
    // In-memory storage (replace with Room database in production)
    private val sessions = mutableMapOf<Long, AnalysisSession>()
    private val violations = mutableMapOf<Long, MutableList<PostureViolation>>()

    /**
     * Save a complete analysis session
     */
    suspend fun saveAnalysisSession(session: AnalysisSession): Result<Long> = try {
        sessions[session.id] = session
        violations[session.id] = session.violations.toMutableList()
        Result.success(session.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get analysis session by ID
     */
    suspend fun getAnalysisSession(sessionId: Long): AnalysisSession? = 
        sessions[sessionId]

    /**
     * Get all sessions for a student
     */
    suspend fun getStudentSessions(studentId: Long): List<AnalysisSession> =
        sessions.values.filter { it.studentId == studentId }

    /**
     * Delete analysis session
     */
    suspend fun deleteAnalysisSession(sessionId: Long): Result<Unit> = try {
        sessions.remove(sessionId)
        violations.remove(sessionId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Add violation to session
     */
    suspend fun addViolation(violation: PostureViolation): Result<Long> = try {
        val violationList = violations.getOrPut(violation.sessionId) { mutableListOf() }
        violationList.add(violation)
        Result.success(violation.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get violations for a session
     */
    suspend fun getSessionViolations(sessionId: Long): List<PostureViolation> =
        violations[sessionId] ?: emptyList()

    /**
     * Search sessions by criteria
     */
    suspend fun searchSessions(
        studentId: Long? = null,
        direction: String? = null
    ): List<AnalysisSession> {
        return sessions.values.filter { session ->
            (studentId == null || session.studentId == studentId) &&
            (direction == null || session.direction.name == direction)
        }
    }

    /**
     * Get statistics for a session
     */
    suspend fun getSessionStatistics(sessionId: Long): SessionStatistics? {
        val session = sessions[sessionId] ?: return null
        
        return SessionStatistics(
            sessionId = sessionId,
            frameCount = session.frames.size,
            metricsCount = session.metrics.size,
            violationCount = session.violations.size,
            criticalViolationCount = session.violations.count { it.severity.name == "CRITICAL" },
            avgConfidence = if (session.frames.isNotEmpty()) {
                session.frames.flatMap { it.landmarks }.map { it.confidence }.average().toFloat()
            } else {
                0f
            }
        )
    }

    /**
     * Clear all sessions (useful for testing)
     */
    suspend fun clearAllSessions(): Result<Unit> = try {
        sessions.clear()
        violations.clear()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Session statistics
 */
data class SessionStatistics(
    val sessionId: Long,
    val frameCount: Int,
    val metricsCount: Int,
    val violationCount: Int,
    val criticalViolationCount: Int,
    val avgConfidence: Float
)
