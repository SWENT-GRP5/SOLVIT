package com.android.solvit.shared.model.jobs

import kotlinx.coroutines.flow.Flow

interface JobDashboardRepository {
  fun getCurrentJobs(): Flow<List<Job>>
  fun getPendingJobs(): Flow<List<Job>>
  fun getHistoryJobs(): Flow<List<Job>>
}
