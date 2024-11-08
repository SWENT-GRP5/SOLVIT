package com.android.solvit.shared.model.jobs

import kotlinx.coroutines.flow.Flow

interface JobDashboardRepository {
  fun getCurrentJobs(): Flow<List<Job>>
}
